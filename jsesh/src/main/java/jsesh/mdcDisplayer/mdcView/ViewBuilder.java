/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdcDisplayer.mdcView;

import jsesh.mdc.model.ModelElement;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * Expert builder for creating views.
 * @author Serge Rosmorduc
 *
 * TODO: In fact, having a plugable viewBuilder is not that interesting. 
 * TODO: Consider using a class instead of an interface.
 * 
 * This file is free software under the Gnu Lesser Public License.
 */
public interface ViewBuilder {

    public MDCView buildView(ModelElement elt, DrawingSpecification drawingSpecifications);

    /**
     * Recompute the layout of a view. If a view has been modified since it was
     * built, this methods recomputes its layout.
     *
     * @param v
     * @param drawingSpecifications
     */
    public void reLayout(MDCView v, DrawingSpecification drawingSpecifications);

    /**
     * Build a partial view of an element.
     * <p>
     * The view will correspond to all sub elements of model between indexes
     * start and end.
     * <p>
     * mode will typically be a TopItemList which we want to render partially.
     *
     * @param model
     * @param start
     * @param end
     * @return the view built for this element part.
     */
    public MDCView buildView(ModelElement model, int start, int end, DrawingSpecification drawingSpecifications);
}
