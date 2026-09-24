/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.mdwlexer.Expression.CharacterSet;
import org.qenherkhopeshef.mdwlexer.Expression.CharacterSet.Range;
import org.qenherkhopeshef.mdwlexer.Expression.Complement;
import org.qenherkhopeshef.mdwlexer.Expression.Epsilon;
import org.qenherkhopeshef.mdwlexer.Expression.Intersection;
import org.qenherkhopeshef.mdwlexer.Expression.Repeat;
import org.qenherkhopeshef.mdwlexer.Expression.Sequence;
import org.qenherkhopeshef.mdwlexer.Expression.Union;

class ExpressionTest {
    private static final int MAX = Character.MAX_CODE_POINT;

    @Test
    void mergesOverlappingAndAdjacentRanges() {
        CharacterSet set = new CharacterSet(List.of(
                new Range('x', 'z'), new Range('a', 'c'), new Range('b', 'e'), new Range('f', 'f')));

        assertEquals(List.of(new Range('a', 'f'), new Range('x', 'z')), set.ranges());
        assertEquals(CharacterSet.range('a', 'f'), new CharacterSet(List.of(new Range('a', 'c'), new Range('d', 'f'))));
    }

    @Test
    void negatesRelativeToTheWholeCodePointSpace() {
        assertEquals(List.of(new Range(0, 'a' - 1), new Range('a' + 1, MAX)),
                CharacterSet.of('a').negated().ranges());
        assertEquals(List.of(new Range('a' + 1, MAX)), CharacterSet.range(0, 'a').negated().ranges());
        assertEquals(CharacterSet.NONE, CharacterSet.ANY.negated());
        assertEquals(CharacterSet.ANY, CharacterSet.NONE.negated());
        assertEquals(CharacterSet.range('a', 'z'), CharacterSet.range('a', 'z').negated().negated());
    }

    @Test
    void rejectsInvalidRangesAndRepeats() {
        assertThrows(IllegalArgumentException.class, () -> new Range('b', 'a'));
        assertThrows(IllegalArgumentException.class, () -> new Range(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> new Range(0, MAX + 1));
        assertThrows(IllegalArgumentException.class, () -> new Repeat(empty(), -1, 2));
        assertThrows(IllegalArgumentException.class, () -> new Repeat(empty(), 3, 2));
        assertThrows(IllegalArgumentException.class, () -> new Sequence(List.of()));
        assertThrows(NullPointerException.class, () -> new Complement(null));
    }

    @Test
    void buildsTheExpressionsOfTheDsl() {
        Expression letter = characterRange('a', 'z');

        assertEquals(new Epsilon(), empty());
        assertEquals(new Sequence(List.of(letter, character('_'))), sequence(letter, character('_')));
        assertEquals(new Union(List.of(letter, empty())), union(letter, empty()));
        assertEquals(new Intersection(List.of(letter, repeat(letter))), intersection(letter, repeat(letter)));
        assertEquals(new Complement(letter), complement(letter));
        assertEquals(new Repeat(letter, 0, 1), optional(letter));
        assertEquals(new Repeat(letter, 0, Repeat.UNBOUNDED), repeat(letter));
        assertEquals(new Repeat(letter, 1, Repeat.UNBOUNDED), repeat(letter, 1, Repeat.UNBOUNDED));
        assertEquals(CharacterSet.of('a').negated(), negatedCharacter('a'));
        assertEquals(CharacterSet.range('a', 'z').negated(), negatedCharacterRange('a', 'z'));
    }

    @Test
    void combinesCharacterSets() {
        CharacterSet lower = CharacterSet.range('a', 'z');
        CharacterSet vowels = new CharacterSet(List.of(new Range('a', 'a'), new Range('e', 'e')));

        assertEquals(new CharacterSet(List.of(new Range('a', 'e'), new Range('x', 'z'))),
                CharacterSet.range('a', 'c').union(CharacterSet.range('b', 'e')).union(CharacterSet.range('x', 'z')));
        assertEquals(vowels, lower.intersection(vowels));
        assertEquals(new CharacterSet(List.of(new Range('b', 'd'), new Range('f', 'z'))), lower.minus(vowels));
        assertEquals(CharacterSet.NONE, lower.minus(lower));
    }

    @Test
    void foldsCharacterSetsIntoOneClass() {
        assertEquals(
                new CharacterSet(List.of(new Range('A', 'Z'), new Range('_', '_'), new Range('a', 'z'))),
                union(characterRange('a', 'z'), characterRange('A', 'Z'), character('_')));
        assertEquals(
                new CharacterSet(List.of(new Range('a', 'p'), new Range('r', 'z'))),
                intersection(characterRange('a', 'z'), negatedCharacter('q')));
        assertEquals(
                new CharacterSet(List.of(new Range('a', 'e'), new Range('g', 'h'), new Range('j', 'z'))),
                difference(characterRange('a', 'z'), oneOf("if")));
    }

    @Test
    void buildsDifferenceOfOtherExpressionsWithComplement() {
        Expression letters = repeat(characterRange('a', 'z'));
        Expression keyword = literal("if");

        assertEquals(new Intersection(List.of(letters, new Complement(keyword))), difference(letters, keyword));
    }

    @Test
    void buildsLiteralsByCodePoint() {
        assertEquals(new Sequence(List.of(character('i'), character('f'))), literal("if"));
        assertEquals(character('a'), literal("a"));
        assertEquals(empty(), literal(""));
        assertEquals(character(0x1F600), literal(new String(Character.toChars(0x1F600))));
    }

    @Test
    void buildsOneOfAnyCharacterAndOneOrMore() {
        assertEquals(CharacterSet.range('a', 'c'), oneOf("cab"));
        assertEquals(new CharacterSet(List.of(new Range('+', '+'), new Range('-', '-'))), oneOf("-+"));
        assertEquals(CharacterSet.NONE, oneOf(""));
        assertEquals(CharacterSet.ANY, anyCharacter());
        assertEquals(new Repeat(character('a'), 1, Repeat.UNBOUNDED), oneOrMore(character('a')));
    }

    @Test
    void buildsNegatedMultiCharacterClasses() {
        assertEquals(new CharacterSet(List.of(new Range(0, '"' - 1), new Range('"' + 1, '\\' - 1),
                new Range('\\' + 1, MAX))), noneOf("\\\""));
        assertEquals(CharacterSet.range('a', 'c').negated(), noneOf("cba"));
        assertEquals(negatedCharacter('a'), noneOf("a"));
        assertEquals(CharacterSet.ANY, noneOf(""));
        assertEquals(CharacterSet.range(1, MAX), noneOf("\0"));
    }

    @Test
    void collapsesTrivialSequencesUnionsAndIntersections() {
        Expression letter = characterRange('a', 'z');

        assertEquals(new Epsilon(), sequence());
        assertEquals(letter, sequence(letter));
        assertEquals(CharacterSet.NONE, union());
        assertEquals(letter, union(letter));
        assertEquals(new Repeat(CharacterSet.ANY, 0, Repeat.UNBOUNDED), intersection());
        assertEquals(letter, intersection(letter));
    }
}
