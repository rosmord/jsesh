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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.anyCharacter;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.character;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.characterRange;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.literal;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.noneOf;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.oneOrMore;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.repeat;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.sequence;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.union;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/// A full test with a simple language.
///
/// - Strings with `\"` escaped quotes
/// - reserved words: `if`, `else`, `while`, `return`, `var`, `function`
/// - identifiers: `[a-zA-Z_][a-zA-Z0-9_]*`
/// - Operators: `+`, `-`, `*`, `/`, `=`, `==`, `!=`, `<`, `>`, `<=`, `>=`
/// - punctuation: `(`, `)`, `{`, `}`, `;`
/// - integers
/// - real numbers
class FullTest {

    private enum TokenType {
        // Reserved words come first so they outrank IDENTIFIER on a same-length match.
        IF, ELSE, WHILE, RETURN, VAR, FUNCTION,
        IDENTIFIER,
        REAL, INTEGER, STRING,
        EQ, NE, LE, GE, ASSIGN, LT, GT, PLUS, MINUS, STAR, SLASH,
        LPAREN, RPAREN, LBRACE, RBRACE, SEMICOLON
    }

    private static Lexicon<TokenType> lexicon;

    @BeforeAll
    static void buildLexicon() {
        Expression letter = union(characterRange('a', 'z'), characterRange('A', 'Z'), character('_'));
        Expression digit = characterRange('0', '9');
        Expression identifier = sequence(letter, repeat(union(letter, digit)));
        Expression integer = oneOrMore(digit);
        Expression real = sequence(oneOrMore(digit), character('.'), oneOrMore(digit));
        Expression escapedCharacter = sequence(character('\\'), anyCharacter());
        Expression stringCharacter = union(noneOf("\"\\"), escapedCharacter);
        Expression string = sequence(character('"'), repeat(stringCharacter), character('"'));

        lexicon = LexiconBuilder.<TokenType>newBuilder()
                .rule(TokenType.IF, literal("if"))
                .rule(TokenType.ELSE, literal("else"))
                .rule(TokenType.WHILE, literal("while"))
                .rule(TokenType.RETURN, literal("return"))
                .rule(TokenType.VAR, literal("var"))
                .rule(TokenType.FUNCTION, literal("function"))
                .rule(TokenType.IDENTIFIER, identifier)
                .rule(TokenType.REAL, real)
                .rule(TokenType.INTEGER, integer)
                .rule(TokenType.STRING, string)
                .rule(TokenType.EQ, literal("=="))
                .rule(TokenType.NE, literal("!="))
                .rule(TokenType.LE, literal("<="))
                .rule(TokenType.GE, literal(">="))
                .rule(TokenType.ASSIGN, literal("="))
                .rule(TokenType.LT, literal("<"))
                .rule(TokenType.GT, literal(">"))
                .rule(TokenType.PLUS, literal("+"))
                .rule(TokenType.MINUS, literal("-"))
                .rule(TokenType.STAR, literal("*"))
                .rule(TokenType.SLASH, literal("/"))
                .rule(TokenType.LPAREN, literal("("))
                .rule(TokenType.RPAREN, literal(")"))
                .rule(TokenType.LBRACE, literal("{"))
                .rule(TokenType.RBRACE, literal("}"))
                .rule(TokenType.SEMICOLON, literal(";"))
                .build();
    }

    @Test
    void lexiconSize() {
        int states = lexicon.automaton().states().size();
        int classes = lexicon.classifier().classCount();
        System.out.println("Full-language lexicon: " + states + " DFA states, " + classes
                + " character classes, " + lexicon.tokenTypes().size() + " token types");

        assertEquals(TokenType.values().length, lexicon.tokenTypes().size());
        assertTrue(states > 0);
    }

