/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.application.actions.generic;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;

/**
 * @author rosmord
 *
 */
public abstract class BasicAction extends AbstractAction {

	protected MDCDisplayerAppliWorkflow workflow;

	/**
	 * @param name
	 * @param icon
	 */
	public BasicAction(String name, Icon icon, MDCDisplayerAppliWorkflow workflow) {
		super(name, icon);
		this.workflow= workflow;
	}

	/**
	 * @param name
	 */
	public BasicAction(String name, MDCDisplayerAppliWorkflow workflow) {
		super(name);
		this.workflow= workflow;
	}
	
	
}
