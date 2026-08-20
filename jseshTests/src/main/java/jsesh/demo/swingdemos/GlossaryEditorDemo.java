package jsesh.demo.swingdemos;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jsesh.defaults.HieroglyphResources;
import jsesh.defaults.HieroglyphResourcesBuilder;
import jsesh.glossary.Glossary;
import jsesh.glossary.GlossaryManager;
import jsesh.render.style.JSeshStyle;
import jsesh.ui.editor.JSeshStyleReference;
import jsesh.ui.glossary.JGlossaryEditor;
import jsesh.ui.glossary.JGlossaryEditorFrame;

public class GlossaryEditorDemo {
    JGlossaryEditor jGlossaryEditor;

    public GlossaryEditorDemo() {
        JSeshStyleReference styleRef = new JSeshStyleReference(
            JSeshStyle.DEFAULT.copy()
                .geometry(g -> g.scaleToHeight(40.0))
            .build()
        );
        
        GlossaryManager glossaryManager = new GlossaryManager();
        glossaryManager.read();
        Glossary glossary = glossaryManager.getGlossary();
        HieroglyphResources resources = HieroglyphResourcesBuilder.buildFullWithExplicitGlossary(glossary);
        jGlossaryEditor = new JGlossaryEditor(glossaryManager, styleRef, resources);
        JGlossaryEditorFrame frame = new JGlossaryEditorFrame(jGlossaryEditor);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GlossaryEditorDemo());
    }
}
