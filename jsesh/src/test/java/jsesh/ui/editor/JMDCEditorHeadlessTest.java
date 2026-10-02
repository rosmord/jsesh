package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.Test;

/// Checks that [JMDCEditor] can be created without a display.
class JMDCEditorHeadlessTest {

    @Test
    void editorCanBeCreatedInHeadlessMode() {
        assertTrue(GraphicsEnvironment.isHeadless(), "this test must run in headless mode");
        JMDCEditor editor = new JMDCEditor();
        assertNotNull(editor);
    }
}
