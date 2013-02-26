package jsesh.mdcDisplayer.mdcView;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.SubCadrat;
import jsesh.mdcDisplayer.drawingElements.ViewBox;

/**
 * Base class for all view elements. A view element is a view of an MDC model
 * element.
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
 * One might think of a subview which will be partly outside the view.
 * 
 * The scale is in fact the <em>Inner scale</em> of the view, i.e. the scale
 * used to draw elements inside the view (and the subviews). So, scaling a view
 * won't always modify its width or height. If one wants a view to be just large
 * enough for its subviews, one should call fitToSubViews(),
 * <em>after all subviews have been placed and computed</em>. If some subview is
 * modified thereafter, fitToSubViews() should be called once more.
 * 
 * <p>
 * deltaBaseX and deltaBaseY are modifiers which describe the difference between
 * the "standard" base lines and column and those for this view. Normally, the
 * tops of the views are aligned in horizontal text. Note that those values are
 * used for computing the position of the view. Once this is done, the position
 * is completely defined.
 * 
 * <p>
 * This file is free Software under the LGPL. (c) Serge Rosmorduc
 * 
 * <h3>Use of views to represent pages:</h3>
 * Note that views can be used to represent pages. It's quite easy to have a
 * whole document view (the size of a page), and each page is itself a view.
 * Now, if we want to draw a particular page, the page "n" will be the "nth"
 * page of the view. The software must of course be aware of this.
 * <p>
 * for drawing a whole document as <em>one</em> drawing, the layout algorithm
 * will do the following :
 * <ul>
 * <li>if there is no page specification, simply build a view of the whole
 * document
 * <li>if there is a page specification, build a global view of the document,
 * but lay out the subviews one above the other (and not one following the
 * other).
 * </ul>
 * 
 * @author rosmord
 */

public class MDCView implements ViewBox {

	private static final AffineTransform IDENTITY = new AffineTransform();
	// ATTRIBUTES.

	/**
	 * Rotation angle of this model.
	 */
	private float angle;

	/**
	 * This view's displacement relative to the reference point.
	 */

	private double deltaBaseX, deltaBaseY;

	/**
	 * True if this view can be stretched to accomodate a larger width.
	 */
	private boolean xStretchable;

	/**
	 * True if this view can be stretched to accomodate a larger height.
	 */
	private boolean yStretchable;

	/**
	 * Determines the view internal coordinate system.
	 */

	private TextDirection direction;

	/**
	 * The underlying model
	 */
	private ModelElement model;

	/**
	 * Parent view, if any;
	 */

	private MDCView parent;

	private MDCView next;

	private MDCView previous;

	Point2D.Double position;

	/**
	 * subviews, if any
	 */

	private List subViews;

	/**
	 * size of this view (seen from <em>outside</rm>).
	 */

	private float width, height;

	// **************************************************************************************************
	// BASIC METHODS

	private float xScale;

	private float yScale;

	/**
	 * Constructor for ContainerView.
	 * 
	 * @param model
	 */
	public MDCView(ModelElement model) {
		setModel(model);
		xScale = 1.0F;
		yScale = 1.0F;
		angle = 0f;
		height = 0f;
		width = 0f;
		subViews = null;
		// affineTransform = null;
		position = new Point2D.Double();
		direction = TextDirection.LEFT_TO_RIGHT;
		parent = null;
		previous = null;
		next = null;
		xStretchable = false;
		yStretchable = false;
	}

	/**
	 * Add a subview to a view.
	 * 
	 * @param subview
	 */

	public void add(MDCView subview) {
		addAt(getNumberOfSubviews(), subview);
	}

	/**
	 * Fix "previous" and "next" fields for a particular element. if index is
	 * out of bounds, no-op
	 * 
	 * @param index
	 */
	private void fixSlibingsAt(int index) {
		if (index < 0 || index >= getNumberOfSubviews())
			return;
		MDCView subv = getSubView(index);
		// previous element
		if (index > 0)
			subv.previous = getSubView(index - 1);
		else
			subv.previous = null;
		// next
		if (index < getNumberOfSubviews() - 1)
			subv.next = getSubView(index + 1);
		else
			subv.next = null;

	}

	/**
	 * @param i
	 * @param subView
	 */
	public void addAt(int i, MDCView subView) {
		getSubViews().add(i, subView);
		fixSlibingsAt(i - 1);
		fixSlibingsAt(i);
		fixSlibingsAt(i + 1);
		subView.parent = this;
	}

	/**
	 * center the subviews horizontally for this view.
	 */

	public void centerSubViewHorizontally() {
		for (int i = 0; i < getNumberOfSubviews(); i++) {
			MDCView sub = getSubView(i);
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
			MDCView sub = getSubView(i);
			float space = (getInternalHeight() - sub.getHeight()) / 2.0f;
			sub.getPosition().y = space;
		}
	}

