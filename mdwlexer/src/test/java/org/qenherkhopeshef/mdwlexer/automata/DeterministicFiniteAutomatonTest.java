package org.qenherkhopeshef.mdwlexer.automata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class DeterministicFiniteAutomatonTest {
    @Test
    void representsOneTargetPerInputAndAcceptingTokens() {
        DeterministicFiniteAutomaton.Builder<Integer, Token> builder =
                new DeterministicFiniteAutomaton.Builder<>();
        State start = builder.addState();
        State accepting = builder.addState();

        builder.initialState(start)
                .transition(start, 7, accepting)
                .accepting(accepting, Token.WORD);

        DeterministicFiniteAutomaton<Integer, Token> automaton = builder.build();

        assertEquals(Optional.of(accepting), automaton.transition(start, 7));
        assertEquals(Optional.of(Token.WORD), automaton.acceptingTokenType(accepting));
    }

    @Test
    void rejectsConflictingTargetsForOneInput() {
        DeterministicFiniteAutomaton.Builder<Integer, Token> builder =
                new DeterministicFiniteAutomaton.Builder<>();
        State start = builder.addState();
        State first = builder.addState();
        State second = builder.addState();

        builder.transition(start, 7, first);

        assertThrows(IllegalArgumentException.class, () -> builder.transition(start, 7, second));
    }

    private enum Token {
        WORD
    }
}