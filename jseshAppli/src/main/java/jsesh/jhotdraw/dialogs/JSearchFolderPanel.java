/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2016)
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est un programme informatique servant à mettre en place
 * une base de données linguistique pour l'égyptien ancien.

 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence [CeCILL|CeCILL-B|CeCILL-C] telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".

 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.

 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.

 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package jsesh.jhotdraw.dialogs;

import java.io.File;
import javax.swing.*;

import jsesh.editor.JMDCField;
import jsesh.jhotdraw.Messages;
import jsesh.search.MdCSearchQuery;
import net.miginfocom.swing.MigLayout;

/**
 * "Search Folder" Panel for JSesh.
 *
 * @author rosmord
 */
public final class JSearchFolderPanel extends JFrame {

    JFormattedTextField folderField;
    JButton chooseFolderButton;
    JMDCField mDCField;
    JCheckBox searchGlyphsCheckBox;
    JButton searchButton;
    JButton cancelButton;
    JLabel messageField;

    JTable resultTable;

    public JSearchFolderPanel() {
        this.mDCField = new JMDCField();
        this.folderField= new JFormattedTextField(new File("."));
        this.folderField.setEditable(false);
        this.chooseFolderButton= new JButton(Messages.getString("browse"));
        this.searchGlyphsCheckBox = new JCheckBox(Messages.getString("searchFolder.searchGlyphsCheckBox.text"));
        this.searchGlyphsCheckBox.setSelected(true);
        this.searchButton = new JButton(Messages.getString("search"));
        this.cancelButton = new JButton(Messages.getString("cancel"));
        
        this.resultTable= new JTable();
        this.messageField= new JLabel("0");
        
        this.setLayout(new MigLayout("fill",
                "[right]rel[grow,fill]", "[]10[]"));
        this.add(new JLabel(Messages.getString("folder")));
        this.add(folderField, "span, split 2, growx 1, pushx");
        this.add(chooseFolderButton, "grow 0, sizegroup bttn, wrap");
        this.add(new JLabel(Messages.getString("query")));
        this.add(mDCField, "growx, wrap");
        this.add(searchGlyphsCheckBox, "span 2,wrap");
        this.add(new JScrollPane(resultTable), "span, growx, growy, push, wrap");
        this.add(new JLabel(Messages.getString("searchFolder.state")));
        this.add(messageField, "span, growx 1, pushx");
        this.add(searchButton, "tag ok, span, split 2, sizegroup bttn");
        this.add(cancelButton, "tag cancel, sizegroup bttn");
        this.pack();
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

    public JMDCField getmDCField() {
        return mDCField;
    }

    public JCheckBox getSearchGlyphsCheckBox() {
        return searchGlyphsCheckBox;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JTable getResultTable() {
        return resultTable;
    }

    public MdCSearchQuery getQuery() {
        return null;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public JLabel getMessageField() {
        return messageField;
    }

    
    public static void main(String[] args) {
        JSearchFolderPanel p= new JSearchFolderPanel();
        p.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        p.setVisible(true);
    }
}
