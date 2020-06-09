/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
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
import jsesh.search.clientApi.SearchTarget;
import jsesh.search.ui.SearchPanelFactory;
import net.miginfocom.swing.MigLayout;
import jsesh.search.ui.specifications.JSearchFormModelIF;

/**
 * Interactive demo of Wildcard search.
 *
 * @author rosmord
 */
public class WildCardMain {

    JFrame frame = new JFrame("Test Wildcard");
    JWildcardPanel panel = SearchPanelFactory.createWildCardPanelForEmbedding(new LocalSearchTarget());
    JMDCEditor editor = new JMDCEditor();
    List<MDCPosition> answers;
    
    String mdc = "D36-W11-G1-D58-D46-!\n" +
            "W11-G1-Q3:O49-R11-D46-!\n" +
    "D21-L1-N35:D58-N35:D58-D40-G1-D21-D46-!\n" +
    "G1";
    
    public WildCardMain() {
        mettreEnPage();
        editor.setMDCText(mdc);    
        // Separated interfaces.
        JSearchFormModelIF formModel = panel;        
        // note : give a bit more programmatic control on the interface ?
        //formModel.setMdcQuery("A-QUERYSKIP-d"); 
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WildCardMain());
    }

    private void mettreEnPage() {
        frame.setLayout(new MigLayout(""));
        frame.add(new JScrollPane(editor), "push, grow, wrap");
        frame.add(panel, "pushx, growx, wrap");        
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
