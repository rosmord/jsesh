package org.qenherkhopeshef.mdwlexer;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.qenherkhopeshef.mdwlexer.Expression.CharacterSet;
import org.qenherkhopeshef.mdwlexer.Expression.CharacterSet.Range;

/// Partitions the Unicode code point space into the smallest set of disjoint classes such
/// that every [CharacterSet] appearing in a group of expressions is exactly a union of
/// classes: e.g. given `[a-z]`, `i` and `f`, it produces one class each for
/// `i`, `f`, and every maximal run of `[a-z]` left over.
///
/// Downstream, a lexer maps each input code point to its class with [#classOf] and
/// drives the automaton with that class id instead of the raw code point, so a state never
/// needs one transition per code point (which would be infeasible for e.g. "any character")
/// but only one per class.
public final class CharacterClassifier {
    private final int[] classStarts;

    private CharacterClassifier(int[] classStarts) {
        this.classStarts = classStarts;
    }

    /// Builds the classifier for every [CharacterSet] reachable from the given
    /// expressions (including inside sequences, unions, repeats, etc).
    public static CharacterClassifier forExpressions(Collection<Expression> expressions) {
        TreeSet<Integer> cuts = new TreeSet<>();
        cuts.add(Character.MIN_CODE_POINT);
        expressions.forEach(expression -> collectCuts(expression, cuts));
        return new CharacterClassifier(cuts.stream().mapToInt(Integer::intValue).toArray());
    }

    /// The class id (0-based) that `codepoint` belongs to.
    public int classOf(int codepoint) {
        int index = Arrays.binarySearch(classStarts, codepoint);
        return index >= 0 ? index : -index - 2;
    }

    /// The number of distinct classes in this partition.
    public int classCount() {
        return classStarts.length;
    }

    /// The classes that exactly cover `set`. Since every range boundary of every
    /// [CharacterSet] passed to [#forExpressions] was used to cut the partition,
    /// each of `set`'s ranges is itself a union of whole classes, never a partial overlap.
    public Set<Integer> classesOf(CharacterSet set) {
        Set<Integer> classes = new LinkedHashSet<>();
        for (Range range : set.ranges()) {
            int firstClass = classOf(range.from());
            int lastClass = classOf(range.to());
            for (int characterClass = firstClass; characterClass <= lastClass; characterClass++) {
                classes.add(characterClass);
            }
        }
        return classes;
    }

    /// Human-readable label for a class, e.g. `'i'` for a single-codepoint class, or
    /// `'a'-'z'` for a range. Non-printable code points are shown as `U+XXXX`.
    public String describe(int characterClass) {
        int from = classStarts[characterClass];
        int to = characterClass + 1 < classStarts.length ? classStarts[characterClass + 1] - 1 : Character.MAX_CODE_POINT;
        return from == to ? describeCodePoint(from) : describeCodePoint(from) + "-" + describeCodePoint(to);
    }

    private static String describeCodePoint(int codePoint) {
        if (codePoint >= 0x21 && codePoint <= 0x7e) {
            return "'" + (char) codePoint + "'";
        }
        return String.format("U+%04X", codePoint);
    }

    private static void collectCuts(Expression expression, TreeSet<Integer> cuts) {
        switch (expression) {
            case CharacterSet set -> set.ranges().forEach(range -> {
                cuts.add(range.from());
                if (range.to() < Character.MAX_CODE_POINT) {
                    cuts.add(range.to() + 1);
                }
            });
            case Expression.Sequence sequence -> sequence.parts().forEach(part -> collectCuts(part, cuts));
            case Expression.Union union -> union.alternatives().forEach(part -> collectCuts(part, cuts));
            case Expression.Intersection intersection ->
                    intersection.operands().forEach(part -> collectCuts(part, cuts));
            case Expression.Complement complement -> collectCuts(complement.operand(), cuts);
            case Expression.Repeat repeat -> collectCuts(repeat.operand(), cuts);
            case Expression.Epsilon epsilon -> {
            }
        }
    }
}
