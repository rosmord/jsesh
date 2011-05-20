/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editorSoftware.actions;

import java.awt.event.ActionEvent;

import jsesh.editorSoftware.MDCDisplayerAppliWorkflow;
import jsesh.editorSoftware.actions.generic.MDCIconAction;

@SuppressWarnings("serial")
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