	/**
	 * Suppresses all subviews for this view.
	 */
	public void clear() {
		for (int i = 0; i < getSubViews().size(); i++) {
			getSubView(i).parent = null;
			getSubView(i).next = null;
			getSubView(i).previous = null;
		}
		getSubViews().clear();
	}

	/**
	 * Utility function building the affine transform associated with the view
	 * from data such as scale, angle, x, y.
	 * 
	 * This view will be used to draw and place elements inside the view.
	 */

	private AffineTransform computeAffineTransform() {
		AffineTransform affineTransform;
		// We expect that in lots of cases, the affine transform will be
		// identity.
		// point. It will be the drawing system's job to position the correct
		// origin.
		// IMPORTANT : We suppressed all data relative to translation in this
		// method,
		// for the translation is handled before calling.
		if (xScale == 0 && yScale == 0 && angle == 0) {
			affineTransform = IDENTITY;
		} else {
			affineTransform = new AffineTransform();
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
		return affineTransform;
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

			double y = 0;
			i = iterator();
			while (i.hasNext()) {
				MDCView v = i.next();
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
			double x = 0;
			while (i.hasNext()) {
				MDCView v = i.next();
				v.resetPos();
				v.getPosition().x = x;
				x += v.getWidth() + interSpace;
			}
		}
	}

	/**
	 * Compute the dimensions of this view so that it contains exactly its
	 * subviews.
	 * <p>
	 * If doTranslate is true, the subviews will be moved; else they won't, and
	 * the view might actually be larger than needed.
	 * 
	 * @param doTranslate
	 *            if true, subviews will be translated.
	 */
	public void fitToSubViews(boolean doTranslate) {
		width = 0f;
		height = 0f;
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
			MDCView subView = it.next();
			Rectangle2D subViewArea = new Rectangle2D.Double(
					subView.getPosition().x, subView.getPosition().y,
					subView.getWidth(), subView.getHeight());
			if (area == null)
				area = subViewArea;
			else
				area.add(subViewArea);
		}

		if (doTranslate) {
			// Now, bring the origin to 0,0.
			it = iterator();
			while (it.hasNext()) {
				MDCView subView = it.next();
				subView.getPosition().x -= area.getMinX();
				subView.getPosition().y -= area.getMinY();
			}
			width = (float) area.getWidth() * xScale;
			height = (float) area.getHeight() * yScale;
		} else {
			width = (float) (area.getMaxX()) * xScale;
			height = (float) (area.getMaxY()) * yScale;
		}
	}

	/**
	 * returns the affine transformation used to draw the view's elements.
	 * 
	 * @return AffineTransform
	 */
	public AffineTransform getAffineTransform() {
		AffineTransform affineTransform = computeAffineTransform();
		/*
		 * if (affineTransform == null) computeAffineTransform();
		 */
		return affineTransform;
	}

	/**
	 * Returns the angle.
	 * 
	 * @return float
	 */
	public float getAngle() {
		return angle;
	}

	// Subview manipulation

	/**
	 * gets the horizontal relative position.
	 * <p>
	 * When computing the layout, we use the notion of current point (somehow as
	 * in HTML layout), which is where the current algorithm would want to place
	 * this view. However, some views have they own idea about their placement. For instance,
	 * latin text will not have the same vertical alignment as quadrants.  So {@link #deltaBaseX} and {@link #deltaBaseY}
	 * are somehow similar to the notion of relative position in HTML.
	 * 
	 * <p> Note that this is actually only used for latin text (as of 2013/2/25).
	 * 
	 * @return Returns the deltaBaseX.
	 */
	public double getDeltaBaseX() {
		return deltaBaseX;
	}

	/**
	 * gets the vertical relative position.
	 * 
	 * @return Returns the deltaBaseY.
	 * @see #getDeltaBaseX()
	 */
	public double getDeltaBaseY() {
		return deltaBaseY;
	}

	/**
	 * @return Returns the direction.
	 */
	public TextDirection getDirection() {
		return direction;
	}

	/**
	 * Method getFirstSubView. This method is mostly useful for view with only
	 * one subview.
	 * 
	 * @return ContainerView : the fist sub view of this view.
	 * @throws IndexOutOfBoundsException
	 *             is there is no first subview
	 */
	public MDCView getFirstSubView() {
		return (MDCView) getSubViews().get(0);
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

	// Secondary functions

	/**
	 * @return the last subView.
	 */
	public MDCView getLastSubView() {
		if (getNumberOfSubviews() > 0)
			return getSubView(getNumberOfSubviews() - 1);
		else
			return null;
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
			MDCView subview = i.next();
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
			MDCView subview = i.next();
			if (result < subview.getWidth())
				result = subview.getWidth();
		}
		return result;
	}

