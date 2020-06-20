package jsesh.utilitysoftwares.signinfoeditor.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Controls a table where data can be added and removed. The table model should
 * implement GrowableModel.
 * 
 * @author rosmord
 */

public class GrowableTableControl {
	JTable table;
	JTextField field;
	JButton removeButton;

	/**
	 * Create a growable control and bind it to the corresponding components.
	 * 
	 * @param table
	 *            a table, whose model <em>must</em> implement GrowableModel.
	 * @see GrowableModel
	 */
	public GrowableTableControl(JTable table, JButton addButton,
			JButton removeButton, JTextField field) {
		super();
		this.table = table;
		/*
		 * Stop editing when focus is lost.
		 * See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5007652
		 * (This is definitly a bug IMHO)
		 *
		 * An alternative is to add the following code in the remove
		 * method:
		 * 
		 *	TableCellEditor cellEditor = table.getCellEditor();
		 *	if (cellEditor != null)
		 *		cellEditor.stopCellEditing();
		 *
		¨*/
		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		this.field = field;
		this.removeButton = removeButton;
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				add();
				clearText();
			}
		});

		field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				add();
				clearText();
			}
		});

		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				remove();
			}
		});

		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						setButtonState();
					}
				});
		table.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				if ("model".equals(evt.getPropertyName())) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							setButtonState();
						}
					});
				}
			}
		});
	}

	/**
	 * Update the "minus" button state.
	 */
	protected void setButtonState() {
		int sel = table.getSelectedRow();
		if (sel != -1) {
			GrowableModel model = (GrowableModel) table.getModel();
			removeButton.setEnabled(model.canRemove(sel));
		} else
			removeButton.setEnabled(false);
	}

	public void clearText() {
		field.setText("");
	}

	public void add() {
		String code = field.getText();
		GrowableModel model = (GrowableModel) table.getModel();
		model.addRow(code);
	}

	public void remove() {
		int sel = table.getSelectedRow();
		if (sel != -1) {
			/*
			TableCellEditor cellEditor = table.getCellEditor();
			if (cellEditor != null)
				cellEditor.stopCellEditing();
				*/
			GrowableModel model = (GrowableModel) table.getModel();
			model.removeRow(sel);
		}
	}
}