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

 As a counterpart to the access to the source code and  rights to copy,
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
import java.awt.event.ActionListener;
import java.beans.EventHandler;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jsesh.i18n.I18n;

/**
 * Very specific class for 'remove' button in our glossary.
 * <p> A reusable one would be nice.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
@SuppressWarnings("serial")
public class JRemoveButtonCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer{

	private GlossaryTableModel model;
	private JButton  active;
	private int currentRow;
	
	public JRemoveButtonCell(GlossaryTableModel model) {
		this.model= model;
		this.active= new JButton(I18n.getString("JRemoveButtonCell.remove.label"));
		active.addActionListener(EventHandler.create(ActionListener.class, this, "remove"));
		active.setMaximumSize(active.getMinimumSize());
	}
	
	public Object getCellEditorValue() {
		return null;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		return active;
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.currentRow= row;
		return active;
	}
	
	public void remove() {
		model.delete(currentRow);
		stopCellEditing();
	}

	public static double getMaxWidth() {
		JButton button = new JButton(I18n.getString("JRemoveButtonCell.remove.label"));
		return button.getPreferredSize().getWidth();
	}
}
