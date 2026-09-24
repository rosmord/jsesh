/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.character;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.characterRange;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.oneOrMore;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

/// Lexical states: several sub-lexica, switched declaratively by rules or explicitly by the caller.
class LexerStateTest {
    private static final String PROPERTIES = "PROPERTIES";

    private enum Tok {
        WORD, OPEN, CLOSE, NUMBER, DIGITS_AS_WORD
    }

    private static final Expression LETTERS = oneOrMore(characterRange('a', 'z'));
    private static final Expression DIGITS = oneOrMore(characterRange('0', '9'));

    /// Outside brackets, digits belong to words; inside, they are numbers and spaces are significant.
    private static Lexicon<Tok> lexicon() {
        return LexiconBuilder.<Tok>newBuilder()
                .rule(Tok.WORD, LETTERS)
                .rule(Tok.DIGITS_AS_WORD, DIGITS)
                .rule(Tok.OPEN, character('['), PROPERTIES)
                .state(PROPERTIES)
                .noWhitespaceSkipping()
                .rule(Tok.NUMBER, DIGITS)
                .rule(Tok.WORD, character(' '))
                .rule(Tok.CLOSE, character(']'), Lexicon.INITIAL_STATE)
                .build();
    }

    @Test
    void declaredTransitionsSwitchState() throws IOException {
        Lexer<Tok> lexer = lexicon().newLexer(new StringReader("ab 12[3 4] 5"));

        assertToken(lexer, Tok.WORD, "ab");
        assertToken(lexer, Tok.DIGITS_AS_WORD, "12");
        assertEquals(Lexicon.INITIAL_STATE, lexer.state());
        assertToken(lexer, Tok.OPEN, "[");
        assertEquals(PROPERTIES, lexer.state());
        assertToken(lexer, Tok.NUMBER, "3");
        assertToken(lexer, Tok.WORD, " ");
        assertToken(lexer, Tok.NUMBER, "4");
        assertToken(lexer, Tok.CLOSE, "]");
        assertEquals(Lexicon.INITIAL_STATE, lexer.state());
        assertToken(lexer, Tok.DIGITS_AS_WORD, "5");
        assertEquals(Optional.empty(), lexer.nextToken());
    }

    @Test
    void explicitBeginStateSwitchesState() throws IOException {
        Lexer<Tok> lexer = lexicon().newLexer(new StringReader("12 34"));

        assertToken(lexer, Tok.DIGITS_AS_WORD, "12");
        lexer.beginState(PROPERTIES);
        assertToken(lexer, Tok.WORD, " ");
        assertToken(lexer, Tok.NUMBER, "34");
    }

    @Test
    void switchingStateAfterLookaheadLosesNoInput() throws IOException {
        // Scanning "ab" reads "1" as look-ahead before backing off; the PROPERTIES scan must see it.
        Lexer<Tok> lexer = lexicon().newLexer(new StringReader("ab1"));

        assertToken(lexer, Tok.WORD, "ab");
        lexer.beginState(PROPERTIES);
        assertToken(lexer, Tok.NUMBER, "1");
    }

    @Test
    void whitespaceSkippingIsPerState() {
        Lexer<Tok> lexer = lexicon().newLexer(new StringReader(""));

        assertTrue(lexer.isSkippingWhitespace());
        lexer.beginState(PROPERTIES);
        assertFalse(lexer.isSkippingWhitespace());
    }

    @Test
    void ruleIsOnlyActiveInItsState() {
        Lexer<Tok> lexer = lexicon().newLexer(new StringReader("]"));

        assertThrows(LexicalException.class, lexer::nextToken);
    }

    @Test
    void introspectsStates() {
        Lexicon<Tok> lexicon = lexicon();

        assertEquals(Set.of(Lexicon.INITIAL_STATE, PROPERTIES), lexicon.states());
        assertEquals(Set.of(Tok.NUMBER, Tok.WORD, Tok.CLOSE), lexicon.tokenTypes(PROPERTIES));
        assertEquals(Set.of(Tok.values()), lexicon.tokenTypes());
        assertEquals(Optional.of(PROPERTIES), lexicon.nextState(Lexicon.INITIAL_STATE, Tok.OPEN));
        assertEquals(Optional.empty(), lexicon.nextState(PROPERTIES, Tok.NUMBER));
    }

    @Test
    void unknownStateIsRejected() {
        Lexer<Tok> lexer = lexicon().newLexer(new StringReader(""));

        assertThrows(IllegalArgumentException.class, () -> lexer.beginState("NOPE"));
    }

    @Test
    void transitionToUndefinedStateFailsAtBuild() {
        LexiconBuilder<Tok> builder = LexiconBuilder.<Tok>newBuilder()
                .rule(Tok.OPEN, character('['), "NOPE");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void emptyStateFailsAtBuild() {
        LexiconBuilder<Tok> builder = LexiconBuilder.<Tok>newBuilder()
                .rule(Tok.WORD, LETTERS)
                .state(PROPERTIES);

        assertThrows(IllegalStateException.class, builder::build);
    }

    private static void assertToken(Lexer<Tok> lexer, Tok type, String text) throws IOException {
        Token<Tok> token = lexer.nextToken().orElseThrow();
        assertEquals(type, token.type());
        assertEquals(text, token.text());
    }
}
