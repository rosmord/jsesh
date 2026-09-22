package jsesh.parser.lex;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import java_cup.runtime.Symbol;
import jsesh.model.constants.SymbolCodes;
import jsesh.model.constants.ToggleType;

/**
 * Test of the lexer behaviour.
 * LexTest
 *
 * Each test feeds a snippet of Manuel de Codage to the lexer and checks the
 * token(s) it produces, aiming to cover every construct recognised by
 * {@code MDCLexAux.l} at least once.
 */
public class LexTest {

    /**
     * Builds a lexer for {@code mdc}. All lexer construction goes through
     * here, so the way it is instantiated can be changed in one place.
     */
    private MDCLex newLexer(String mdc) {
        return new MDCLex(new StringReader(mdc));
    }

    /**
     * Runs a lexer to completion, collecting every token it produces,
     * including the trailing EOF token.
     */
    private List<Symbol> tokens(MDCLex lex) throws IOException {
        List<Symbol> result = new ArrayList<>();
        Symbol token;
        do {
            token = lex.next_token();
            result.add(token);
        } while (token.sym != MDCSymbols.EOF);
        return result;
    }

    private List<Symbol> tokens(String mdc) throws IOException {
        return tokens(newLexer(mdc));
    }

    private Symbol firstToken(String mdc) throws IOException {
        return tokens(mdc).get(0);
    }

    private int subType(Symbol token) {
        return ((MDCSubType) token.value).getSubType();
    }

    private void assertSign(Symbol token, int type, String text) {
        assertEquals(MDCSymbols.HIEROGLYPH, token.sym);
        MDCSign sign = (MDCSign) token.value;
        assertEquals(type, sign.getType());
        assertEquals(text, sign.getString());
    }

    private void assertToggle(Symbol token, ToggleType expected) {
        assertEquals(MDCSymbols.TOGGLE, token.sym);
        assertEquals(expected, token.value);
    }

    // --- Old-style cartouches/enclosures ("<...") ---

    @Test
    public void testRoundEnclosureG() throws IOException {
        Symbol token = firstToken("<G");
        assert (token.value instanceof MDCStartOldCartouche);
        MDCStartOldCartouche start = (MDCStartOldCartouche) token.value;
        assertAll(
                "",
                () -> assertEquals(MDCCartoucheType.CIRCULAR_ENCLOSURE, start.getCartoucheType()),
                () -> assertEquals('a', start.getPart()));
    }

    @Test
    public void testRoundEnclosureg1() throws IOException {
        Symbol token = firstToken("<g");
        assert (token.value instanceof MDCCartouche);
        MDCCartouche start = (MDCCartouche) token.value;
        assertAll(
                "",
                () -> assertEquals(MDCCartoucheType.CIRCULAR_ENCLOSURE, start.getCartoucheType()),
                () -> assertEquals(1, start.getPart()));
    }

    @Test
    public void testRoundEnclosureg0() throws IOException {
        Symbol token = firstToken("<g0");
        assert (token.value instanceof MDCCartouche);
        MDCCartouche start = (MDCCartouche) token.value;
        assertAll(
                "",
                () -> assertEquals(MDCCartoucheType.CIRCULAR_ENCLOSURE, start.getCartoucheType()),
                () -> assertEquals(0, start.getPart()));
    }

    @Test
    public void testOldCartoucheDefault() throws IOException {
        Symbol token = firstToken("<");
        assertEquals(MDCSymbols.BEGINOLDCARTOUCHE, token.sym);
        MDCStartOldCartouche start = (MDCStartOldCartouche) token.value;
        assertAll(
                () -> assertEquals(MDCCartoucheType.CARTOUCHE, start.getCartoucheType()),
                () -> assertEquals('a', start.getPart()));
    }

    @Test
    public void testOldCartoucheTypedPart() throws IOException {
        Symbol token = firstToken("<Sb");
        MDCStartOldCartouche start = (MDCStartOldCartouche) token.value;
        assertAll(
                () -> assertEquals(MDCCartoucheType.SEREKH, start.getCartoucheType()),
                () -> assertEquals('b', start.getPart()));
    }

