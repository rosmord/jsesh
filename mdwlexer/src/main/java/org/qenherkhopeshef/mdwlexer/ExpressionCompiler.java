package org.qenherkhopeshef.mdwlexer;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qenherkhopeshef.mdwlexer.Expression.CharacterSet;
import org.qenherkhopeshef.mdwlexer.Expression.Repeat;
import org.qenherkhopeshef.mdwlexer.automata.AutomataHelper;
import org.qenherkhopeshef.mdwlexer.automata.DeterministicFiniteAutomaton;
import org.qenherkhopeshef.mdwlexer.automata.NondeterministicFiniteAutomaton;
import org.qenherkhopeshef.mdwlexer.automata.State;

/// Compiles [Expression]s into an epsilon-NFA (Thompson construction) over character
/// classes rather than raw code points: the input alphabet `I` of the resulting
/// automaton is the integer class id assigned by a [CharacterClassifier], so a state
/// gets one transition per distinguishable class of characters instead of one per code point.
///
/// [Expression.Complement] and [Expression.Intersection] can't be built by
/// gluing NFA fragments (see their javadoc): instead their operands are each compiled and
/// determinized on their own, combined as DFAs (complement over every class the
/// [CharacterClassifier] knows about, so it agrees with the rest of the compiled rules
/// on what the alphabet is; intersection via the product construction), and spliced back in
/// as a fragment.
public final class ExpressionCompiler {
    private ExpressionCompiler() {
    }

    /// The result of compiling a set of rules: the shared NFA, and the classifier it was built with.
    public record Result<T>(NondeterministicFiniteAutomaton<Integer, T> automaton, CharacterClassifier classifier) {
    }

    /// Compiles a single expression, recognized as `tokenType`, into an NFA.
    public static <T> Result<T> compile(Expression expression, T tokenType) {
        return compile(Map.of(tokenType, expression));
    }

