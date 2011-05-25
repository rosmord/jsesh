/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editorSoftware.actions;

import java.awt.event.ActionEvent;

import jsesh.editorSoftware.MDCDisplayerAppliWorkflow;
import jsesh.editorSoftware.actions.generic.BasicAction;

public class RotationAction extends BasicAction {
	/**
	 * 
	 */
	private final MDCDisplayerAppliWorkflow workflow;
	private int angle;

	public RotationAction(int angle, MDCDisplayerAppliWorkflow workflow) {
		super(workflow,"" + angle + "deg.");
		this.workflow = workflow;
		this.angle = angle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		workflow.getEditor().getWorkflow().setAngle(angle);
	}
}