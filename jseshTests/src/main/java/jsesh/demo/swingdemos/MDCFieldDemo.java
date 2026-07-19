package jsesh.demo.swingdemos;

import java.awt.FlowLayout;

import javax.swing.JFrame;

import jsesh.ui.editor.JMDCField;

/**
 * Simple demonstration of the MDC editor.
 */
public class MDCFieldDemo extends JFrame {

    JMDCField editor;

    public MDCFieldDemo() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        editor = new JMDCField();
        add(editor);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new MDCFieldDemo();
        });
    }
}