    @Test
    public void testOldCartouchePartOnly() throws IOException {
        Symbol token = firstToken("<b");
        MDCStartOldCartouche start = (MDCStartOldCartouche) token.value;
        assertAll(
                () -> assertEquals(MDCCartoucheType.CARTOUCHE, start.getCartoucheType()),
                () -> assertEquals('b', start.getPart()));
    }

    // --- New-style cartouches ("<s0", "s0>", ...) ---

    @Test
    public void testCartoucheBeginDigitOnly() throws IOException {
        Symbol token = firstToken("<1");
        assertEquals(MDCSymbols.BEGINCARTOUCHE, token.sym);
        MDCCartouche start = (MDCCartouche) token.value;
        assertAll(
                () -> assertEquals(MDCCartoucheType.CARTOUCHE, start.getCartoucheType()),
                () -> assertEquals(1, start.getPart()));
    }

    @Test
    public void testCartoucheEndDefault() throws IOException {
        Symbol token = firstToken(">");
        assertEquals(MDCSymbols.ENDCARTOUCHE, token.sym);
        MDCCartouche end = (MDCCartouche) token.value;
        assertAll(
                () -> assertEquals(MDCCartoucheType.CARTOUCHE, end.getCartoucheType()),
                () -> assertEquals(2, end.getPart()));
    }

    @Test
    public void testCartoucheEndTypedWithPart() throws IOException {
        Symbol token = firstToken("s0>");
        MDCCartouche end = (MDCCartouche) token.value;
        assertAll(
                () -> assertEquals(MDCCartoucheType.SEREKH, end.getCartoucheType()),
                () -> assertEquals(0, end.getPart()));
    }

    @Test
    public void testCartoucheEndTypedDefaultPart() throws IOException {
        Symbol token = firstToken("h>");
        MDCCartouche end = (MDCCartouche) token.value;
        assertAll(
                () -> assertEquals(MDCCartoucheType.HOUT, end.getCartoucheType()),
                () -> assertEquals(2, end.getPart()));
    }

    @Test
    public void testCartoucheEndDigitOnly() throws IOException {
        Symbol token = firstToken("1>");
        MDCCartouche end = (MDCCartouche) token.value;
        assertAll(
                () -> assertEquals(MDCCartoucheType.CARTOUCHE, end.getCartoucheType()),
                () -> assertEquals(1, end.getPart()));
    }

    // --- Philological marks ("[[", "]]", "[{", "}]", ...) ---

    @Test
    public void testPhilologyErasedSigns() throws IOException {
        Symbol begin = firstToken("[[");
        Symbol end = firstToken("]]");
        assertAll(
                () -> assertEquals(MDCSymbols.BEGINPHIL, begin.sym),
                () -> assertEquals(SymbolCodes.ERASEDSIGNS, subType(begin)),
                () -> assertEquals(MDCSymbols.ENDPHIL, end.sym),
                () -> assertEquals(SymbolCodes.ERASEDSIGNS, subType(end)));
    }

    @Test
    public void testPhilologyEditorSuperfluous() throws IOException {
        Symbol begin = firstToken("[{");
        Symbol end = firstToken("}]");
        assertAll(
                () -> assertEquals(MDCSymbols.BEGINPHIL, begin.sym),
                () -> assertEquals(SymbolCodes.EDITORSUPERFLUOUS, subType(begin)),
                () -> assertEquals(MDCSymbols.ENDPHIL, end.sym),
                () -> assertEquals(SymbolCodes.EDITORSUPERFLUOUS, subType(end)));
    }

