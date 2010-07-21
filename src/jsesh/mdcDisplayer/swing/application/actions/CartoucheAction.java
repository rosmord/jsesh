/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.application.actions;

import java.awt.event.ActionEvent;

import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;
import jsesh.mdcDisplayer.swing.application.actions.generic.MDCIconAction;

public class CartoucheAction extends MDCIconAction {
	private int end;

	private int start;

	private int type;

	/**
	 * @param mdcText
	 * @param type
	 * @param start
	 * @param end
	 */
	public CartoucheAction(int type, int start, int end, String mdcText, MDCDisplayerAppliWorkflow workflow) {
		super(workflow,mdcText);
		this.type = type;
		this.start = start;
		this.end = end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		workflow.addCartouche(type, start, end);
	}

}