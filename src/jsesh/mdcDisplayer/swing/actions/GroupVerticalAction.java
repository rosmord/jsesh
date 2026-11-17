/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.actions;

import java.awt.event.ActionEvent;

import jsesh.mdcDisplayer.swing.actions.generic.AbstractSelectionAction;
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
		d.getWorkflow().groupVertical();	
	}
}
