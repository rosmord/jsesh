package org.qenherkhopeshef.mdwlexer.mdc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class MdcLexerTest {

    private static List<MdcSymbol> scanAll(String source) {
        MdcLexer scanner = MdcLexicon.instance().newLexer(source);
        List<MdcSymbol> symbols = new ArrayList<>();
        Optional<MdcSymbol> next;
        while ((next = scanner.nextSymbol()).isPresent()) {
            symbols.add(next.get());
        }
        return symbols;
    }

    private static List<MdcSymbolCode> codesOf(String source) {
        return scanAll(source).stream().map(MdcSymbol::code).toList();
    }

    @Test
    void plainSignCodeIsRecognizedAsHieroglyph() {
        List<MdcSymbol> symbols = scanAll("A1");

        assertEquals(1, symbols.size());
        MdcSymbol symbol = symbols.get(0);
        assertEquals(MdcSymbolCode.HIEROGLYPH, symbol.code());
        assertEquals("A1", symbol.text());
        assertEquals(new MdcSign(SignSubType.Plain.MDC_CODE, "A1"), symbol.value());
    }

    @Test
    void gardinerCodeWithTrailingLetterIsOneSign() {
        List<MdcSymbol> symbols = scanAll("N35A");

        assertEquals(1, symbols.size());
        assertEquals("N35A", symbols.get(0).text());
    }

    @Test
    void redAndBlackPointsHaveTheirOwnSubtypeWhenNotExtendedByMaximalMunch() {
        // "o"/"O" only get their special subtype in isolation: right next to another letter or
        // digit, the generic sign-code rule matches longer and wins instead (e.g. "oO" is one
        // 2-character MDC_CODE sign, exactly like the original grammar's rule 393 vs. 384/385).
        assertEquals(new MdcSign(SignSubType.Plain.RED_POINT, "o"), scanAll("o").get(0).value());
        assertEquals(new MdcSign(SignSubType.Plain.BLACK_POINT, "O"), scanAll("O").get(0).value());

        List<MdcSymbol> merged = scanAll("oO");
        assertEquals(1, merged.size());
        assertEquals(new MdcSign(SignSubType.Plain.MDC_CODE, "oO"), merged.get(0).value());
    }

    @Test
    void separatorAndSignsAreDistinguished() {
        List<MdcSymbolCode> codes = codesOf("A1-B2");

        assertEquals(List.of(MdcSymbolCode.HIEROGLYPH, MdcSymbolCode.SEPARATOR, MdcSymbolCode.HIEROGLYPH), codes);
    }

    @Test
    void separatorRunIsOneToken() {
        List<MdcSymbol> symbols = scanAll("--__- ");

        assertEquals(1, symbols.size());
        assertEquals(MdcSymbolCode.SEPARATOR, symbols.get(0).code());
        assertEquals("--__- ", symbols.get(0).text());
    }

    @Test
    void zoneAndQuadratKeywordsWinOverGenericSignCode() {
        assertEquals(List.of(MdcSymbolCode.ZONE), codesOf("zone"));
        assertEquals(List.of(MdcSymbolCode.QUADRAT), codesOf("quadrat"));
        // "zoner" is not the literal "zone": the generic sign-code rule wins by length.
        List<MdcSymbol> symbols = scanAll("zoner");
        assertEquals(MdcSymbolCode.HIEROGLYPH, symbols.get(0).code());
        assertEquals("zoner", symbols.get(0).text());
    }

    @Test
    void bareHashIsOverwriteOnlyRightAfterASign() {
        // Not after a sign: HASH_ALONE resolves to a shading toggle.
        assertEquals(List.of(MdcSymbolCode.TOGGLE), codesOf("#"));

        // Right after a hieroglyph, the same bare "#" means OVERWRITE.
        List<MdcSymbol> symbols = scanAll("A1#");
        assertEquals(List.of(MdcSymbolCode.HIEROGLYPH, MdcSymbolCode.OVERWRITE),
                symbols.stream().map(MdcSymbol::code).toList());
    }

    @Test
    void hashWithDigitsIsShadingRegardlessOfContext() {
        List<MdcSymbol> symbols = scanAll("#1234");

        assertEquals(1, symbols.size());
        assertEquals(MdcSymbolCode.SHADING, symbols.get(0).code());
        assertEquals("#1234", symbols.get(0).value());
    }

    @Test
    void shadingAcceptsAnyAscendingSubsetOfQuadrants() {
        // "#13" is a real, distinct shading pattern (quadrants 1 and 3), not a lex error.
        List<MdcSymbol> symbols = scanAll("#13");

        assertEquals(1, symbols.size());
        assertEquals(MdcSymbolCode.SHADING, symbols.get(0).code());
        assertEquals("#13", symbols.get(0).value());
    }

    @Test
    void horizontalRuleParsesItsPositions() {
        List<MdcSymbol> symbols = scanAll("{l12,34}");

        assertEquals(1, symbols.size());
        assertEquals(MdcSymbolCode.HRULE, symbols.get(0).code());
        assertEquals(new HRule('l', 12, 34), symbols.get(0).value());
    }

    @Test
    void alphabeticTextUnescapesEmbeddedPlus() {
        List<MdcSymbol> symbols = scanAll("+ffoo\\+bar");

        assertEquals(1, symbols.size());
        assertEquals(MdcSymbolCode.TEXT, symbols.get(0).code());
        assertEquals(new AlphabeticText('f', "foo+bar"), symbols.get(0).value());
    }

    @Test
    void alphabeticTextStopsBeforeAPlusFollowedByALowercaseLetter() {
        // An unescaped "+" followed by a lowercase letter starts a *new* alphabetic-text token.
        List<MdcSymbol> symbols = scanAll("+fabc+ddef");

        assertEquals(2, symbols.size());
        assertEquals(new AlphabeticText('f', "abc"), symbols.get(0).value());
        assertEquals(new AlphabeticText('d', "def"), symbols.get(1).value());
    }

    @Test
    void cartoucheStartAndEndParsePartAsAnInt() {
        List<MdcSymbol> symbols = scanAll("<s2s2>");

        assertEquals(2, symbols.size());
        assertEquals(MdcSymbolCode.BEGIN_CARTOUCHE, symbols.get(0).code());
        assertEquals(new Cartouche('s', 2), symbols.get(0).value());
        assertEquals(MdcSymbolCode.END_CARTOUCHE, symbols.get(1).code());
        assertEquals(new Cartouche('s', 2), symbols.get(1).value());
    }

    @Test
    void bareAngleBracketsAreAnOldCartoucheStartAndANewCartoucheEnd() {
        // Asymmetric on purpose: the original grammar has no "bare old cartouche end" rule.
        List<MdcSymbol> symbols = scanAll("<>");

        assertEquals(MdcSymbolCode.BEGIN_OLD_CARTOUCHE, symbols.get(0).code());
        assertEquals(new OldCartoucheStart('c', 'a'), symbols.get(0).value());
        assertEquals(MdcSymbolCode.END_CARTOUCHE, symbols.get(1).code());
        assertEquals(new Cartouche('c', 2), symbols.get(1).value());
    }

    @Test
    void oldCartoucheStartKeepsPartAsAChar() {
        List<MdcSymbol> symbols = scanAll("<Sb");

        assertEquals(1, symbols.size());
        assertEquals(new OldCartoucheStart('S', 'b'), symbols.get(0).value());
    }

    @Test
    void philologyBracketsDefaultToSubtypeSymbols() {
        List<MdcSymbol> symbols = scanAll("[[A1]]");

        assertEquals(3, symbols.size());
        assertEquals(MdcSymbolCode.BEGIN_PHIL, symbols.get(0).code());
        assertEquals(PhilologyKind.ERASED_SIGNS, symbols.get(0).value());
        assertEquals(MdcSymbolCode.END_PHIL, symbols.get(2).code());
        assertEquals(PhilologyKind.ERASED_SIGNS, symbols.get(2).value());
    }

    @Test
    void philologyAsSignsModeProducesHieroglyphsInstead() {
        MdcLexer scanner = MdcLexicon.instance().newLexer("[[A1]]");
        scanner.setPhilologyAsSigns(true);

        MdcSymbol begin = scanner.nextSymbol().orElseThrow();
        assertEquals(MdcSymbolCode.HIEROGLYPH, begin.code());
        assertEquals(new MdcSign(new SignSubType.Philology(PhilologyKind.ERASED_SIGNS, true), "[["), begin.value());

        scanner.nextSymbol(); // the A1 sign in between
        MdcSymbol end = scanner.nextSymbol().orElseThrow();
        assertEquals(MdcSymbolCode.HIEROGLYPH, end.code());
        assertEquals(new MdcSign(new SignSubType.Philology(PhilologyKind.ERASED_SIGNS, false), "]]"), end.value());
    }

    @Test
    void openBraceSwitchesToPropertiesStateAndBackOnCloseBrace() {
        List<MdcSymbol> symbols = scanAll("A1[width=12,name=foo]B2");

        List<MdcSymbolCode> codes = symbols.stream().map(MdcSymbol::code).toList();
        assertEquals(List.of(
                MdcSymbolCode.HIEROGLYPH, MdcSymbolCode.OPEN_BRACE,
                MdcSymbolCode.IDENTIFIER, MdcSymbolCode.EQUAL, MdcSymbolCode.INTEGER, MdcSymbolCode.COMMA,
                MdcSymbolCode.IDENTIFIER, MdcSymbolCode.EQUAL, MdcSymbolCode.IDENTIFIER,
                MdcSymbolCode.CLOSE_BRACE, MdcSymbolCode.HIEROGLYPH),
                codes);
        MdcSymbol width = symbols.get(4);
        assertEquals(12, width.value());
    }

    @Test
    void doubleCurlyBracesAlsoSwitchState() {
        List<MdcSymbolCode> codes = codesOf("{{color=red}}");

        assertEquals(List.of(MdcSymbolCode.DOUBLE_LEFT_CURLY, MdcSymbolCode.IDENTIFIER,
                MdcSymbolCode.EQUAL, MdcSymbolCode.IDENTIFIER, MdcSymbolCode.DOUBLE_RIGHT_CURLY), codes);
    }

    @Test
    void propertiesStateIgnoresPlainWhitespaceUnconditionally() {
        List<MdcSymbolCode> codes = codesOf("[ a = 1 ]");

        assertEquals(List.of(MdcSymbolCode.OPEN_BRACE, MdcSymbolCode.IDENTIFIER, MdcSymbolCode.EQUAL,
                MdcSymbolCode.INTEGER, MdcSymbolCode.CLOSE_BRACE), codes);
    }

    @Test
    void wordEndAndSentenceEndAreSuppressedWithoutAPrecedingSign() {
        // Plain space/underscore runs with nothing expecting them produce no symbol at all.
        assertTrue(scanAll("  ").isEmpty());
        assertTrue(scanAll("__").isEmpty());
    }

    @Test
    void wordEndFollowsASignOrModifierOrClosingCurly() {
        assertEquals(List.of(MdcSymbolCode.HIEROGLYPH, MdcSymbolCode.WORD_END, MdcSymbolCode.HIEROGLYPH),
                codesOf("A1 B2"));

        assertEquals(List.of(MdcSymbolCode.HIEROGLYPH, MdcSymbolCode.SENTENCE_END, MdcSymbolCode.HIEROGLYPH),
                codesOf("A1  B2"));
    }

    @Test
    void resetReturnsToInitialStateWithoutMovingPosition() {
        MdcLexer scanner = MdcLexicon.instance().newLexer("[a=1]");
        scanner.nextSymbol(); // OPEN_BRACE, now in PROPERTIES state, position == 1
        scanner.reset();

        assertEquals(1, scanner.position());

        // Back in YYINITIAL: "a" is read as a sign code, not a PROPERTIES identifier.
        MdcSymbol next = scanner.nextSymbol().orElseThrow();
        assertEquals(MdcSymbolCode.HIEROGLYPH, next.code());
        assertEquals(2, scanner.position());
    }

    @Test
    void endOfInputYieldsEmpty() {
        MdcLexer scanner = MdcLexicon.instance().newLexer("A1");
        scanner.nextSymbol();
        assertFalse(scanner.nextSymbol().isPresent());
        assertFalse(scanner.nextSymbol().isPresent());
    }

    @Test
    void unrecognizedCharacterFallsBackToUnknown() {
        List<MdcSymbol> symbols = scanAll("§");

        assertEquals(1, symbols.size());
        assertEquals(MdcSymbolCode.UNKNOWN, symbols.get(0).code());
        assertEquals("§", symbols.get(0).value());
    }

    @Test
    void modifiersAreRecognizedInTheirThreeForms() {
        assertEquals(List.of(MdcSymbolCode.MODIFIER), codesOf("\\?"));
        assertEquals(List.of(MdcSymbolCode.MODIFIER), codesOf("\\R-5"));
        assertEquals(List.of(MdcSymbolCode.MODIFIER), codesOf("\\top12"));
    }

    @Test
    void quotedTextKeepsAnEscapedClosingBracketInside() {
        List<MdcSymbol> symbols = scanAll("\"a\\]b\"");

        assertEquals(1, symbols.size());
        assertEquals(new MdcSign(SignSubType.Plain.SMALL_TEXT, "\"a\\]b\""), symbols.get(0).value());
    }

    @Test
    void lineEndReadsThePercentageOrDefaultsToOneHundred() {
        List<MdcSymbol> symbols = scanAll("!=200% !");

        assertEquals(2, symbols.size());
        assertEquals(MdcSymbolCode.LINE_END, symbols.get(0).code());
        assertEquals(200, symbols.get(0).value());
        assertEquals(100, symbols.get(1).value());
    }

    @Test
    void pageEndBeatsLineEndOnADoubleBang() {
        assertEquals(List.of(MdcSymbolCode.PAGE_END), codesOf("!!"));
    }

    @Test
    void ligatureMarkersArePreferredOverShorterPrefixes() {
        assertEquals(List.of(MdcSymbolCode.LIG_AFTER), codesOf("&&&"));
        assertEquals(List.of(MdcSymbolCode.DOUBLE_AMP), codesOf("&&"));
        assertEquals(List.of(MdcSymbolCode.AMP), codesOf("&"));
        assertEquals(List.of(MdcSymbolCode.LIG_BEFORE), codesOf("^^^"));
        assertEquals(List.of(MdcSymbolCode.LIG_BEFORE), codesOf("^^"));
        assertEquals(List.of(MdcSymbolCode.TOGGLE), codesOf("^"));
    }
}
