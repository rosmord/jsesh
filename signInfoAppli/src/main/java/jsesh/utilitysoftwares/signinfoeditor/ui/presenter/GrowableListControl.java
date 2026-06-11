package jsesh.utilitysoftwares.signinfoeditor.ui.presenter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextField;

import org.qenherkhopeshef.utils.StringUtils;

import jsesh.utilitysoftwares.signinfoeditor.viewmodel.GrowableModel;

/**
 * Controls a list where data can be added and removed. The list model
 * should implement GrowableModel.
 * 
 * @author rosmord
 */

public class GrowableListControl {

	private record ControlComponents<T>(JList<T> jList, JTextField field) {
	}

	private GrowableListControl() {
	}

	/**
	 * Create a growable control and bind it to the corresponding components.
	 * 
	 * @param jList a JList, whose model <em>must</em> implement GrowableModel.
	 * @see GrowableModel
	 */
	public static <T> void bind(JList<T> jList, JButton addButton,
			JButton removeButton, JTextField field) {

		final ControlComponents<T> components = new ControlComponents<>(jList, field);

		addButton.addActionListener(e -> {
			add(components);
			clearText(components);
		});

		field.addActionListener(e -> {
			add(components);
			clearText(components);
		});

		removeButton.addActionListener(e -> remove(components));
	}

	private static <T> void clearText(ControlComponents<T> components) {
		components.field().setText("");
	}

	private static <T> void add(ControlComponents<T> components) {
		JList<T> jList = components.jList();
		String code = components.field().getText();
		StringUtils.doIfNotEmpty(code, s -> {
			GrowableModel model = (GrowableModel) jList.getModel();
			model.addRow(s);
		});
	}

	private static <T> void remove(ControlComponents<T> components) {
		JList<T> jList = components.jList();
		int sel = jList.getSelectedIndex();
		if (sel != -1) {
			GrowableModel model = (GrowableModel) jList
					.getModel();
			model.removeRow(sel);
		}
	}
}