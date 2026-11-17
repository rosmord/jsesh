/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.editor.actions;

import java.awt.event.ActionEvent;

import jsesh.mdcDisplayer.swing.actionsUtils.AbstractSelectionAction;
import jsesh.mdcDisplayer.swing.editor.JMDCEditor;

/**
 * @author S. Rosmorduc
 *
 */
public class GroupHorizontalAction extends AbstractSelectionAction {

	public GroupHorizontalAction() {
		super("group horizontal");
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		JMDCEditor d=  (JMDCEditor) e.getSource();
		if (d.isEditable())
			d.getWorkflow().groupHorizontal();	
	}
	
	
}
