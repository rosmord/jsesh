package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.io.mdc.MdCModelWriter;
import jsesh.mdcreader.MDCParserModelGenerator;

/// Tests for [InnerEdit].
class InnerEditTest {

    private static HieroglyphicText parse(String mdc) throws Exception {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private static MDCPosition p(int... path) {
        return MDCPosition.of(path);
    }

    /// Applies a result to the text, as the editor does, and returns the MDC code.
    private static String apply(HieroglyphicText t, MDCPosition pos, InnerEdit.Result r) {
        int top = pos.getPath()[0];
        t.removeTopItems(p(top), p(top + 1));
        r.replacement().ifPresent(item -> t.addAllAt(p(top), List.of(item)));
        return new MdCModelWriter().toMdC(t);
    }

    private static String insert(String mdc, MDCPosition pos, String sign, MDCPosition expectedCaret)
            throws Exception {
        HieroglyphicText t = parse(mdc);
        InnerEdit.Result r = new InnerEdit(t, pos).insertSign(new Hieroglyph(sign)).orElseThrow();
        assertEquals(expectedCaret, r.caret());
        return apply(t, pos, r);
    }

    private static String delete(String mdc, MDCPosition pos, boolean before, MDCPosition expectedCaret)
            throws Exception {
        HieroglyphicText t = parse(mdc);
        InnerEdit edit = new InnerEdit(t, pos);
        InnerEdit.Result r = (before ? edit.deleteBefore() : edit.deleteAfter()).orElseThrow();
        assertEquals(expectedCaret, r.caret());
        return apply(t, pos, r);
    }

    @Test
    void insertInARow() throws Exception {
        assertEquals("A1*C1*B1*D1", insert("A1*B1*D1", p(0, 0, 1), "C1", p(0, 0, 2)));
    }

    @Test
    void insertBetweenRows() throws Exception {
        assertEquals("A1:C1:B1", insert("A1:B1", p(0, 1), "C1", p(0, 2)).replace("-", ""));
    }

    @Test
    void insertInALigature() throws Exception {
        assertEquals("A1&C1&B1", insert("A1&B1", p(0, 0, 0, 1), "C1", p(0, 0, 0, 2)));
    }

    @Test
    void insertInAQuadratOfACartouche() throws Exception {
        assertEquals("<-A1-B1*D1*C1->", insert("<-A1-B1*C1->", p(0, 0, 0, 0, 1, 0, 1), "D1", p(0, 0, 0, 0, 1, 0, 2)));
    }

    @Test
    void insertOnlyChangesTheQuadratWhichHoldsThePosition() throws Exception {
        assertEquals("X1-A1*C1*B1*D1-Y1", insert("X1-A1*B1*D1-Y1", p(1, 0, 1), "C1", p(1, 0, 2)));
    }

    @Test
    void deleteInARow() throws Exception {
        assertEquals("B1*C1", delete("A1*B1*C1", p(0, 0, 1), true, p(0)));
        assertEquals("A1*C1", delete("A1*B1*C1", p(0, 0, 1), false, p(0, 0, 1)));
        assertEquals("A1*B1", delete("A1*B1*C1", p(0, 0, 2), false, p(1)));
    }

    @Test
    void deleteBetweenRows() throws Exception {
        assertEquals("B1", delete("A1:B1", p(0, 1), true, p(0)));
        assertEquals("A1", delete("A1:B1", p(0, 1), false, p(1)));
        assertEquals("A1:C1", delete("A1:B1:C1", p(0, 1), false, p(0, 1)).replace("-", ""));
    }

    @Test
    void ligatureReducedToOneSignBecomesThisSign() throws Exception {
        assertEquals("B1", delete("A1&B1", p(0, 0, 0, 1), true, p(0)));
        assertEquals("A1", delete("A1&B1", p(0, 0, 0, 1), false, p(1)));
        assertEquals("A1&C1", delete("A1&B1&C1", p(0, 0, 0, 1), false, p(0, 0, 0, 1)));
    }

    @Test
    void deleteInAQuadratOfACartouche() throws Exception {
        // The quadrat is left with one sign: the cursor goes back to the gap of the cartouche.
        assertEquals("<-A1-C1->", delete("<-A1-B1*C1->", p(0, 0, 0, 0, 1, 0, 1), true, p(0, 0, 0, 0, 1)));
    }

    @Test
    void gapsOfACartoucheAreNotInnerPositions() throws Exception {
        // They are edited as a scope (jsesh.document.EditingScope).
        HieroglyphicText t = parse("<-A1-B1->");
        assertTrue(new InnerEdit(t, p(0, 0, 0, 0, 1)).insertSign(new Hieroglyph("C1")).isEmpty());
        assertTrue(new InnerEdit(t, p(0, 0, 0, 0, 1)).deleteBefore().isEmpty());
        assertTrue(new InnerEdit(t, p(0, 0, 0, 0, 1)).deleteAfter().isEmpty());
    }

    @Test
    void topLevelAndInvalidPositionsAreRefused() throws Exception {
        HieroglyphicText t = parse("A1*B1");
        assertTrue(new InnerEdit(t, p(1)).insertSign(new Hieroglyph("C1")).isEmpty());
        assertTrue(new InnerEdit(t, p(1)).deleteBefore().isEmpty());
        assertTrue(new InnerEdit(t, p(0, 5, 1)).deleteAfter().isEmpty());
    }

    @Test
    void theTextIsNotModifiedByTheComputation() throws Exception {
        HieroglyphicText t = parse("A1*B1*C1");
        new InnerEdit(t, p(0, 0, 1)).deleteBefore();
        new InnerEdit(t, p(0, 0, 1)).insertSign(new Hieroglyph("D1"));
        assertEquals("A1*B1*C1", new MdCModelWriter().toMdC(t));
    }

    @Test
    void canonicalizeClimbsFromEdges() throws Exception {
        HieroglyphicText t = parse("A1*B1-C1&D1");
        assertEquals(p(0), new PositionNavigator(t).canonicalize(p(0, 0, 0)));
        assertEquals(p(1), new PositionNavigator(t).canonicalize(p(0, 0, 2)));
        assertEquals(p(1), new PositionNavigator(t).canonicalize(p(0, 1)));
        assertEquals(p(0, 0, 1), new PositionNavigator(t).canonicalize(p(0, 0, 1)));
        assertEquals(p(1), new PositionNavigator(t).canonicalize(p(1)));
        assertEquals(p(1, 0, 0, 1), new PositionNavigator(t).canonicalize(p(1, 0, 0, 1)));
        // The edge of a ligature is the gap around the ligature, not a stop of its own.
        assertEquals(p(1), new PositionNavigator(t).canonicalize(p(1, 0, 0, 0)));
    }

    @Test
    void canonicalizeKeepsCartoucheEdges() throws Exception {
        HieroglyphicText t = parse("<-A1-B1->");
        assertEquals(p(0, 0, 0, 0, 0), new PositionNavigator(t).canonicalize(p(0, 0, 0, 0, 0)));
        assertEquals(p(0, 0, 0, 0, 2), new PositionNavigator(t).canonicalize(p(0, 0, 0, 0, 2)));
    }
}
