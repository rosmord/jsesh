package jsesh.glossaryEditor;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import jsesh.editor.JMDCField;

@SuppressWarnings("serial")
class JGlossaryPanel extends JPanel {
	
	private JTextField keyField= new JTextField();
	private JMDCField mdcField= new JMDCField();
	private JTable table= new JTable();
	private JButton deleteButton= new JButton();
	private JButton addButton= new JButton();

	JGlossaryPanel() {
		// WRITE Auto-generated constructor stub
	}
	
	public void prepareLayout() {
		
	}
	
	public JTable getTable() {
		return table;
	}
	
	public JButton getDeleteButton() {
		return deleteButton;
	}

	/**
	 * @return the keyField
	 */
	public JTextField getKeyField() {
		return keyField;
	}

	/**
	 * @return the mdcField
	 */
	public JMDCField getMdcField() {
		return mdcField;
	}

	/**
	 * @return the addButton
	 */
	public JButton getAddButton() {
		return addButton;
	}
	

}