    @Test
    void tokenizesASmallProgram() throws IOException {
        String program = """
                function main() {
                  var x = 10;
                  var y = 3.14;
                  if (x >= 10) {
                    return x + y;
                  } else {
                    x = x - 1;
                  }
                  while (x > 0) {
                    x = x / 2;
                  }
                  var s = "hello \\"world\\"";
                }
                """;

        List<TokenType> types = typesOf(program);

        assertEquals(List.of(
                TokenType.FUNCTION, TokenType.IDENTIFIER, TokenType.LPAREN, TokenType.RPAREN, TokenType.LBRACE,
                TokenType.VAR, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.INTEGER, TokenType.SEMICOLON,
                TokenType.VAR, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.REAL, TokenType.SEMICOLON,
                TokenType.IF, TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.GE, TokenType.INTEGER,
                TokenType.RPAREN, TokenType.LBRACE,
                TokenType.RETURN, TokenType.IDENTIFIER, TokenType.PLUS, TokenType.IDENTIFIER, TokenType.SEMICOLON,
                TokenType.RBRACE, TokenType.ELSE, TokenType.LBRACE,
                TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.IDENTIFIER, TokenType.MINUS, TokenType.INTEGER,
                TokenType.SEMICOLON,
                TokenType.RBRACE,
                TokenType.WHILE, TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.GT, TokenType.INTEGER,
                TokenType.RPAREN, TokenType.LBRACE,
                TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.IDENTIFIER, TokenType.SLASH, TokenType.INTEGER,
                TokenType.SEMICOLON,
                TokenType.RBRACE,
                TokenType.VAR, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.STRING, TokenType.SEMICOLON,
                TokenType.RBRACE),
                types);

        List<Token<TokenType>> tokens = tokenizeAll(program);
        Token<TokenType> stringToken = tokens.stream()
                .filter(token -> token.type() == TokenType.STRING)
                .findFirst()
                .orElseThrow();
        String stringLiteral = "\"hello \\\"world\\\"\"";
        assertEquals(stringLiteral, stringToken.text());
    }

    @Test
    void stringLiteralKeepsAnEscapedQuoteInsideItself() throws IOException {
        // Source text: "dsfdsf\"dfsfds" -- one string token, the escaped quote does not end it.
        String source = "\"" + "dsfdsf" + "\\" + "\"" + "dfsfds" + "\"";

        List<Token<TokenType>> tokens = tokenizeAll(source);

        assertEquals(1, tokens.size());
        assertEquals(TokenType.STRING, tokens.get(0).type());
        assertEquals(source, tokens.get(0).text());
    }

    @Test
    void unterminatedStringIsALexicalError() {
        String source = "\"" + "no closing quote";

        assertThrows(LexicalException.class, () -> tokenizeAll(source));
    }

    @Test
    void twoCharacterOperatorsAreNotSplitIntoTwoTokens() throws IOException {
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.GE, TokenType.INTEGER), typesOf("x>=10"));
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.LE, TokenType.INTEGER), typesOf("x<=10"));
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.EQ, TokenType.INTEGER), typesOf("x==10"));
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.NE, TokenType.INTEGER), typesOf("x!=10"));
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.INTEGER), typesOf("x=10"));
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.LT, TokenType.INTEGER), typesOf("x<10"));
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.GT, TokenType.INTEGER), typesOf("x>10"));
    }

    @Test
    void realNumberRequiresDigitsOnBothSidesOfTheDot() throws IOException {
        assertEquals(List.of(TokenType.REAL), typesOf("3.14"));
        assertEquals(List.of(TokenType.INTEGER), typesOf("42"));

        // Maximal munch consumes "42" as an INTEGER, backtracking off the trailing dot since
        // it isn't followed by a digit; the lone "." left over then matches no rule at all.
        assertThrows(LexicalException.class, () -> typesOf("42."));
    }

    @Test
    void reservedWordsAreNotIdentifiers() throws IOException {
        assertEquals(
                List.of(TokenType.IF, TokenType.ELSE, TokenType.WHILE, TokenType.RETURN, TokenType.VAR,
                        TokenType.FUNCTION),
                typesOf("if else while return var function"));
        assertEquals(List.of(TokenType.IDENTIFIER), typesOf("iffy"));
        assertEquals(List.of(TokenType.IDENTIFIER), typesOf("_ifElse2"));
    }

    private static List<TokenType> typesOf(String source) throws IOException {
        return tokenizeAll(source).stream().map(Token::type).toList();
    }

    private static List<Token<TokenType>> tokenizeAll(String source) throws IOException {
        List<Token<TokenType>> tokens = new ArrayList<>();
        Lexer<TokenType> lexer = lexicon.newLexer(new StringReader(source));
        Optional<Token<TokenType>> next;
        while ((next = lexer.nextToken()).isPresent()) {
            tokens.add(next.get());
        }
        return tokens;
    }
}
