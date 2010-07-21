/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.application.actions;

import java.awt.event.ActionEvent;

import jsesh.mdc.model.ModelElement;
import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;
import jsesh.mdcDisplayer.swing.application.actions.generic.BasicAction;

/**
 * Simple action class for adding new elements (from a prototype).
 * @author rosmord
 *
 */
public class InsertElementAction extends BasicAction {


	ModelElement element;

	/**
	 * 
	 * @param element a prototype of the element to add.
	 * @param workflow the workflow 
	 * 
	 */
	public InsertElementAction(String name, ModelElement element, MDCDisplayerAppliWorkflow workflow) {
		super(workflow, name);
		this.element = element;
	}

	public void actionPerformed(ActionEvent e) {
		workflow.insertElement(element);
	}
}
