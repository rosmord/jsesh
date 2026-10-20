package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.model.CaretGranularity;
import jsesh.model.MDCPosition;

/// Editing with [JMDCEditorWorkflow] when the cursor stands inside a quadrat.
class JMDCEditorWorkflowInnerEditTest {

    private static JMDCEditorWorkflow workflow(String mdc) throws Exception {
        HieroglyphicTextModel model = new HieroglyphicTextModel();
        model.setMDCCode(mdc);
        JMDCEditorWorkflow w = new JMDCEditorWorkflow(model, code -> null);
        w.setCaretGranularity(CaretGranularity.INNER);
        return w;
    }

    private static MDCPosition p(int... path) {
        return MDCPosition.of(path);
    }

    private static void type(JMDCEditorWorkflow w, String keys) {
        for (char c : keys.toCharArray()) {
            w.keyTyped(c);
        }
    }

    @Test
    void typingInsertsInsideTheRow() throws Exception {
        JMDCEditorWorkflow w = workflow("A1*B1*D1");
        w.setCursor(p(0, 0, 1));
        type(w, "C1 ");
        assertEquals("A1*C1*B1*D1", w.getMDCCode());
        assertEquals(p(0, 0, 2), w.getCaret().getInsertExactPosition());
    }

    @Test
    void successiveInsertionsStayInsideTheQuadrat() throws Exception {
        JMDCEditorWorkflow w = workflow("A1*B1*D1");
        w.setCursor(p(0, 0, 1));
        type(w, "C1 E1 ");
        assertEquals("A1*C1*E1*B1*D1", w.getMDCCode());
        assertEquals(p(0, 0, 3), w.getCaret().getInsertExactPosition());
    }

    @Test
    void typingBetweenRowsAndInALigature() throws Exception {
        JMDCEditorWorkflow w = workflow("A1:B1-C1&D1");
        w.setCursor(p(0, 1));
        type(w, "E1 ");
        assertEquals("A1:E1:B1-C1&D1", w.getMDCCode());
        w.setCursor(p(1, 0, 0, 1));
        type(w, "F1 ");
        assertEquals("A1:E1:B1-C1&F1&D1", w.getMDCCode());
    }

    @Test
    void separatorsAreNotNeededAtAnInnerPosition() throws Exception {
        JMDCEditorWorkflow w = workflow("A1*B1");
        w.setCursor(p(0, 0, 1));
        type(w, "C1:");
        assertEquals("A1*C1*B1", w.getMDCCode());
    }

    @Test
    void undoRestoresTheText() throws Exception {
        JMDCEditorWorkflow w = workflow("A1*B1*D1");
        w.setCursor(p(0, 0, 1));
        type(w, "C1 ");
        w.undo();
        assertEquals("A1*B1*D1", w.getMDCCode());
        w.redo();
        assertEquals("A1*C1*B1*D1", w.getMDCCode());
    }

    @Test
    void backspaceRemovesTheElementBeforeTheCursor() throws Exception {
        JMDCEditorWorkflow w = workflow("X1-A1*B1*C1-Y1");
        w.setCursor(p(1, 0, 2));
        type(w, "\b");
        assertEquals("X1-A1*C1-Y1", w.getMDCCode());
        assertEquals(p(1, 0, 1), w.getCaret().getInsertExactPosition());
    }

    @Test
    void backspaceKeepsTheCursorInTheRowWhenOneSignIsLeft() throws Exception {
        JMDCEditorWorkflow w = workflow("X1-A1*B1-Y1");
        w.setCursor(p(1, 0, 1));
        type(w, "\b");
        assertEquals("X1-B1-Y1", w.getMDCCode());
        // The start of the row is a position of its own, distinct from the gap before the quadrat.
        assertEquals(p(1, 0, 0), w.getCaret().getInsertExactPosition());
        assertTrue(w.getCaret().isInsertInner());
    }

