/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.application.actions;

import java.awt.event.ActionEvent;

import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;
import jsesh.mdcDisplayer.swing.application.actions.generic.MDCIconAction;

/**
 * Shade a particular sign.
 * @author rosmord
 *
 */
public class ShadeSignAction extends MDCIconAction {

	/**
	 * The shading to apply to the current sign.
	 */
	private int shade;

	/**
	 * @param shade a combination of shadingCodes.
	 * @param mdc
	 * @param workflow TODO
	 */
	public ShadeSignAction(int shade, String mdc, MDCDisplayerAppliWorkflow workflow) {
		super(workflow,mdc);
		this.shade = shade;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		workflow.doShadeSign(shade);
	}
}