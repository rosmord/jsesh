package jsesh.mdcDisplayer.mdcView;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.SubCadrat;

/**
 * Base class for all view elements. A view element is a view of an MDC model
 * element. THIS IS AN ATTEMPT AT A SIMPLER VIEW CLASS.
 * 
 * 
 * <p>
 * <b>About coordinates : </b> we take hereby the convention that the y axis is
 * oriented bottomward (as in so-called "text mode").
 * 
 * <p>
 * Note that the view class is <em>not</em> intended to be "intelligent". The
 * methods available are quite numerous, but very, very basic. The layout is
 * done by the layout classes. In particular, no reference is made nor should be
 * made in this class to DrawingSpecification.
 * 
 * <p>
 * A view's coordinates are expressed in the reference system of its container
 * view. A view, as a container, can be oriented left-to-right or right-to-left
 * (I don't see currently any interest for top-to-bottom vs. bottom-to-top). In
 * left-to-right-system, 0,0 is the top-left part of the container view, and the
 * coordinates designate the top-left corner of the subview. In right-to-left
 * system, 0,0 is the top-right corner, the coordinates designate the top-right
 * corner of the subview, and the x axis is oriented to the right, as far as
 * views go. Drawing will take place in the normal way (? is this a good idea
 * ?).
 * 
 * <p>
 * any view contains the following information :
 * 
 * <p>
 * Scaling and dimensions : scaling and dimensions are really two different
 * things in this class. An important thing to note is that
 * <em> a view's dimensions are quite independant of those of its subviews.</em>
 * So, scaling a view won't modify its width or height. If one wants a view to
 * be just large enough for its subviews, one should call fitToSubViews(),
 * <em>after all subviews have been placed and computed</em>. If some subview
 * is modified thereafter, fitToSubViews() should be called once more.
 * 
 * <p>
 * deltaBaseLine and deltaBaseColumn are modifiers which describe the difference
 * between the "standard" base lines and column and those for this view.
 * Normally, the tops of the views are aligned in horizontal text. Note that
 * those values are used for computing the position of the view. Once this is
 * done, the position is completely defined.
 * 
 * <p>
 * <p>
 * This file is free Software under the LGPL. (c) Serge Rosmorduc
 * 
 * @author rosmord
 * @see RelativePosition
 */

public class SimpleMDCView extends Observable implements Observer {

	Point2D.Double position;

	/**
	 * Determines the view internal coordinate system.
	 */

	private TextDirection direction;

	// ATTRIBUTES.

	/**
	 * Rotation angle of this model.
	 */
	private float angle;

	/**
	 * The underlying model
	 */
	private ModelElement model;

	/**
	 * Parent view, if any;
	 */

	private SimpleMDCView parent;

	/**
	 * subviews, if any
	 */

	private List subViews;

	/**
	 * This view's displacement relative to the reference point.
	 */

	private double deltaBaseX, deltaBaseY;

	/**
	 * size of this view (seen from <em>outside</rm>).
	 */

	private float width, height;

	/**
	 * Internal scale of this view (relative to the parent view's scale)
	 * 
	 * Note that the scale is an <em>internal affair</em> for the container.
	 * Changing the scale has no effect on the view's width and height.
	 * 
	 */

	// TODO : make the subview coordinates as independant of the view's
	// dimensions as possible.
	private float xScale, yScale;

	/**
	 * 
	 * affine transformation used to draw the view's elements.
	 */

	transient private AffineTransform affineTransform;

	/**
	 * Default transformation (identity !)
	 */
	static private AffineTransform defaultAffineTransform = null;

	// **************************************************************************************************
	// BASIC METHODS

	/**
	 * Constructor for ContainerView.
	 * 
	 * @param model
	 */
	public SimpleMDCView(ModelElement model) {
		setModel(model);
		xScale = 1.0F;
		yScale = 1.0F;
		angle = 0f;
		height = 0f;
		width = 0f;
		// margins = null;
		subViews = null;
		affineTransform = null;
		position = new Point2D.Double();
		direction = TextDirection.LEFT_TO_RIGHT;
		parent = null;
	}

