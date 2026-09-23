package org.qenherkhopeshef.mdwlexer.automata;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/// An immutable deterministic finite automaton without epsilon transitions.
public final class DeterministicFiniteAutomaton<I, T> implements FiniteAutomaton<I, T> {
    private final State initialState;
    private final Set<State> states;
    private final Map<State, Map<I, State>> transitions;
    private final Map<State, T> acceptingStates;

    private DeterministicFiniteAutomaton(
            State initialState,
            Set<State> states,
            Map<State, Map<I, State>> transitions,
            Map<State, T> acceptingStates) {
        this.initialState = initialState;
        this.states = Collections.unmodifiableSet(new LinkedHashSet<>(states));
        Map<State, Map<I, State>> transitionCopy = new LinkedHashMap<>();
        transitions.forEach((state, outgoing) ->
                transitionCopy.put(state, Collections.unmodifiableMap(new LinkedHashMap<>(outgoing))));
        this.transitions = Collections.unmodifiableMap(transitionCopy);
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

    /// The single state reachable from `state` by consuming `input`, if any.
    ///
    /// Prefer this over [#transitions(State, Object)] when the DFA's determinism
    /// is exactly what's needed: it returns the (at most one) target directly instead of a set.
    public Optional<State> transition(State state, I input) {
        return Optional.ofNullable(transitions.getOrDefault(state, Map.of()).get(input));
    }

    @Override
    public Set<State> transitions(State state, I input) {
        return transition(state, input).map(Set::of).orElseGet(Set::of);
    }

    /// The token type recognized by this state, if any.
    /// @param state
    /// @return
    @Override
    public Optional<T> acceptingTokenType(State state) {
        return Optional.ofNullable(acceptingStates.get(state));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("DeterministicFiniteAutomaton{\n");
        result.append("  initialState: ").append(initialState).append('\n');
        result.append("  states: ").append(states).append('\n');
        result.append("  transitions:\n");
        Set<I> inputs = inputs();
        for (State state : states) {
            for (I input : inputs) {
                transition(state, input).ifPresent(target -> result.append("    ")
                        .append(state).append(" --").append(input).append("--> ")
                        .append(target).append('\n'));
            }
        }
        result.append("  acceptingStates:\n");
        for (State state : states) {
            acceptingTokenType(state).ifPresent(tokenType -> result.append("    ")
                    .append(state).append(" -> ").append(tokenType).append('\n'));
        }
        return result.append('}').toString();
    }

    public static final class Builder<I, T> {
        private final Set<State> states = new LinkedHashSet<>();
        private final Map<State, Map<I, State>> transitions = new LinkedHashMap<>();
        private final Map<State, T> acceptingStates = new LinkedHashMap<>();
        private State initialState;

        public State addState() {
            State state = new State(states.size());
            states.add(state);
            return state;
        }

        /// Declare a state as _the_ initial state of the automaton.
        /// This method must be called exactly once before [#build()].
        /// @param state an existing state
        /// @return this builder for chaining.
        public Builder<I, T> initialState(State state) {
            requireState(state);
            initialState = state;
            return this;
        }

        /// Declares a state as accepting and associate it with one of the possible token types.
        /// @param state
        /// @param tokenType
        /// @return
        public Builder<I, T> accepting(State state, T tokenType) {
            requireState(state);
            acceptingStates.put(state, Objects.requireNonNull(tokenType, "tokenType"));
            return this;
        }

        public Builder<I, T> transition(State from, I input, State to) {
            requireState(from);
            requireState(to);
            Objects.requireNonNull(input, "input");
            Map<I, State> outgoing = transitions.computeIfAbsent(from, ignored -> new LinkedHashMap<>());
            State previous = outgoing.putIfAbsent(input, to);
            if (previous != null && !previous.equals(to)) {
                throw new IllegalArgumentException("A DFA input cannot have two target states");
            }
            return this;
        }

        public DeterministicFiniteAutomaton<I, T> build() {
            if (initialState == null) {
                throw new IllegalStateException("An initial state is required");
            }
            return new DeterministicFiniteAutomaton<>(initialState, states, transitions, acceptingStates);
        }

        private void requireState(State state) {
            if (!states.contains(state)) {
                throw new IllegalArgumentException("State does not belong to this automaton: " + state);
            }
        }
    }
}