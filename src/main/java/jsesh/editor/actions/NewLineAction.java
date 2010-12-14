/*
 * Created on 1 janv. 2002
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.editor.JMDCEditor;

/**
 * @author S. Rosmorduc
 *
 */
public class NewLineAction extends EditorAction {
	public NewLineAction(JMDCEditor editor) {
		super(editor, "New Line");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (editor.isEditable())
			editor.getWorkflow().insertNewLine();
	}
	

}
