/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editorSoftware.actions;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import jsesh.editorSoftware.MDCDisplayerAppliWorkflow;
import jsesh.editorSoftware.actions.generic.BasicAction;

/**
 * Open a new text in the editor.
 * @author rosmord
 *
 */
public class OpenTextAction extends BasicAction {

	/**
	 * @param name
	 * @param icon
	 * @param workflow
	 */
	public OpenTextAction(String name, Icon icon, MDCDisplayerAppliWorkflow workflow) {
		super(workflow, name, icon);
	}

	/**
	 * @param name
	 * @param workflow
	 */
	public OpenTextAction(String name, MDCDisplayerAppliWorkflow workflow) {
		super(workflow, name);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		workflow.loadText();
	}

}
