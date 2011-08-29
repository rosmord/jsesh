/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.varia;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;

/**
 * Demonstration of a problem in the MIG layout.
 * 
 * The MigLayout doesn't deal correctly with the JEditorPane, when displayed
 * with JOptionPane.
 * The problem occurs with most UI LAF (except Quaqua).
 * Tested on Ubuntu and Mac, both with java 6.
 * @author rosmord
 */
public class MigProblemDemoi {

    JPanel panel;

    public MigProblemDemoi() {
        panel = new JPanel();
        panel.setLayout(new MigLayout());
        panel.add(new JEditorPane("text/html", "hello <b>word</b><p>Demo"), "wrap");
        panel.add(new JTextField("hello"));
    }

    // This works
    public void displayInJFrame() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(panel);
        f.pack();
        f.setVisible(true);
    }

    public void displayInDialog() {
        JDialog f = new JDialog();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.add(panel);
        f.pack();
        f.setVisible(true);
    }

    // This fails
    public void displayWithJOptionPanel() {
        MigProblemDemoi demo = new MigProblemDemoi();
        JOptionPane.showConfirmDialog(null, demo.panel);
    }

    public static void main(String a[]) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new MigProblemDemoi().displayWithJOptionPanel();
                //new MigProblemDemoi().displayInDialog();;
            }
        });
    }
}