    @Test
    void typingAtTheEndOfARowAddsToTheRow() throws Exception {
        JMDCEditorWorkflow w = workflow("X1-A1*B1-Y1");
        w.setCursor(p(1, 0, 2));
        type(w, "C1 ");
        assertEquals("X1-A1*B1*C1-Y1", w.getMDCCode());
        assertEquals(p(1, 0, 3), w.getCaret().getInsertExactPosition());
        type(w, "D1 ");
        assertEquals("X1-A1*B1*C1*D1-Y1", w.getMDCCode());
    }

    @Test
    void typingAtTheStartOfARowAddsToTheRow() throws Exception {
        JMDCEditorWorkflow w = workflow("X1-A1*B1-Y1");
        w.setCursor(p(1, 0, 0));
        type(w, "C1 ");
        assertEquals("X1-C1*A1*B1-Y1", w.getMDCCode());
        assertEquals(p(1, 0, 1), w.getCaret().getInsertExactPosition());
    }

    @Test
    void typingAtTheBottomOfAQuadratAddsARow() throws Exception {
        JMDCEditorWorkflow w = workflow("X1-A1*B1-Y1");
        w.setCursor(p(1, 1));
        type(w, "C1 ");
        assertEquals("X1-A1*B1:C1-Y1", w.getMDCCode());
        assertEquals(p(1, 2), w.getCaret().getInsertExactPosition());
    }

    @Test
    void typingOutsideTheQuadratAddsAQuadrat() throws Exception {
        JMDCEditorWorkflow w = workflow("X1-A1*B1-Y1");
        w.setCursor(p(2));
        type(w, "C1 ");
        assertEquals("X1-A1*B1-C1-Y1", w.getMDCCode());
    }

    @Test
    void backspaceAndDeleteDoNothingBeyondTheEdges() throws Exception {
        JMDCEditorWorkflow w = workflow("X1-A1*B1-Y1");
        w.setCursor(p(1, 0, 0));
        type(w, "\b");
        assertEquals("X1-A1*B1-Y1", w.getMDCCode());
        assertEquals(p(1, 0, 0), w.getCaret().getInsertExactPosition());
        w.setCursor(p(1, 0, 2));
        w.doDelete();
        assertEquals("X1-A1*B1-Y1", w.getMDCCode());
        assertEquals(p(1, 0, 2), w.getCaret().getInsertExactPosition());
    }

    @Test
    void backspaceKeepsTheCursorInsideWhenPossible() throws Exception {
        JMDCEditorWorkflow w = workflow("A1*B1*C1*D1");
        w.setCursor(p(0, 0, 2));
        type(w, "\b");
        assertEquals("A1*C1*D1", w.getMDCCode());
        assertEquals(p(0, 0, 1), w.getCaret().getInsertExactPosition());
    }

    @Test
    void deleteRemovesTheElementAfterTheCursor() throws Exception {
        JMDCEditorWorkflow w = workflow("A1*B1*C1*D1");
        w.setCursor(p(0, 0, 2));
        w.doDelete();
        assertEquals("A1*B1*D1", w.getMDCCode());
        assertEquals(p(0, 0, 2), w.getCaret().getInsertExactPosition());
        w.undo();
        assertEquals("A1*B1*C1*D1", w.getMDCCode());
    }

    @Test
    void deleteDoesNothingAtTopLevel() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-B1");
        w.setCursor(p(1));
        w.doDelete();
        assertEquals("A1-B1", w.getMDCCode());
    }

    @Test
    void editingInACartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1-B1->");
        w.setCursor(p(0, 0, 0, 0, 1));
        type(w, "C1 ");
        assertEquals("<-A1-C1-B1->", w.getMDCCode());
        type(w, "\b");
        assertEquals("<-A1-B1->", w.getMDCCode());
        assertEquals(p(0, 0, 0, 0, 1), w.getCaret().getInsertExactPosition());
    }
}