	// Basic Setters and getters

	/**
	 * Returns the model, if any.
	 * 
	 * @return ModelElement
	 */
	public ModelElement getModel() {
		return model;
	}

	/**
	 * returns the view which contain this subview. Can return null, if the view
	 * has no parent.
	 * 
	 * @return parent.
	 */
	public SimpleMDCView getParent() {
		return parent;
	}

	/**
	 * Returns the angle.
	 * 
	 * @return float
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * Returns the view's actual height.
	 * 
	 * @return float
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * Returns the width of the view.
	 * 
	 * @return float
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Sets the view's height to h.
	 * 
	 * @param h
	 *            The h to set
	 */
	public void setHeight(float h) {
		this.height = h;
	}

	/**
	 * Sets the width of the view. This takes scaling into account. The size of
	 * the scaled view will be w.
	 * 
	 * @param w
	 *            The width of this view.
	 */

	public void setWidth(float w) {
		this.width = w;
	}

	/**
	 * Returns the xScale.
	 * 
	 * @return float
	 */
	public float getXScale() {
		return xScale;
	}

	/**
	 * Returns the yScale.
	 * 
	 * @return float
	 */
	public float getYScale() {
		return yScale;
	}

	/**
	 * Sets the xScale. This is the scale applied to the internal elements of
	 * this view. Doesn't change the view's external dimensions.
	 * 
	 * @param xScale
	 *            The xScale to set
	 */
	public void setXScale(float xScale) {
		affineTransform = null;
		this.xScale = xScale;
	}

	/**
	 * Sets the yScale. This is the scale applied to the internal elements of
	 * this view. Doesn't change the view's external dimensions.
	 * 
	 * @param yScale
	 *            The yScale to set
	 */
	public void setYScale(float yScale) {
		affineTransform = null;
		this.yScale = yScale;
	}

	/**
	 * Sets the angle.
	 * 
	 * @param angle
	 *            The angle to set
	 */
	public void setAngle(float angle) {
		affineTransform = null;
		this.angle = angle;
	}

	// Subview manipulation

	/**
	 * Add a subview to a view.
	 * 
	 * @param subview
	 */

	public void add(SimpleMDCView subview) {
		getSubViews().add(subview);
		subview.parent = this;
	}

	/**
	 * @param i
	 * @param subView
	 */
	public void addAt(int i, SimpleMDCView subView) {
		getSubViews().add(i, subView);
		subView.parent = this;
	}

	/**
	 * Method getFirstSubView. This method is mostly useful for view with only
	 * one subview.
	 * 
	 * @return ContainerView : the fist sub view of this view.
	 * @throws IndexOutOfBoundsException
	 *             is there is no first subview
	 */
	public SimpleMDCView getFirstSubView() {
		return (SimpleMDCView) getSubViews().get(0);
	}

	public int getNumberOfSubviews() {
		if (subViews == null) {
			return 0;
		} else {
			return subViews.size();
		}
	}

	public List getSubViews() {
		if (subViews == null)
			subViews = new ArrayList();
		return subViews;
	}

	public ViewIterator iterator() {
		return new ViewIterator(this, getSubViews().listIterator());
	}

	public ViewIterator iterator(int idx) {
		return new ViewIterator(this, getSubViews().listIterator(idx));
	}

	// Secondary functions

	/**
	 * Utility function building the affine transform associated with the view
	 * from data such as scale, angle, x, y.
	 * 
	 */

