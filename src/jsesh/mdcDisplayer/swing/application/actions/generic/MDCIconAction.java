/*
 * Created on 12 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.application.actions.generic;



import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;
import jsesh.swingUtils.ImageIconFactory;

/**
 * An action whose icon is built from an MDCtype.
 * 
 * @author S. Rosmorduc
 * 
 */
abstract public class MDCIconAction extends BasicAction {

	public MDCIconAction(MDCDisplayerAppliWorkflow workflow, String mdcText) {
		super(workflow, "",ImageIconFactory.buildImage(mdcText));
	}
}