    @Test
    public void testPhilologyPreviouslyReadable() throws IOException {
        Symbol begin = firstToken("[\"");
        Symbol end = firstToken("\"]");
        assertAll(
                () -> assertEquals(MDCSymbols.BEGINPHIL, begin.sym),
                () -> assertEquals(SymbolCodes.PREVIOUSLYREADABLE, subType(begin)),
                () -> assertEquals(MDCSymbols.ENDPHIL, end.sym),
                () -> assertEquals(SymbolCodes.PREVIOUSLYREADABLE, subType(end)));
    }

    @Test
    public void testPhilologyScribeAddition() throws IOException {
        Symbol begin = firstToken("['");
        Symbol end = firstToken("']");
        assertAll(
                () -> assertEquals(MDCSymbols.BEGINPHIL, begin.sym),
                () -> assertEquals(SymbolCodes.SCRIBEADDITION, subType(begin)),
                () -> assertEquals(MDCSymbols.ENDPHIL, end.sym),
                () -> assertEquals(SymbolCodes.SCRIBEADDITION, subType(end)));
    }

    @Test
    public void testPhilologyEditorAddition() throws IOException {
        Symbol begin = firstToken("[&");
        Symbol end = firstToken("&]");
        assertAll(
                () -> assertEquals(MDCSymbols.BEGINPHIL, begin.sym),
                () -> assertEquals(SymbolCodes.EDITORADDITION, subType(begin)),
                () -> assertEquals(MDCSymbols.ENDPHIL, end.sym),
                () -> assertEquals(SymbolCodes.EDITORADDITION, subType(end)));
    }

    @Test
    public void testPhilologyMinorAddition() throws IOException {
        Symbol begin = firstToken("[(");
        Symbol end = firstToken(")]");
        assertAll(
                () -> assertEquals(MDCSymbols.BEGINPHIL, begin.sym),
                () -> assertEquals(SymbolCodes.MINORADDITION, subType(begin)),
                () -> assertEquals(MDCSymbols.ENDPHIL, end.sym),
                () -> assertEquals(SymbolCodes.MINORADDITION, subType(end)));
    }

    @Test
    public void testPhilologyDubious() throws IOException {
        Symbol begin = firstToken("[?");
        Symbol end = firstToken("?]");
        assertAll(
                () -> assertEquals(MDCSymbols.BEGINPHIL, begin.sym),
                () -> assertEquals(SymbolCodes.DUBIOUS, subType(begin)),
                () -> assertEquals(MDCSymbols.ENDPHIL, end.sym),
                () -> assertEquals(SymbolCodes.DUBIOUS, subType(end)));
    }

    @Test
    public void testPhilologyAsSigns() throws IOException {
        MDCLex lex = newLexer("[[");
        lex.setPhilologyAsSigns(true);
        Symbol token = lex.next_token();
        assertEquals(MDCSymbols.HIEROGLYPH, token.sym);
        MDCSign sign = (MDCSign) token.value;
        assertAll(
                () -> assertEquals(SymbolCodes.BEGINERASE, sign.getType()),
                () -> assertEquals("[[", sign.getString()));
    }

    // --- Hieroglyph signs ---

    @Test
    public void testHieroglyphMdcCodeBacktick() throws IOException {
        assertSign(firstToken("`"), SymbolCodes.MDCCODE, "`");
    }

    @Test
    public void testHieroglyphGardinerCode() throws IOException {
        assertSign(firstToken("A1"), SymbolCodes.MDCCODE, "A1");
    }

    @Test
    public void testHieroglyphAlphanumericCode() throws IOException {
        assertSign(firstToken("abc123"), SymbolCodes.MDCCODE, "abc123");
    }

    @Test
    public void testHieroglyphAtCode() throws IOException {
        assertSign(firstToken("@x"), SymbolCodes.MDCCODE, "@x");
    }

    @Test
    public void testHieroglyphRedRoundPoint() throws IOException {
        assertSign(firstToken("o"), SymbolCodes.REDPOINT, "o");
    }

    @Test
    public void testHieroglyphBlackRoundPoint() throws IOException {
        assertSign(firstToken("O"), SymbolCodes.BLACKPOINT, "O");
    }

