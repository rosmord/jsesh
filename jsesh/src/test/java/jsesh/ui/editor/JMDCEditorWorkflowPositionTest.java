package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.document.caret.MDCCaret;
import jsesh.io.mdc.MdCModelWriter;
import jsesh.model.MDCPosition;
import jsesh.model.constants.SymbolCodes;

/// Cursor and selection handling of [JMDCEditorWorkflow].
///
/// The expected values were recorded on the workflow as it was before the
/// switch from `int` to [MDCPosition] in the caret and text-model API, so
/// that these tests check that the switch changed nothing.
class JMDCEditorWorkflowPositionTest {

    /// Positions: `0 |A1| 1 |A2| 2 |!| 3 |B1| 4 |B2| 5 |!| 6 |C1| 7`
    private static final String LINES = "A1-A2-!B1-B2-!C1";

    private static MDCPosition pos(int i) {
        return new MDCPosition(i);
    }

    private static JMDCEditorWorkflow workflow(String mdc) throws Exception {
        HieroglyphicTextModel model = new HieroglyphicTextModel();
        model.setMDCCode(mdc);
        return new JMDCEditorWorkflow(model, code -> null);
    }

    private static void assertSelection(JMDCEditorWorkflow w, int insert, int mark) {
        MDCCaret caret = w.getCaret();
        assertEquals(pos(insert), caret.getInsertPosition(), "insert");
        assertEquals(pos(mark), caret.getMarkPosition(), "mark");
    }

    @Test
    void selectAll() throws Exception {
        JMDCEditorWorkflow w = workflow(LINES);
        w.setCursor(pos(4));
        w.selectAll();
        assertSelection(w, 0, 7);
        assertEquals(w.getMDCCode(),
                new MdCModelWriter().toMdC(w.getSelectionAsHieroglyphicText()));
    }

    @Test
    void selectCurrentLine() throws Exception {
        int[][] expected = {{0, 2}, {0, 2}, {0, 2}, {3, 5}, {3, 5}, {3, 5}, {6, 7}, {6, 7}};
        for (int i = 0; i < expected.length; i++) {
            JMDCEditorWorkflow w = workflow(LINES);
            w.setCursor(pos(i));
            w.selectCurrentLine();
            assertSelection(w, expected[i][0], expected[i][1]);
        }
    }

    @Test
    void selectCurrentPage() throws Exception {
        // 0 |A1| 1 |!!| 2 |B1| 3 |B2| 4 |!!| 5 |C1| 6
        int[][] expected = {{0, 1}, {0, 1}, {2, 4}, {2, 4}, {2, 4}, {5, 6}};
        for (int i = 0; i < expected.length; i++) {
            JMDCEditorWorkflow w = workflow("A1-!!B1-B2-!!C1");
            w.setCursor(pos(i));
            w.selectCurrentPage();
            assertSelection(w, expected[i][0], expected[i][1]);
        }
    }

    @Test
    void currentLineIncludesItsFinalBreak() throws Exception {
        JMDCEditorWorkflow w = workflow(LINES);
        w.setCursor(pos(4));
        assertEquals("B1-B2-!\n", w.getCurrentLineAsString());
        w.setCursor(pos(7));
        assertEquals("C1", w.getCurrentLineAsString());
    }

    @Test
    void setCurrentLineTo() throws Exception {
        JMDCEditorWorkflow w = workflow(LINES);
        w.setCursor(pos(4));
        assertTrue(w.setCurrentLineTo("D1-D2"));
        assertEquals("A1-A2-!\nD1-D2-C1", w.getMDCCode());
        assertEquals(pos(3), w.getCaret().getInsertPosition());
    }

    @Test
    void selectionIsIndependentOfTheMarkSide() throws Exception {
        JMDCEditorWorkflow w = workflow(LINES);
        w.setCursor(pos(5));
        w.getCaret().setMarkPosition(pos(2));
        assertEquals("!\nB1-B2", new MdCModelWriter().toMdC(w.getSelectionAsHieroglyphicText()));
    }

