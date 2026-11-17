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
public class GroupVerticalAction extends AbstractSelectionAction {

	public GroupVerticalAction() {
		super("group vertical");
	}
	
	public void actionPerformed(ActionEvent e) {
		JMDCEditor d=  (JMDCEditor) e.getSource();
		if (d.isEditable())
			d.getWorkflow().groupVertical();	
	}
}
