/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editorSoftware.actions;

import java.awt.event.ActionEvent;

import jsesh.editorSoftware.MDCDisplayerAppliWorkflow;
import jsesh.editorSoftware.actions.generic.BasicAction;

public class SizeAction extends BasicAction {
	/**
	 * 
	 */
	private final MDCDisplayerAppliWorkflow workflow;
	private int size;

	public SizeAction(int size,MDCDisplayerAppliWorkflow workflow) {
		super(workflow,"" + size + "%");
		this.workflow = workflow;
		this.size = size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		this.workflow.getEditor().getWorkflow().resizeSign(size);
	}
}