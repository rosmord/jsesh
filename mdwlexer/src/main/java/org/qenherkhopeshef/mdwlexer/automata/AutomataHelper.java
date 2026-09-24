/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer.automata;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/// Operations for converting between automaton representations.
///
/// Basically, all the interesting algorithms are here.
public final class AutomataHelper {
    private AutomataHelper() {
    }

    /// Produces a deterministic finite automaton equivalent to the given nondeterministic one.
    ///
    /// The resulting automaton is not minimized.
    /// @param <I> the alphabet type (input symbols)
    /// @param <T> the token type (enum)
    /// @param nondeterministic
    /// @return
    public static <I, T extends Enum<T>> DeterministicFiniteAutomaton<I, T> determinize(
            NondeterministicFiniteAutomaton<I, T> nondeterministic) {
        DeterministicFiniteAutomaton.Builder<I, T> deterministicBuilder =
                new DeterministicFiniteAutomaton.Builder<>();
        Map<Set<State>, State> deterministicStates = new LinkedHashMap<>();
        Deque<Set<State>> pending = new ArrayDeque<>();

        // Create the initial state, associated with the epsilon closure of
        // the initial state of the nondeterministic automaton.
        Set<State> initialSubset = epsilonClosure(nondeterministic, Set.of(nondeterministic.initialState()));
        State initialState = deterministicBuilder.addState();
        deterministicBuilder.initialState(initialState);
        deterministicStates.put(initialSubset, initialState);
        pending.add(initialSubset);

        // extend all candidate states.
        while (!pending.isEmpty()) {
            Set<State> subset = pending.remove();
            State from = deterministicStates.get(subset);
            // If the subset contains accepting states, find the one
            // with the highest priority (lowest ordinal) and declare the new state
            // as accepting for that token type.
            acceptingTokenType(nondeterministic, subset)
                    .ifPresent(tokenType -> deterministicBuilder.accepting(from, tokenType));

            // Extends the state for each possible input.
            for (I input : nondeterministic.inputs()) {
                // The states directly reachable (without epsilon transitions)
                Set<State> directTargets = new LinkedHashSet<>();
                for (State state : subset) {
                    directTargets.addAll(nondeterministic.transitions(state, input));
                }
                if (directTargets.isEmpty()) {
                    continue;
                }

                Set<State> targetSubset = epsilonClosure(nondeterministic, directTargets);
                State target = deterministicStates.get(targetSubset);
                if (target == null) {
                    target = deterministicBuilder.addState();
                    deterministicStates.put(targetSubset, target);
                    pending.add(targetSubset);
                }
                deterministicBuilder.transition(from, input, target);
            }
        }

        return deterministicBuilder.build();
    }

    /// Minimize a deterministic finite automaton.
    /// Moore's algorithm.
    /// @param <I> the alphabet type (input symbols)
    /// @param <T> the token type (enum)
    /// @param automaton the source automaton
    /// @return a new deterministic finite automaton, equivalent to the given one, but with the minimum number of states.
    public static <I, T> DeterministicFiniteAutomaton<I, T> minimize(
            DeterministicFiniteAutomaton<I, T> automaton) {
        Set<State> reachableStates = reachableStates(automaton);
        // Create a partition of the states : non final and finals depending on resulting token type.
        List<Set<State>> partitions = initialPartitions(automaton, reachableStates);

        // Yet another closure:
        boolean changed;
        do {
            Map<State, Integer> partitionByState = partitionByState(partitions);
            List<Set<State>> refined = new ArrayList<>();
            for (Set<State> partition : partitions) {
                // Build a map from signature to sets of states with that signature
                Map<MinimizationSignature<I, T>, Set<State>> signaturesToStateSet = new LinkedHashMap<>();
                for (State state : partition) {                    
                    MinimizationSignature<I, T> signature = signature(
                            automaton, state, partitionByState);
                    Set<State> signatureSet = signaturesToStateSet.computeIfAbsent(signature, ignored -> new LinkedHashSet<>());
                    signatureSet.add(state);
                }
                // The new partition will contain all the sets of states with the same signature.
                refined.addAll(signaturesToStateSet.values());
            }
            changed = !refined.equals(partitions); // Is the new partition different from the previous one
            partitions = refined; // update
        } while (changed);

        // Build the new automaton from the partition.
        Map<State, Integer> partitionByState = partitionByState(partitions);
        DeterministicFiniteAutomaton.Builder<I, T> builder =
                new DeterministicFiniteAutomaton.Builder<>();
        Map<Integer, State> minimizedStates = new LinkedHashMap<>();
        Deque<Integer> pending = new ArrayDeque<>();
        int initialPartition = partitionByState.get(automaton.initialState());
        minimizedStates.put(initialPartition, builder.addState());
        builder.initialState(minimizedStates.get(initialPartition));
        pending.add(initialPartition);

        while (!pending.isEmpty()) {
            int partitionIndex = pending.remove();
            Set<State> partition = partitions.get(partitionIndex);
            State minimizedState = minimizedStates.get(partitionIndex);
            State representative = partition.iterator().next();
            automaton.acceptingTokenType(representative)
                    .ifPresent(tokenType -> builder.accepting(minimizedState, tokenType));

            for (I input : automaton.inputs()) {
                Optional<State> target = automaton.transition(representative, input);
                if (target.isEmpty()) {
                    continue;
                }
                int targetPartition = partitionByState.get(target.get());
                State minimizedTarget = minimizedStates.get(targetPartition);
                if (minimizedTarget == null) {
                    minimizedTarget = builder.addState();
                    minimizedStates.put(targetPartition, minimizedTarget);
                    pending.add(targetPartition);
                }
                builder.transition(minimizedState, input, minimizedTarget);
            }
        }

        return builder.build();
    }