    @Test
    public void testHieroglyphSmallText() throws IOException {
        assertSign(firstToken("\"hello\""), SymbolCodes.SMALLTEXT, "\"hello\"");
    }

    @Test
    public void testHieroglyphSpacingSigns() throws IOException {
        assertSign(firstToken("."), SymbolCodes.HALFSPACE, ".");
        assertSign(firstToken(".."), SymbolCodes.FULLSPACE, "..");
    }

    @Test
    public void testHieroglyphShadingSigns() throws IOException {
        assertSign(firstToken("//"), SymbolCodes.FULLSHADE, "//");
        assertSign(firstToken("v/"), SymbolCodes.VERTICALSHADE, "v/");
        assertSign(firstToken("/"), SymbolCodes.QUATERSHADE, "/");
        assertSign(firstToken("h/"), SymbolCodes.HORIZONTALSHADE, "h/");
    }

    // --- Toggles ("#", "#b", "$r", "?", "^", ...) ---

    @Test
    public void testShadingToggleVariants() throws IOException {
        assertToggle(firstToken("-#-"), ToggleType.SHADINGTOGGLE);
        assertToggle(firstToken("-#"), ToggleType.SHADINGTOGGLE);
        assertToggle(firstToken("#-"), ToggleType.SHADINGTOGGLE);
        assertToggle(firstToken("#"), ToggleType.SHADINGTOGGLE);
    }

    @Test
    public void testShadingOnOffToggle() throws IOException {
        assertToggle(firstToken("#b"), ToggleType.SHADINGON);
        assertToggle(firstToken("#e"), ToggleType.SHADINGOFF);
    }

    @Test
    public void testColorToggle() throws IOException {
        assertToggle(firstToken("$r"), ToggleType.RED);
        assertToggle(firstToken("$b"), ToggleType.BLACK);
        assertToggle(firstToken("$"), ToggleType.BLACKRED);
    }

    @Test
    public void testLacunaToggle() throws IOException {
        assertToggle(firstToken("?"), ToggleType.LACUNA);
        assertToggle(firstToken("??"), ToggleType.LINELACUNA);
    }

    @Test
    public void testOmmitToggle() throws IOException {
        assertToggle(firstToken("^"), ToggleType.OMMIT);
    }

    // --- Shading patterns and overwrite ("#1234", "##", sign followed by "#") ---

    @Test
    public void testShadingPattern() throws IOException {
        Symbol full = firstToken("#1234");
        assertEquals(MDCSymbols.SHADING, full.sym);
        assertEquals(15, ((MDCShading) full.value).getShading());

        Symbol partial = firstToken("#12");
        assertEquals(3, ((MDCShading) partial.value).getShading());
    }

    @Test
    public void testShadingAndOverwriteAfterSign() throws IOException {
        List<Symbol> withShading = tokens("A1#-");
        assertEquals(MDCSymbols.HIEROGLYPH, withShading.get(0).sym);
        assertEquals(MDCSymbols.SHADING, withShading.get(1).sym);
        assertEquals(15, ((MDCShading) withShading.get(1).value).getShading());

        List<Symbol> withOverwrite = tokens("A1#");
        assertEquals(MDCSymbols.HIEROGLYPH, withOverwrite.get(0).sym);
        assertEquals(MDCSymbols.OVERWRITE, withOverwrite.get(1).sym);
    }

    @Test
    public void testExplicitOverwrite() throws IOException {
        assertEquals(MDCSymbols.OVERWRITE, firstToken("##").sym);
    }

    // --- Modifiers ("\?", "\a12", "\R-3") ---

    @Test
    public void testModifierQuestionMark() throws IOException {
        MDCModifier modifier = (MDCModifier) firstToken("\\?").value;
        assertEquals("?", modifier.getName());
        assertNull(modifier.getIntValue());
    }

