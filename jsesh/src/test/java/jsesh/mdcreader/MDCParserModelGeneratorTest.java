package jsesh.mdcreader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jsesh.model.AlphabeticCharacter;
import jsesh.model.Cadrat;
import jsesh.model.Cartouche;
import jsesh.model.constants.CartouchePart;
import jsesh.model.HBox;
import jsesh.model.Hieroglyph;
import jsesh.model.HorizontalListElement;
import jsesh.model.MdcComment;
import jsesh.model.ModelElementDeepAdapter;
import jsesh.model.constants.ScriptCode;
import jsesh.model.TopItemList;
import jsesh.parser.MDCSyntaxError;

/**
 * Structural tests for {@link MDCParserModelGenerator}: unlike the existing
 * parser-related tests (which only compare parsed trees via equalsIgnoreId,
 * or flatten them to a list of codes), these tests inspect the actual shape
 * of the parsed model, and the parser's error reporting.
 */
public class MDCParserModelGeneratorTest {

    private TopItemList parse(String mdc) throws MDCSyntaxError {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private Hieroglyph hieroglyphAt(HBox hBox, int i) {
        return (Hieroglyph) hBox.getHorizontalListElementAt(i);
    }

    @Test
    public void testEmptyString() throws MDCSyntaxError {
        TopItemList topItemList = parse("");
        assertEquals(0, topItemList.getNumberOfChildren());
    }

    @Test
    public void testJuxtaposition_threeTopItems() throws MDCSyntaxError {
        TopItemList topItemList = parse("i-w-r");
        assertEquals(3, topItemList.getNumberOfChildren());

        String[] expectedCodes = {"i", "w", "r"};
        for (int i = 0; i < 3; i++) {
            Cadrat cadrat = (Cadrat) topItemList.getTopItemAt(i);
            assertEquals(1, cadrat.getNumberOfHBoxes());
            HBox hBox = cadrat.getHBox(0);
            assertEquals(1, hBox.getNumberOfChildren());
            assertEquals(expectedCodes[i], hieroglyphAt(hBox, 0).getCode());
        }
    }

    @Test
    public void testSuperposition_sameHBox() throws MDCSyntaxError {
        TopItemList topItemList = parse("p*t");
        assertEquals(1, topItemList.getNumberOfChildren());

        Cadrat cadrat = (Cadrat) topItemList.getTopItemAt(0);
        assertEquals(1, cadrat.getNumberOfHBoxes());
        HBox hBox = cadrat.getHBox(0);
        assertEquals(2, hBox.getNumberOfChildren());
        assertEquals("p", hieroglyphAt(hBox, 0).getCode());
        assertEquals("t", hieroglyphAt(hBox, 1).getCode());
    }

    @Test
    public void testStacking_twoHBoxes() throws MDCSyntaxError {
        TopItemList topItemList = parse("a:m");
        assertEquals(1, topItemList.getNumberOfChildren());

        Cadrat cadrat = (Cadrat) topItemList.getTopItemAt(0);
        assertEquals(2, cadrat.getNumberOfHBoxes());
        assertEquals("a", hieroglyphAt(cadrat.getHBox(0), 0).getCode());
        assertEquals("m", hieroglyphAt(cadrat.getHBox(1), 0).getCode());
    }

    /**
     * Raw typed codes are kept as-is by the parser: "i-w-r" does not become
     * the canonicalized Gardiner codes ("M17", "G43", "D21"). Canonicalization
     * happens later, e.g. via ManuelDeCodage / MdcUnicodeTable.
     */
    @Test
    public void testCodesAreNotCanonicalized() throws MDCSyntaxError {
        TopItemList topItemList = parse("i");
        Cadrat cadrat = (Cadrat) topItemList.getTopItemAt(0);
        assertEquals("i", hieroglyphAt(cadrat.getHBox(0), 0).getCode());
    }

    /**
     * "-" is purely a cosmetic, optional separator between top items (cadrats);
     * it never merges anything. ":" only fuses the preceding and following
     * elements into the same cadrat when it directly touches them (no "-" in
     * between). So in "i-w-r:a-C1-m-pt:p*t", only "r:a" and "pt:p*t" (colon
     * directly adjacent, no dash) end up sharing a cadrat; everything else is
     * its own top item. This asymmetry is exactly the kind of thing worth
     * pinning down with a test.
     */
    @Test
    public void testCombinedStructure() throws MDCSyntaxError {
        // Original MDC : i-w-r:a-C1-m-pt:p*t
        TopItemList topItemList = parse("i-w-r:a-C1-m-pt:p*t");
        assertEquals(6, topItemList.getNumberOfChildren());

        assertEquals("i", hieroglyphAt(((Cadrat) topItemList.getTopItemAt(0)).getHBox(0), 0).getCode());
        assertEquals("w", hieroglyphAt(((Cadrat) topItemList.getTopItemAt(1)).getHBox(0), 0).getCode());

        Cadrat item2 = (Cadrat) topItemList.getTopItemAt(2);
        assertEquals(2, item2.getNumberOfHBoxes());
        assertEquals("r", hieroglyphAt(item2.getHBox(0), 0).getCode());
        assertEquals("a", hieroglyphAt(item2.getHBox(1), 0).getCode());

        assertEquals("C1", hieroglyphAt(((Cadrat) topItemList.getTopItemAt(3)).getHBox(0), 0).getCode());
        assertEquals("m", hieroglyphAt(((Cadrat) topItemList.getTopItemAt(4)).getHBox(0), 0).getCode());

        Cadrat item5 = (Cadrat) topItemList.getTopItemAt(5);
        assertEquals(2, item5.getNumberOfHBoxes());
        assertEquals("pt", hieroglyphAt(item5.getHBox(0), 0).getCode());
        HBox lastHBox = item5.getHBox(1);
        assertEquals(2, lastHBox.getNumberOfChildren());
        assertEquals("p", hieroglyphAt(lastHBox, 0).getCode());
        assertEquals("t", hieroglyphAt(lastHBox, 1).getCode());
    }

    @Test
    public void testModifiers() throws MDCSyntaxError {
        TopItemList topItemList = parse("m\\det\\col50");
        Cadrat cadrat = (Cadrat) topItemList.getTopItemAt(0);
        Hieroglyph hieroglyph = hieroglyphAt(cadrat.getHBox(0), 0);
        assertEquals("m", hieroglyph.getCode());
        assertTrue(hieroglyph.getModifiers().getBoolean("det"));
        assertEquals(50, hieroglyph.getModifiers().getInteger("col"));
    }

    @Test
    public void testCartouche() throws MDCSyntaxError {
        TopItemList topItemList = parse("<A1>");
        assertEquals(1, topItemList.getNumberOfChildren());

        Cadrat cadrat = (Cadrat) topItemList.getTopItemAt(0);
        assertEquals(1, cadrat.getNumberOfHBoxes());
        HBox hBox = cadrat.getHBox(0);
        assertEquals(1, hBox.getNumberOfChildren());

        HorizontalListElement element = hBox.getHorizontalListElementAt(0);
        assertTrue(element instanceof Cartouche);
        Cartouche cartouche = (Cartouche) element;
        assertEquals(CartouchePart.FIRST, cartouche.getStartPart());
        assertEquals(CartouchePart.SECOND, cartouche.getEndPart());
        assertEquals(1, cartouche.getBasicItemList().getNumberOfChildren());
    }

    @Test
    public void testPartialCartouches() throws MDCSyntaxError {
        // Reused from TestPartialCartoucheDrawingsWithOrientation, a known-good MDC string.
        TopItemList topItemList = parse("<1--0>-ra-mn:n-xpr-<0--2>");
        assertTrue(topItemList.getNumberOfChildren() > 0);
    }

    @Test
    public void testSyntaxError_unclosedCartouche() {
        assertThrows(MDCSyntaxError.class, () -> parse("<A1"));
    }

    @Test
    public void testSyntaxError_leadingColon() {
        assertThrows(MDCSyntaxError.class, () -> parse(":a"));
    }

    @Test
    public void testAlphabeticText_oneElementPerCharacter() throws MDCSyntaxError {
        TopItemList topItemList = parse("+lab+s-A1");
        assertEquals(3, topItemList.getNumberOfChildren());
        AlphabeticCharacter a = (AlphabeticCharacter) topItemList.getTopItemAt(0);
        assertEquals('a', a.getCodePoint());
        assertEquals(ScriptCode.LATIN, a.getScript());
        assertEquals('b', ((AlphabeticCharacter) topItemList.getTopItemAt(1)).getCodePoint());
        assertTrue(topItemList.getTopItemAt(2) instanceof Cadrat);
    }

    @Test
    public void testAlphabeticText_uppercaseTransliteration() throws MDCSyntaxError {
        TopItemList topItemList = parse("+t^xpr+s");
        assertEquals(3, topItemList.getNumberOfChildren());
        assertTrue(((AlphabeticCharacter) topItemList.getTopItemAt(0)).isUppercase());
    }

    @Test
    public void testComment() throws MDCSyntaxError {
        TopItemList topItemList = parse("A1-++some note+s-A2");
        assertEquals(3, topItemList.getNumberOfChildren());
        MdcComment comment = (MdcComment) topItemList.getTopItemAt(1);
        assertEquals("some note", comment.getText());
    }

    @Test
    public void testUnknownScriptCode() throws MDCSyntaxError {
        TopItemList topItemList = parse("+fxy+s");
        AlphabeticCharacter x = (AlphabeticCharacter) topItemList.getTopItemAt(0);
        assertEquals(ScriptCode.OTHER, x.getScript());
        assertEquals('f', x.getMdcScriptCode());
    }

    @Test
    public void testAlphabeticText_stateOnEachCharacter() throws MDCSyntaxError {
        TopItemList topItemList = parse("$r-+lab+s-$b");
        assertEquals(2, topItemList.getNumberOfChildren());
        assertTrue(topItemList.getTopItemAt(0).isRed());
        assertTrue(topItemList.getTopItemAt(1).isRed());
    }

    @Test
    public void testAlphabeticText_inCartouche() throws MDCSyntaxError {
        TopItemList topItemList = parse("<-+lab+s-ra->");
        Cartouche[] found = new Cartouche[1];
        topItemList.accept(new ModelElementDeepAdapter() {
            @Override
            public void visitCartouche(Cartouche c) {
                found[0] = c;
            }
        });
        assertEquals(3, found[0].getBasicItemList().getNumberOfChildren());
        assertTrue(found[0].getBasicItemList().getChildAt(0) instanceof AlphabeticCharacter);
        assertTrue(found[0].getBasicItemList().getChildAt(2) instanceof Cadrat);
    }
}
