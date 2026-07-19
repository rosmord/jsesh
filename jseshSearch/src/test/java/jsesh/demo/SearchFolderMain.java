package jsesh.demo;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jsesh.defaults.HieroglyphResources;
import jsesh.defaults.HieroglyphResourcesBuilder;
import jsesh.render.style.JSeshStyle;
import jsesh.ui.editor.JSeshStyleReference;
import jsesh.search.clientApi.CorpusSearchHit;
import jsesh.search.ui.JSearchFolderPanel;
import jsesh.search.ui.SearchPanelFactory;
import jsesh.search.ui.SearchUIResources;

/**
 * Demo class for JSearchFolderPanel.
 *
 * @author rosmord
 */
public class SearchFolderMain {

    private JFrame frame;
    private JSearchFolderPanel searchFolderPanel;
    

    public SearchFolderMain() {

        frame = new JFrame();
        JSeshStyleReference styleRef = new JSeshStyleReference(JSeshStyle.DEFAULT);
        HieroglyphResources hieroglyphResources = HieroglyphResourcesBuilder.buildEmbedded();
        
        SearchUIResources resouces = new SearchUIResources(styleRef, hieroglyphResources);
		searchFolderPanel = SearchPanelFactory.createSearchFolderPanel(this::showCorpusSearchHit, resouces);
        frame.add(searchFolderPanel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void showCorpusSearchHit(CorpusSearchHit hit) {
        JOptionPane.showMessageDialog(frame, "Opening "+hit);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SearchFolderMain());
    }
}
