/*
 * JTagEditor.java
 *
 * Created on 27 septembre 2007, 19:46
 */

package jsesh.utilitysoftwares.signinfoeditor.ui.form;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import jsesh.utilitysoftwares.signinfoeditor.ui.utils.IconFactory;
import net.miginfocom.swing.MigLayout;

/**
 * GUI Panel for managing lists of tags.
 * @author  rosmord
 */
public class JTagEditor extends JPanel {
    
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JButton labelAddButton;
    private JTextField labelField;
    private JButton labelRemoveButton;
    private JTable labelTable;
    private JButton tagAddButton;
    private JTextField tagNameField;
    private JList<String> tagNameList;
    private JButton tagRemoveButton;

	/** Creates new form JTagEditor */
    public JTagEditor() {
        initComponents();
    }
    
    private void initComponents() {

        tagNameField = new JTextField();
        jLabel1 = new JLabel();
        jScrollPane1 = new JScrollPane();
        tagNameList = new JList<>();
        jLabel2 = new JLabel();
        jScrollPane2 = new JScrollPane();
        labelTable = new JTable();
        labelAddButton = new JButton();
        labelRemoveButton = new JButton();
        tagAddButton = new JButton();
        tagRemoveButton = new JButton();
        labelField = new JTextField();

        tagNameField.setColumns(10);

        jLabel1.setText("Tag");
        
        jScrollPane1.setViewportView(tagNameList);

        jLabel2.setText("Tag labels");

        jScrollPane2.setViewportView(labelTable);

        labelAddButton.setIcon(IconFactory.getAddIcon());

        labelRemoveButton.setIcon(IconFactory.getRemoveIcon());

        tagAddButton.setIcon(IconFactory.getAddIcon());

        tagRemoveButton.setIcon(IconFactory.getRemoveIcon());

        setLayout(new MigLayout("insets dialog, wrap 6", "[][][][][grow,fill][]", "[][grow]"));
        // Row 1: input row
        add(jLabel1);
        add(tagNameField);
        add(tagAddButton, "gapright 18");
        add(jLabel2);
        add(labelField, "growx");
        add(labelAddButton);
        // Row 2: scrollpane row (grows vertically)
        add(jScrollPane1, "span 2, grow");
        add(tagRemoveButton, "top, gapright 18");
        add(jScrollPane2, "span 2, grow");
        add(labelRemoveButton, "top");
    }
    
	/**
	 * @return the labelAddButton
	 */
	public JButton getLabelAddButton() {
		return labelAddButton;
	}

	/**
	 * @return the labelField
	 */
	public JTextField getLabelField() {
		return labelField;
	}

	/**
	 * @return the labelRemoveButton
	 */
	public JButton getLabelRemoveButton() {
		return labelRemoveButton;
	}

	/**
	 * @return the labelTable
	 */
	public JTable getLabelTable() {
		return labelTable;
	}

	/**
	 * @return the tagAddButton
	 */
	public JButton getTagAddButton() {
		return tagAddButton;
	}

	/**
	 * @return the tagNameField
	 */
	public JTextField getTagNameField() {
		return tagNameField;
	}

	/**
	 * @return the tagNameList
	 */
	public JList<String> getTagNameList() {
		return tagNameList;
	}

	/**
	 * @return the tagRemoveButton
	 */
	public JButton getTagRemoveButton() {
		return tagRemoveButton;
	}
    
}
