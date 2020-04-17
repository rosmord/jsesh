/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.search.ui;

import jsesh.search.ui.specifications.JSearchEmbeddableFormFieldsIF;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import jsesh.editor.JMDCField;
import jsesh.editor.MdCSearchQuery;
import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.ResourcesHieroglyphicFontManager;
import jsesh.mdc.model.TopItemList;
import jsesh.resources.JSeshMessages;
import jsesh.search.quadrant.QuadrantSearchQuery;
import jsesh.search.ui.specifications.JSearchFormModelIF;
import jsesh.search.wildcard.WildCardQuery;
import net.miginfocom.swing.MigLayout;

/**
 * Search fields used by dialogs for JSesh searches. Used both for in-document
 * searches and out-of-document searches.
 *
 * <p>
 * To ease the use of this widget, we publish two interfaces : one gives access
 * to its fields, and the other to its underlying model.
 *
 * @author rosmord
 */
class JSearchEmbeddableForm extends JPanel implements JSearchEmbeddableFormFieldsIF, JSearchFormModelIF {

    private static final String FONT_PATH = "/jsesh/search/wildcard";

    /**
     * Fields...
     */
    private final JMDCField searchField;
    private final JCheckBox matchLayoutCheckBox;
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
    JSearchEmbeddableForm() {
        setupFont();
        this.searchField = new JMDCField();
        this.addSkipButton = new JButton("*");
        this.addSetButton = new JButton("[...]");
        this.matchLengthSpinner = new JSpinner();
        this.matchLengthSpinnerLabel = new JLabel("Max. match Length");

        this.matchLayoutCheckBox = new JCheckBox("Whole Quadrants Match");

        this.addSkipButton.setToolTipText(JSeshMessages.getString("jsesh.search.skip.tooltip"));
        this.addSetButton.setToolTipText(JSeshMessages.getString("jsesh.search.set.tooltip"));

        this.signOriented = new JComponent[]{addSetButton, addSkipButton, matchLengthSpinner, matchLengthSpinnerLabel};
        SpinnerModel spinnerModel = new SpinnerNumberModel(0, 0, 1000, 1);
        matchLengthSpinner.setModel(spinnerModel);
        prepareLayout();
        enableControls();
    }

    private void enableControls() {
        addSetButton.addActionListener(e -> addSet());
        addSkipButton.addActionListener(e -> addSkip());
        this.matchLayoutCheckBox.addActionListener(e -> wholeQuadrantSelect());
    }

    private void prepareLayout() {
        this.setLayout(new MigLayout("fillx, insets 0"));
        this.add(searchField, "span, grow, wrap 10");
        this.add(matchLayoutCheckBox, "span 2, wrap 10");
        this.add(addSkipButton, "sg bt");
        this.add(addSetButton, "sg bt");
        this.add(matchLengthSpinnerLabel);
        this.add(matchLengthSpinner, "wrap 10");
    }

    @Override
    public JMDCField getSearchField() {
        return searchField;
    }

    private TopItemList getSearchFieldContent() {
        return searchField.getHieroglyphicTextModel().getModel();
    }

    @Override
    public MdCSearchQuery getQuery() {
        MdCSearchQuery result;
        if (matchLayoutCheckBox.isSelected()) {
            result = new QuadrantSearchQuery(getSearchFieldContent());
        } else {
            result = new WildCardQuery(getSearchFieldContent(), (Integer) matchLengthSpinner.getValue());
        }
        return result;
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
            b.setEnabled(!this.matchLayoutCheckBox.isSelected());
        }
    }

    @Override
    public JCheckBox getMatchLayoutCheckBox() {
        return matchLayoutCheckBox;
    }

    @Override
    public JButton getAddSkipButton() {
        return addSkipButton;
    }

    @Override
    public JButton getAddSetButton() {
        return addSetButton;
    }

    @Override
    public JSpinner getMatchLengthSpinner() {
        return matchLengthSpinner;
    }

    @Override
    public void setMdcQuery(String mdcQuery) {
        this.searchField.setMDCText(mdcQuery);
    }

    @Override
    public String getMdcQueryAsText() {
        return this.searchField.getMDCText();
    }

    @Override
    public void setMaxMatchLength(int max) {
        this.matchLengthSpinner.setValue(max);
    }

    @Override
    public int getMaxMatchLength() {        
        return (Integer)this.matchLengthSpinner.getValue();
    }

    @Override
    public void setMatchLayout(boolean matchLayout) {
        this.matchLayoutCheckBox.setSelected(matchLayout);
    }

    @Override
    public boolean isMatchLayout() {
        return this.matchLayoutCheckBox.isSelected();
    }

}
