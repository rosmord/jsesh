package org.qenherkhopeshef.mdwlexer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/// An immutable extended regular expression over Unicode code points.
///
/// Besides the classical operators (sequence, union, repetition), it has the boolean
/// operators [Intersection] and [Complement]. Such expressions still describe
/// regular languages, but intersection and complement cannot be built by gluing NFA
/// fragments: they have to go through a DFA.
///
/// An expression only describes a language. Which token it produces, and its priority,
/// are decided where the rules are collected, not here.
///
/// Expressions are meant to be created through [ExpressionBuilder].
public sealed interface Expression {

    /// Matches the empty string, and nothing else.
    record Epsilon() implements Expression {
    }

    /// Matches exactly one character, taken from a set of code points.
    ///
    /// The set is kept as sorted, non-overlapping, non-adjacent ranges, so equal sets are
    /// equal records. Negation is relative to the whole code point space
    /// ([Character#MIN_CODE_POINT] to [Character#MAX_CODE_POINT]) and is computed
    /// right away; there is no "negated" flag. An empty set matches nothing (the empty language).
    record CharacterSet(List<Range> ranges) implements Expression {
        public static final CharacterSet NONE = new CharacterSet(List.of());
        public static final CharacterSet ANY =
                new CharacterSet(List.of(new Range(Character.MIN_CODE_POINT, Character.MAX_CODE_POINT)));

        public CharacterSet {
            ranges = normalize(ranges);
        }

        public static CharacterSet of(int codepoint) {
            return range(codepoint, codepoint);
        }

        /// An inclusive range.
        public static CharacterSet range(int startCodepoint, int endCodepoint) {
            return new CharacterSet(List.of(new Range(startCodepoint, endCodepoint)));
        }

        /// The code points which are in this set, or in the other one.
        public CharacterSet union(CharacterSet other) {
            List<Range> all = new ArrayList<>(ranges);
            all.addAll(other.ranges);
            return new CharacterSet(all);
        }

        /// The code points which are in both this set and the other one.
        public CharacterSet intersection(CharacterSet other) {
            return negated().union(other.negated()).negated();
        }

        /// The code points which are in this set, but not in the other one.
        public CharacterSet minus(CharacterSet other) {
            return negated().union(other).negated();
        }

        /// Every code point which is not in this set. Note: this is one character, not a language complement.
        public CharacterSet negated() {
            List<Range> gaps = new ArrayList<>();
            int next = Character.MIN_CODE_POINT;
            for (Range range : ranges) {
                if (range.from() > next) {
                    gaps.add(new Range(next, range.from() - 1));
                }
                next = range.to() + 1;
            }
            if (next <= Character.MAX_CODE_POINT) {
                gaps.add(new Range(next, Character.MAX_CODE_POINT));
            }
            return new CharacterSet(gaps);
        }

        private static List<Range> normalize(List<Range> ranges) {
            List<Range> sorted = new ArrayList<>(Objects.requireNonNull(ranges, "ranges"));
            sorted.sort(Comparator.comparingInt(Range::from));
            List<Range> merged = new ArrayList<>();
            for (Range range : sorted) {
                int last = merged.size() - 1;
                if (last >= 0 && range.from() <= merged.get(last).to() + 1) {
                    Range previous = merged.get(last);
                    merged.set(last, new Range(previous.from(), Math.max(previous.to(), range.to())));
                } else {
                    merged.add(range);
                }
            }
            return List.copyOf(merged);
        }

        /// An inclusive range of code points.
        public record Range(int from, int to) {
            public Range {
                if (from < Character.MIN_CODE_POINT || to > Character.MAX_CODE_POINT || from > to) {
                    throw new IllegalArgumentException("Invalid code point range: " + from + ".." + to);
                }
            }
        }
    }

    /// Matches the parts one after the other.
    record Sequence(List<Expression> parts) implements Expression {
        public Sequence {
            parts = nonEmptyCopy(parts, "Sequence");
        }
    }

    /// Matches whatever any of the alternatives matches.
    record Union(List<Expression> alternatives) implements Expression {
        public Union {
            alternatives = nonEmptyCopy(alternatives, "Union");
        }
    }

    /// Matches what all the operands match.
    record Intersection(List<Expression> operands) implements Expression {
        public Intersection {
            operands = nonEmptyCopy(operands, "Intersection");
        }
    }

    /// Matches every string which the operand does not match (over all code points).
    record Complement(Expression operand) implements Expression {
        public Complement {
            Objects.requireNonNull(operand, "operand");
        }
    }

    /// Matches between `min` and `max` repetitions of the operand.
    ///
    /// Covers `?` (0..1), the Kleene star (0..[#UNBOUNDED]), `+`
    /// (1..[#UNBOUNDED]) and `{n,m}`.
    record Repeat(Expression operand, int min, int max) implements Expression {
        /// Value of `max` when there is no upper bound.
        public static final int UNBOUNDED = -1;

        public Repeat {
            Objects.requireNonNull(operand, "operand");
            if (min < 0) {
                throw new IllegalArgumentException("min must be non-negative: " + min);
            }
            if (max != UNBOUNDED && max < min) {
                throw new IllegalArgumentException("max must be at least min, or UNBOUNDED: " + min + ".." + max);
            }
        }
    }

    private static List<Expression> nonEmptyCopy(List<Expression> expressions, String what) {
        List<Expression> copy = List.copyOf(Objects.requireNonNull(expressions, "expressions"));
        if (copy.isEmpty()) {
            throw new IllegalArgumentException(what + " needs at least one expression");
        }
        return copy;
    }
}