	private void computeAffineTransform() {
		// We expect that in lots of cases, the affine transform will be
		// identity.
		// Note that the startPoint is relative to some position, which may or
		// may not be the current reference
		// point. It will be the drawing system's job to position the correct
		// origin.
		// IMPORTANT : We suppressed all data relative to translation in this
		// method,
		// for the translation is handled before calling.
		// if (getStartPoint().getDx() == 0 && getStartPoint().getDy() == 0 &&
		// xScale == 0 && yScale == 0 && angle == 0)
		if (xScale == 0 && yScale == 0 && angle == 0) {
			affineTransform = getDefaultAffineTransform();
		} else {
			affineTransform = new AffineTransform();
			// then translation. (NO MORE)
			/*
			 * if (getStartPoint().getDx() != 0 || getStartPoint().getDy() != 0) {
			 * affineTransform.translate(getStartPoint().getDx(),
			 * getStartPoint().getDy()); }
			 */
			// rotation and scale
			// Rotation
			if (angle != 0) {
				affineTransform.rotate(angle);
			}
			// scale
			if (xScale != 1.0 || yScale != 1.0) {
				affineTransform.scale(xScale, yScale);
			}
		}
	}

	/**
	 * returns the affine transformation used to draw the view's elements.
	 * 
	 * @return AffineTransform
	 */
	public AffineTransform getAffineTransform() {
		if (affineTransform == null)
			computeAffineTransform();
		return affineTransform;
	}

	/**
	 * returns the height of this view, from the point of view of its contents.
	 * When drawing a view, we work as if it wasn't scaled, so this method
	 * should be used instead of getHeight.
	 * 
	 * @return the height of this view, from the point of view of its contents.
	 */
	public float getInternalHeight() {
		return height / yScale;
	}

	/**
	 * returns the width of this view, from the point of view of its contents.
	 * When drawing a view, we work as if it wasn't scaled, so this method
	 * should be used instead of getwidth.
	 * 
	 * @return the width of this view, from the point of view of its contents.
	 */
	public float getInternalWidth() {
		return width / xScale;
	}

	/**
	 * @return Returns the deltaBaseX.
	 */
	public double getDeltaBaseX() {
		return deltaBaseX;
	}

	/**
	 * @param deltaBaseX
	 *            The deltaBaseX to set.
	 */
	public void setDeltaBaseX(double deltaBaseX) {
		this.deltaBaseX = deltaBaseX;
	}

	/**
	 * @return Returns the deltaBaseY.
	 */
	public double getDeltaBaseY() {
		return deltaBaseY;
	}

	/**
	 * @param deltaBaseY
	 *            The deltaBaseY to set.
	 */
	public void setDeltaBaseY(double deltaBaseY) {
		this.deltaBaseY = deltaBaseY;
	}

	/**
	 * @return Returns the direction.
	 */
	public TextDirection getDirection() {
		return direction;
	}

	/**
	 * @param direction
	 *            The direction to set.
	 */
	public void setDirection(TextDirection direction) {
		this.direction = direction;
	}

	/**
	 * @return Returns the position.
	 */
	public Point2D.Double getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            The position to set.
	 */
	public void setPosition(Point2D.Double position) {
		this.position = position;
	}

	/**
	 * Resets all information position for this view to 0.
	 * 
	 */
	public void resetPos() {
		position.x = 0;
		position.y = 0;
		this.deltaBaseX = 0;
		this.deltaBaseY = 0;
		direction = TextDirection.LEFT_TO_RIGHT;
	}

	/**
	 * Sets the model.
	 * 
	 * @param model
	 *            The model to set
	 */
	public void setModel(ModelElement model) {
		this.model = model;
	}

	/**
	 * Sets the view's internal scale. This method changes the view's
	 * dimensions. Sets the scale.
	 * 
	 * @param scale
	 *            The scale to set
	 */
	public void reScale(float scale) {
		float oldXScale = xScale;
		float oldYScale = yScale;
		affineTransform = null;
		this.xScale = scale * oldXScale;
		this.yScale = scale * oldYScale;
		width = width * scale;
		height = height * scale;
	}

	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 * 
	 */
	public void update(Observable o, Object arg) {
		// If the origin is the ModelElement,
		// it has been modified.

		// Look at the message.

		// Recompute the view's size. NO. THIS IS NOT THE VIEW'S BUSINESS.
		// Notify the objets which draw this view,
		// Transmitting the message.
		setChanged();
		notifyObservers();
	}

