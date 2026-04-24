/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.ui;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import jsesh.defaults.JseshFontKit;
import jsesh.defaults.SimpleFontKit;
import jsesh.editor.JMDCField;
import jsesh.editor.JSeshStyleReference;
import jsesh.editor.MdCSearchQuery;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.mdc.model.TopItemList;
import jsesh.resources.JSeshMessages;
import jsesh.search.quadrant.QuadratSearchQuery;
import jsesh.search.ui.specifications.JMdCSearchEmbeddableFormFieldsIF;
import jsesh.search.ui.specifications.JMdCSearchFormModelIF;
import jsesh.search.wildcard.VariantLevelForSearch;
import jsesh.search.wildcard.WildCardConstants;
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
@SuppressWarnings("serial")
class JMdCSearchEmbeddableForm extends JPanel implements JMdCSearchEmbeddableFormFieldsIF, JMdCSearchFormModelIF {


    /*
     * Fields for MdC search
     */
    private final JMDCField searchField;
    private final JCheckBox matchLayoutCheckBox;
    private final JButton addSkipButton;
    private final JButton addSetButton;
    private final JSpinner matchLengthSpinner;
    private final JLabel matchLengthSpinnerLabel;
    private final JComboBox<VariantLevelForSearch> variantLevel;
   
    /**
     * Buttons which are meaningless for whole quadrat search...
     */
    private final JComponent signOriented[];

    /**
     * Some query need fine access to information about sign variants.
     */
    private final HieroglyphDatabaseInterface hieroglyphDatabase;

    /**
     * Create a panel for search form.
     * @param target
     */
    JMdCSearchEmbeddableForm(JSeshStyleReference styleReference, JseshFontKit fontKit) {               
        JseshFontKit newFontKit = new SimpleFontKit(WildcardFont.getInstance().addToFont(fontKit.hieroglyphShapeRepository()),
             fontKit.possibilityRepository(), fontKit.hieroglyphDatabase());
        this.hieroglyphDatabase = newFontKit.hieroglyphDatabase();

        // MdC Search
        this.searchField = new JMDCField(100, styleReference,newFontKit);

        
        
        this.addSkipButton = new JButton("*");
        this.addSetButton = new JButton("[...]");
        this.matchLengthSpinner = new JSpinner();
        this.matchLengthSpinnerLabel = new JLabel(JSeshMessages.getString("jsesh.search.maxLength.text"));

        this.matchLayoutCheckBox = new JCheckBox(JSeshMessages.getString("jsesh.search.matchLayout.text"));

        this.addSkipButton.setToolTipText(JSeshMessages.getString("jsesh.search.skip.tooltip"));
        this.addSetButton.setToolTipText(JSeshMessages.getString("jsesh.search.set.tooltip"));
        this.matchLengthSpinner.setToolTipText(JSeshMessages.getString("jsesh.search.maxLength.tooltip"));
        
        this.variantLevel = new JComboBox<>(VariantLevelForSearch.values());

        this.signOriented = new JComponent[]{
            addSetButton, addSkipButton, matchLengthSpinner, matchLengthSpinnerLabel, variantLevel};
        SpinnerModel spinnerModel = new SpinnerNumberModel(0, 0, 1000, 1);
        matchLengthSpinner.setModel(spinnerModel);
       
        prepareLayout();
        enableControls();
    }

    private void enableControls() {
        addSetButton.addActionListener(e -> addSet());
        addSkipButton.addActionListener(e -> addSkip());
        this.matchLayoutCheckBox.addActionListener(e -> wholeQuadratSelect());
    }

    private void prepareLayout() {
        
        this.setLayout(new MigLayout("fillx, insets 0"));
        this.add(searchField, "span, grow, wrap 10");
        this.add(matchLayoutCheckBox, "span 2");
        this.add(variantLevel, "wrap 10");
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
            result = new QuadratSearchQuery(getSearchFieldContent());
        } else {
            result = new WildCardQuery(getSearchFieldContent(), 
                    (Integer) matchLengthSpinner.getValue(),
                    hieroglyphDatabase,
                    (VariantLevelForSearch)variantLevel.getSelectedItem()
            );
        }
        return result;
    }



    private void wholeQuadratSelect() {
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
        return (Integer) this.matchLengthSpinner.getValue();
    }

    @Override
    public void setMatchLayout(boolean matchLayout) {
        this.matchLayoutCheckBox.setSelected(matchLayout);
    }

    @Override
    public boolean isMatchLayout() {
        return this.matchLayoutCheckBox.isSelected();
    }
    

    public void addSet() {        
    	getSearchField().insert(WildCardConstants.QUERY_SET_BEGIN);
    	getSearchField().insert(WildCardConstants.QUERY_SET_END);
    	getSearchField().getWorkflow().cursorPrevious();
    	getSearchField().requestFocusInWindow();
    }

    public void addSkip() {
    	getSearchField().insert(WildCardConstants.QUERY_SKIP);
    	getSearchField().requestFocusInWindow();
    }

}