    @Test
    void setMarkToCursorSelectsNothing() throws Exception {
        JMDCEditorWorkflow w = workflow(LINES);
        w.setCursor(pos(4));
        w.setMarkToCursor();
        assertSelection(w, 4, 4);
        assertFalse(w.getCaret().hasSelection());
    }

    @Test
    void expandSelectionMovesTheInsertOnly() throws Exception {
        JMDCEditorWorkflow w = workflow(LINES);
        w.setCursor(pos(2));
        w.expandSelection(1);
        w.expandSelection(1);
        assertSelection(w, 4, 2);
        w.expandSelection(-1);
        w.expandSelection(-1);
        w.expandSelection(-1);
        assertSelection(w, 1, 2);
    }

    @Test
    void wordNavigation() throws Exception {
        // 12 alphabetic characters and one hieroglyph:
        // "ab cd  ef" | A1 | "gh"
        JMDCEditorWorkflow w = workflow("+lab cd  ef+s-A1-+lgh+s");
        int[] next = {2, 5, 9, 10, 12, 12};
        for (int expected : next) {
            w.cursorNextWord();
            assertEquals(pos(expected), w.getCaret().getInsertPosition());
        }
        int[] previous = {10, 9, 7, 3, 0, 0};
        for (int expected : previous) {
            w.cursorPreviousWord();
            assertEquals(pos(expected), w.getCaret().getInsertPosition());
        }
    }

    @Test
    void expandSelectionByWord() throws Exception {
        JMDCEditorWorkflow w = workflow("+lab cd  ef+s-A1-+lgh+s");
        w.setCursor(pos(3));
        w.expandSelectionByWord(1);
        assertSelection(w, 5, 3);
        w.expandSelectionByWord(1);
        assertSelection(w, 9, 3);
        w.expandSelectionByWord(-1);
        assertSelection(w, 7, 3);
    }

    @Test
    void insertMDCAtTheCursor() throws Exception {
        JMDCEditorWorkflow w = workflow(LINES);
        w.setCursor(pos(4));
        w.insertMDC("D1-D2");
        assertEquals("A1-A2-!\nB1-D1-D2-B2-!\nC1", w.getMDCCode());
        assertEquals(pos(6), w.getCaret().getInsertPosition());
    }

    @Test
    void insertLineNumber() throws Exception {
        JMDCEditorWorkflow w = workflow(LINES);
        w.setCursor(pos(3));
        w.insertLineNumber("vo, 3");
        assertEquals("A1-A2-!\n|vo, 3-B1-B2-!\nC1", w.getMDCCode());
        HieroglyphicTextModel model = w.getHieroglyphicTextModel();
        assertEquals("", model.getOriginalDocumentCoordinates(pos(3)));
        assertEquals("vo, 3", model.getOriginalDocumentCoordinates(pos(4)));
    }

    @Test
    void addPhilologicalMarkupAroundTheSelection() throws Exception {
        JMDCEditorWorkflow w = workflow(LINES);
        w.getHieroglyphicTextModel().setPhilologyIsSign(true);
        w.setCursor(pos(4));
        w.getCaret().setMarkPosition(pos(1));
        assertTrue(w.addPhilologicalMarkup(SymbolCodes.BEGINEDITORADDITION / 2));
        assertEquals("A1-[&-A2-!\nB1-&]-B2-!\nC1", w.getMDCCode());
        assertSelection(w, 5, 2);
    }

    @Test
    void paintZoneInRed() throws Exception {
        JMDCEditorWorkflow w = workflow(LINES);
        w.setCursor(pos(1));
        w.getCaret().setMarkPosition(pos(4));
        w.paintZoneInRed();
        assertEquals("A1-$r-A2-$b-!\n$r-B1-$b-B2-!\nC1", w.getMDCCode());
    }
}
