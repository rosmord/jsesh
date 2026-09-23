package org.qenherkhopeshef.mdwlexer.automata;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/// An immutable epsilon-NDFA. A final state's value is the token type accepted
/// at that state; non-final states have no value.
///
/// @param <I> the type of input symbols
/// @param <T> the token type
///
/// The NFDA should be built using the [Builder] class.
public final class NondeterministicFiniteAutomaton<I, T> implements FiniteAutomaton<I, T> {
    private final State initialState;
    private final Set<State> states;
    private final Map<State, Map<I, Set<State>>> transitions;
    private final Map<State, Set<State>> epsilonTransitions;
    private final Map<State, T> acceptingStates;

    private NondeterministicFiniteAutomaton(
            State initialState,
            Set<State> states,
            Map<State, Map<I, Set<State>>> transitions,
            Map<State, Set<State>> epsilonTransitions,
            Map<State, T> acceptingStates) {
        this.initialState = initialState;
        this.states = immutableSet(states);
        this.transitions = immutableTransitionMap(transitions);
        this.epsilonTransitions = immutableSetMap(epsilonTransitions);
        this.acceptingStates = Collections.unmodifiableMap(new LinkedHashMap<>(acceptingStates));
    }

    @Override
    public State initialState() {
        return initialState;
    }

    @Override
    public Set<State> states() {
        return states;
    }

    @Override
    public Set<I> inputs() {
        Set<I> inputs = new LinkedHashSet<>();
        transitions.values().forEach(outgoing -> inputs.addAll(outgoing.keySet()));
        return Collections.unmodifiableSet(inputs);
    }

    @Override
    public Set<State> transitions(State state, I input) {
        return transitions.getOrDefault(state, Map.of()).getOrDefault(input, Set.of());
    }

    @Override
    public Set<State> epsilonTransitions(State state) {
        return epsilonTransitions.getOrDefault(state, Set.of());
    }

    @Override
    public Optional<T> acceptingTokenType(State state) {
        return Optional.ofNullable(acceptingStates.get(state));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("NondeterministicFiniteAutomaton{\n");
        result.append("  initialState: ").append(initialState).append('\n');
        result.append("  states: ").append(states).append('\n');
        result.append("  transitions:\n");
        Set<I> inputs = inputs();
        for (State state : states) {
            for (I input : inputs) {
                for (State target : transitions(state, input)) {
                    result.append("    ").append(state).append(" --").append(input)
                            .append("--> ").append(target).append('\n');
                }
            }
        }
        result.append("  epsilonTransitions:\n");
        for (State state : states) {
            for (State target : epsilonTransitions(state)) {
                result.append("    ").append(state).append(" --epsilon--> ").append(target).append('\n');
            }
        }
        result.append("  acceptingStates:\n");
        for (State state : states) {
            acceptingTokenType(state).ifPresent(tokenType -> result.append("    ")
                    .append(state).append(" -> ").append(tokenType).append('\n'));
        }
        return result.append('}').toString();
    }

    private static <E> Set<E> immutableSet(Set<E> values) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(values));
    }

    private static <K, V> Map<K, Set<V>> immutableSetMap(Map<K, Set<V>> values) {
        Map<K, Set<V>> copy = new LinkedHashMap<>();
        values.forEach((key, value) -> copy.put(key, immutableSet(value)));
        return Collections.unmodifiableMap(copy);
    }

    private static <I> Map<State, Map<I, Set<State>>> immutableTransitionMap(
            Map<State, Map<I, Set<State>>> values) {
        Map<State, Map<I, Set<State>>> copy = new LinkedHashMap<>();
        values.forEach((state, outgoing) -> copy.put(state, immutableSetMap(outgoing)));
        return Collections.unmodifiableMap(copy);
    }

    public static final class Builder<I, T> {
        private final Set<State> states = new LinkedHashSet<>();
        private final Map<State, Map<I, Set<State>>> transitions = new LinkedHashMap<>();
        private final Map<State, Set<State>> epsilonTransitions = new LinkedHashMap<>();
        private final Map<State, T> acceptingStates = new LinkedHashMap<>();
        private State initialState;

        public State addState() {
            State state = new State(states.size());
            states.add(state);
            return state;
        }

        public Builder<I, T> initialState(State state) {
            requireState(state);
            initialState = state;
            return this;
        }

        public Builder<I, T> accepting(State state, T tokenType) {
            requireState(state);
            acceptingStates.put(state, Objects.requireNonNull(tokenType, "tokenType"));
            return this;
        }

        public Builder<I, T> transition(State from, I input, State to) {
            requireState(from);
            requireState(to);
            Objects.requireNonNull(input, "input");
            transitions.computeIfAbsent(from, ignored -> new LinkedHashMap<>())
                    .computeIfAbsent(input, ignored -> new LinkedHashSet<>())
                    .add(to);
            return this;
        }

        public Builder<I, T> epsilon(State from, State to) {
            requireState(from);
            requireState(to);
            epsilonTransitions.computeIfAbsent(from, ignored -> new LinkedHashSet<>()).add(to);
            return this;
        }

        public NondeterministicFiniteAutomaton<I, T> build() {
            if (initialState == null) {
                throw new IllegalStateException("An initial state is required");
            }
            return new NondeterministicFiniteAutomaton<>(
                    initialState, states, transitions, epsilonTransitions, acceptingStates);
        }

        private void requireState(State state) {
            if (!states.contains(state)) {
                throw new IllegalArgumentException("State does not belong to this automaton: " + state);
            }
        }
    }
}