	static private AffineTransform getDefaultAffineTransform() {
		if (defaultAffineTransform == null) {
			defaultAffineTransform = new AffineTransform();
		}
		return defaultAffineTransform;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer b = new StringBuffer("[v ");
		b.append(model.getClass());
		if (model instanceof Hieroglyph) {
			Hieroglyph hie = (Hieroglyph) model;
			b.append("(" + hie.getCode() + ")");
		}
		b.append(" w :");
		b.append(width);
		b.append(" h :");
		b.append(height);
		b.append("position : ");
		b.append(getPosition());
		b.append(" s :");
		b.append(xScale);
		b.append(" r :");
		b.append(angle);
		if (subViews != null) {
			b.append(" ");
			b.append(subViews.toString());
		}
		b.append("]");
		return b.toString();
	}

	// ********************************************************************************************************************
	// Auxiliary methods for manipulating views and organizing subviews.

	/**
	 * center the subviews horizontally for this view.
	 */

	public void centerSubViewHorizontally() {
		for (int i = 0; i < getNumberOfSubviews(); i++) {
			SimpleMDCView sub = getSubView(i);
			float space = (getInternalWidth() - sub.getWidth()) / 2.0f;
			sub.getPosition().x = space;
		}
	}

	/**
	 * Center the subviews vertically.
	 * 
	 */
	public void centerSubViewsVertically() {
		for (int i = 0; i < getNumberOfSubviews(); i++) {
			SimpleMDCView sub = getSubView(i);
			float space = (getInternalHeight() - sub.getHeight()) / 2.0f;
			sub.getPosition().y = space;
		}
	}

	/**
	 * Distribute subviews vertically, so that they fill the space. Their
	 * dimensions are not modified. If there is only one subview nothing is
	 * done.
	 */

	public void distributeFromTopToBottom() {
		if (getNumberOfSubviews() > 1) {
			ViewIterator i = iterator();
			// Compute the space needed for the subviews.
			float minH = getSumOfSubViewsHeights();
			// Space left will be distributed :
			float interSpace = (getInternalHeight() - minH)
					/ (getNumberOfSubviews() - 1);
			float previousHeight = 0.0f;
			double y = 0;
			i = iterator();
			while (i.hasNext()) {
				SimpleMDCView v = i.next();
				v.getPosition().y = y;
				y += v.getHeight() + interSpace;
			}
		}
	}

	/**
	 * (Re)distribute this view's subview, so that they fill the available
	 * horizontal space. the subview's scales and shape doesn't change
	 */

	public void distributeHorizontally() {
		int n = getNumberOfSubviews();
		if (n > 1) {
			// Compute the available space.
			// Space left will be distributed :
			float minW = getSumOfSubViewsWidths();
			float interSpace = (getInternalWidth() - minW) / (n - 1);
			ViewIterator i = iterator();
			if (direction.isLeftToRight()) {
				double x = 0;
				while (i.hasNext()) {
					SimpleMDCView v = i.next();
					v.resetPos();
					v.getPosition().x = 0;
					x += v.getWidth() + interSpace;
				}
			}
		}
	}

	/**
	 * @param skip
	 *            skip betweens subviews. Distribute subviews vertically,
	 *            beginning from the top. The subview's scales and shape doesn't
	 *            change, and they don't fill the available space.
	 */

	public void stackTop(float skip) {
		ViewIterator i = iterator();
		double y = 0;
		while (i.hasNext()) {
			SimpleMDCView v = i.next();
			v.resetPos();
			v.getPosition().y = y;
			y += v.getHeight() + skip;
		}
		fitToSubViews();
	}