    /// Complements a DFA relative to `alphabet`: the result accepts exactly the strings
    /// over `alphabet` that `automaton` rejects, including strings that run off a
    /// missing transition (a DFA that doesn't explicitly reject an input is treated as if it
    /// did). This is only the classical automaton complement — every string `automaton`
    /// doesn't match — when `alphabet` is the automaton's whole intended input alphabet,
    /// not just the symbols it happens to have transitions for, so pass every symbol the result
    /// may ever need to consume, even ones `automaton` itself never transitions on.
    /// @param <I> the alphabet type (input symbols)
    /// @param <T> the token type to mark newly-accepting states with
    /// @param automaton the automaton to complement
    /// @param alphabet every input symbol the result must handle; must be a superset of `automaton.inputs()`
    /// @param tokenType the token type recorded on every state that becomes accepting
    /// @return a new automaton accepting the complement language
    public static <I, T> DeterministicFiniteAutomaton<I, T> complement(
            DeterministicFiniteAutomaton<I, ?> automaton, Set<I> alphabet, T tokenType) {
        if (!alphabet.containsAll(automaton.inputs())) {
            throw new IllegalArgumentException(
                    "alphabet must cover every input the automaton already transitions on");
        }
        DeterministicFiniteAutomaton.Builder<I, T> builder = new DeterministicFiniteAutomaton.Builder<>();
        Map<State, State> copies = new LinkedHashMap<>();
        for (State state : automaton.states()) {
            copies.put(state, builder.addState());
        }
        State dead = builder.addState();
        builder.initialState(copies.get(automaton.initialState()));

        for (State state : automaton.states()) {
            State copy = copies.get(state);
            for (I input : alphabet) {
                State target = automaton.transition(state, input).map(copies::get).orElse(dead);
                builder.transition(copy, input, target);
            }
            if (automaton.acceptingTokenType(state).isEmpty()) {
                builder.accepting(copy, tokenType);
            }
        }
        for (I input : alphabet) {
            builder.transition(dead, input, dead);
        }
        builder.accepting(dead, tokenType);

        return builder.build();
    }

    /// Intersects two DFAs via the product construction: the result accepts exactly the
    /// strings that both `first` and `second` accept. A missing transition in
    /// either operand is treated as an implicit reject, so the product has a transition on
    /// an input only where both operands do; there's no need to complete either DFA first.
    /// @param <I> the alphabet type (input symbols)
    /// @param <T> the token type to mark newly-accepting states with
    /// @param first one automaton to intersect
    /// @param second the other automaton to intersect
    /// @param tokenType the token type recorded on every state that becomes accepting
    /// @return a new automaton accepting the intersection language
    public static <I, T> DeterministicFiniteAutomaton<I, T> intersect(
            DeterministicFiniteAutomaton<I, ?> first, DeterministicFiniteAutomaton<I, ?> second, T tokenType) {
        DeterministicFiniteAutomaton.Builder<I, T> builder = new DeterministicFiniteAutomaton.Builder<>();
        Set<I> alphabet = new LinkedHashSet<>(first.inputs());
        alphabet.addAll(second.inputs());

        Map<StatePair, State> productStates = new LinkedHashMap<>();
        Deque<StatePair> pending = new ArrayDeque<>();

        StatePair initialPair = new StatePair(first.initialState(), second.initialState());
        State initialState = builder.addState();
        builder.initialState(initialState);
        productStates.put(initialPair, initialState);
        pending.add(initialPair);

        while (!pending.isEmpty()) {
            StatePair pair = pending.remove();
            State from = productStates.get(pair);
            if (first.acceptingTokenType(pair.first()).isPresent()
                    && second.acceptingTokenType(pair.second()).isPresent()) {
                builder.accepting(from, tokenType);
            }

            for (I input : alphabet) {
                Optional<State> firstTarget = first.transition(pair.first(), input);
                Optional<State> secondTarget = second.transition(pair.second(), input);
                if (firstTarget.isEmpty() || secondTarget.isEmpty()) {
                    continue;
                }
                StatePair targetPair = new StatePair(firstTarget.get(), secondTarget.get());
                State target = productStates.get(targetPair);
                if (target == null) {
                    target = builder.addState();
                    productStates.put(targetPair, target);
                    pending.add(targetPair);
                }
                builder.transition(from, input, target);
            }
        }

        return builder.build();
    }

