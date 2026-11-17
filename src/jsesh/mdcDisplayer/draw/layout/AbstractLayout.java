package jsesh.mdcDisplayer.draw.layout;

import jsesh.mdc.model.ModelElementAdapter;
import jsesh.mdcDisplayer.draw.DrawingSpecifications;
import jsesh.mdcDisplayer.mdcView.MDCView;


/**
 * This file is free Software 
 * under the GNU LESSER GENERAL PUBLIC LICENCE.
 * 
 * (c) Serge Rosmorduc
 * @author Serge Rosmorduc
 *
 */

/**
 * An utility implementation of layout.
 * <p>To write an actual Layouter, extends this class and 
 * overwrite the visit.... methods inherited from the ModelElementAdapter.
 * 
 * <p>In these methods, the view to layout is accessible as "currentView".
 * 
 * <p>Note that layout is not supposed to be called recursively. 
 * The recursion is done through ViewBuilder.
 *  
 * <p> When layout is called on a view, the following conditions can be supposed to be fullfilled :
 * <ul>
 * <it> the subview for the current view exist.
 * <it> layout has been already called on these subviews.
 * </ul>
 * 
 * <p> The visitor you write should do the following :
 * <ul>
 * <it> compute and set currentView's dimensions (using the subviews and the associated element) ;
 * <it> setting the scale and position of subviews.
 * <it> it is possible to change the scale of currentview.
 * </ul>
 * <p> However, the position of the currentview is supposed to be set from its parentview, so don't change it.
 * @see Layout
 * @see SimpleLayout
 */

public abstract class AbstractLayout extends ModelElementAdapter implements Layout {

	/**
	 * The view which is currently built.
	 */
	protected MDCView currentView;

	/**
	 * The drawing specifications in use. Only set during the view building.
	 */
	protected DrawingSpecifications drawingSpecifications;
	
	protected boolean centerSigns = false;

	/**
	 * compute the view size (width and height)
	 * @see jsesh.mdcDisplayer.draw.layout.Layout#layout(jsesh.mdcDisplayer.mdcView.ContainerView)
	 */
	final public void layout(MDCView view, DrawingSpecifications drawingSpecifications) {
		currentView= view;
		this.drawingSpecifications= drawingSpecifications;
		view.getModel().accept(this);
		currentView= null;
		this.drawingSpecifications= null;
	}
	
	public void reset() {};
	
	/**
	 * AbstractLayout provides a No-op implentation of preLayoutHook.
	 * 
	 * @see jsesh.mdcDisplayer.draw.layout.Layout#preLayoutHook(jsesh.mdcDisplayer.mdcView.MDCView)
	 */
	public void preLayoutHook(MDCView view)
	{
	}

	
	public boolean isCenterSigns() {
		return centerSigns;
	}
	public void setCenterSigns(boolean centerSigns) {
		this.centerSigns = centerSigns;
	}
}
