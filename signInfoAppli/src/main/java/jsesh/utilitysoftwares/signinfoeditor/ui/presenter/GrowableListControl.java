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

public class GrowableListControl<T> {
	JList<T> jList;
	JTextField field;

	/**
	 * Create a growable control and bind it to the corresponding components.
	 * @param jList a JList, whose model <em>must</em> implement GrowableModel.
	 * @see GrowableModel
	 */
	public GrowableListControl(JList<T> jList, JButton addButton,
			JButton removeButton, JTextField field) {
		super();
		this.jList = jList;
		this.field = field;

		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				add();
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
	}

	public void clearText() {
		field.setText("");
	}
	
	public void add() {
		String code= field.getText();
		StringUtils.doIfNotEmpty(code, s -> {
				GrowableModel model = (GrowableModel) jList.getModel();
				model.addRow(s);
		});
	}

	public void remove() {
		int sel = jList.getSelectedIndex();
		if (sel != -1) {
			GrowableModel model = (GrowableModel) jList
					.getModel();
			model.removeRow(sel);
		}
	}
}