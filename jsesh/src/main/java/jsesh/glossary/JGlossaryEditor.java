/*
 Copyright Serge Rosmorduc
 contributor(s) : Serge J. P. Thomas for the fonts
 serge.rosmorduc@qenherkhopeshef.org

 This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

 This software is governed by the CeCILL license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source codeField and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.glossary;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import jsesh.editor.JMDCField;
import jsesh.i18n.I18n;
import jsesh.swing.renderers.MdCTableCellRenderer;

/**
 * Graphical glossary editor for JSesh.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
@SuppressWarnings("serial")
public class JGlossaryEditor extends JPanel {

	/**
	 * Optional JFrame, if we want to create a separate editor.
	 */
	private JFrame frame;
	
	private JTextField codeField;
	private JMDCField mdcField;
	private JTable table;
	private JButton okButton;
	private GlossaryTableModel model;
	private ButtonUpdater buttonUpdater = new ButtonUpdater();

	public JGlossaryEditor() {
		model = new GlossaryTableModel();
		codeField = new JTextField(10);
		mdcField = new JMDCField();
		table = new JTable(model);
	    table.setTableHeader(null);

		// table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		fixTable();

		okButton = new JButton(I18n.getString("JGlossaryEditor.add.label"));
		okButton.addActionListener(EventHandler.create(ActionListener.class,
				this, "addEntry"));
		mdcField.getHieroglyphicTextModel().addObserver(
				EventHandler.create(Observer.class, buttonUpdater, "run"));
		codeField.addActionListener(EventHandler.create(ActionListener.class,
				buttonUpdater, "run"));
		prepareLayout();
	}

	private void fixTable() {
		table.getColumnModel().getColumn(1)
				.setCellRenderer(new MdCTableCellRenderer());
		JRemoveButtonCell removeButton = new JRemoveButtonCell(model);
		table.getColumnModel().getColumn(2).setCellRenderer(removeButton);
		table.getColumnModel().getColumn(2).setCellEditor(removeButton);

		table.getColumnModel().getColumn(2)
				.setMaxWidth(8 + (int) JRemoveButtonCell.getMaxWidth());
		table.setRowHeight(34);
	}

	public void addEntry() {
		String code = codeField.getText().replaceAll("[^a-zA-Z0-9]", "");
		String mdc = mdcField.getMDCText();
		if ("".equals(mdc) || "".equals(code)) return;
		model.addEntry(code, mdc);
		codeField.setText("");
		mdcField.clearText();
		fixTable(); // adding corresponds in fact to table reset in the present system.
	}

	private void prepareLayout() {
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(layout);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.2;
		add(codeField, c);

		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		add(okButton, c);

		c.gridx = 1;
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(mdcField, c);

		c.weighty = 1;
		c.weightx = 0.5;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		add(new JScrollPane(table), c);
		setFocusCycleRoot(true);
		setFocusTraversalPolicy(new ExplicitFocusPolicy(okButton,
				new Component[] { codeField, okButton, mdcField, table }));
	}

	public void prepareToAdd(String mdc) {
		mdcField.setMDCText(mdc);
		codeField.requestFocusInWindow();		
	}

	private class ButtonUpdater implements Runnable {
		public void run() {
		}
	}
	
	public JFrame getFrame() {
		if (frame== null) {
			frame= new JFrame();
			frame.add(this);
			frame.pack();
		}
		return frame;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
                    JFrame demo = new JFrame();
                    JGlossaryEditor editor = new JGlossaryEditor();
                    demo.add(editor);
                    demo.pack();
                    demo.setVisible(true);
                    demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                });
	}
}
