/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer.automata;

final class DemoSmallAutomatonHelper {
    enum Input {
        I,
        F,
        IDENTIFIER_CHARACTER,
        FOUR
    }

    record BuiltAutomaton(
            NondeterministicFiniteAutomaton<Input, DemoSmallAutomaton.Token> automaton,
            State start,
            State ifAfterI,
            State ifAccepted,
            State identifier,
            State dead) {
    }

    private DemoSmallAutomatonHelper() {
    }

    static BuiltAutomaton build() {
        NondeterministicFiniteAutomaton.Builder<Input, DemoSmallAutomaton.Token> builder =
                new NondeterministicFiniteAutomaton.Builder<>();
        State start = builder.addState();
        State ifAfterI = builder.addState();
        State ifAccepted = builder.addState();
        State identifier = builder.addState();
        State dead = builder.addState();

        builder.initialState(start)
                .epsilon(start, ifAfterI)
                .epsilon(start, identifier)
                .transition(start, Input.FOUR, dead)
                .transition(ifAfterI, Input.F, ifAccepted)
                .transition(start, Input.I, ifAfterI)
                .transition(identifier, Input.IDENTIFIER_CHARACTER, identifier)
                .accepting(ifAccepted, DemoSmallAutomaton.Token.IF)
                .accepting(identifier, DemoSmallAutomaton.Token.IDENTIFIER);

        return new BuiltAutomaton(builder.build(), start, ifAfterI, ifAccepted, identifier, dead);
    }
}