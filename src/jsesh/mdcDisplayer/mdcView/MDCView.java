package jsesh.mdcDisplayer.mdcView;

import java.awt.geom.AffineTransform;
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
 * element.
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
 * views are intended to be relatively independant of one another. Insertion of
 * a new view should cause little modification. For this reason, position is
 * normally expressed relatively. Normally, x and y for a view are expressed
 * relatively to the previous view.
 * 
 * <p>
 * The key to view positioning is the class RelativePosition. A RelativePosition
 * describes a move which can be relative to the current reference point or a
 * position in this view's container.
 * 
 * <p>
 * When drawing subviews, the system works this way :
 * <ul>
 * <li>the <b>reference point </b> is set from the container view's
 * firstSubViewPosition.</li>
 * 
 * <li>then for each subview subv, the actual drawing reference is computed by
 * using subv's startPoint (which is also a RelativePosition). This moves the
 * current <b>reference point </b>;</li>
 * 
 * <li>then the subview's dx,dy couple is used to draw the subview relatively
 * to the current <b>reference point </b>. this has no influence on the
 * <b>reference point </b>. In other terms, it has no influence at all on the
 * position of the <em>next</em> subview. It has an influence on the way this
 * view is drawn, but that's all. Typically, dx and dy should be used for fine
 * positioning of a view. For instance, as cartouches are larger than normal
 * cadrat, dx,dy is used to start drawing the cartouche a little above the
 * normal first position.</li>
 * 
 * <li>then, the <b>reference point </b> is moved according to the current
 * view's nextViewPosition. <em>nextViewPosition</em> influences the next view
 * position, but has <em>no influence</em> on the current view.</li>
 * </ul>
 * <p>
 * In general, the position of the next view will be computed from the width of
 * the current view ; so the information will be in nextViewPosition.
 * 
 * <p>
 * For page breaks, if we want to draw a line, the page break's view position
 * will be relative to the container view, not to the previous view. So the
 * previous view will move the reference point, but the page break will move it
 * to an absolute position within the container with its startPoint. It will
 * also be able to move the next element one line down, using its
 * nextViewPosition.
 * 
 * <p>
 * any view contains the following information :
 * 
 * <p>
 * Scaling and dimensions : scaling and dimensions are really two different
 * things in this class. An important thing to note is that
 * <em> a view's dimensions are quite independant of those of its subviews.</em> So,
 * scaling a view won't modify its width or height. If one wants a view to be just large
 * enough for its subviews, one should call fitToSubViews(), <em>after all subviews have been placed and computed</em>.
 * If some subview is modified thereafter, fitToSubViews() should be called once more.
 * <p>
 * This file is free Software under the LGPL. (c) Serge Rosmorduc
 * 
 * @author rosmord
 * @see RelativePosition
 */

public class MDCView extends Observable implements Observer {

	// ATTRIBUTES.

	/**
	 * Rotation angle of this model.
	 */
	private float angle;

	/**
	 * Should this view be expanded to fill its container if needed ?
	 */
	private boolean fillX, fillY;

	/**
	 * The underlying model
	 */
	private ModelElement model;

	/**
	 * Parent view, if any;
	 */

	private MDCView parent;

	/**
	 * subviews, if any
	 */

	private List subViews;

	/**
	 * The position of the first subview for this view.
	 */
	private RelativePosition firstSubViewPosition;

	/**
	 * The position of the view which follows this one.
	 */

	private RelativePosition nextViewPosition;

	/**
	 * reference point for the <em>next view</em>, if it is relative to the
	 * current one.
	 * 
	 * <p>
	 * This allows us to cleanly separate a view's information from its
	 * neightbours'. For instance, let's consider a line made of three views in
	 * a left-to-right layout.
	 * <p>
	 * The first view, v1 will have relativeTo at WEST, and x,y at 0, and dx=
	 * v1.width() + skip. The next view, v2, will have relativeTo at PREVIOUS,
	 * and dx= v2.width() + skip. The last view, v3, will have relativeTo at
	 * PREVIOUS, dx= v3.width() + skip. The view for the lineBreak will be
	 * invisible, and have relativeTo at WEST, x and dx at 0, y at lineHeight.
	 * 
	 * x and y are mostly to be used for absolute positioning (relativeTo at
	 * NORTH | WEST)
	 *  
	 */

	/**
	 * This view's displacement relative to the reference point.
	 */

	private float dx, dy;

	/**
	 * size of this view (seen from outside).
	 */