	/**
	 * Returns the model, if any.
	 * 
	 * @return ModelElement
	 */
	public ModelElement getModel() {
		return model;
	}

	public int getNumberOfSubviews() {
		if (subViews == null) {
			return 0;
		} else {
			return subViews.size();
		}
	}

	/**
	 * returns the view which contain this subview. Can return null, if the view
	 * has no parent.
	 * 
	 * @return parent.
	 */
	public MDCView getParent() {
		return parent;
	}

	/**
	 * @return Returns the position.
	 */
	public Point2D.Double getPosition() {
		return position;
	}

	/**
	 * @param i
	 * @return the subview number i.
	 */
	public MDCView getSubView(int i) {
		return (MDCView) subViews.get(i);
	}

	public List getSubViews() {
		if (subViews == null)
			subViews = new ArrayList();
		return subViews;
	}

	/**
	 * @return float
	 */
	public float getSumOfSubViewsHeights() {
		ViewIterator i;
		float result = 0f;
		i = iterator();
		while (i.hasNext()) {
			MDCView subview = i.next();
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
			MDCView subview = i.next();
			result += subview.getWidth();
		}
		return result;
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

	public ViewIterator iterator() {
		return new ViewIterator(this, getSubViews().listIterator());
	}

	public ViewIterator iterator(int idx) {
		return new ViewIterator(this, getSubViews().listIterator(idx));
	}

	/**
	 * @param idx
	 */
	public void remove(int idx) {
		MDCView subv = getSubView(idx);
		subv.parent = null;
		subViews.remove(idx);
		fixSlibingsAt(idx - 1);
		fixSlibingsAt(idx);
	}

	public void remove(int a, int b) {
		for (int i = b - 1; i >= a; i--) {
			MDCView subv = getSubView(i);
			subv.parent = null;
			subv.next = null;
			subv.previous = null;
			subViews.remove(i);
		}
		fixSlibingsAt(a - 1);
		fixSlibingsAt(a);
	}

	// ********************************************************************************************************************
	// Auxiliary methods for manipulating views and organizing subviews.

	/**
	 * @param i
	 * @param subv
	 */
	public void replaceSubView(int i, MDCView subv) {
		subViews.set(i, subv);
		fixSlibingsAt(i - 1);
		fixSlibingsAt(i);
		fixSlibingsAt(i + 1);
		subv.parent = this;
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
		// affineTransform = null;
		this.xScale = scale * oldXScale;
		this.yScale = scale * oldYScale;
		width = width * scale;
		height = height * scale;
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
		xStretchable = false;
		yStretchable = false;
		// affineTransform = null;
	}

	/**
	 * Resets all information position for this view to 0.
	 * 
	 */
	public void resetPos() {
		position.x = 0;
		position.y = 0;
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
	 * @param maxWidth
	 *            : the maximum width allowed for the subviews
	 */

	public void scaleSubViewsToWidth(float maxWidth) {
		ViewIterator i = iterator();
		while (i.hasNext()) {
			MDCView subView = i.next();
			subView.scaleForMaxWidth(maxWidth);
		}
	}

	public void scaleSubViewToHeight(float maxHeight) {
		ViewIterator i = iterator();
		while (i.hasNext()) {
			MDCView subView = i.next();
			subView.scaleForMaxHeight(maxHeight);
		}
	}

	/**
	 * Uniformally scale the view so that it fit inside the desired dimensions.
	 * Does nothing if the view is already small enough.
	 * 
	 * @param wantedWidth
	 * @param wantedHeight
	 */
	public void scaleToFitDimensions(double wantedWidth, double wantedHeight) {
		if (getWidth() < wantedWidth && getHeight() < wantedHeight)
			return;
		double zoneScaleW = 1;
		double zoneScaleH = 1;
		if (getWidth() != 0)
			zoneScaleW = wantedWidth / getWidth();
		if (getHeight() != 0)
			zoneScaleH = wantedHeight / getHeight();
		xScale = yScale = (float) Math.min(zoneScaleH, zoneScaleW);
		width = width * xScale;
		height = height * yScale;
	}

	/**
	 * scale the view horizontally, so that is content fills the width passed as
	 * argument. The vertical scale remains unchanged.
	 * 
	 * @param w
	 *            : the target width
	 */
	public void scaleToWidth(float w) {
		if (getInternalWidth() != 0) {
			xScale = w / getInternalWidth();
			width = w;
		} else {
			xScale = 1;
			width = w;
		}
	}

	/**
	 * Sets the angle.
	 * 
	 * @param angle
	 *            The angle to set
	 */
	public void setAngle(float angle) {
		// affineTransform = null;
		this.angle = angle;
	}

	/**
	 * Sets the horizontal relative position.
	 * 
	 * @param deltaBaseX
	 *            The deltaBaseX to set.
	 * @see #getDeltaBaseX()
	 */
	public void setDeltaBaseX(double deltaBaseX) {
		this.deltaBaseX = deltaBaseX;
	}

	/**
	 * Sets the vertical relative position.
	 * 
	 * @param deltaBaseY
	 *            The deltaBaseY to set.
	 * @see #getDeltaBaseX()
	 */
	public void setDeltaBaseY(double deltaBaseY) {
		this.deltaBaseY = deltaBaseY;
	}

	/**
	 * @param direction
	 *            The direction to set.
	 */
	public void setDirection(TextDirection direction) {
		this.direction = direction;
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
	 * Sets the model.
	 * 
	 * @param model
	 *            The model to set
	 */
	public void setModel(ModelElement model) {
		this.model = model;
	}

	/**
	 * @param position
	 *            The position to set.
	 */
	public void setPosition(Point2D.Double position) {
		this.position = position;
	}

	/**
	 * Sets the height of all subviews of this view to the view's height, if
	 * possible.
	 */
	public void setSubViewsToHeight() {
		float tmph = getInternalHeight();
		for (ViewIterator it = iterator(); it.hasNext();) {
			MDCView subv = it.next();
			if (subv.getModel() instanceof SubCadrat) {
				subv.setHeight(tmph);
				subv.distributeFromTopToBottom();
			}
		}
		stickSubViewsTo(ViewSide.BOTTOM);
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
	 * Sets the xScale. This is the scale applied to the internal elements of
	 * this view. Doesn't change the view's external dimensions.
	 * 
	 * @param xScale
	 *            The xScale to set
	 */
	public void setXScale(float xScale) {
		// affineTransform = null;
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
		// affineTransform = null;
		this.yScale = yScale;
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
			MDCView v = i.next();
			v.resetPos();
			v.getPosition().y = y;
			y += v.getHeight() + skip;
		}
		fitToSubViews(true);
	}

	/**
	 * stick the subviews to one side of this view.
	 * 
	 * @param side
	 *            a ViewSide
	 * 
	 */
	public void stickSubViewsTo(ViewSide side) {
		ViewIterator i = iterator();

		while (i.hasNext()) {
			MDCView subView = (MDCView) i.next();

			if (side.equals(ViewSide.BOTTOM)) {
				subView.getPosition().y = getInternalHeight()
						- subView.getHeight();
			} else if (side.equals(ViewSide.TOP)) {
				subView.getPosition().y = 0;
			} else if (side.equals(ViewSide.END)) {
				subView.getPosition().x = getWidth() - subView.getWidth();
			} else {
				// viewside.start
				subView.getPosition().x = 0;
			}
		}
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
		b.append("direction : " + direction.toString());
		b.append(" s :");
		b.append(xScale);
		b.append(" r :");
		b.append(angle);
		if (deltaBaseX != 0) {
			b.append(" deltax: ");
			b.append(deltaBaseX);
		}
		if (deltaBaseY != 0) {
			b.append(" deltay: ");
			b.append(deltaBaseY);
		}
		if (subViews != null) {
			b.append(" ");
			b.append(subViews.toString());
		}
		b.append("]");
		return b.toString();
	}

	/**
	 * Returns the next view on the same level, if any ; else returns null.
	 * 
	 * @return a view
	 */
	public MDCView getNext() {
		return next;
	}

	/**
	 * Returns the previous view on the same level, if any; else returns null.
	 * 
	 * @return Returns a view
	 */
	public MDCView getPrevious() {
		return previous;
	}

	/**
	 * Tells if the next slibing view, as placed, can be considered as
	 * horizontally adjacent to this one.
	 * <p>
	 * Useful for shading and the like.
	 * 
	 * @return a boolean
	 */
	public boolean nextIsHorizontallyAdjacent() {
		return (next != null && next.getPosition().y < getPosition().y
				+ getHeight());
	}

	/**
	 * True if this view can be stretched to accomodate a larger width (default
	 * : false).
	 * 
	 * @return Returns the xStretchable.
	 */
	public boolean isXStretchable() {
		return xStretchable;
	}

	/**
	 * @param stretchable
	 *            The xStretchable to set.
	 */
	public void setXStretchable(boolean stretchable) {
		xStretchable = stretchable;
	}

	/**
	 * True if this view can be stretched to accomodate a larger height (default
	 * : false).
	 * 
	 * @return Returns the yStretchable.
	 */
	public boolean isYStretchable() {
		return yStretchable;
	}

	/**
	 * @param stretchable
	 *            The yStretchable to set.
	 */
	public void setYStretchable(boolean stretchable) {
		yStretchable = stretchable;
	}

}