/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.ui;

import jsesh.search.clientApi.SearchTarget;
import javax.swing.JPanel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jsesh.editor.JMDCField;
import jsesh.editor.MdCSearchQuery;
import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.graphics.HieroglyphicFontManager;
import jsesh.hieroglyphs.graphics.ResourcesHieroglyphicFontManager;
import jsesh.resources.JSeshMessages;
import net.miginfocom.swing.MigLayout;
import jsesh.search.ui.specifications.JSearchFormModelIF;

/**
 * UI for wildcard search functions.
 *
 *
 * TODO code duplication here with JMdCSearchEmbeddableForm. Remove it. 
 *  
 * @author rosmord
 */
@SuppressWarnings("serial")
public final class JWildcardPanel extends JPanel implements JSearchFormModelIF {


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
    
    public JWildcardPanel(SearchTarget target, HieroglyphicFontManager fontManager) {
        this(target, true, fontManager);
    }

    /**
     * Creates a new JWildcardPanel.
     *
     * @param target
     * @param hasInsets : choose if we want an inner margin. Sets to false if we
     * embed the panel in a larger one.
     */
    public JWildcardPanel(SearchTarget target, boolean hasInsets, HieroglyphicFontManager fontManager) {
        this.hasInsets = hasInsets;
        this.searchTarget = target;
        this.embeddableForm = new JSearchEmbeddableForm(fontManager);
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



    @Override
    public MdCSearchQuery getQuery() {
        return embeddableForm.getQuery();
    }
    

}