    /// Compiles a set of rules into one shared NFA: every rule's expression becomes a branch
    /// from a common initial state, accepting as its associated token type.
    public static <T> Result<T> compile(Map<T, Expression> rules) {
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("At least one rule is required");
        }
        CharacterClassifier classifier = CharacterClassifier.forExpressions(rules.values());
        NondeterministicFiniteAutomaton.Builder<Integer, T> builder = new NondeterministicFiniteAutomaton.Builder<>();
        State start = builder.addState();
        builder.initialState(start);
        rules.forEach((tokenType, expression) -> {
            Fragment fragment = compileFragment(expression, classifier, builder);
            builder.epsilon(start, fragment.start());
            builder.accepting(fragment.end(), tokenType);
        });
        return new Result<>(builder.build(), classifier);
    }

    /// An NFA fragment: an entry and an exit state, with no transitions outside of it.
    private record Fragment(State start, State end) {
    }

    private static <T> Fragment compileFragment(
            Expression expression,
            CharacterClassifier classifier,
            NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        return switch (expression) {
            case Expression.Epsilon ignored -> epsilonFragment(builder);
            case CharacterSet set -> characterSetFragment(set, classifier, builder);
            case Expression.Sequence sequence -> sequenceFragment(sequence.parts(), classifier, builder);
            case Expression.Union union -> unionFragment(union.alternatives(), classifier, builder);
            case Repeat repeat -> repeatFragment(repeat, classifier, builder);
            case Expression.Intersection intersection -> intersectionFragment(intersection, classifier, builder);
            case Expression.Complement complement -> complementFragment(complement, classifier, builder);
        };
    }

    /// The marker token type used to build standalone, throwaway DFAs for complement/intersection operands.
    private enum Marker {
        MATCHES_OPERAND
    }

    private static <T> Fragment complementFragment(
            Expression.Complement complement,
            CharacterClassifier classifier,
            NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        DeterministicFiniteAutomaton<Integer, Marker> operandDfa =
                determinizedOperandAutomaton(complement.operand(), classifier);
        return embed(AutomataHelper.complement(operandDfa, allClasses(classifier), Marker.MATCHES_OPERAND), builder);
    }

    private static <T> Fragment intersectionFragment(
            Expression.Intersection intersection,
            CharacterClassifier classifier,
            NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        DeterministicFiniteAutomaton<Integer, Marker> result = null;
        for (Expression operand : intersection.operands()) {
            DeterministicFiniteAutomaton<Integer, Marker> operandDfa =
                    determinizedOperandAutomaton(operand, classifier);
            result = result == null
                    ? operandDfa
                    : AutomataHelper.intersect(result, operandDfa, Marker.MATCHES_OPERAND);
        }
        return embed(result, builder);
    }

    /// Compiles `operand` on its own, as a standalone, determinized DFA.
    private static DeterministicFiniteAutomaton<Integer, Marker> determinizedOperandAutomaton(
            Expression operand, CharacterClassifier classifier) {
        NondeterministicFiniteAutomaton.Builder<Integer, Marker> operandBuilder =
                new NondeterministicFiniteAutomaton.Builder<>();
        Fragment operandFragment = compileFragment(operand, classifier, operandBuilder);
        operandBuilder.initialState(operandFragment.start());
        operandBuilder.accepting(operandFragment.end(), Marker.MATCHES_OPERAND);
        return AutomataHelper.determinize(operandBuilder.build());
    }

    /// Every class id `classifier` knows about, i.e. its whole alphabet.
    private static Set<Integer> allClasses(CharacterClassifier classifier) {
        Set<Integer> classes = new LinkedHashSet<>();
        for (int characterClass = 0; characterClass < classifier.classCount(); characterClass++) {
            classes.add(characterClass);
        }
        return classes;
    }

    /// Copies a DFA's states and transitions into `builder` as one fragment: every accepting DFA state epsilon-joins the fragment's exit.
    private static <T> Fragment embed(
            DeterministicFiniteAutomaton<Integer, ?> automaton,
            NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        Map<State, State> copies = new LinkedHashMap<>();
        for (State state : automaton.states()) {
            copies.put(state, builder.addState());
        }
        for (State state : automaton.states()) {
            for (int input : automaton.inputs()) {
                automaton.transition(state, input)
                        .ifPresent(target -> builder.transition(copies.get(state), input, copies.get(target)));
            }
        }
        State end = builder.addState();
        for (State state : automaton.states()) {
            if (automaton.acceptingTokenType(state).isPresent()) {
                builder.epsilon(copies.get(state), end);
            }
        }
        return new Fragment(copies.get(automaton.initialState()), end);
    }

    private static <T> Fragment epsilonFragment(NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        State start = builder.addState();
        State end = builder.addState();
        builder.epsilon(start, end);
        return new Fragment(start, end);
    }

    private static <T> Fragment characterSetFragment(
            CharacterSet set,
            CharacterClassifier classifier,
            NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        State start = builder.addState();
        State end = builder.addState();
        for (int characterClass : classifier.classesOf(set)) {
            builder.transition(start, characterClass, end);
        }
        return new Fragment(start, end);
    }

    private static <T> Fragment sequenceFragment(
            List<Expression> parts,
            CharacterClassifier classifier,
            NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        Fragment result = null;
        for (Expression part : parts) {
            Fragment next = compileFragment(part, classifier, builder);
            result = result == null ? next : concat(result, next, builder);
        }
        return result;
    }

    private static <T> Fragment unionFragment(
            List<Expression> alternatives,
            CharacterClassifier classifier,
            NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        State start = builder.addState();
        State end = builder.addState();
        for (Expression alternative : alternatives) {
            Fragment fragment = compileFragment(alternative, classifier, builder);
            builder.epsilon(start, fragment.start());
            builder.epsilon(fragment.end(), end);
        }
        return new Fragment(start, end);
    }

    private static <T> Fragment repeatFragment(
            Repeat repeat,
            CharacterClassifier classifier,
            NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        Expression operand = repeat.operand();
        int min = repeat.min();
        int max = repeat.max();

        if (max == Repeat.UNBOUNDED) {
            if (min == 0) {
                return star(compileFragment(operand, classifier, builder), builder);
            }
            if (min == 1) {
                return plus(compileFragment(operand, classifier, builder), builder);
            }
            Fragment mandatory = repeatExactly(operand, min - 1, classifier, builder);
            Fragment tail = plus(compileFragment(operand, classifier, builder), builder);
            return concat(mandatory, tail, builder);
        }

        Fragment result = repeatExactly(operand, min, classifier, builder);
        for (int i = 0; i < max - min; i++) {
            Fragment optionalCopy = optional(compileFragment(operand, classifier, builder), builder);
            result = concat(result, optionalCopy, builder);
        }
        return result;
    }

    /// `count` fresh copies of `operand` chained in sequence; [#epsilonFragment] if zero.
    private static <T> Fragment repeatExactly(
            Expression operand,
            int count,
            CharacterClassifier classifier,
            NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        if (count == 0) {
            return epsilonFragment(builder);
        }
        Fragment result = compileFragment(operand, classifier, builder);
        for (int i = 1; i < count; i++) {
            result = concat(result, compileFragment(operand, classifier, builder), builder);
        }
        return result;
    }

    private static <T> Fragment concat(
            Fragment first, Fragment second, NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        builder.epsilon(first.end(), second.start());
        return new Fragment(first.start(), second.end());
    }

    /// Thompson construction for `inner*` (zero or more).
    private static <T> Fragment star(Fragment inner, NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        State start = builder.addState();
        State end = builder.addState();
        builder.epsilon(start, inner.start());
        builder.epsilon(start, end);
        builder.epsilon(inner.end(), inner.start());
        builder.epsilon(inner.end(), end);
        return new Fragment(start, end);
    }

    /// Thompson construction for `inner+` (one or more).
    private static <T> Fragment plus(Fragment inner, NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        State start = builder.addState();
        State end = builder.addState();
        builder.epsilon(start, inner.start());
        builder.epsilon(inner.end(), inner.start());
        builder.epsilon(inner.end(), end);
        return new Fragment(start, end);
    }

    /// Thompson construction for `inner?` (zero or one).
    private static <T> Fragment optional(
            Fragment inner, NondeterministicFiniteAutomaton.Builder<Integer, T> builder) {
        State start = builder.addState();
        State end = builder.addState();
        builder.epsilon(start, inner.start());
        builder.epsilon(start, end);
        builder.epsilon(inner.end(), end);
        return new Fragment(start, end);
    }
}
