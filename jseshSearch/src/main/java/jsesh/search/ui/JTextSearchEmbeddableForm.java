
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jsesh.editor.MdCSearchQuery;
import jsesh.resources.JSeshMessages;
import jsesh.search.plainText.PlainTextSearchQuery;
import jsesh.search.ui.specifications.JTextSearchFormModelIF;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author rosmord
 */
class JTextSearchEmbeddableForm extends JPanel implements JTextSearchFormModelIF {
    
    private JTextField searchField;

    public JTextSearchEmbeddableForm() {
        searchField = new JTextField(10);
        prepareLayout();
        enableControls();
    }
    
    private void prepareLayout() {        
        this.setLayout(new MigLayout("insets 0", "[][grow]"));
        this.add(new JLabel(JSeshMessages.getString("jsesh.search.text.label")), "");
        this.add(searchField, "span, grow, wrap 10");        
    }

    
    
    @Override
    public String getQueryAsText() {
        return searchField.getText();
    }

    @Override
    public void setQuery(String text) {
        searchField.setText(text);
    }

    @Override
    public MdCSearchQuery getQuery() {
        return new PlainTextSearchQuery(getQueryAsText());
    }

    private void enableControls() {
        // DO NOTHING FOR NOW ?
    }
 
}
