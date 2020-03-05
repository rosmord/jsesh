/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.demo;

import java.util.List;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import jsesh.editor.JMDCEditor;
import jsesh.editor.MdCSearchQuery;
import jsesh.mdc.model.MDCPosition;
import jsesh.search.ui.JWildcardPanel;
import jsesh.search.ui.SearchTarget;
import net.miginfocom.swing.MigLayout;

/**
 * Interactive demo of Wildcard search.
 *
 * @author rosmord
 */
public class WildCardMain {

    JFrame frame = new JFrame("Test Wildcard");
    JWildcardPanel panel = new JWildcardPanel(new LocalSearchTarget());
    JMDCEditor editor = new JMDCEditor();
    List<MDCPosition> answers;
    
    String mdc = "D36-W11-G1-D58-D46-!\n" +
            "W11-G1-Q3:O49-R11-D46-!\n" +
    "D21-L1-N35:D58-N35:D58-D40-G1-D21-D46-!\n" +
    "G1";
    
    public WildCardMain() {
        mettreEnPage();
        editor.setMDCText(mdc);
        panel.getSearchField().setMDCText("A-QUERYSKIP-d");
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WildCardMain());
    }

    private void mettreEnPage() {
        frame.setLayout(new MigLayout("", "[grow 1]", "[grow 1]10[grow 0]"));
        frame.add(new JScrollPane(editor), "wrap, grow");
        frame.add(panel, "wrap");        
    }

    private  class LocalSearchTarget implements SearchTarget {

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public void doSearch(MdCSearchQuery query) {
            answers = editor.doSearch(query);
            nextSearch();
        }

        @Override
        public void nextSearch() {
            if (answers != null && ! answers.isEmpty()) {
                MDCPosition pos = answers.get(0);
                editor.getWorkflow().setCursor(pos);
                answers = answers.subList(1, answers.size());
            }
        }

    }
}
