package jsesh.ui.glossary;

import java.awt.BorderLayout;

import javax.swing.JFrame;

/**
 * A frame displaying a glossary editor.
 * JGlossaryEditorFrame
 */
public class JGlossaryEditorFrame extends JFrame {
    JGlossaryEditor glossaryEditor;

    public JGlossaryEditorFrame(JGlossaryEditor jGlossaryEditor) {
        this.glossaryEditor = jGlossaryEditor;
        setLayout(new BorderLayout());
        add(glossaryEditor, BorderLayout.CENTER);
        pack();
    }

    public JGlossaryEditor getGlossaryEditor() {
        return glossaryEditor;
    }

    public void prepareToAdd(String mdc) {
        glossaryEditor.prepareToAdd(mdc);
    }

    
}
