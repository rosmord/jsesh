package org.qenherkhopeshef.mdwlexer;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.qenherkhopeshef.mdwlexer.Expression.CharacterSet;
import org.qenherkhopeshef.mdwlexer.Expression.CharacterSet.Range;

/// A partition of Unicode characters into classes.
/// 
/// Basically, those classes will contain characters **ranges** with the same behavior viz the automata, 
/// dramatically reducing the number of different cases an automaton must deal with.
/// 
/// For instance, if lexer always deal with `[a-z]` in an uniform way, we will map all those characters to the same class,
/// and the automaton will have only one transition for all of them, instead of 26 transitions.
/// 
/// If we have rules for `i` and `f` as well as for `[a-z]`, we will have end up with:
/// 
/// - a class for `[a-h]`
/// - a class for `[j-e]`
/// - a class for `[g-z]`
/// - a class for `i`
/// - a class for `f`.
/// - a class for characters before `a`
/// - a class for characters after `z`
/// 
public final class CharacterClassifier {
    private final int[] classStarts;

    private CharacterClassifier(int[] classStarts) {
        this.classStarts = classStarts;
    }

    /// Builds the classifier for every [CharacterSet] reachable from the given expressions.
    ///
    /// This includes sequences, unions, repeats, etc.
    public static CharacterClassifier forExpressions(Collection<Expression> expressions) {
        TreeSet<Integer> cuts = new TreeSet<>();
        cuts.add(Character.MIN_CODE_POINT);
        expressions.forEach(expression -> collectCuts(expression, cuts));
        return new CharacterClassifier(cuts.stream().mapToInt(Integer::intValue).toArray());
    }

    /// The class id (0-based) that `codepoint` belongs to.
    /// @param the codepoint of a Unicode character
    /// @return the class id (0-based) that `codepoint` belongs to.
    public int classOf(int codepoint) {
        int index = Arrays.binarySearch(classStarts, codepoint);
        // binarySearch is a bit weird. If the element is found, it returns its index.
        // else, it returns -insertionPoint-1, where insertionPoint is the index of the first element greater than the key.
        // The class in this second case is at insertionPoint-1, hence the computation below.
        return index >= 0 ? index : -index - 2;
    }

    /// The number of distinct classes in this partition.
    public int classCount() {
        return classStarts.length;
    }

    /// The classes that exactly cover `set`.
    ///  Since every range boundary of every
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
            case Expression.Epsilon _ -> {
                // Nothing to do.
            }
        }
    }
}
