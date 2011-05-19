/*
 * Created on 1 janv. 2002
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor.actions.text;

import java.awt.event.ActionEvent;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;

/**
 * @author S. Rosmorduc
 *  
 */
@SuppressWarnings("serial")
public class NewPageAction extends EditorAction {

	public NewPageAction(JMDCEditor editor) {
		super(editor, "New Page");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (editor.isEditable())
			editor.getWorkflow().insertPageBreak();
	
	}
	
	
}