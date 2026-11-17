/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdcDisplayer.draw;

import jsesh.mdc.model.ModelElement;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;

/**
 * @author Serge Rosmorduc
 *
 * This file is free software under the Gnu Lesser Public License.
 */
public interface ViewBuilder
{
	
	// IMPORTANT : move the drawingSpecifications out of the builder. They should be given as arguments to the methods.
	// This will simplify the architecture ; it will be symetric between building and drawing
	// alternative solution : the building might produce a complete object : a couple specifications + view.
	// This would be more logical, as a view is not completely meaningful without its specifications.
	DrawingSpecifications getDrawingSpecifications();
	
	public MDCView buildView(ModelElement elt);
	
	/**
	 * Recompute the layout of a view.
	 * If a view has been modified since it was built,
	 * this methods recomputes its layout.
	 * @param v
	 */
	public void reLayout(MDCView v);

	/**
	 * Build a partial view of an element.
	 * <p> The view will correspond to all sub elements of model between indexes start and end.
	 * <p> mode will typically be a TopItemList which we want to render partially.
	 * @param model
	 * @param start
	 * @param end
	 * @return the view built for this element part.
	 */
	public MDCView buildView(ModelElement model, int start, int end);
}