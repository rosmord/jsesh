package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/// Wrapping of the lines to the width of the editor (a view-level option).
class JMDCEditorWrapTest {

    private static final String LONG_LINE = "A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1";

    @Test
    void narrowEditorWrapsItsLines() {
        JMDCEditor editor = new JMDCEditor();
        editor.setMDCText(LONG_LINE);
        double height = editor.getView().getHeight();
        double width = editor.getView().getWidth();

        editor.setWrapToComponentWidth(true);
        editor.setBounds(0, 0, (int) (editor.getScale() * width / 3), 100);
        assertTrue(editor.getView().getHeight() > 2 * height, "wrapped");
        assertTrue(editor.getView().getWidth() <= width / 3 + 1);
        assertTrue(editor.getScrollableTracksViewportWidth());

        // Widening the editor gives the original layout back.
        editor.setBounds(0, 0, (int) (editor.getScale() * width * 2), 100);
        assertEquals(height, editor.getView().getHeight(), 1e-6);
    }

    @Test
    void wrappingIsOffByDefaultAndDoesNotChangeTheDocumentStyle() {
        JMDCEditor editor = new JMDCEditor();
        editor.setMDCText(LONG_LINE);
        double height = editor.getView().getHeight();
        assertFalse(editor.isWrapToComponentWidth());
        editor.setBounds(0, 0, 50, 100);
        assertEquals(height, editor.getView().getHeight(), 1e-6);

        editor.setWrapToComponentWidth(true);
        editor.setBounds(0, 0, 51, 100);
        assertFalse(editor.getJSeshStyle().geometry().isWrapping());
        assertFalse(editor.getRenderContext().jseshStyle().geometry().isWrapping());

        editor.setWrapToComponentWidth(false);
        assertEquals(height, editor.getView().getHeight(), 1e-6);
    }

    @Test
    void smallTextFillsTheViewportSoThatTheBackgroundIsTheEditorsOne() {
        JMDCEditor editor = new JMDCEditor();
        editor.setMDCText("A1");
        javax.swing.JScrollPane pane = new javax.swing.JScrollPane(editor);
        pane.setSize(800, 600);
        pane.doLayout();
        javax.swing.JViewport viewport = pane.getViewport();
        viewport.setSize(800, 600);
        assertTrue(editor.getScrollableTracksViewportWidth());
        assertTrue(editor.getScrollableTracksViewportHeight());
        // Large text: scrolls as usual.
        editor.setMDCText(String.join("-", java.util.Collections.nCopies(10, LONG_LINE)));
        viewport.setSize(100, 10);
        assertFalse(editor.getScrollableTracksViewportWidth());
        assertFalse(editor.getScrollableTracksViewportHeight());
    }
}
