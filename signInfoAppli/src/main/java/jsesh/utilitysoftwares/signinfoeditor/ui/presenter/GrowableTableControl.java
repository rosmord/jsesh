package jsesh.utilitysoftwares.signinfoeditor.ui.presenter;

import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.qenherkhopeshef.utils.StringUtils;

import jsesh.utilitysoftwares.signinfoeditor.viewmodel.GrowableModel;

/**
 * Controls a table where data can be added and removed. The table model should
 * implement GrowableModel.
 * 
 * @author rosmord
 */

public class GrowableTableControl {

	private GrowableTableControl() {
	}

	private record ControlComponents(JTable table, JButton addButton, JButton removeButton, JTextField field) {
	}

	/**
	 * Bind controls for a table whose content can be extended using String data.
	 * Binds a table, an "add" button, a "remove" button and a text field together.
	 * 
	 * <p>
	 * The string data from the field is used, but might be exploited to create
	 * other data in the model.
	 * 
	 * @param table
	 *              a table, whose model <em>must</em> implement GrowableModel.
	 * @see GrowableModel
	 */
	public static void bind(final JTable table, final JButton addButton,
			final JButton removeButton, final JTextField field) {
		/*
		 * Stop editing when focus is lost.
		 * See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5007652
		 * (This is definitly a bug IMHO)
		 *
		 * An alternative is to add the following code in the remove
		 * method:
		 * 
		 * TableCellEditor cellEditor = table.getCellEditor();
		 * if (cellEditor != null)
		 * cellEditor.stopCellEditing();
		 *
		 * ¨
		 */
		final ControlComponents components = new ControlComponents(table, addButton, removeButton, field);

		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		addButton.addActionListener(
				e -> {
					add(components);
					clearText(components);
				});

		field.addActionListener(e -> {
			add(components);
			clearText(components);
		});

		removeButton.addActionListener(e -> remove(components));

		table.getSelectionModel().addListSelectionListener(
				e -> setButtonState(components));
		table.addPropertyChangeListener(
			evt -> propertyChange(evt, components)
		);
	}

	private static void propertyChange(PropertyChangeEvent evt, ControlComponents components) {
		if ("model".equals(evt.getPropertyName())) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setButtonState(components);
				}
			});
		}
	}

	/**
	 * Update the "minus" button state.
	 */
	private static void setButtonState(ControlComponents components) {
		JTable table = components.table();
		JButton removeButton = components.removeButton();
		int sel = table.getSelectedRow();
		if (sel != -1) {
			GrowableModel model = (GrowableModel) table.getModel();
			removeButton.setEnabled(model.canRemove(sel));
		} else
			removeButton.setEnabled(false);
	}

	private static void clearText(ControlComponents components) {
		components.field().setText("");
	}

	private static void add(ControlComponents components) {
		JTable table = components.table();
		JTextField field = components.field();
		String code = field.getText();
		StringUtils.doIfNotEmpty(code, s -> {
			GrowableModel model = (GrowableModel) table.getModel();
			model.addRow(s);
		});
	}

	private static void remove(ControlComponents components) {
		JTable table = components.table();
		int sel = table.getSelectedRow();
		if (sel != -1) {
			/*
			 * TableCellEditor cellEditor = table.getCellEditor();
			 * if (cellEditor != null)
			 * cellEditor.stopCellEditing();
			 */
			GrowableModel model = (GrowableModel) table.getModel();
			model.removeRow(sel);
		}
	}
}