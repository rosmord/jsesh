/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.application.actions;

import java.awt.event.ActionEvent;

import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;
import jsesh.mdcDisplayer.swing.application.actions.generic.MDCIconAction;

public class AddPhilologyAction extends MDCIconAction {
	int code;
	
	public AddPhilologyAction(String mdc, int code, MDCDisplayerAppliWorkflow workflow) {
		super(workflow, mdc);
		this.code= code;
	}
	
	public void actionPerformed(ActionEvent e) {
		workflow.addPhilologicalMarkup(
				code);
	}
}