/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.ui;

import java.io.File;
import javax.swing.*;

import jsesh.resources.JSeshMessages;
import net.miginfocom.swing.MigLayout;

/**
 * "Search Folder" Panel for JSesh.
 *
 * @author rosmord
 */
public final class JSearchFolderPanel extends JPanel {

    private JFormattedTextField folderField;
    private JButton chooseFolderButton;
    private JSearchEmbeddableForm searchForm;
  
    private JButton searchButton;
    private JButton cancelButton;
    private JLabel messageField;
    private JTable resultTable;

    JSearchFolderPanel() {
        createFields();
        layoutFields();
    }

    private void createFields() {
        this.folderField = new JFormattedTextField(new File("."));
        this.folderField.setEditable(false);
        this.searchForm = new JSearchEmbeddableForm();
        this.chooseFolderButton = new JButton(JSeshMessages.getString("generic.browse.text"));
        this.searchButton = new JButton(JSeshMessages.getString("generic.search.text"));
        this.cancelButton = new JButton(JSeshMessages.getString("generic.cancel.text"));
        this.resultTable = new JTable();
        this.messageField = new JLabel("0");
    }

    private void layoutFields() {
        this.setLayout(new MigLayout("fill",
                "[right]rel[grow,fill]", "[]10[]"));
        this.add(new JLabel(JSeshMessages.getString("generic.folder.text")));
        this.add(folderField, "span, split 2, growx 1, pushx");
        this.add(chooseFolderButton, "grow 0, sizegroup bttn, wrap");
        this.add(new JLabel(JSeshMessages.getString("generic.query.text")));
        this.add(searchForm, "growx, wrap");        
        this.add(new JScrollPane(resultTable), "span, growx, growy, push, wrap");
        this.add(new JLabel(JSeshMessages.getString("jsesh.search.folder.state")));
        this.add(messageField, "span, growx 1, pushx");
        this.add(searchButton, "tag ok, span, split 2, sizegroup bttn");
        this.add(cancelButton, "tag cancel, sizegroup bttn");
    }

    public File getFolder() {
        return (File) folderField.getValue();
    }

    public void setFolder(File folder) {
        folderField.setValue(folder);
    }

    public JFormattedTextField getFolderField() {
        return folderField;
    }

    public JButton getChooseFolderButton() {
        return chooseFolderButton;
    }


    public JButton getSearchButton() {
        return searchButton;
    }

    public JTable getResultTable() {
        return resultTable;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public JLabel getMessageField() {
        return messageField;
    }

    public JSearchEmbeddableForm getSearchForm() {
        return searchForm;
    }
    
    
}
