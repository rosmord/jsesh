/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.search.ui;

import jsesh.search.Messages;
import javax.swing.JPanel;

import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import jsesh.editor.JMDCField;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.utils.MDCCodeExtractor;
import jsesh.editor.MdCSearchQuery;
import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.ResourcesHieroglyphicFontManager;
import jsesh.search.quadrant.QuadrantSearchQuery;
import jsesh.search.simple.SignStringSearchQuery;
import jsesh.search.wildcard.WildCardQuery;
import net.miginfocom.swing.MigLayout;

/**
 * UI for wildcard search functions.
 *
 * @author rosmord
 */
public final class JWildcardPanel extends JPanel {

    private static final String FONT_PATH = "/jsesh/search/wildcard";

    private final SearchTarget searchTarget;

    private final JMDCField searchField;
    private final JButton searchButton;
    private final JButton nextButton;
    private final JButton addSkipButton;
    private final JButton addSetButton;

    public JWildcardPanel(SearchTarget target) {
        setupFont();
        this.searchTarget = target;
        this.searchField = new JMDCField();
        this.searchButton = new JButton(Messages.getString("search"));
        this.nextButton = new JButton(Messages.getString("jsesh.search.findNext.text"));
        this.addSkipButton = new JButton("*");
        this.addSetButton = new JButton("[...]");

        this.setLayout(new MigLayout());
        this.add(searchField, "span 2,wrap");
        this.add(addSkipButton, "");
        this.add(addSetButton, "wrap");
        this.add(searchButton, "");
        this.add(nextButton, "");

        addSetButton.addActionListener(e -> addSet());
        addSkipButton.addActionListener(e -> addSkip());
        searchButton.addActionListener(e -> doSearch());
        nextButton.addActionListener(e -> nextSearch());
    }

    public void startSearch() {
        setVisible(true);
    }

    public JMDCField getSearchField() {
        return searchField;
    }

    private void doSearch() {
        if (searchTarget.isAvailable()) {
            try {
                MdCSearchQuery query;
                List<String> l = new MDCCodeExtractor().getCodesAsList(searchField.getMDCText());
                
                // the string here is a bit tooooo long.
                query = new WildCardQuery(searchField.getHieroglyphicTextModel().getModel());
                searchTarget.doSearch(query);
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

}
