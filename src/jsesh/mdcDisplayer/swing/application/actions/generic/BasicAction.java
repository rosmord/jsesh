/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.application.actions.generic;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;

/**
 * Base class for a number of actions in our application.
 * Optionnaly, the action can be aware of a number of events, and activate accordingly.
 * @author rosmord
 */
public abstract class BasicAction extends AbstractAction {

	protected MDCDisplayerAppliWorkflow workflow;

	/**
	 * @param name
	 * @param icon
	 */
	public BasicAction(MDCDisplayerAppliWorkflow workflow, String name, Icon icon) {
		super(name, icon);
		this.workflow= workflow;
	}

	/**
	 * @param name
	 */
	public BasicAction(MDCDisplayerAppliWorkflow workflow, String name) {
		super(name);
		this.workflow= workflow;
	}
	
	public BasicAction(MDCDisplayerAppliWorkflow workflow, String name, boolean caretAware) {
		super(name);
		if (caretAware)
			workflow.registerCaretAwareAction(this);
		this.workflow= workflow;
	}
	
}