    /// A pair of states, one from each operand automaton of [#intersect].
    private record StatePair(State first, State second) {
    }

    /// Compute states reachable from the initial state by transitive closure of the transition relation.
    /// @param <I>
    /// @param <T>
    /// @param automaton
    /// @return
    private static <I, T> Set<State> reachableStates(DeterministicFiniteAutomaton<I, T> automaton) {
        Set<State> reachable = new LinkedHashSet<>();
        Deque<State> pending = new ArrayDeque<>();
        reachable.add(automaton.initialState());
        pending.add(automaton.initialState());
        while (!pending.isEmpty()) {
            State state = pending.remove();
            for (I input : automaton.inputs()) {
                Optional<State> target = automaton.transition(state, input);
                if (target.isPresent() && reachable.add(target.get())) {
                    pending.add(target.get());
                }
            }
        }
        return reachable;
    }

    /// Compute the initial partition of states for minimization.
    /// @param <I>
    /// @param <T>
    /// @param automaton
    /// @param states
    /// @return
    private static <I, T> List<Set<State>> initialPartitions(
            DeterministicFiniteAutomaton<I, T> automaton,
            Set<State> states) {
        Map<Optional<T>, Set<State>> groups = new LinkedHashMap<>();
        for (State state : states) {
            groups.computeIfAbsent(automaton.acceptingTokenType(state), ignored -> new LinkedHashSet<>()).add(state);
        }
        return new ArrayList<>(groups.values());
    }

    /// Map states to partition index in the list.
    /// @param partitions
    /// @return
    private static Map<State, Integer> partitionByState(List<Set<State>> partitions) {
        Map<State, Integer> result = new LinkedHashMap<>();
        for (int index = 0; index < partitions.size(); index++) {
            for (State state : partitions.get(index)) {
                result.put(state, index);
            }
        }
        return result;
    }

    /// Returns a couple of the token type accepted by the state, if any,
    /// and a list giving for each input the index of the partition it leads to.
    ///
    /// the partition indexes will correspond to any partition which can be reached directly from the state, with any input.
    /// @param <I>
    /// @param <T>
    /// @param automaton the original deterministic finite automaton
    /// @param state the state for which we compute the signature
    /// @param partitionByState a map from states to partition indexes.
    /// @return the minimization signature of this state.
    private static <I, T> MinimizationSignature<I, T> signature(
            DeterministicFiniteAutomaton<I, T> automaton,
            State state,
            Map<State, Integer> partitionByState) {
        List<Integer> targets = new ArrayList<>();
        for (I input : automaton.inputs()) {
            targets.add(automaton.transition(state, input)
                    .map(partitionByState::get)
                    .orElse(-1));
        }
        return new MinimizationSignature<>(automaton.acceptingTokenType(state), targets);
    }

    /// A couple : (optional token type, reacheable partition for each input).
    /// MinimizationSignature
    /// @param tokenType
    /// @param targets
    private record MinimizationSignature<I, T>(Optional<T> tokenType, List<Integer> targets) {
        private MinimizationSignature {
            Objects.requireNonNull(tokenType);
            targets = List.copyOf(targets);
        }
    }

    /// Compute the epsilon closure of a set of states in a nondeterministic finite automaton.
    ///
    /// The epsilon closure of a set of states is the set of states reachable from those states
    /// by following zero or more epsilon transitions.
    /// @param <I> the alphabet type (input symbols)
    /// @param <T> output token type (enum), when the automaton is used as a lexer.
    /// @param automaton
    /// @param states
    /// @return
    private static <I, T> Set<State> epsilonClosure(
            NondeterministicFiniteAutomaton<I, T> automaton,
            Set<State> states) {
        Set<State> closure = new LinkedHashSet<>(states);
        Deque<State> pending = new ArrayDeque<>(states);
        while (!pending.isEmpty()) {
            State state = pending.remove();
            for (State target : automaton.epsilonTransitions(state)) {
                if (closure.add(target)) {
                    pending.add(target);
                }
            }
        }
        return closure;
    }

    /// Return the highest priority accepting token type for a set of states, if any.
    /// @param <I>
    /// @param <T>
    /// @param automaton
    /// @param states
    /// @return
    private static <I, T extends Enum<T>> Optional<T> acceptingTokenType(
            NondeterministicFiniteAutomaton<I, T> automaton,
            Set<State> states) {
        T highestPriority = null;
        for (State state : states) {
            Optional<T> tokenType = automaton.acceptingTokenType(state);
            if (tokenType.isPresent()
                    && (highestPriority == null || tokenType.get().ordinal() < highestPriority.ordinal())) {
                highestPriority = tokenType.get();
            }
        }
        return Optional.ofNullable(highestPriority);
    }
}