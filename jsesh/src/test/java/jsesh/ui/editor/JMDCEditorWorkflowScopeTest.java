package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.model.CaretGranularity;
import jsesh.model.MDCPosition;
import jsesh.model.TextScope;

/// The editor works inside cartouches as it works at the top level of the
/// text: their content is an editing scope.
class JMDCEditorWorkflowScopeTest {

    /// The content of the cartouche, when it is the first item of the text.
    private static final TextScope CARTOUCHE = TextScope.of(0, 0, 0, 0);

    private static JMDCEditorWorkflow workflow(String mdc) throws Exception {
        HieroglyphicTextModel model = new HieroglyphicTextModel();
        model.setMDCCode(mdc);
        return new JMDCEditorWorkflow(model, code -> null);
    }

    private static MDCPosition p(int... path) {
        return MDCPosition.of(path);
    }

    private static MDCPosition inCartouche(int gap) {
        return CARTOUCHE.positionAt(gap);
    }

    private static void type(JMDCEditorWorkflow w, String keys) {
        for (char c : keys.toCharArray()) {
            w.keyTyped(c);
        }
    }

    @Test
    void arrowsEnterCartouchesWithQuadratGranularity() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1-B1->-C1");
        assertEquals(CaretGranularity.QUADRAT, w.getCaretGranularity());
        w.setCursor(p(0));
        w.cursorNext();
        assertEquals(inCartouche(0), w.getCaret().getInsertExactPosition());
        w.cursorNext();
        w.cursorNext();
        w.cursorNext();
        assertEquals(p(1), w.getCaret().getInsertExactPosition());
        w.cursorPrevious();
        assertEquals(inCartouche(2), w.getCaret().getInsertExactPosition());
    }

    @Test
    void typedSignsAreGroupedInACartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1->");
        w.setCursor(inCartouche(1));
        type(w, "B1*C1 D1:E1 ");
        assertEquals("<-A1-B1*C1-D1:E1->", w.getMDCCode());
        assertEquals(inCartouche(3), w.getCaret().getInsertExactPosition());
    }

    @Test
    void ligaturesAreTypedInACartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1->");
        w.setCursor(inCartouche(1));
        type(w, "B1&C1 ");
        assertEquals("<-A1-B1&C1->", w.getMDCCode());
    }

    @Test
    void undoKeepsTheCursorInTheCartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1-B1->");
        w.setCursor(inCartouche(1));
        type(w, "C1 ");
        assertEquals("<-A1-C1-B1->", w.getMDCCode());
        w.undo();
        assertEquals("<-A1-B1->", w.getMDCCode());
        assertEquals(inCartouche(1), w.getCaret().getInsertExactPosition());
        w.redo();
        assertEquals("<-A1-C1-B1->", w.getMDCCode());
        assertEquals(inCartouche(2), w.getCaret().getInsertExactPosition());
    }

    @Test
    void backspaceInACartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("X1-<-A1-B1->");
        // At the start of the cartouche, there is nothing to erase in it.
        w.setCursor(p(1, 0, 0, 0, 0));
        type(w, "\b");
        assertEquals("X1-<-A1-B1->", w.getMDCCode());
        w.setCursor(p(1, 0, 0, 0, 2));
        type(w, "\b");
        assertEquals("X1-<-A1->", w.getMDCCode());
        assertEquals(p(1, 0, 0, 0, 1), w.getCaret().getInsertExactPosition());
    }

    @Test
    void groupingASelectionInACartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1-B1-C1->");
        w.setCursor(inCartouche(0));
        w.setMark(inCartouche(2));
        assertTrue(w.getCaret().hasSelection());
        assertEquals(CARTOUCHE, w.getCaret().getScope());
        assertTrue(w.groupVertically());
        assertEquals("<-A1:B1-C1->", w.getMDCCode());
    }

    @Test
    void aSelectionLeavingTheCartoucheCoversIt() throws Exception {
        JMDCEditorWorkflow w = workflow("X1-<-A1-B1->-Y1");
        w.setCursor(p(1, 0, 0, 0, 1));
        w.setMark(p(2));
        assertEquals(TextScope.ROOT, w.getCaret().getScope());
        assertEquals(1, w.getSelectionAsHieroglyphicText().getNumberOfChildren());
        w.removeSelectedText();
        assertEquals("X1-Y1", w.getMDCCode());
    }

    @Test
    void lineBreaksAreRefusedInACartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1-B1->");
        w.setCursor(inCartouche(1));
        w.insertNewLine();
        assertEquals("<-A1-B1->", w.getMDCCode());
    }

    @Test
    void mdcIsInsertedInACartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1-B1->");
        w.setCursor(inCartouche(1));
        w.insertMDC("C1-D1");
        assertEquals("<-A1-C1-D1-B1->", w.getMDCCode());
    }

    @Test
    void signModificationsInACartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1-B1->-C1");
        w.setCursor(inCartouche(1));
        w.reverseSign();
        String code = w.getMDCCode();
        assertTrue(code.startsWith("<-A1\\"), code);
        assertFalse(code.contains("C1\\"), code);
    }

    @Test
    void quadratGranularitySnapsToTheGapOfTheCartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1*B1->");
        w.setCaretGranularity(CaretGranularity.INNER);
        w.setCursor(p(0, 0, 0, 0, 0, 0, 1));
        assertTrue(w.getCaret().isInsertInner());
        w.setCaretGranularity(CaretGranularity.QUADRAT);
        assertEquals(inCartouche(0), w.getCaret().getInsertExactPosition());
        assertFalse(w.getCaret().isInsertInner());
    }

    @Test
    void quadratGranularityLeavesSubQuadrats() throws Exception {
        JMDCEditorWorkflow w = workflow("n:x*(t:tA)-B1");
        jsesh.model.PositionNavigator nav = new jsesh.model.PositionNavigator(w.getCaret().getModel());
        java.util.List<MDCPosition> inner = nav.stops(0, CaretGranularity.INNER);
        assertFalse(inner.isEmpty());
        for (MDCPosition stop : inner) {
            w.setCaretGranularity(CaretGranularity.INNER);
            w.setCursor(stop);
            w.setCaretGranularity(CaretGranularity.QUADRAT);
            assertTrue(w.getCaret().getInsertExactPosition().isTop(), stop + " -> " + w.getCaret().getInsertExactPosition());
        }
    }

    @Test
    void topLevelEditingIsUnchanged() throws Exception {
        JMDCEditorWorkflow w = workflow("A1");
        w.setCursor(p(1));
        type(w, "B1*C1 ");
        assertEquals("A1-B1*C1", w.getMDCCode());
        type(w, "\b");
        assertEquals("A1", w.getMDCCode());
    }
}
