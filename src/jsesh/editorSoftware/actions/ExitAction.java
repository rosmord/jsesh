/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editorSoftware.actions;

import java.awt.event.ActionEvent;

import jsesh.editorSoftware.MDCDisplayerAppliWorkflow;
import jsesh.editorSoftware.actions.generic.BasicAction;

/**
 * @author rosmord
 *
 */
public class ExitAction extends BasicAction {

	/**
	 * @param name
	 * @param workflow
	 */
	public ExitAction(String name, MDCDisplayerAppliWorkflow workflow) {
		super(workflow, name);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		workflow.exit();
	}

}
