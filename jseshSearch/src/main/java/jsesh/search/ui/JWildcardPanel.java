package jsesh.search.ui;

import jsesh.search.Messages;
import javax.swing.JPanel;

import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
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
    private final JMDCField searchField;
    private final JCheckBox wholeQuadrantsCheckBox;
    private final JButton searchButton;
    private final JButton nextButton;
    private final JButton addSkipButton;
    private final JButton addSetButton;
    private final JSpinner matchLengthSpinner;
    private final JLabel matchLengthSpinnerLabel;

    /**
     * Buttons which are meaningless for whole quadrant search...
     */
    private final JComponent signOriented[];

    /**
     *
     * @param target
     */
    public JWildcardPanel(SearchTarget target) {
        setupFont();
        this.searchTarget = target;
        this.searchField = new JMDCField();
        this.searchButton = new JButton(Messages.getString("jsesh.search.search.text"));
        this.nextButton = new JButton(Messages.getString("jsesh.search.findNext.text"));
        this.addSkipButton = new JButton("*");
        this.addSetButton = new JButton("[...]");
        this.matchLengthSpinner = new JSpinner();
        this.matchLengthSpinnerLabel = new JLabel("Max. match Length");

        this.wholeQuadrantsCheckBox = new JCheckBox("Whole Quadrants Match");

        this.addSkipButton.setToolTipText(Messages.getString("jsesh.search.skip.tooltip"));
        this.addSetButton.setToolTipText(Messages.getString("jsesh.search.set.tooltip"));

        this.signOriented = new JComponent[]{addSetButton, addSkipButton, matchLengthSpinner, matchLengthSpinnerLabel};
        SpinnerModel spinnerModel = new SpinnerNumberModel(0, 0, 1000, 1);
        matchLengthSpinner.setModel(spinnerModel);
        prepareLayout();
        enableControls();
    }

    private void enableControls() {
        addSetButton.addActionListener(e -> addSet());
        addSkipButton.addActionListener(e -> addSkip());
        searchButton.addActionListener(e -> doSearch());
        nextButton.addActionListener(e -> nextSearch());
        this.wholeQuadrantsCheckBox.addActionListener(e -> wholeQuadrantSelect());
    }

    private void prepareLayout() {
        this.setLayout(new MigLayout());
        this.add(searchField, "span, grow, wrap 10");
        this.add(wholeQuadrantsCheckBox, "span 2, wrap 10");
        this.add(addSkipButton, "sg bt");
        this.add(addSetButton, "sg bt");
        this.add(matchLengthSpinnerLabel);
        this.add(matchLengthSpinner, "wrap 10");

        this.add(searchButton, "sg bt, tag apply");
        this.add(nextButton, "sg bt, tag next");
    }

    public JMDCField getSearchField() {
        return searchField;
    }

    private TopItemList getSearchFieldContent() {
        return searchField.getHieroglyphicTextModel().getModel();
    }

    private void doSearch() {
        if (searchTarget.isAvailable()) {
            try {
                if (wholeQuadrantsCheckBox.isSelected()) {
                    QuadrantSearchQuery query = new QuadrantSearchQuery(getSearchFieldContent());
                    searchTarget.doSearch(query);
                } else {
                    WildCardQuery query;
                    List<String> l = new MDCCodeExtractor().getCodesAsList(searchField.getMDCText());
                    query = new WildCardQuery(getSearchFieldContent(), (Integer) matchLengthSpinner.getValue());
                    searchTarget.doSearch(query);
                }
            } catch (MDCSyntaxError e) {
                throw new RuntimeException(e);
            }
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

    private void addSet() {
        searchField.insert("QUERYSETB");
        searchField.insert("QUERYSETE");
        searchField.getWorkflow().cursorPrevious();
        searchField.requestFocusInWindow();
    }

    private void addSkip() {
        searchField.insert("QUERYSKIP");
        searchField.requestFocusInWindow();
    }

    private void wholeQuadrantSelect() {
        for (JComponent b : this.signOriented) {
            b.setEnabled(!this.wholeQuadrantsCheckBox.isSelected());
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
