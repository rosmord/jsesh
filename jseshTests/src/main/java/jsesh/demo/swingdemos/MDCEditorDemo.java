package jsesh.demo.swingdemos;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import jsesh.editor.JMDCEditor;

/**
 * Simple demonstration of the MDC editor.
 */
public class MDCEditorDemo extends JFrame {

    JMDCEditor editor;

    public MDCEditorDemo() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        editor = new JMDCEditor();
        add(new JScrollPane(editor), BorderLayout.CENTER);
        setSize(800, 800);
        setVisible(true);        
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new MDCEditorDemo();
        });
    }
}
