package jsesh.mdcDisplayer.draw.layout;

import jsesh.mdcDisplayer.draw.DrawingSpecifications;
import jsesh.mdcDisplayer.mdcView.MDCView;

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
 * @see jsesh.mdcDisplayer.draw.ViewBuilder
 * @see jsesh.mdcDisplayer.draw.layout.SimpleLayout
 * (c) Serge Rosmorduc
 * @author Serge Rosmorduc
 *
 */
public interface Layout {
	/**
	 * Reset the internal state of the Layout before working on a 
	 * new text chunk. 
	 *
	 */
	void reset();
		
	/**
	 * compute the view size (width and height). If the view has subviews,
	 * layout should compute their position in the parent view and scale them if required.
	 * Note that layout is <em>not</em> a recursive function : when a view is built, 
	 * its subviews have already been processed by another call to layout.
	 * @param view
	 */
	void layout(MDCView view, DrawingSpecifications drawingSpecifications);
	
	/**
	 * This method is called before anything is done on the view. 
	 * Most notably, it's called before the subviews are built.
	 * It might be used when the layout of the internal elements depends on their 
	 * container.
	 * Currently, its main actual use is to change the sign centering preferences
	 * when laying out a cadrat. 
	 * @param view
	 */
	void preLayoutHook(MDCView view);

}
