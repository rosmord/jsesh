package jsesh.search.ui;

import jsesh.search.clientApi.SearchTarget;
import javax.swing.JPanel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jsesh.editor.JMDCField;
import jsesh.editor.MdCSearchQuery;
import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.ResourcesHieroglyphicFontManager;
import jsesh.resources.JSeshMessages;
import jsesh.search.ui.specifications.JSearchFormModelIF;
import net.miginfocom.swing.MigLayout;

/**
 * UI for wildcard search functions.
 *
 * @author rosmord
 */
public final class JWildcardPanel extends JPanel implements JSearchFormModelIF {

    private static final String FONT_PATH = "/jsesh/search/wildcard";

    /**
     * Links to owner application.
     */
    private final SearchTarget searchTarget;

    /**
     * Fields...
     */
    private final JSearchEmbeddableForm embeddableForm;
    private final JButton searchButton;
    private final JButton nextButton;

    private final boolean hasInsets;
    
    public JWildcardPanel(SearchTarget target) {
        this(target, true);
    }

    /**
     * Creates a new JWildcardPanel.
     *
     * @param target
     * @param hasInsets : choose if we want an inner margin. Sets to false if we
     * embed the panel in a larger one.
     */
    JWildcardPanel(SearchTarget target, boolean hasInsets) {
        this.hasInsets = hasInsets;
        setupFont();
        this.searchTarget = target;
        this.embeddableForm = new JSearchEmbeddableForm();
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
        String migLayoutSpec = hasInsets?"fill":"fill, insets 0";
        this.setLayout(new MigLayout(migLayoutSpec));
        this.add(embeddableForm, "span, grow, wrap 15");

        this.add(searchButton, "sg bt, tag apply");
        this.add(nextButton, "sg bt, tag next");
    }

    private void doSearch() {
        if (searchTarget.isAvailable()) {
            MdCSearchQuery query = embeddableForm.getQuery();
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

    public JMDCField getSearchField() {
        return embeddableForm.getSearchField();
    }

    @Override
    public MdCSearchQuery getQuery() {
        return embeddableForm.getQuery();
    }

    @Override
    public void setMdcQuery(String mdcQuery) {
        embeddableForm.setMdcQuery(mdcQuery);
    }

    @Override
    public String getMdcQueryAsText() {
        return embeddableForm.getMdcQueryAsText();
    }

    @Override
    public void setMaxMatchLength(int max) {
        embeddableForm.setMaxMatchLength(max);
    }

    @Override
    public int getMaxMatchLength() {
        return embeddableForm.getMaxMatchLength();
    }

    @Override
    public void setMatchLayout(boolean matchLayout) {
        embeddableForm.setMatchLayout(matchLayout);
    }

    @Override
    public boolean isMatchLayout() {
        return embeddableForm.isMatchLayout();
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
