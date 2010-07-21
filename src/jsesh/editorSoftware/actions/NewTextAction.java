/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editorSoftware.actions;

import java.awt.event.ActionEvent;

import jsesh.editorSoftware.MDCDisplayerAppliWorkflow;
import jsesh.editorSoftware.actions.generic.BasicAction;

public final class NewTextAction extends BasicAction {
	/**
	 * 
	 */
	private final MDCDisplayerAppliWorkflow workflow;

	public NewTextAction(String name, MDCDisplayerAppliWorkflow workflow) {
		super(workflow,name);
		this.workflow = workflow;
	}

	public void actionPerformed(ActionEvent e) {
		this.workflow.newText();
	}
}