    @Test
    public void testModifierWithValue() throws IOException {
        MDCModifier modifier = (MDCModifier) firstToken("\\a12").value;
        assertEquals("a", modifier.getName());
        assertEquals(12, modifier.getIntValue());
    }

    @Test
    public void testModifierRWithNegativeValue() throws IOException {
        MDCModifier modifier = (MDCModifier) firstToken("\\R-3").value;
        assertEquals("R", modifier.getName());
        assertEquals(-3, modifier.getIntValue());
    }

    // --- Word/sentence separation, driven by lexer spacing state ---

    @Test
    public void testWordEndAfterSign() throws IOException {
        List<Symbol> t = tokens("A1 ");
        assertEquals(MDCSymbols.HIEROGLYPH, t.get(0).sym);
        assertEquals(MDCSymbols.WORDEND, t.get(1).sym);
        assertEquals(MDCSymbols.EOF, t.get(2).sym);
    }

    @Test
    public void testSentenceEndAfterSign() throws IOException {
        List<Symbol> t = tokens("A1  ");
        assertEquals(MDCSymbols.HIEROGLYPH, t.get(0).sym);
        assertEquals(MDCSymbols.SENTENCEEND, t.get(1).sym);
        assertEquals(MDCSymbols.EOF, t.get(2).sym);
    }

    @Test
    public void testSeparator() throws IOException {
        assertEquals(MDCSymbols.SEPARATOR, firstToken("-").sym);
        // Trailing dashes/spaces after a separator collapse into a single token.
        assertEquals(2, tokens("---").size());
    }

    // --- Parentheses, ampersands, ligatures, grammar mark ---

    @Test
    public void testParentheses() throws IOException {
        assertEquals(MDCSymbols.BPAR, firstToken("(").sym);
        assertEquals(MDCSymbols.EPAR, firstToken(")").sym);
    }

    @Test
    public void testAmpersandVariants() throws IOException {
        assertEquals(MDCSymbols.AMP, firstToken("&").sym);
        assertEquals(MDCSymbols.DOUBLEAMP, firstToken("&&").sym);
        assertEquals(MDCSymbols.DOUBLEAMP, firstToken("**").sym);
        assertEquals(MDCSymbols.LIGAFTER, firstToken("&&&").sym);
    }

    @Test
    public void testLigatureBefore() throws IOException {
        assertEquals(MDCSymbols.LIGBEFORE, firstToken("^^").sym);
        assertEquals(MDCSymbols.LIGBEFORE, firstToken("^^^").sym);
    }

    @Test
    public void testGrammar() throws IOException {
        assertEquals(MDCSymbols.GRAMMAR, firstToken("=").sym);
    }

    @Test
    public void testColonAndStar() throws IOException {
        assertEquals(MDCSymbols.COLON, firstToken(":").sym);
        assertEquals(MDCSymbols.STAR, firstToken("*").sym);
    }

    // --- Page/line control, tab stops, tabbing ---

    @Test
    public void testPageEnd() throws IOException {
        assertEquals(MDCSymbols.PAGEEND, firstToken("!!").sym);
    }

    @Test
    public void testLineEndDefaultSkip() throws IOException {
        Symbol token = firstToken("!");
        assertEquals(MDCSymbols.LINEEND, token.sym);
        assertEquals(100, token.value);
    }

    @Test
    public void testLineEndWithPercentage() throws IOException {
        Symbol token = firstToken("!=200%");
        assertEquals(MDCSymbols.LINEEND, token.sym);
        assertEquals(200, token.value);
    }

    @Test
    public void testTabStop() throws IOException {
        Symbol token = firstToken("?5");
        assertEquals(MDCSymbols.TABSTOP, token.sym);
        assertEquals(5, token.value);
    }

    @Test
    public void testTabbing() throws IOException {
        assertEquals(MDCSymbols.TABBING, firstToken("%").sym);
        assertEquals(MDCSymbols.TABBINGCLEAR, firstToken("%clear").sym);
    }

