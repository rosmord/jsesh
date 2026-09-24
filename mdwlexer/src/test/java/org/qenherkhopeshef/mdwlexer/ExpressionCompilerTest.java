/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.character;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.characterRange;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.complement;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.empty;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.intersection;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.literal;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.oneOrMore;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.repeat;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.sequence;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.union;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.mdwlexer.automata.AutomataHelper;
import org.qenherkhopeshef.mdwlexer.automata.DeterministicFiniteAutomaton;
import org.qenherkhopeshef.mdwlexer.automata.State;

class ExpressionCompilerTest {
    private static final Expression LETTER = union(characterRange('a', 'z'), characterRange('A', 'Z'), character('_'));
    private static final Expression DIGIT = characterRange('0', '9');

    private record Lexicon(DeterministicFiniteAutomaton<Integer, DemoToken> dfa, CharacterClassifier classifier) {
        Optional<DemoToken> run(String text) {
            State state = dfa.initialState();
            for (int codepoint : text.codePoints().toArray()) {
                Optional<State> next = dfa.transition(state, classifier.classOf(codepoint));
                if (next.isEmpty()) {
                    return Optional.empty();
                }
                state = next.get();
            }
            return dfa.acceptingTokenType(state);
        }
    }

    @Test
    void compilesAndDeterminizesASmallLexicon() {
        Expression identifier = sequence(LETTER, repeat(union(LETTER, DIGIT)));
        Expression integer = oneOrMore(DIGIT);
        Expression ifKeyword = literal("if");

        Lexicon lexicon = compileAndMinimize(Map.of(
                DemoToken.IF, ifKeyword,
                DemoToken.IDENTIFIER, identifier,
                DemoToken.INTEGER, integer));

        assertEquals(Optional.of(DemoToken.IF), lexicon.run("if"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("iffy"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("x1"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("_leading"));
        assertEquals(Optional.of(DemoToken.INTEGER), lexicon.run("123"));
        assertEquals(Optional.empty(), lexicon.run("1x"));
        assertEquals(Optional.empty(), lexicon.run(""));
        assertEquals(Optional.empty(), lexicon.run("1if"));
    }

    @Test
    void supportsBoundedRepeats() {
        Expression twoToFourAs = repeat(character('a'), 2, 4);

        Lexicon lexicon = compileAndMinimize(Map.of(DemoToken.IDENTIFIER, twoToFourAs));

        assertEquals(Optional.empty(), lexicon.run("a"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("aa"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("aaa"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("aaaa"));
        assertEquals(Optional.empty(), lexicon.run("aaaaa"));
    }

    @Test
    void exactZeroRepeatsOnlyMatchesTheEmptyString() {
        Expression zeroAs = repeat(character('a'), 0, 0);

        Lexicon lexicon = compileAndMinimize(Map.of(DemoToken.IDENTIFIER, zeroAs));

        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run(""));
        assertEquals(Optional.empty(), lexicon.run("a"));
    }

    @Test
    void compilesIntersection() {
        Expression aOrB = union(character('a'), character('b'));
        Expression evenLength = repeat(sequence(aOrB, aOrB));
        Expression endsInB = sequence(repeat(aOrB), character('b'));
        Expression evenLengthEndingInB = intersection(evenLength, endsInB);

        Lexicon lexicon = compileAndMinimize(Map.of(DemoToken.IDENTIFIER, evenLengthEndingInB));

        assertEquals(Optional.empty(), lexicon.run(""));
        assertEquals(Optional.empty(), lexicon.run("b"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("ab"));
        assertEquals(Optional.empty(), lexicon.run("aab"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("abab"));
        assertEquals(Optional.empty(), lexicon.run("ba"));
    }

    @Test
    void intersectionOfDisjointLanguagesMatchesNothing() {
        // a+ and b+ share no string at all, not even the empty one, so the intersection's DFA
        // has no reachable accepting state.
        Expression oneOrMoreAs = oneOrMore(character('a'));
        Expression oneOrMoreBs = oneOrMore(character('b'));
        Expression neverMatches = intersection(oneOrMoreAs, oneOrMoreBs);

        Lexicon lexicon = compileAndMinimize(Map.of(DemoToken.IDENTIFIER, neverMatches));

        assertEquals(Optional.empty(), lexicon.run(""));
        assertEquals(Optional.empty(), lexicon.run("a"));
        assertEquals(Optional.empty(), lexicon.run("b"));
        assertEquals(Optional.empty(), lexicon.run("ab"));
    }

    @Test
    void compilesComplement() {
        Expression identifier = sequence(LETTER, repeat(union(LETTER, DIGIT)));
        Expression notAnIdentifier = complement(identifier);

        Lexicon lexicon = compileAndMinimize(Map.of(DemoToken.IDENTIFIER, notAnIdentifier));

        assertEquals(Optional.empty(), lexicon.run("abc"));
        assertEquals(Optional.empty(), lexicon.run("x1"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run(""));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("1abc"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("123"));
    }

    @Test
    void doubleComplementIsTheOriginalLanguage() {
        Expression digits = oneOrMore(DIGIT);
        Expression doubleNegated = complement(complement(digits));

        Lexicon lexicon = compileAndMinimize(Map.of(DemoToken.INTEGER, doubleNegated));

        assertEquals(Optional.of(DemoToken.INTEGER), lexicon.run("123"));
        assertEquals(Optional.empty(), lexicon.run("12a"));
        assertEquals(Optional.empty(), lexicon.run(""));
    }

    /// Test the pattern `(a*b)|ε` and its complement.
    @Test    
    void complementsAStarBOrEpsilonOverAlphabetABC() {
        Expression aStarB = sequence(repeat(character('a')), character('b'));
        Expression aStarBOrEpsilon = union(aStarB, empty());
        Expression complementOfAStarBOrEpsilon = complement(aStarBOrEpsilon);

        Lexicon lexicon = compileAndMinimize(Map.of(DemoToken.IDENTIFIER, complementOfAStarBOrEpsilon));

        assertEquals(Optional.empty(), lexicon.run(""));
        assertEquals(Optional.empty(), lexicon.run("b"));
        assertEquals(Optional.empty(), lexicon.run("aaab"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("c"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("aa"));
        assertEquals(Optional.of(DemoToken.IDENTIFIER), lexicon.run("aabb"));
    }

    private static Lexicon compileAndMinimize(Map<DemoToken, Expression> rules) {
        ExpressionCompiler.Result<DemoToken> compiled = ExpressionCompiler.compile(rules);
        DeterministicFiniteAutomaton<Integer, DemoToken> dfa =
                AutomataHelper.minimize(AutomataHelper.determinize(compiled.automaton()));
        return new Lexicon(dfa, compiled.classifier());
    }
}
