/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.application.actions;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;
import jsesh.mdcDisplayer.swing.application.actions.generic.BasicAction;

/**
 * @author rosmord
 *
 */
public class SaveTextAction extends BasicAction {

	/**
	 * @param name
	 * @param icon
	 * @param workflow
	 */
	public SaveTextAction(String name, Icon icon,
			MDCDisplayerAppliWorkflow workflow) {
		super(name, icon, workflow);
	}

	/**
	 * @param name
	 * @param workflow
	 */
	public SaveTextAction(String name, MDCDisplayerAppliWorkflow workflow) {
		super(name, workflow);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		workflow.saveText();
	}

}
