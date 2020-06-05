
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import jsesh.editor.MdCSearchQuery;
import jsesh.search.ui.specifications.JMdCSearchFormModelIF;
import jsesh.search.ui.specifications.SearchType;
import jsesh.search.ui.specifications.JSearchFormModelIF;
import jsesh.search.ui.specifications.JSelectableSearchIF;
import jsesh.search.ui.specifications.JTextSearchFormModelIF;

/**
 * Full search panel, including all possible searches, and allowing navigation
 * among them.
 *
 * @author rosmord
 */
class JSearchEmbeddableForm extends JPanel implements JSearchFormModelIF, JSelectableSearchIF {

    private final JComboBox<SearchType> chooseSearchTypeCB;
    private final JMdCSearchEmbeddableForm mdCSearchForm;
    private final JTextSearchEmbeddableForm textSearchForm;
    private final JPanel mainPanel;
    private final CardLayout cardLayout;

    public JSearchEmbeddableForm() {
        chooseSearchTypeCB = new JComboBox(SearchType.values());
        mdCSearchForm = new JMdCSearchEmbeddableForm();
        textSearchForm = new JTextSearchEmbeddableForm();
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        prepareLayout();
        activateComponents();
    }
    
    
    private void activateComponents() {
        chooseSearchTypeCB.addActionListener(e -> displayPanel());
        displayPanel();
    }
    
    
    private void displayPanel() {
        SearchType selected = getSearchType();
        cardLayout.show(mainPanel, selected.name());
    }
    
    private void prepareLayout() {
        setLayout(new BorderLayout());
        add(chooseSearchTypeCB, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        mainPanel.setLayout(cardLayout);
        mainPanel.add(mdCSearchForm, SearchType.MDC_SEARCH.name());
        mainPanel.add(textSearchForm, SearchType.SIMPLE_TEXT_SEARCH.name());
    }
    
    
    @Override
    public MdCSearchQuery getQuery() {
        switch (getSearchType()) {
            case MDC_SEARCH:
                return mdCSearchForm.getQuery();                
            case SIMPLE_TEXT_SEARCH:
                return textSearchForm.getQuery();
            default:
                throw new RuntimeException("no search type ???");
        }
    }

    @Override
    public SearchType getSearchType() {
        return (SearchType) chooseSearchTypeCB.getSelectedItem(); 
    }

    @Override
    public void setSearchType(SearchType searchType) {
        chooseSearchTypeCB.setSelectedItem(searchType);
    }

    @Override
    public JMdCSearchFormModelIF getMdcSearchForm() {
        return mdCSearchForm;
    }

    @Override
    public JTextSearchFormModelIF getTextSearchForm() {
        return textSearchForm;
    }

    
}
