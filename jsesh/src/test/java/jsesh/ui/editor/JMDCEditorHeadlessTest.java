package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.Test;

/// Checks that [JMDCEditor] can be created without a display.
class JMDCEditorHeadlessTest {

    @Test
    void editorCanBeCreatedInHeadlessMode() {
        // Gradle sets java.awt.headless; when run from an IDE, skip rather than fail.
        assumeTrue(GraphicsEnvironment.isHeadless(), "this test only makes sense in headless mode");
        JMDCEditor editor = new JMDCEditor();
        assertNotNull(editor);
    }
}
