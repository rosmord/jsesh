/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editorSoftware.actions;

import java.awt.event.ActionEvent;

import jsesh.editorSoftware.MDCDisplayerAppliWorkflow;
import jsesh.editorSoftware.actions.generic.BasicAction;

public class EditGroupAction extends BasicAction {
	public EditGroupAction(String name, MDCDisplayerAppliWorkflow workflow) {
		super(workflow, name);
	}

	public void actionPerformed(ActionEvent e) {
		workflow.editGroup();
	}
}