	/**
	 * gets the highest subview's height.
	 * 
	 * @return float
	 */
	public float getMaximalHeightOfSubView() {
		ViewIterator i;
		float result = 0f;
		i = iterator();
		while (i.hasNext()) {
			SimpleMDCView subview = i.next();
			if (result < subview.getHeight())
				result = subview.getHeight();
		}
		return result;
	}

	/**
	 * gets the widest subview's width.
	 * 
	 * @return float
	 */
	public float getMaximalWidthOfSubView() {
		ViewIterator i;
		float result = 0f;
		i = iterator();
		while (i.hasNext()) {
			SimpleMDCView subview = i.next();
			if (result < subview.getWidth())
				result = subview.getWidth();
		}
		return result;
	}

	/**
	 * @return float
	 */
	public float getSumOfSubViewsHeights() {
		ViewIterator i;
		float result = 0f;
		i = iterator();
		while (i.hasNext()) {
			SimpleMDCView subview = i.next();
			result += subview.getHeight();
		}
		return result;
	}

	/**
	 * @return float
	 */
	public float getSumOfSubViewsWidths() {
		ViewIterator i;
		float result = 0f;
		i = iterator();
		while (i.hasNext()) {
			SimpleMDCView subview = i.next();
			result += subview.getWidth();
		}
		return result;
	}

	/**
	 * Ensures the height for this view is lesser than maxHeight, by setting the
	 * scale accordingly if necessary.
	 * 
	 * @param maxHeight
	 *            the maximum height for this view element. 0 means no limit.
	 * 
	 */
	public void scaleForMaxHeight(float maxHeight) {
		if (maxHeight == 0f)
			return;
		if (getHeight() > maxHeight) {
			float sc = maxHeight / getHeight();
			reScale(sc);
		}
	}

	/**
	 * Ensures the width for this view is lesser than maxWidth, by setting the
	 * scale accordingly if necessary.
	 * 
	 * @param maxWidth
	 *            the maximum width for this view element. 0 means no limit.
	 * 
	 */
	public void scaleForMaxWidth(float maxWidth) {
		if (maxWidth == 0f)
			return;
		if (getWidth() > maxWidth) {
			float sc = maxWidth / getWidth();
			reScale(sc);
		}
	}

	/**
	 * Method scaleSubViewsToWidth.
	 * 
	 * @param maxWidth :
	 *            the maximum width allowed for the subviews
	 */

	public void scaleSubViewsToWidth(float maxWidth) {
		ViewIterator i = iterator();
		while (i.hasNext()) {
			SimpleMDCView subView = i.next();
			subView.scaleForMaxWidth(maxWidth);
		}
	}

	public void scaleSubViewToHeight(float maxHeight) {
		ViewIterator i = iterator();
		while (i.hasNext()) {
			SimpleMDCView subView = i.next();
			subView.scaleForMaxHeight(maxHeight);
		}
	}

	/**
	 * stick the subviews to one side of this view.
	 * 
	 * @param side
	 *            one of RelativePosition.SOUTH, WEST, NORTH, EAST.
	 */
	public void stickSubViewsTo(ViewSide side) {
		ViewIterator i = iterator();

		while (i.hasNext()) {
			SimpleMDCView subView = (SimpleMDCView) i.next();

			switch (side.getId()) {
			case ViewSide.BOTTOM.getId():
				subView.getPosition().y = getInternalHeight()
						- subView.getHeight();
				break;
			case ViewSide.TOP.getId():
				subView.getPosition().y = 0;
				break;
			case ViewSide.END.getId():
				subView.getPosition().x = getWidth() - subView.getWidth();
				break;
			case ViewSide.START.getId():
				subView.getPosition().x = 0;
				break;
			}
		}
	}

	/**
	 * @param i
	 * @return the subview number i.
	 */
	public SimpleMDCView getSubView(int i) {
		return (SimpleMDCView) subViews.get(i);
	}