	private float width, height;

	/**
	 * Position of this view relative to the previous reference point, expressed
	 * in units of the enclosing view. x,y is exactly the position of the upper
	 * left point of the view.
	 */

	// private float x, y;
	private RelativePosition startPoint;

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

	//**************************************************************************************************
	// BASIC METHODS

	/**
	 * Constructor for ContainerView.
	 * @param model
	 */
	public MDCView(ModelElement model) {
		setModel(model);
		xScale = 1.0F;
		yScale = 1.0F;
		angle = 0f;
		fillX = false;
		fillY = false;
		height = 0f;
		width = 0f;
		// margins = null;
		subViews = null;
		affineTransform = null;
		firstSubViewPosition = null;
		startPoint = new RelativePosition(0, RelativePosition.PREVIOUS, 0,
				RelativePosition.PREVIOUS);
		nextViewPosition = new RelativePosition(0, RelativePosition.PREVIOUS,
				0, RelativePosition.PREVIOUS);
		parent = null;
		resetPos();
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
	public MDCView getParent() {
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
	 * Returns the position of the top-left origin of this view's drawing,
	 * relative to the current reference point.
	 * 
	 * @return relative start position.
	 */
	public RelativePosition getStartPoint() {
		return startPoint;
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
	 * Returns the fillX.
	 * 
	 * @return boolean
	 */
	public boolean isFillX() {
		return fillX;
	}

	/**
	 * Returns the fillY.
	 * 
	 * @return boolean
	 */
	public boolean isFillY() {
		return fillY;
	}

	/**
	 * @return position of first subview.
	 */
	public RelativePosition getFirstSubViewPosition() {
		if (firstSubViewPosition == null) {
			firstSubViewPosition = new RelativePosition(0,
					RelativePosition.WEST, 0, RelativePosition.NORTH);
		}
		return firstSubViewPosition;
	}

	/**
	 * @return position of next view.
	 */
	public RelativePosition getNextViewPosition() {
		return nextViewPosition;
	}

	/**
	 * @param position
	 */
	public void setFirstSubViewPosition(RelativePosition position) {
		firstSubViewPosition = position;
	}

	/**
	 * @param position
	 */
	public void setNextViewPosition(RelativePosition position) {
		nextViewPosition = position;
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

	/**
	 * Sets the fillX.
	 * 
	 * @param fillX
	 *            The fillX to set
	 */
	public void setFillX(boolean fillX) {
		affineTransform = null;
		this.fillX = fillX;
	}

	/**
	 * Sets the fillY.
	 * 
	 * @param fillY
	 *            The fillY to set
	 */
	public void setFillY(boolean fillY) {
		affineTransform = null;
		this.fillY = fillY;
	}

	// Subview manipulation

	/**
	 * Add a subview to a view.
	 * 
	 * @param subview
	 */

	public void add(MDCView subview) {
		getSubViews().add(subview);
		subview.parent = this;
	}

	/**
	 * @param i
	 * @param subView
	 */
	public void addAt(int i, MDCView subView) {
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
	public MDCView getFirstSubView() {
		return (MDCView) getSubViews().get(0);
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
		//if (getStartPoint().getDx() == 0 && getStartPoint().getDy() == 0 &&
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
	 * Resets all information position for this view to 0.
	 *  
	 */
	public void resetPos() {
		RelativePosition p = getNextViewPosition();
		p.setDx(0);
		p.setDy(0);
		p.setXAnchor(RelativePosition.PREVIOUS);
		p.setYAnchor(RelativePosition.PREVIOUS);
		p = getStartPoint();
		p.setDx(0);
		p.setDy(0);
		p.setXAnchor(RelativePosition.PREVIOUS);
		p.setYAnchor(RelativePosition.PREVIOUS);
	}

	/**
	 * TODO : SEE IF THIS METHOD IS USEFUL. scale the view. This scaling is
	 * relative to the view's current dimensions, not to its absolute
	 * dimensions, for which setScale() can be used.
	 * 
	 * @param sc
	 */
	/*
	 * private void scale(float sc) { affineTransform = null; xScale = xScale *
	 * sc; yScale = yScale * sc; }
	 */

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
		b.append("start point : ");
		b.append(getStartPoint().toString());
		b.append(" next pos :");
		b.append(nextViewPosition.toString());
		b.append(" s :");
		b.append(xScale);
		b.append(" r :");
		b.append(angle);
		if (subViews != null) {
			b.append(" ");
			b.append(subViews.toString());
		}
		if (firstSubViewPosition != null)
			b.append(firstSubViewPosition.toString());
		b.append("]");
		return b.toString();
	}

	//********************************************************************************************************************
	// Auxiliary methods for manipulating views and organizing subviews.

	/**
	 * center the subviews horizontally for this view.
	 */

	public void centerSubViewHorizontally() {
		for (int i = 0; i < getNumberOfSubviews(); i++) {
			MDCView sub = getSubView(i);
			float space = (getInternalWidth() - sub.getWidth()) / 2.0f;
			sub.getStartPoint().setDx(space);
			sub.getStartPoint().setXAnchor(RelativePosition.WEST);
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
			sub.getStartPoint().setDy(space);
			sub.getStartPoint().setYAnchor(RelativePosition.NORTH);
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
			getFirstSubViewPosition().setValues(0, RelativePosition.WEST, 0,
					RelativePosition.NORTH);
			i = iterator();
			while (i.hasNext()) {
				MDCView v = i.next();
				v.resetPos();
				v.getNextViewPosition().setDy(v.getHeight() + interSpace);
			}
		}
	}

	/**
	 * (Re)distribute this view's subview, so that they fill the available
	 * horizontal space. the subview's scales and shape doesn't change
	 * 
	 * @param orientation
	 *            one of ModelELement.RIGHT_TO_LEFT or
	 *            ModelELement.LEFT_TO_RIGHT
	 *            <p>
	 *            Does nothing if there is only one subview.
	 */

	public void distributeHorizontally(TextDirection orientation) {
		int n = getNumberOfSubviews();
		if (n > 1) {
			// Compute the available space.
			// Space left will be distributed :
			float minW = getSumOfSubViewsWidths();
			float interSpace = (getInternalWidth() - minW) / (n - 1);
			ViewIterator i = iterator();
			if (orientation.equals(TextDirection.LEFT_TO_RIGHT)) {
				// setSubViewReference(MDCView.WEST, MDCView.NORTH);
				RelativePosition p = getFirstSubViewPosition();
				p.setValues(0f, RelativePosition.WEST, 0f,
						RelativePosition.NORTH);
				while (i.hasNext()) {
					MDCView v = i.next();
					v.resetPos();
					v.getNextViewPosition().setDx(v.getWidth() + interSpace);
				}
			} else {
				// setSubViewReference(MDCView.EAST, MDCView.NORTH);
				// setSubX(0);
				// setSubY(0);
				RelativePosition p = getFirstSubViewPosition();
				p.setValues(getInternalWidth(), RelativePosition.WEST, 0f,
						RelativePosition.NORTH);

				while (i.hasNext()) {
					MDCView v = i.next();
					v.resetPos();
					v.getStartPoint().setDx(-v.getWidth());
					v.getNextViewPosition().setValues(-interSpace, RelativePosition.PREVIOUS, 0f, RelativePosition.PREVIOUS);
				}
			}
		}
	}

	/**
	 * @param skip skip betweens subviews.
	 * Distribute subviews vertically, beginning from the top. The subview's
	 * scales and shape doesn't change, and they don't fill the available space.
	 */

	// TODO : I think I did a mistake on this one. Currently, I call it for
	// columns, which are not
	// filled !!!!
	public void stackTop(float skip) {
		RelativePosition p = getFirstSubViewPosition();
		p.setValues(0f, RelativePosition.WEST, 0f,
				RelativePosition.NORTH);
		ViewIterator i= iterator();
		while (i.hasNext()) {
			MDCView v = i.next();
			v.resetPos();
			v.getNextViewPosition().setDy(v.getHeight() + skip);
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
	 * Ensures the height for this view is lesser than maxHeight, by setting the
	 * scale accordingly if necessary.
	 * 
	 * @param maxHeight the maximum height for this view element. 0 means no limit.
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
	 * @param maxWidth the maximum width for this view element. 0 means no limit.
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
			MDCView subView = (MDCView) i.next();
			subView.scaleForMaxWidth(maxWidth);
		}
	}

	public void scaleSubViewToHeight(float maxHeight) {
		ViewIterator i = iterator();
		while (i.hasNext()) {
			MDCView subView = (MDCView) i.next();
			subView.scaleForMaxHeight(maxHeight);
		}
	}

	/**
	 * stick the subviews to one side of this view.
	 * 
	 * @param side one of RelativePosition.SOUTH, WEST, NORTH, EAST.
	 */
	public void stickSubViewsTo(int side) {
		ViewIterator i = iterator();
		RelativePosition p;

		p = getFirstSubViewPosition();

		while (i.hasNext()) {
			MDCView subView = (MDCView) i.next();

			switch (side) {
			case RelativePosition.SOUTH:
				p.setDy(-subView.getHeight());
				p.setYAnchor(side);
				break;
			case RelativePosition.NORTH:
				p.setDy(0f);
				p.setYAnchor(side);
				break;
			case RelativePosition.EAST:
				p.setDx(-subView.getWidth());
				p.setXAnchor(side);
				break;
			case RelativePosition.WEST:
				p.setDx(0f);
				p.setXAnchor(side);
				break;
			}

			p = subView.getNextViewPosition();
		}

	}


	/**
	 * @param i
	 * @return the subview number i.
	 */
	public MDCView getSubView(int i) {
		return (MDCView) subViews.get(i);
	}

	/*
	 * public void setMargins(Margins margins) { this.margins = margins; }
	 */

	/**
	 * @param position
	 */
	public void setStartPoint(RelativePosition position) {
		startPoint = position;
	}

	/**
	 * Return the x axis displacement between the reference point and the
	 * position where the drawing should start.
	 * 
	 * @return dx.
	 */
	public float getDx() {
		return dx;
	}

	/**
	 * Return the y axis displacement between the reference point and the
	 * position where the drawing should start.
	 * 
	 * @return dy
	 */
	public float getDy() {
		return dy;
	}

	/**
	 * @param f
	 */
	public void setDy(float f) {
		dy = f;
	}

	/**
	 * @param f
	 */
	public void setDx(float f) {
		dx = f;
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
	 * 
	 * TODO : MODIFY THE SUBVIEWS THEMSELVES TO BE SURE THAT THE RESULT IS SECURE
	 */
	public void fitToSubViews() {
		width = 0f;
		height = 0f;
		float x = 0f;
		float y = 0f;
		Rectangle2D area= new Rectangle2D.Double();
		
		RelativePosition prev = getFirstSubViewPosition();
		for (int i = 0; i < getNumberOfSubviews(); i++) {
			MDCView subv = getSubView(i);
			x = prev.computeXTranslation(this, x);
			y = prev.computeYTranslation(this, y);
			x = subv.getStartPoint().computeXTranslation(this, x);
			y = subv.getStartPoint().computeYTranslation(this, y);
			area.add(new Rectangle2D.Double(x+subv.getDx(),y+subv.getDy(), subv.getWidth(), subv.getHeight()));	
			prev= subv.getNextViewPosition();
		}
		// The following may be buggy if we mix subviews relative to the left and to the right side of this view.		
		width= (float) area.getWidth()*xScale;
		height= (float) area.getHeight()*yScale;
		// See all subviews. Ensures that the coordinates are correct.
		
	}

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
	 * @param idx
	 */
	public void remove(int idx) {
		MDCView subv = getSubView(idx);
		subv.parent = null;
		subViews.remove(idx);
	}

	public void remove(int a, int b) {
		for (int i = b - 1; i >= a; i--) {
			MDCView subv= getSubView(i);
			subv.parent= null;
			subViews.remove(i);
		}
	}

	/**
	 * @param i
	 * @param subv
	 */
	public void replaceSubView(int i, MDCView subv) {
		subViews.set(i, subv);
		subv.parent = this;
	}

    /**
     * resets a view's dimensions, scale, internal starting point...
     * doesn't modify the external position information.
     */
    public void reset() {
        setWidth(0f);
        setHeight(0f);        
        setModel(model);
		xScale = 1.0F;
		yScale = 1.0F;
		angle = 0f;
		fillX = false;
		fillY = false;
		height = 0f;
		width = 0f;
		affineTransform = null;
		if (firstSubViewPosition != null)
		    getFirstSubViewPosition().setValues(0,
	                RelativePosition.WEST, 0, RelativePosition.NORTH);				
		resetPos();        
    }

    /**
     * Sets the height of all subviews of this view to the view's height, if possible.
     */
    public void setSubViewsToHeight() {
        	float tmph= getInternalHeight();
        	for (ViewIterator it= iterator(); it.hasNext(); ) {
        	    MDCView subv= it.next();
        	    if (subv.getModel() instanceof SubCadrat) {
        	        subv.setHeight(tmph);
        	        subv.distributeFromTopToBottom();
        	    }
        	}
        	stickSubViewsTo(RelativePosition.SOUTH);        
    }
}