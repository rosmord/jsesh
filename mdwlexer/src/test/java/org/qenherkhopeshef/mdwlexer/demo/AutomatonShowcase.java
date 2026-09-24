/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer.demo;

import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.anyCharacter;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.character;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.characterRange;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.complement;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.intersection;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.literal;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.repeat;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.sequence;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.union;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.mdwlexer.DemoToken;
import org.qenherkhopeshef.mdwlexer.Expression;
import org.qenherkhopeshef.mdwlexer.ExpressionCompiler;

/// Build and display a few examples as markdown/Mermaid.
/// Not a real test: renders a handful of illustrative languages through every automaton stage
/// (NFA, determinized, minimized) as markdown files with embedded Mermaid diagrams, under
/// `build/demo-automata/`, for visual inspection.
///
/// Tagged `demo` so `gradle test` skips it; run it with `gradle demo`.
///
@Tag("demo")
class AutomatonShowcase {
    private static final Expression LETTER = union(characterRange('a', 'z'), characterRange('A', 'Z'), character('_'));
    private static final Expression DIGIT = characterRange('0', '9');

    @Test
    void keywordVersusIdentifier() {
        Expression ifKeyword = literal("if");
        Expression identifier = sequence(LETTER, repeat(union(LETTER, DIGIT)));

        ExpressionCompiler.Result<DemoToken> compiled = ExpressionCompiler.compile(Map.of(
                DemoToken.IF, ifKeyword,
                DemoToken.IDENTIFIER, identifier));

        MarkdownAutomatonReport.write(
                "Keyword if vs identifier",
                "The alphabet split the project README describes: `i` and `f` get their own "
                        + "classes because `if` needs to tell them apart from the rest of "
                        + "`[a-zA-Z_]`. Transition labels show each class's code points, via "
                        + "`CharacterClassifier.describe`.",
                compiled.automaton(), compiled.classifier()::describe);
    }

    @Test
    void intersectionOfEvenLengthAndEndsInB() {
        Expression aOrB = union(character('a'), character('b'));
        Expression evenLength = repeat(sequence(aOrB, aOrB));
        Expression endsInB = sequence(repeat(aOrB), character('b'));
        Expression evenLengthEndingInB = intersection(evenLength, endsInB);

        ExpressionCompiler.Result<DemoToken> compiled =
                ExpressionCompiler.compile(evenLengthEndingInB, DemoToken.IDENTIFIER);

        MarkdownAutomatonReport.write(
                "Even-length strings over a,b ending in b",
                "An intersection of two simple languages. The NFA stage already embeds two "
                        + "separately-determinized operand DFAs as fragments (see "
                        + "`ExpressionCompiler.intersectionFragment`).",
                compiled.automaton(), compiled.classifier()::describe);
    }

    @Test
    void intersectionAorBStarAndLengthTwo() {
        Expression aOrB = union(character('a'), character('b'));
        Expression aOrBStar = repeat(aOrB);
        Expression lengthTwo = sequence(anyCharacter(), anyCharacter());
        Expression intersection = intersection(aOrBStar, lengthTwo);

        ExpressionCompiler.Result<DemoToken> compiled =
                ExpressionCompiler.compile(intersection, DemoToken.IDENTIFIER);

        MarkdownAutomatonReport.write(
                "Intersection of (ab)\\* and length 2",
                "An intersection of two very simple languages.",
                compiled.automaton(), compiled.classifier()::describe);
    }



    @Test
    void intersectionAStarBStarAndLengthTwo() {
        Expression aStarbStar = sequence(repeat(character('a')), repeat(character('b')));
        Expression lengthTwo = sequence(anyCharacter(), anyCharacter());
        Expression intersection = intersection(aStarbStar, lengthTwo);

        ExpressionCompiler.Result<DemoToken> compiled =
                ExpressionCompiler.compile(intersection, DemoToken.IDENTIFIER);

        MarkdownAutomatonReport.write(
                "Intersection of a\\*b\\* and length 2",
                "An intersection of two very simple languages.",
                compiled.automaton(), compiled.classifier()::describe);
    }

    @Test
    void complementOfIdentifier() {
        Expression identifier = sequence(LETTER, repeat(union(LETTER, DIGIT)));
        Expression notAnIdentifier = complement(identifier);

        ExpressionCompiler.Result<DemoToken> compiled =
                ExpressionCompiler.compile(notAnIdentifier, DemoToken.IDENTIFIER);

        MarkdownAutomatonReport.write(
                "Complement of identifier",
                "Everything that is not `[a-zA-Z_][a-zA-Z0-9_]*`, including the empty string.",
                compiled.automaton(), compiled.classifier()::describe);
    }
}