	/**
	 * Suppresses all subviews for this view.
	 */
	public void clear() {
		for (int i = 0; i < getSubViews().size(); i++) {
			getSubView(i).parent = null;
		}
		getSubViews().clear();
	}

	/**
	 * scale the view horizontally, so that is content fills the width passed as
	 * argument. The vertical scale remains unchanged.
	 * 
	 * @param w :
	 *            the target width
	 */
	public void scaleToWidth(float w) {
		if (getInternalWidth() != 0)
			xScale = w / getInternalWidth();
		else
			xScale = 1;
	}

	/**
	 * Compute the dimensions of this view so that it contains exactly its
	 * subviews.
	 */
	public void fitToSubViews() {
		width = 0f;
		height = 0f;
		float x = 0f;
		float y = 0f;
		Rectangle2D area = null;

		if (getNumberOfSubviews() == 0) {
			width = 0;
			height = 0;
		}

		/*
		 * The following algorithm is independant of the actual view
		 * orientation.
		 */

		// Compute the actual view area
		ViewIterator it = iterator();
		while (it.hasNext()) {
			SimpleMDCView subView = it.next();
			Rectangle2D subViewArea = new Rectangle2D.Double(subView
					.getPosition().x, subView.getPosition().y, subView
					.getWidth(), subView.getHeight());
			if (area == null)
				area = subViewArea;
			else
				area.add(subViewArea);
		}

		// Now, bring the origin to 0,0.
		it = iterator();
		while (it.hasNext()) {
			SimpleMDCView subView = it.next();
			subView.getPosition().x-= area.getMinX();
			subView.getPosition().y-= area.getMinY();
		}

		width = (float) area.getWidth() * xScale;
		height = (float) area.getHeight() * yScale;
	}

	/**
	 * @return the last subView.
	 */
	public SimpleMDCView getLastSubView() {
		if (getNumberOfSubviews() > 0)
			return getSubView(getNumberOfSubviews() - 1);
		else
			return null;
	}

	/**
	 * @param idx
	 */
	public void remove(int idx) {
		SimpleMDCView subv = getSubView(idx);
		subv.parent = null;
		subViews.remove(idx);
	}

	public void remove(int a, int b) {
		for (int i = b - 1; i >= a; i--) {
			SimpleMDCView subv = getSubView(i);
			subv.parent = null;
			subViews.remove(i);
		}
	}

	/**
	 * @param i
	 * @param subv
	 */
	public void replaceSubView(int i, SimpleMDCView subv) {
		subViews.set(i, subv);
		subv.parent = this;
	}

	/**
	 * resets a view's dimensions, scale, internal starting point... doesn't
	 * modify the external position information.
	 */
	public void reset() {
		setWidth(0f);
		setHeight(0f);
		setModel(model);
		xScale = 1.0F;
		yScale = 1.0F;
		angle = 0f;

		height = 0f;
		width = 0f;
		affineTransform = null;
		resetPos();
	}

	/**
	 * Sets the height of all subviews of this view to the view's height, if
	 * possible.
	 */
	public void setSubViewsToHeight() {
		float tmph = getInternalHeight();
		for (ViewIterator it = iterator(); it.hasNext();) {
			SimpleMDCView subv = it.next();
			if (subv.getModel() instanceof SubCadrat) {
				subv.setHeight(tmph);
				subv.distributeFromTopToBottom();
			}
		}
		stickSubViewsTo(ViewSide.BOTTOM);
	}

	/**
	 * Uniformally scale the view so that it fit inside the desired dimensions.
	 * 
	 * @param wantedWidth
	 * @param wantedHeight
	 */
	public void scaleToFitDimensions(double wantedWidth, double wantedHeight) {
		double zoneScaleW = 1;
		double zoneScaleH = 1;
		if (getWidth() != 0)
			zoneScaleW = wantedWidth / getWidth();
		if (getHeight() != 0)
			zoneScaleH = wantedHeight / getHeight();
		xScale = yScale = (float) Math.min(zoneScaleH, zoneScaleW);
	}

}