package jsesh.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import jsesh.model.constants.SymbolCodes;
import jsesh.model.constants.ToggleType;
import jsesh.parser.ast.AstAbsoluteGroup;
import jsesh.parser.ast.AstAlphabeticText;
import jsesh.parser.ast.AstBasicItemList;
import jsesh.parser.ast.AstCadrat;
import jsesh.parser.ast.AstCartouche;
import jsesh.parser.ast.AstComplexLigature;
import jsesh.parser.ast.AstDocument;
import jsesh.parser.ast.AstHBox;
import jsesh.parser.ast.AstHRule;
import jsesh.parser.ast.AstHieroglyph;
import jsesh.parser.ast.AstLigature;
import jsesh.parser.ast.AstLineBreak;
import jsesh.parser.ast.AstModifier;
import jsesh.parser.ast.AstModifierList;
import jsesh.parser.ast.AstOption;
import jsesh.parser.ast.AstOptionList;
import jsesh.parser.ast.AstOverwrite;
import jsesh.parser.ast.AstPageBreak;
import jsesh.parser.ast.AstPhilology;
import jsesh.parser.ast.AstStartHieroglyphicText;
import jsesh.parser.ast.AstSubCadrat;
import jsesh.parser.ast.AstSuperscript;
import jsesh.parser.ast.AstTabStop;
import jsesh.parser.ast.AstTabbing;
import jsesh.parser.ast.AstTabbingClear;
import jsesh.parser.ast.AstToggle;
import jsesh.parser.ast.AstZoneStart;
import jsesh.parser.lex.MDCCartoucheType;

/**
 * Exhaustive, construct-by-construct tests of the Manuel de Codage grammar,
 * each comparing the whole parsed {@link jsesh.parser.ast.AstDocument} against
 * a hand-built expected tree (via the {@code of}/{@code builder} factories on
 * the AST node types), rather than picking apart individual fields as
 * {@link MDCParserAstGeneratorTest} does.
 * <p>Every MDC snippet here was checked directly against the grammar
 * ({@code jsesh/src/jcup/MDCParse.y}) and lexer ({@code jsesh/src/jlex/MDCLexAux.l}),
 * not just against the parser's observed behavior.
 */
public class MDCParserAstSyntaxTest {

    private AstDocument parse(String mdc) throws MDCSyntaxError {
        return new MDCParserAstGenerator().parse(mdc);
    }

    /**
     * {@link MDCParserFacade} defaults to {@code philologyAsSigns = true}
     * (Winglyph-style: {@code [[}, {@code ]]}, etc. are ordinary sign codes).
     * The philology <em>grouping</em> constructs tested here need it off
     * (tksesh-style), which {@link MDCParserAstGenerator#setPhilologyAsSigns}
     * exposes but does not default to.
     */
    private AstDocument parseWithPhilologyGrouping(String mdc) throws MDCSyntaxError {
        MDCParserAstGenerator generator = new MDCParserAstGenerator();
        generator.setPhilologyAsSigns(false);
        return generator.parse(mdc);
    }

    // --- Juxtaposition, superposition, stacking ------------------------

    @Test
    public void testEmptyString() throws MDCSyntaxError {
        assertEquals(AstDocument.of(), parse(""));
    }

