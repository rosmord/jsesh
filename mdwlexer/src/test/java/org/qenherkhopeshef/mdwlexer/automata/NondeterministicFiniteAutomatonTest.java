package org.qenherkhopeshef.mdwlexer.automata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

class NondeterministicFiniteAutomatonTest {
    @Test
    void representsInputAndEpsilonTransitionsAndAcceptingTokens() {
        NondeterministicFiniteAutomaton.Builder<Integer, Token> builder =
                new NondeterministicFiniteAutomaton.Builder<>();
        State start = builder.addState();
        State branch = builder.addState();
        State accepting = builder.addState();

        builder.initialState(start)
                .epsilon(start, branch)
                .transition(branch, 7, accepting)
                .accepting(accepting, Token.WORD);

        NondeterministicFiniteAutomaton<Integer, Token> automaton = builder.build();

        assertEquals(Set.of(branch), automaton.epsilonTransitions(start));
        assertEquals(Set.of(accepting), automaton.transitions(branch, 7));
        assertEquals(java.util.Optional.of(Token.WORD), automaton.acceptingTokenType(accepting));
    }

    @Test
    void requiresAnInitialState() {
        NondeterministicFiniteAutomaton.Builder<Integer, Token> builder =
                new NondeterministicFiniteAutomaton.Builder<>();
        builder.addState();

        assertThrows(IllegalStateException.class, builder::build);
    }

    private enum Token {
        WORD
    }
}