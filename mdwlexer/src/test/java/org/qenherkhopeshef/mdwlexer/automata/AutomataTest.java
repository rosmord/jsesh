package org.qenherkhopeshef.mdwlexer.automata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class AutomataTest {
    @Test
    void determinizesTheSmallAutomaton() {
        DemoSmallAutomatonHelper.BuiltAutomaton built = DemoSmallAutomatonHelper.build();
        DeterministicFiniteAutomaton<DemoSmallAutomatonHelper.Input, DemoSmallAutomaton.Token> automaton =
                AutomataHelper.determinize(built.automaton());

        State initial = automaton.initialState();
        State afterIfPrefix = automaton.transition(initial, DemoSmallAutomatonHelper.Input.I).orElseThrow();
        State ifAccepted = automaton.transition(afterIfPrefix, DemoSmallAutomatonHelper.Input.F).orElseThrow();
        State identifier = automaton.transition(
                initial, DemoSmallAutomatonHelper.Input.IDENTIFIER_CHARACTER).orElseThrow();
        State dead = automaton.transition(initial, DemoSmallAutomatonHelper.Input.FOUR).orElseThrow();

        assertEquals(5, automaton.states().size());
        assertEquals(Optional.of(DemoSmallAutomaton.Token.IDENTIFIER), automaton.acceptingTokenType(initial));
        assertEquals(Optional.empty(), automaton.acceptingTokenType(afterIfPrefix));
        assertEquals(Optional.of(DemoSmallAutomaton.Token.IF), automaton.acceptingTokenType(ifAccepted));
        assertEquals(Optional.of(DemoSmallAutomaton.Token.IDENTIFIER), automaton.acceptingTokenType(identifier));
        assertEquals(Optional.empty(), automaton.acceptingTokenType(dead));
        assertEquals(Optional.of(identifier), automaton.transition(
                identifier, DemoSmallAutomatonHelper.Input.IDENTIFIER_CHARACTER));
    }

    @Test
    void usesEnumDeclarationOrderWhenSubsetHasMultipleAcceptingStates() {
        NondeterministicFiniteAutomaton.Builder<Input, Token> builder =
                new NondeterministicFiniteAutomaton.Builder<>();
        State start = builder.addState();
        State lowerPriority = builder.addState();
        State higherPriority = builder.addState();
        builder.initialState(start)
                .epsilon(start, lowerPriority)
                .epsilon(start, higherPriority)
                .accepting(lowerPriority, Token.IDENTIFIER)
                .accepting(higherPriority, Token.IF);

        DeterministicFiniteAutomaton<Input, Token> automaton = AutomataHelper.determinize(builder.build());

        assertEquals(Optional.of(Token.IF), automaton.acceptingTokenType(automaton.initialState()));
    }

    @Test
    void minimizesEquivalentStatesAndRemovesUnreachableStates() {
        DeterministicFiniteAutomaton.Builder<Input, Token> builder =
                new DeterministicFiniteAutomaton.Builder<>();
        State start = builder.addState();
        State first = builder.addState();
        State second = builder.addState();
        builder.addState();
        builder.initialState(start)
                .transition(start, Input.SYMBOL, first)
                .transition(first, Input.SYMBOL, first)
                .transition(second, Input.SYMBOL, second)
                .accepting(first, Token.IF)
                .accepting(second, Token.IF);

        DeterministicFiniteAutomaton<Input, Token> minimized = AutomataHelper.minimize(builder.build());

        assertEquals(2, minimized.states().size());
        State minimizedAccepting = minimized.transition(minimized.initialState(), Input.SYMBOL).orElseThrow();
        assertEquals(Optional.of(Token.IF), minimized.acceptingTokenType(minimizedAccepting));
        assertEquals(Optional.of(minimizedAccepting), minimized.transition(minimizedAccepting, Input.SYMBOL));
    }

        @Test
        void rendersAutomataAsReadableStrings() {
                DemoSmallAutomatonHelper.BuiltAutomaton built = DemoSmallAutomatonHelper.build();
                String nondeterministic = built.automaton().toString();
                String deterministic = AutomataHelper.determinize(built.automaton()).toString();

                assertEquals(true, nondeterministic.contains("initialState: " + built.start()));
                assertEquals(true, nondeterministic.contains(
                                "    " + built.start() + " --I--> " + built.ifAfterI() + "\n"));
                assertEquals(true, nondeterministic.contains("epsilonTransitions:"));
                assertEquals(true, nondeterministic.contains("-> IF"));
                assertEquals(true, deterministic.contains("DeterministicFiniteAutomaton{"));
                assertEquals(true, deterministic.contains("acceptingStates:"));
                assertEquals(true, deterministic.contains("-> IDENTIFIER"));
        }

    @Test
    void rendersOneTransitionPerLine() {
        NondeterministicFiniteAutomaton.Builder<Input, Token> builder =
                new NondeterministicFiniteAutomaton.Builder<>();
        State q0 = builder.addState();
        State q1 = builder.addState();
        State q2 = builder.addState();
        builder.initialState(q0)
                .transition(q0, Input.SYMBOL, q1)
                .transition(q0, Input.SYMBOL, q2)
                .epsilon(q1, q2)
                .accepting(q2, Token.IF);

        assertEquals("""
                NondeterministicFiniteAutomaton{
                  initialState: q0
                  states: [q0, q1, q2]
                  transitions:
                    q0 --SYMBOL--> q1
                    q0 --SYMBOL--> q2
                  epsilonTransitions:
                    q1 --epsilon--> q2
                  acceptingStates:
                    q2 -> IF
                }""", builder.build().toString());
    }

    @Test
    void rendersNondeterministicAutomatonAsMermaid() {
        NondeterministicFiniteAutomaton.Builder<String, Token> builder =
                new NondeterministicFiniteAutomaton.Builder<>();
        State q0 = builder.addState();
        State q1 = builder.addState();
        State q2 = builder.addState();
        builder.initialState(q0)
                .transition(q0, "a", q1)
                .transition(q0, "a", q2)
                .transition(q1, "say \"<hi>\"", q1)
                .epsilon(q1, q2)
                .accepting(q2, Token.IF);

        assertEquals("""
                flowchart LR
                    q0(("q0"))
                    q1(("q1"))
                    q2((("q2<br/>IF")))
                    start(["start"]) --> q0
                    q0 -->|"a"| q1
                    q0 -->|"a"| q2
                    q1 -->|"say #quot;#lt;hi#gt;#quot;"| q1
                    q1 -.->|"#949;"| q2
                """, builder.build().toMermaid());
    }

    @Test
    void rendersDeterministicAutomatonAsMermaid() {
        DeterministicFiniteAutomaton.Builder<Input, Token> builder =
                new DeterministicFiniteAutomaton.Builder<>();
        State q0 = builder.addState();
        State q1 = builder.addState();
        builder.initialState(q0)
                .transition(q0, Input.SYMBOL, q1)
                .transition(q1, Input.SYMBOL, q1)
                .accepting(q1, Token.IDENTIFIER);

        assertEquals("""
                flowchart LR
                    q0(("q0"))
                    q1((("q1<br/>IDENTIFIER")))
                    start(["start"]) --> q0
                    q0 -->|"SYMBOL"| q1
                    q1 -->|"SYMBOL"| q1
                """, builder.build().toMermaid());
    }

    private enum Input {
        SYMBOL
    }

    private enum Token {
        IF,
        IDENTIFIER
    }
}