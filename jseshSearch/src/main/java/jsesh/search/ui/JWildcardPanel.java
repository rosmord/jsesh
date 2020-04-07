package jsesh.search.ui;

import jsesh.search.clientApi.SearchTarget;
import javax.swing.JPanel;

import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import jsesh.editor.JMDCField;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.utils.MDCCodeExtractor;
import jsesh.editor.MdCSearchQuery;
import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.ResourcesHieroglyphicFontManager;
import jsesh.mdc.model.TopItemList;
import jsesh.resources.JSeshMessages;
import jsesh.search.quadrant.QuadrantSearchQuery;
import jsesh.search.wildcard.WildCardQuery;
import net.miginfocom.swing.MigLayout;

/**
 * UI for wildcard search functions.
 *
 * @author rosmord
 */
public final class JWildcardPanel extends JPanel {

    private static final String FONT_PATH = "/jsesh/search/wildcard";

    /**
     * Links to owner application.
     */
    private final SearchTarget searchTarget;

    /**
     * Fields...
     */
    private final JSearchSearchFields jseshFields;
    private final JButton searchButton;
    private final JButton nextButton;
  

    /**
     * Buttons which are meaningless for whole quadrant search...
     */

    /**
     *
     * @param target
     */
    JWildcardPanel(SearchTarget target) {
        setupFont();
        this.searchTarget = target;
        this.jseshFields = new JSearchSearchFields();
        this.searchButton = new JButton(JSeshMessages.getString("jsesh.search.search.text"));
        this.nextButton = new JButton(JSeshMessages.getString("jsesh.search.findNext.text"));        
        prepareLayout();
        enableControls();
    }

    private void enableControls() {
        searchButton.addActionListener(e -> doSearch());
        nextButton.addActionListener(e -> nextSearch());
    }

    private void prepareLayout() {
        this.setLayout(new MigLayout());
        this.add(jseshFields, "span, grow, wrap 15");

        this.add(searchButton, "sg bt, tag apply");
        this.add(nextButton, "sg bt, tag next");
    }

  

    private void doSearch() {
        if (searchTarget.isAvailable()) {
            MdCSearchQuery query = jseshFields.getQuery();
            searchTarget.doSearch(query);
        }
    }

    private void nextSearch() {
        if (searchTarget.isAvailable()) {
            searchTarget.nextSearch();
        }
    }

    /**
     * Add the additional signs for wildcards (*, [ and ]).
     */
    private void setupFont() {
        DefaultHieroglyphicFontManager manager = DefaultHieroglyphicFontManager.getInstance();
        if (manager.get("QUERYSKIP") == null) {
            manager.addHieroglyphicFontManager(new ResourcesHieroglyphicFontManager(FONT_PATH));
        }
    }


    /**
     * Just to test the display of this object...
     *
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            JWildcardPanel panel = new JWildcardPanel(new SearchTarget() {
                @Override
                public boolean isAvailable() {
                    return true;
                }

                @Override
                public void doSearch(MdCSearchQuery query) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void nextSearch() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
            frame.add(panel);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

}
