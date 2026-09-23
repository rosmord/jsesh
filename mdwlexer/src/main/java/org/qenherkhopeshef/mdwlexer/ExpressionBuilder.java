package org.qenherkhopeshef.mdwlexer;

import java.util.Arrays;
import java.util.List;

import org.qenherkhopeshef.mdwlexer.Expression.CharacterSet;
import org.qenherkhopeshef.mdwlexer.Expression.CharacterSet.Range;
import org.qenherkhopeshef.mdwlexer.Expression.Complement;
import org.qenherkhopeshef.mdwlexer.Expression.Epsilon;
import org.qenherkhopeshef.mdwlexer.Expression.Intersection;
import org.qenherkhopeshef.mdwlexer.Expression.Repeat;
import org.qenherkhopeshef.mdwlexer.Expression.Sequence;
import org.qenherkhopeshef.mdwlexer.Expression.Union;

/// Static factory methods for building expressions, which will be converted to parts of automata.
///
/// Meant to be used through a static import, as a small DSL:
/// ```java
/// import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.*;
///
/// Expression identifier = sequence(characterRange('a', 'z'), repeat(characterRange('a', 'z')));
/// ```
public final class ExpressionBuilder {
    private ExpressionBuilder() {
    }

    /// An empty expression, not suitable on its own for defining a token.
    /// @return the empty expression
    public static Expression empty() {
        return new Epsilon();
    }

    /// The expressions, one after the other. With no expression, this is [#empty()].
    public static Expression sequence(Expression... expressions) {
        if (expressions.length == 0) {
            return empty();
        }
        return expressions.length == 1 ? expressions[0] : new Sequence(List.of(expressions));
    }

    /// An expression which matches any of the given expressions.
    /// With no expression, this matches nothing.
    ///
    /// If all the expressions are single characters or character ranges, the result is one
    /// character class, so `union(characterRange('a', 'z'), characterRange('A', 'Z'), character('_'))`
    /// is `[a-zA-Z_]`.
    /// @param expressions
    /// @return
    public static Expression union(Expression... expressions) {
        if (expressions.length == 0) {
            return CharacterSet.NONE;
        }
        if (expressions.length == 1) {
            return expressions[0];
        }
        if (allCharacterSets(expressions)) {
            return Arrays.stream(expressions).map(CharacterSet.class::cast)
                    .reduce(CharacterSet::union).orElseThrow();
        }
        return new Union(List.of(expressions));
    }

    /// An expression which matches anything but the given expression:
    /// every string, of any length, which the given expression does not match.
    /// (Not to be confused with [#negatedCharacter(int)], which is a single character.)
    /// @param expression
    /// @return
    public static Expression complement(Expression expression) {
        return new Complement(expression);
    }

    /// An expression which matches the intersection of the given expressions.
    /// With no expression, this matches every string.
    ///
    /// If all the expressions are single characters or character ranges, the result is one
    /// character class.
    /// @param expressions
    /// @return
    public static Expression intersection(Expression... expressions) {
        if (expressions.length == 0) {
            return new Repeat(CharacterSet.ANY, 0, Repeat.UNBOUNDED);
        }
        if (expressions.length == 1) {
            return expressions[0];
        }
        if (allCharacterSets(expressions)) {
            return Arrays.stream(expressions).map(CharacterSet.class::cast)
                    .reduce(CharacterSet::intersection).orElseThrow();
        }
        return new Intersection(List.of(expressions));
    }

    /// What the expression matches, except what the excluded expression matches.
    ///
    /// If both are single characters or character ranges, the result is one character class,
    /// so `difference(characterRange('a', 'z'), oneOf("if"))` is `[a-z]` without
    /// `i` and `f`. Otherwise it is `intersection(expression, complement(excluded))`.
    /// @param expression
    /// @param excluded
    /// @return
    public static Expression difference(Expression expression, Expression excluded) {
        if (expression instanceof CharacterSet included && excluded instanceof CharacterSet removed) {
            return included.minus(removed);
        }
        return intersection(expression, complement(excluded));
    }

    public static Expression optional(Expression expression) {
        return new Repeat(expression, 0, 1);
    }

    /// Zero or more occurrences.
    public static Expression repeat(Expression expression) {
        return new Repeat(expression, 0, Repeat.UNBOUNDED);
    }

    /// One or more occurrences.
    public static Expression oneOrMore(Expression expression) {
        return new Repeat(expression, 1, Repeat.UNBOUNDED);
    }

    /// Between `min` and `max` occurrences; [Repeat#UNBOUNDED] means no upper bound.
    public static Expression repeat(Expression expression, int min, int max) {
        return new Repeat(expression, min, max);
    }

    public static Expression character(int codepoint) {
        return CharacterSet.of(codepoint);
    }

    /// Any single character.
    public static Expression anyCharacter() {
        return CharacterSet.ANY;
    }

    /// Any one of the characters of the string, like `[abc]`.
    /// With an empty string, this matches nothing.
    /// @param characters the accepted characters
    /// @return the expression
    /// @see #noneOf(String)
    public static Expression oneOf(String characters) {
        return characterSet(characters);
    }

    /// Any single character except those of the string, like `[^abc]`.
    /// With an empty string, this matches any character.
    /// @param characters the excluded characters
    /// @return the expression
    /// @see #oneOf(String)
    public static Expression noneOf(String characters) {
        return characterSet(characters).negated();
    }

    /// The given text, character by character (a keyword, for instance).
    /// With an empty string, this is [#empty()].
    /// @param text the text to match
    /// @return the expression
    public static Expression literal(String text) {
        return sequence(text.codePoints().mapToObj(ExpressionBuilder::character).toArray(Expression[]::new));
    }

    /// Anything but a character.
    /// @param codepoint the excluded character
    /// @return the expression
    public static Expression negatedCharacter(int codepoint) {
        return CharacterSet.of(codepoint).negated();
    }

    /// A character range, inclusive.
    /// @param startCodepoint first character of the range
    /// @param endCodepoint last character of the range
    /// @return the expression
    public static Expression characterRange(int startCodepoint, int endCodepoint) {
        return CharacterSet.range(startCodepoint, endCodepoint);
    }

    public static Expression negatedCharacterRange(int startCodepoint, int endCodepoint) {
        return CharacterSet.range(startCodepoint, endCodepoint).negated();
    }

    private static CharacterSet characterSet(String characters) {
        return new CharacterSet(characters.codePoints().mapToObj(c -> new Range(c, c)).toList());
    }

    private static boolean allCharacterSets(Expression[] expressions) {
        return Arrays.stream(expressions).allMatch(CharacterSet.class::isInstance);
    }
}
