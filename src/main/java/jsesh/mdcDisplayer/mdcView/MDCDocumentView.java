package jsesh.mdcDisplayer.mdcView;

import java.util.WeakHashMap;

import jsesh.mdc.model.ModelElement;

/**
 * The view of an entire MDC document.
 * This is mostly a collection of sub-element views, 
 * but it has a number of features, for instance the capability to retrieve the view for a certain element.
 * @author rosmord
 */
public class MDCDocumentView {

	private WeakHashMap<ModelElement, MDCView> viewMap;
	private MDCView mainView;
	
	/**
	 * Returns the view associated to a certain model element.
	 * Currently, not all elements have this capability (I don't know yet if this is a good choice).
	 * @param elt
	 * @return a view or null.
	 */
	public MDCView getViewFor(ModelElement elt) {
		return null;
	}
}
