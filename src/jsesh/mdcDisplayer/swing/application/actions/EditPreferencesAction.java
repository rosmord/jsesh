/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.application.actions;

import java.awt.event.ActionEvent;

import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;
import jsesh.mdcDisplayer.swing.application.actions.generic.BasicAction;

public class EditPreferencesAction extends BasicAction {
	public EditPreferencesAction(String name, MDCDisplayerAppliWorkflow workflow) {
		super(workflow,name);
	}

	public void actionPerformed(ActionEvent e) {
		workflow.editPreferences();
	}
}