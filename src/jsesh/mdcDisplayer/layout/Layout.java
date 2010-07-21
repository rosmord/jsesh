package jsesh.mdcDisplayer.layout;

import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * Elements of this class are responsible for computing the position and 
 * dimensions of view elements.
 * 
 * <p> A layout can change a view's width and height
 * and it can also change the subview positions and scales.
 * <p> <b> Important </b> - note that layout does just that -- layout -- and
 * does in no way create view elements. MDCView element creation is the task of
 * ViewBuilder. 
 * 
 * 
 * <p> An actual base for implementation can be found in the SimpleLayout class. 
 * 
 * @see jsesh.mdcDisplayer.mdcView.ViewBuilder
 * @see jsesh.mdcDisplayer.layout.SimpleLayout
 * (c) Serge Rosmorduc
 * @author Serge Rosmorduc
 *
 */
public interface Layout {
    
	/**
	 * Reset the internal state of the Layout before working on a 
	 * new text chunk. 
	 * @param drawingSpecifications TODO
	 *
	 */
	void reset(DrawingSpecification drawingSpecifications);
	
	/**
	 * Called when the complete model has been processed.
	 *
	 */
	void cleanup();
		
	/**
	 * compute the view size (width and height). If the view has subviews,
	 * layout should compute their position in the parent view and scale them if required.
	 * Note that layout is <em>not</em> a recursive function : when a view is built, 
	 * its subviews have already been processed by another call to layout.
	 * The embedded variable is used to specify that this view corresponds to an element that
	 * doesn't appears on the upper levels of the text. For instance, it will be true for a cadrat, but false when
	 * this cadrat is embedded within another cadrat. In fact that's the main use of this variable, as spacing can be quite different
	 * (specially in columns). 
	 * @param view
	 * @param depth the depth of the view.
	 */
	void layout(MDCView view, int depth);
	
	/**
	 * This method is called before anything is done on the view. 
	 * Most notably, it's called before the subviews are built.
	 * It might be used when the layout of the internal elements depends on their 
	 * container.
	 * <p>It can also be used for things which should be done to all views.
	 * <p>Currently, its main actual use is to change the sign centering preferences
	 * when laying out a cadrat.
	 * <p>
	 * @param view
	 * @param depth depth of the view. the view for the whole text has depth 0.
	 */
	void preLayoutHook(MDCView view, int depth);

	/**
	 * Called after layout, for cleanup of code from preLayoutHook.
	 * @param result
	 */
	void postLayoutHook(MDCView result, int depth);

}
