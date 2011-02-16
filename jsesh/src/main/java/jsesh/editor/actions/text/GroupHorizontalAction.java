/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor.actions.text;

import java.awt.event.ActionEvent;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.AbstractSelectionAction;

/**
 * @author S. Rosmorduc
 *
 */
public class GroupHorizontalAction extends AbstractSelectionAction {

	public GroupHorizontalAction(JMDCEditor editor) {
		super(editor, "group horizontal");
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
	
		if (editor.isEditable())
			editor.getWorkflow().groupHorizontally();	
	}
	
	
}
