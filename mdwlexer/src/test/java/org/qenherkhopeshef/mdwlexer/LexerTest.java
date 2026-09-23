package org.qenherkhopeshef.mdwlexer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.character;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.characterRange;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.literal;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.oneOrMore;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.repeat;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.sequence;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.union;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

class LexerTest {
    private static final Expression LETTER = union(characterRange('a', 'z'), characterRange('A', 'Z'), character('_'));
    private static final Expression DIGIT = characterRange('0', '9');

    private static Lexicon<DemoToken> demoLexicon() {
        Expression identifier = sequence(LETTER, repeat(union(LETTER, DIGIT)));
        Expression integer = oneOrMore(DIGIT);
        return LexiconBuilder.<DemoToken>newBuilder()
                .rule(DemoToken.IF, literal("if"))
                .rule(DemoToken.ELSE, literal("else"))
                .rule(DemoToken.FUN, literal("fun"))
                .rule(DemoToken.IDENTIFIER, identifier)
                .rule(DemoToken.INTEGER, integer)
                .build();
    }

    @Test
    void scansTokensAndSkipsWhitespace() throws IOException {
        Lexer<DemoToken> lexer = demoLexicon().newLexer(new StringReader("if  x1 42"));

        assertToken(lexer, DemoToken.IF, "if");
        assertToken(lexer, DemoToken.IDENTIFIER, "x1");
        assertToken(lexer, DemoToken.INTEGER, "42");
        assertEquals(Optional.empty(), lexer.nextToken());
    }

    @Test
    void keywordsWinOverIdentifiersOnlyOnAnExactMatch() throws IOException {
        Lexer<DemoToken> lexer = demoLexicon().newLexer(new StringReader("if iffy"));

        assertToken(lexer, DemoToken.IF, "if");
        assertToken(lexer, DemoToken.IDENTIFIER, "iffy");
    }

    @Test
    void tracksTokenStartPosition() throws IOException {
        Lexer<DemoToken> lexer = demoLexicon().newLexer(new StringReader("if x"));

        Token<DemoToken> ifToken = lexer.nextToken().orElseThrow();
        Token<DemoToken> xToken = lexer.nextToken().orElseThrow();

        assertEquals(0, ifToken.position());
        assertEquals(3, xToken.position());
    }

    @Test
    void unmatchedCharacterThrows() {
        Lexer<DemoToken> lexer = demoLexicon().newLexer(new StringReader("#"));

        assertThrows(LexicalException.class, lexer::nextToken);
    }

    @Test
    void disablingWhitespaceSkippingMakesSpacesAnError() throws IOException {
        Lexer<DemoToken> lexer = demoLexicon().newLexer(new StringReader("a b"));
        lexer.setSkipWhitespace(false);

        assertDoesNotThrow(lexer::nextToken);
        assertThrows(LexicalException.class, lexer::nextToken);
    }

    @Test
    void noWhitespaceSkippingBuilderOptionDisablesItByDefault() throws IOException {
        Lexicon<DemoToken> lexicon = LexiconBuilder.<DemoToken>newBuilder()
                .rule(DemoToken.IDENTIFIER, oneOrMore(LETTER))
                .noWhitespaceSkipping()
                .build();
        Lexer<DemoToken> lexer = lexicon.newLexer(new StringReader("a b"));

        assertFalse(lexer.isSkippingWhitespace());
        assertToken(lexer, DemoToken.IDENTIFIER, "a");
        assertThrows(LexicalException.class, lexer::nextToken);
    }

    @Test
    void tokenTypesReflectsDeclaredRules() {
        Lexicon<DemoToken> lexicon = demoLexicon();

        assertEquals(
                Set.of(DemoToken.IF, DemoToken.ELSE, DemoToken.FUN, DemoToken.IDENTIFIER, DemoToken.INTEGER),
                lexicon.tokenTypes());
    }

    private static void assertToken(Lexer<DemoToken> lexer, DemoToken type, String text) throws IOException {
        Token<DemoToken> token = lexer.nextToken().orElseThrow();
        assertEquals(type, token.type());
        assertEquals(text, token.text());
    }
}
