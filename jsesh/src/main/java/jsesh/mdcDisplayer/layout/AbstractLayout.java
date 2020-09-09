package jsesh.mdcDisplayer.layout;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.ModelElementAdapter;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;


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
 * <p>To write an actual "Layouter", extends this class and 
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
	 * The drawing specifications in use.
	 * Only set during the view building.
	 */
	protected DrawingSpecification drawingSpecifications= null;
	
	/**
	 * If true, small signs are currently centered.
	 * This variable may change during layout (for instance, become true in cartouches).
	 */
	protected boolean centerSigns = false;

	/**
	 * Current text orientation : columns or lines.
	 * This variable may change during layout.
	 */
	
	protected TextOrientation currentTextOrientation;
	
	/**
	 * Current text direction.
	 * This variable may change during layout.
	 */
	protected TextDirection currentTextDirection;
	
	protected int depth;
	
	/**
	 * compute the view size (width and height)
	 * @see jsesh.mdcDisplayer.layout.Layout#layout(MDCView, int)
	 */
        @Override
	final public void layout(MDCView view, int depth) {
		currentView= view;
		this.depth= depth;
		commonLayoutHook();
		// Actual layout :
		view.getModel().accept(this);
		// Reset everything, to avoid keeping unnecessary references.
		currentView= null;
	}
	
	// We should probably cut reset in two : one part (a final method ?) would take care of
	// drawingSpecifications, the other would be an actual reset. Alternatively, 
	// maybe drawingSpecifications could be moved to the constructor.
	//
	// the reason this is not currently the case :
	// 		we wanted the view builder to take the layout and the specs as two independant parameters.
	//		some thought should be given to this subject.
	abstract public void reset(DrawingSpecification drawingSpecifications);
	
	/**
	 * AbstractLayout provides a No-op implentation of preLayoutHook.
	 * 
	 * @see jsesh.mdcDisplayer.layout.Layout#preLayoutHook(jsesh.mdcDisplayer.mdcView.MDCView, int)
	 */
	public void preLayoutHook(MDCView view, int depth)
	{
	}

	/**
	 * Method called just before specific layout visitor methods.
	 * <p> Use for code that should be called on all views. Note that this code is called <em>after</em> the subviews are built and laid out.
	 *
	 */
	protected void commonLayoutHook() {
	}
	
	public boolean isCenterSigns() {
		return centerSigns;
	}
	
	public void setCenterSigns(boolean centerSigns) {
		this.centerSigns = centerSigns;
	}
}
