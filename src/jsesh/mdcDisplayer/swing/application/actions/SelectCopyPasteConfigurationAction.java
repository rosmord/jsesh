/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.application.actions;

import java.awt.event.ActionEvent;

import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;
import jsesh.mdcDisplayer.swing.application.actions.generic.BasicAction;

/**
 * Select one of the copy and paste configuration
 * @author rosmord
 *
 */
public class SelectCopyPasteConfigurationAction extends BasicAction {

	private int configurationNumber;
	
	/**
	 * @param name
	 * @param workflow
	 */
	public SelectCopyPasteConfigurationAction(String name, MDCDisplayerAppliWorkflow workflow, int configurationNumber) {
		super(workflow, name);
		this.configurationNumber= configurationNumber;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		workflow.selectCopyPasteConfiguration(configurationNumber);
	}

	
}
