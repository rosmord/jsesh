/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.mdcDisplayer.swing.editor.JMDCEditor;

/**
 * @author S. Rosmorduc
 *  
 */
public class PasteAction extends AbstractAction {

	public PasteAction() {
		super("paste");
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		JMDCEditor editor = (JMDCEditor) e.getSource();
		if (editor.isEditable())
			editor.paste();
	}

}