    @Test
    public void testJuxtaposition() throws MDCSyntaxError {
        AstDocument expected = AstDocument.of(
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("i"))),
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("w"))),
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("r"))));
        assertEquals(expected, parse("i-w-r"));
    }

    @Test
    public void testSuperposition() throws MDCSyntaxError {
        AstDocument expected = AstDocument.of(
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("p"), AstHieroglyph.of("t"))));
        assertEquals(expected, parse("p*t"));
    }

    @Test
    public void testStacking() throws MDCSyntaxError {
        AstDocument expected = AstDocument.of(
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("a")), AstHBox.of(AstHieroglyph.of("m"))));
        assertEquals(expected, parse("a:m"));
    }

    /**
     * "-" is purely a cosmetic, optional separator between top items
     * (cadrats); it never merges anything. ":" only fuses the preceding and
     * following elements into the same cadrat when it directly touches them
     * (no "-" in between).
     */
    @Test
    public void testCombinedStructure() throws MDCSyntaxError {
        AstDocument expected = AstDocument.of(
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("i"))),
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("w"))),
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("r")), AstHBox.of(AstHieroglyph.of("a"))),
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("C1"))),
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("m"))),
                AstCadrat.of(
                        AstHBox.of(AstHieroglyph.of("pt")),
                        AstHBox.of(AstHieroglyph.of("p"), AstHieroglyph.of("t"))));
        assertEquals(expected, parse("i-w-r:a-C1-m-pt:p*t"));
    }

    @Test
    public void testCodesAreNotCanonicalized() throws MDCSyntaxError {
        // "i" stays "i"; canonicalizing to Gardiner codes (e.g. "M17") happens
        // later, via ManuelDeCodage / MdcUnicodeTable, not in the AST.
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(AstHieroglyph.of("i")))), parse("i"));
    }

    // --- Modifiers -------------------------------------------------------

    @Test
    public void testModifierFlag() throws MDCSyntaxError {
        AstHieroglyph expectedGlyph = AstHieroglyph.of("m", AstModifierList.of(AstModifier.flag("det")));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expectedGlyph))), parse("m\\det"));
    }

    @Test
    public void testModifierValue() throws MDCSyntaxError {
        AstHieroglyph expectedGlyph = AstHieroglyph.of("m", AstModifierList.of(AstModifier.of("col", 50)));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expectedGlyph))), parse("m\\col50"));
    }

    @Test
    public void testMultipleModifiers() throws MDCSyntaxError {
        AstHieroglyph expectedGlyph = AstHieroglyph.of("m",
                AstModifierList.of(AstModifier.flag("det"), AstModifier.of("col", 50)));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expectedGlyph))), parse("m\\det\\col50"));
    }

    // --- Grammar flag, word/sentence end ----------------------------------

    @Test
    public void testGrammarFlag() throws MDCSyntaxError {
        AstHieroglyph expectedGlyph = AstHieroglyph.builder("n").grammar().build();
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expectedGlyph))), parse("=n"));
    }

    @Test
    public void testWordEnd() throws MDCSyntaxError {
        // "_" right after a sign is a meaningful (word-ending) space.
        AstDocument expected = AstDocument.of(
                AstCadrat.of(AstHBox.of(AstHieroglyph.builder("i").wordEnd().build())),
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("w"))));
        assertEquals(expected, parse("i_w"));
    }

    @Test
    public void testSentenceEnd() throws MDCSyntaxError {
        // "__" (or two spaces) right after a sign is a sentence-ending space.
        AstDocument expected = AstDocument.of(
                AstCadrat.of(AstHBox.of(AstHieroglyph.builder("i").sentenceEnd().build())),
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("w"))));
        assertEquals(expected, parse("i__w"));
    }

    // --- Shading -----------------------------------------------------------

    @Test
    public void testCadratShading_singleQuarter() throws MDCSyntaxError {
        AstCadrat expected = AstCadrat.builder().hBoxes(AstHBox.of(AstHieroglyph.of("i"))).shading(1).build();
        assertEquals(AstDocument.of(expected), parse("i#1"));
    }

    @Test
    public void testCadratShading_allQuarters() throws MDCSyntaxError {
        AstCadrat expected = AstCadrat.builder().hBoxes(AstHBox.of(AstHieroglyph.of("i"))).shading(15).build();
        assertEquals(AstDocument.of(expected), parse("i#1234"));
    }

    // --- Cadrat options (preserved here; AstModelBuilder drops them) ------

    @Test
    public void testCadratWithOptions() throws MDCSyntaxError {
        AstCadrat expected = AstCadrat.builder()
                .hBoxes(AstHBox.of(AstHieroglyph.of("x")))
                .options(AstOptionList.of(AstOption.flag("foo")))
                .build();
        assertEquals(AstDocument.of(expected), parse("quadrat(x)[foo]"));
    }

    // --- Cartouches ----------------------------------------------------

    @Test
    public void testCartouche_bareDefaultsToFullyDrawn() throws MDCSyntaxError {
        AstCartouche expected = AstCartouche.of(AstCadrat.of(AstHBox.of(AstHieroglyph.of("A1"))));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parse("<A1>"));
    }

    @Test
    public void testCartouche_serekh() throws MDCSyntaxError {
        AstCartouche expected = new AstCartouche(MDCCartoucheType.SEREKH, 1, 2,
                AstBasicItemList.of(AstCadrat.of(AstHBox.of(AstHieroglyph.of("A1")))));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parse("<sA1>"));
    }

    @Test
    public void testCartouche_partialDrawing() throws MDCSyntaxError {
        // "<1" starts a (modern) cartouche only drawing its start ("1"); "0>"
        // closes it without drawing anything at the end ("0"). Empty content.
        AstCartouche expected = new AstCartouche(MDCCartoucheType.CARTOUCHE, 1, 0, AstBasicItemList.of());
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parse("<1--0>"));
    }

    // --- Ligatures and complex ligatures ---------------------------------

    @Test
    public void testLigature() throws MDCSyntaxError {
        AstLigature expected = AstLigature.of(AstHieroglyph.of("p"), AstHieroglyph.of("t"), AstHieroglyph.of("n"));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parse("p&t&n"));
    }

    @Test
    public void testComplexLigature_beforeGroupOnly() throws MDCSyntaxError {
        AstComplexLigature expected = new AstComplexLigature(AstHieroglyph.of("t"), AstHieroglyph.of("w"), null);
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parse("t^^w"));
    }

    @Test
    public void testComplexLigature_afterGroupOnly() throws MDCSyntaxError {
        AstComplexLigature expected = new AstComplexLigature(null, AstHieroglyph.of("w"), AstHieroglyph.of("t"));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parse("w&&&t"));
    }

    @Test
    public void testComplexLigature_bothGroups() throws MDCSyntaxError {
        AstComplexLigature expected = new AstComplexLigature(AstHieroglyph.of("t"), AstHieroglyph.of("w"),
                AstHieroglyph.of("n"));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parse("t^^w&&&n"));
    }

    // --- Overwrite ---------------------------------------------------------

    @Test
    public void testOverwrite() throws MDCSyntaxError {
        AstOverwrite expected = new AstOverwrite(AstHieroglyph.of("a"), AstHieroglyph.of("b"));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parse("a##b"));
    }

    // --- Sub-cadrat ----------------------------------------------------

    @Test
    public void testSubCadrat() throws MDCSyntaxError {
        AstSubCadrat expected = AstSubCadrat.of(AstCadrat.of(AstHBox.of(AstHieroglyph.of("x"))));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parse("(x)"));
    }

    // --- Philology -------------------------------------------------------

    @Test
    public void testPhilology_erasedSigns() throws MDCSyntaxError {
        AstPhilology expected = AstPhilology.of(SymbolCodes.ERASEDSIGNS,
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("x"))));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parseWithPhilologyGrouping("[[x]]"));
    }

    @Test
    public void testPhilology_minorAddition() throws MDCSyntaxError {
        AstPhilology expected = AstPhilology.of(SymbolCodes.MINORADDITION,
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("x"))));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parseWithPhilologyGrouping("[(x)]"));
    }

    // --- Absolute groups ---------------------------------------------------

    @Test
    public void testAbsoluteGroup_defaultPosition() throws MDCSyntaxError {
        AstAbsoluteGroup expected = AstAbsoluteGroup.of(AstHieroglyph.of("A1"), AstHieroglyph.of("B1"));
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parse("A1&&B1"));
    }

    @Test
    public void testAbsoluteGroup_explicitPosition() throws MDCSyntaxError {
        AstAbsoluteGroup expected = AstAbsoluteGroup.of(
                AstHieroglyph.of("A1"),
                AstHieroglyph.builder("B1").position(100, 200, 50).build());
        assertEquals(AstDocument.of(AstCadrat.of(AstHBox.of(expected))), parse("A1&&B1{{100,200,50}}"));
    }

    // --- Toggles: kept as nodes, not folded into item state ---------------

    @Test
    public void testToggles() throws MDCSyntaxError {
        AstDocument expected = AstDocument.of(
                new AstToggle(ToggleType.RED),
                new AstToggle(ToggleType.BLACK),
                new AstToggle(ToggleType.BLACKRED),
                new AstToggle(ToggleType.LACUNA),
                new AstToggle(ToggleType.LINELACUNA),
                new AstToggle(ToggleType.OMMIT),
                new AstToggle(ToggleType.SHADINGON),
                new AstToggle(ToggleType.SHADINGOFF));
        assertEquals(expected, parse("$r-$b-$-?-??-^-#b-#e"));
    }

    // --- Text and superscript ---------------------------------------------

    @Test
    public void testAlphabeticText_latin() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstAlphabeticText('l', "hello")), parse("+lhello"));
    }

    @Test
    public void testAlphabeticText_transliteration() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstAlphabeticText('t', "nfr")), parse("+tnfr"));
    }

    @Test
    public void testSuperscript() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstSuperscript("note")), parse("|note"));
    }

    // --- Start-hieroglyphic-text marker ("+s") -----------------------------

    @Test
    public void testStartHieroglyphicText() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstStartHieroglyphicText()), parse("+s"));
    }

    @Test
    public void testAlphabeticText_and_hieroglyphs() throws MDCSyntaxError {

        String mdc = "+lhello+s-i-w-r";
        AstDocument expected = AstDocument.of(
                new AstAlphabeticText('l', "hello"),
                new AstStartHieroglyphicText(),
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("i"))),
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("w"))),
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("r"))));
        assertEquals(expected, parse(mdc));
    }
     

    // --- Horizontal rules ------------------------------------------------

    @Test
    public void testHRule_thin() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstHRule('l', 100, 200)), parse("{l100,200}"));
    }

    @Test
    public void testHRule_wide() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstHRule('L', 50, 300)), parse("{L50,300}"));
    }

    // --- Line breaks, page breaks, tab stops -------------------------------

    @Test
    public void testLineBreak_defaultSkip() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstLineBreak(100)), parse("!"));
    }

    @Test
    public void testLineBreak_explicitSkip() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstLineBreak(200)), parse("!=200%"));
    }

    @Test
    public void testPageBreak() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstPageBreak()), parse("!!"));
    }

    @Test
    public void testTabStop() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstTabStop(123)), parse("?123"));
    }

    // --- Zone markers, tabbing ---------------------------------------------

    @Test
    public void testZoneStart_bare() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstZoneStart(null)), parse("zone"));
    }

    @Test
    public void testZoneStart_withOptions() throws MDCSyntaxError {
        AstZoneStart expected = new AstZoneStart(AstOptionList.of(AstOption.of("id", 1)));
        assertEquals(AstDocument.of(expected), parse("zone[id=1]"));
    }

    @Test
    public void testTabbing() throws MDCSyntaxError {
        AstTabbing expected = new AstTabbing(AstOptionList.of(AstOption.of("id", 1)));
        assertEquals(AstDocument.of(expected), parse("%[id=1]"));
    }

    @Test
    public void testTabbingClear() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstTabbingClear()), parse("%clear"));
    }
}
