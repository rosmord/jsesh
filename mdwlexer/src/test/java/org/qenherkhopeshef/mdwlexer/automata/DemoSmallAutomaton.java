/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer.automata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

/// A test for building and manipulating a small automaton.
/// Corresponds basically to "if" vs identifier.
///
///  - "if" is token 0, token 1 ;
///  - "identifier" will recognize any token (0, 1 or 2) once,
///    plus then any number of tokens.
///  - token 4 will lead to a dead state.
public class DemoSmallAutomaton {
    public enum Token {
        IF,
        IDENTIFIER
    }

    @Test
    void buildsTheSmallAutomatonWithTokenPriority() {
        DemoSmallAutomatonHelper.BuiltAutomaton built = DemoSmallAutomatonHelper.build();
        NondeterministicFiniteAutomaton<DemoSmallAutomatonHelper.Input, Token> automaton =
            built.automaton();
        State start = built.start();
        State ifAfterI = built.ifAfterI();
        State ifAccepted = built.ifAccepted();
        State identifier = built.identifier();
        State dead = built.dead();

        assertEquals(start, automaton.initialState());
        assertEquals(Set.of(start, ifAfterI, ifAccepted, identifier, dead), automaton.states());
        assertEquals(Set.of(ifAfterI, identifier), automaton.epsilonTransitions(start));
        assertEquals(Set.of(ifAfterI), automaton.transitions(start, DemoSmallAutomatonHelper.Input.I));
        assertEquals(Set.of(ifAccepted), automaton.transitions(ifAfterI, DemoSmallAutomatonHelper.Input.F));
        assertEquals(Set.of(identifier), automaton.transitions(
            identifier, DemoSmallAutomatonHelper.Input.IDENTIFIER_CHARACTER));
        assertEquals(Set.of(dead), automaton.transitions(start, DemoSmallAutomatonHelper.Input.FOUR));
        assertEquals(Token.IF, automaton.acceptingTokenType(ifAccepted).orElseThrow());
        assertEquals(Token.IDENTIFIER, automaton.acceptingTokenType(identifier).orElseThrow());
        assertEquals(Token.IF, Token.values()[0]);
        assertEquals(Token.IDENTIFIER, Token.values()[1]);
    }
}
