/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer.automata;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/// Read-only view shared by [NondeterministicFiniteAutomaton] and
/// [DeterministicFiniteAutomaton].
///
/// Captures the operations that are meaningful regardless of determinism, so
/// that algorithms which don't care about the distinction (e.g. rendering, via the
/// default [#toMermaid()]) can be written once against this interface.
/// [#epsilonTransitions] always returns an empty set here, since a
/// deterministic automaton has none; [NondeterministicFiniteAutomaton]
/// overrides it. Algorithms that rely on genuine DFA guarantees (a single target
/// per input, completion, complement, intersection, ...) should keep using
/// [DeterministicFiniteAutomaton] directly, which exposes additional,
/// DFA-specific methods.
///
/// @param <I> the type of input symbols
/// @param <T> the token type attached to accepting states
public interface FiniteAutomaton<I, T> {
    State initialState();

    Set<State> states();

    Set<I> inputs();

    /// States reachable from `state` by consuming `input`, ignoring
    /// epsilon transitions. A deterministic automaton never returns more than
    /// one state.
    Set<State> transitions(State state, I input);

    /// The token type recognized by this state, if any.
    Optional<T> acceptingTokenType(State state);

    /// States reachable from `state` by an epsilon transition, consuming no input.
    /// Always empty for a deterministic automaton, which has none.
    default Set<State> epsilonTransitions(State state) {
        return Set.of();
    }

    /// Renders this automaton as a Mermaid flowchart, e.g. to paste into a
    /// `mermaid` code block or a `.mmd` file. Transitions are labelled with
    /// `String.valueOf(input)`; use [#toMermaid(Function)] to supply more
    /// readable labels (e.g. when `I` is a raw character class id).
    default String toMermaid() {
        return toMermaid(String::valueOf);
    }

    /// Renders this automaton as a Mermaid flowchart, like [#toMermaid()], but
    /// labelling each transition with `inputLabel.apply(input)` instead of
    /// `input`'s own `toString()` — useful when `I` isn't human-readable
    /// on its own (e.g. a `CharacterClassifier` class id standing in for a set of
    /// code points).
    default String toMermaid(Function<I, String> inputLabel) {
        MermaidDiagram diagram = new MermaidDiagram();
        states().forEach(state -> diagram.state(state, acceptingTokenType(state).orElse(null)));
        diagram.initialState(initialState());
        for (State state : states()) {
            for (I input : inputs()) {
                transitions(state, input).forEach(target -> diagram.transition(state, inputLabel.apply(input), target));
            }
            epsilonTransitions(state).forEach(target -> diagram.epsilonTransition(state, target));
        }
        return diagram.toString();
    }
}