    @Test
    public void testHorizontalRules() throws IOException {
        MDCHRule simple = (MDCHRule) firstToken("{l1,5}").value;
        assertAll(
                () -> assertEquals('l', simple.getLineType()),
                () -> assertEquals(1, simple.getStartPos()),
                () -> assertEquals(5, simple.getEndPos()));

        MDCHRule wide = (MDCHRule) firstToken("{L2,9}").value;
        assertAll(
                () -> assertEquals('L', wide.getLineType()),
                () -> assertEquals(2, wide.getStartPos()),
                () -> assertEquals(9, wide.getEndPos()));
    }

    // --- Zones, quadrats, alphabetic and superscript text ---

    @Test
    public void testZoneAndCadratKeywords() throws IOException {
        assertEquals(MDCSymbols.ZONE, firstToken("zone").sym);
        assertEquals(MDCSymbols.CADRAT, firstToken("quadrat").sym);
    }

    @Test
    public void testStartHieroglyphs() throws IOException {
        assertEquals(MDCSymbols.STARTHIEROGLYPHS, firstToken("+s").sym);
    }

    @Test
    public void testTextSuperscript() throws IOException {
        Symbol token = firstToken("|super");
        assertEquals(MDCSymbols.TEXTSUPER, token.sym);
        assertEquals("super", token.value);
    }

    @Test
    public void testAlphabeticText() throws IOException {
        Symbol token = firstToken("+lHello");
        assertEquals(MDCSymbols.TEXT, token.sym);
        MDCAlphabeticText text = (MDCAlphabeticText) token.value;
        assertAll(
                () -> assertEquals('l', text.getScriptCode()),
                () -> assertEquals("Hello", text.getText()));
    }

    // --- Property blocks: "[...]" and "{{...}}" ---

    @Test
    public void testPropertyBlock() throws IOException {
        List<Symbol> t = tokens("[abc,123=45]");
        assertEquals(MDCSymbols.OPENBRACE, t.get(0).sym);
        assertEquals(MDCSymbols.IDENTIFIER, t.get(1).sym);
        assertEquals("abc", t.get(1).value);
        assertEquals(MDCSymbols.COMMA, t.get(2).sym);
        assertEquals(MDCSymbols.INTEGER, t.get(3).sym);
        assertEquals(123, t.get(3).value);
        assertEquals(MDCSymbols.EQUAL, t.get(4).sym);
        assertEquals(MDCSymbols.INTEGER, t.get(5).sym);
        assertEquals(45, t.get(5).value);
        assertEquals(MDCSymbols.CLOSEBRACE, t.get(6).sym);
        assertEquals(MDCSymbols.EOF, t.get(7).sym);
    }

    @Test
    public void testDoubleCurlyPropertyBlock() throws IOException {
        List<Symbol> t = tokens("{{a=1}} ");
        assertEquals(MDCSymbols.DOUBLELEFTCURLY, t.get(0).sym);
        assertEquals(MDCSymbols.IDENTIFIER, t.get(1).sym);
        assertEquals(MDCSymbols.EQUAL, t.get(2).sym);
        assertEquals(MDCSymbols.INTEGER, t.get(3).sym);
        assertEquals(MDCSymbols.DOUBLERIGHTCURLY, t.get(4).sym);
        // "}}" is treated like a sign for spacing purposes.
        assertEquals(MDCSymbols.WORDEND, t.get(5).sym);
        assertEquals(MDCSymbols.EOF, t.get(6).sym);
    }

    // --- Fallback behaviour ---

    @Test
    public void testUnknownCharacter() throws IOException {
        Symbol token = firstToken(";");
        assertEquals(MDCSymbols.UNKNOWN, token.sym);
        assertEquals(";", token.value);
    }

    @Test
    public void testEndOfFile() throws IOException {
        List<Symbol> t = tokens("");
        assertEquals(1, t.size());
        assertEquals(MDCSymbols.EOF, t.get(0).sym);
    }
}
