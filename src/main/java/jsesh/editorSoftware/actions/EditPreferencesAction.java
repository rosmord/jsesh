/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editorSoftware.actions;

import java.awt.event.ActionEvent;

import jsesh.editorSoftware.MDCDisplayerAppliWorkflow;
import jsesh.editorSoftware.actions.generic.BasicAction;

public class EditPreferencesAction extends BasicAction {
	public EditPreferencesAction(String name, MDCDisplayerAppliWorkflow workflow) {
		super(workflow,name);
	}

	public void actionPerformed(ActionEvent e) {
		workflow.editPreferences();
	}
}