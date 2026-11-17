/*
 * Created on 7 juin 2004
 *
 * This code is published under the LGPL.
 */
package jsesh.mdcDisplayer.mdcView;

import java.awt.geom.Point2D;

/**
 * RelativePosition
 * @author rosmord
 *
 *This code is published under the GNU LGPL.
 */
public class RelativePosition
{
	//	CONSTANT DECLARATIONS

	/**
	 * The view's (x,y) is relative to the right side of the containing view.
	 */

	public static final int EAST= 4;

	/**
	 * The view's (x,y) is relative to the upper side of the containing view.
	 */

	public static final byte NORTH= 1;

	/**
	 * The view's (x,y) is relative to the previous reference point (default)
	 */

	public static final byte PREVIOUS= 0;

	/**
	 * The view's (x,y) is relative to the lower side of the containing view.
	 */

	public static final byte SOUTH= 2;

	/**
	 * The view's (x,y) is relative to the left side of the containing view.
	 */

	public static final byte WEST= 8;

	// FIELDS :

	private float dx, dy;

	private int xAnchor, yAnchor;

	public RelativePosition(float dx, byte xAnchor, float dy, byte yAnchor)
	{
		setValues(dx, xAnchor, dy, yAnchor);
	}

	/**
	 * Sets position to 0,0 relative to the current position.
	 */
	public void clear() {
		setValues(0, PREVIOUS, 0, PREVIOUS);
	}
	
	/**
	 * @return
	 */
	public float getDx()
	{
		return dx;
	}

	/**
	 * @return
	 */
	public float getDy()
	{
		return dy;
	}

	/**
	 * @return
	 */
	public int getXAnchor()
	{
		return xAnchor;
	}

	/**
	 * @return
	 */
	public int getYAnchor()
	{
		return yAnchor;
	}

	/**
	 * @param f
	 */
	public void setDx(float f)
	{
		dx= f;
	}

	/**
	 * @param f
	 */
	public void setDy(float f)
	{
		dy= f;
	}

	/**
	 * @param i
	 */
	public void setXAnchor(int i)
	{
		xAnchor= i;
	}

	/**
	 * @param i
	 */
	public void setYAnchor(int i)
	{
		yAnchor= i;
	}

	/**
	 * 
	 * @param x
	 * @param xAnchor
	 * @param y
	 * @param yAnchor
	 */
	public void setValues(float x, int xAnchor, float y, int yAnchor)
	{
		dx= x;
		dy= y;
		this.xAnchor= xAnchor;
		this.yAnchor= yAnchor;
	}

	/**
	 * Compute the actual horizontal translation to apply to <code>subView</code>.
	 * <p> The coordinates are relative to the <em>container view</em>.
	 * @param container : the container view
	 * @param subView : the subview this relativePosition applies to.
	 * @return
	 */
	public float computeXTranslation(MDCView container, MDCView subView, float currentX)
	{
		switch (getXAnchor())
		{
			case WEST :
				currentX= 0;
				break;
			case EAST :
				currentX= container.getInternalWidth();
				// - subView.getWidth();
				break;
			case PREVIOUS :
				// DO NOTHING
				break;
		}
		return currentX + getDx();
	}

	/**
		 * Compute the actual vertical translation to apply to <code>subView</code>.
		 * <p> The coordinates are relative to the <em>container view</em>.
		 * @param container : the container view
		 * @param subView : the subview this relativePosition applies to.
		 * @return
		 */

	public float computeYTranslation(MDCView container, MDCView subView, float currentY)
	{
		switch (getYAnchor())
		{
			case NORTH :
				currentY= 0;
				break;
			case SOUTH :
				currentY= container.getInternalHeight() - subView.getHeight();
				break;
			case PREVIOUS :
				// DO NOTHING
				break;
		}
		return currentY + getDy();
	}
	
	/**
	 * Moves currentPoint according to RelativePosition specifications.
	 * @param container
	 * @param subView
	 * @param currentPoint
	 */
	
	public void translate(MDCView container, MDCView subView, Point2D currentPoint) {
	    double x= currentPoint.getX();
	    double y= currentPoint.getY();
	    x= computeXTranslation(container,subView, (float) x);
	    y= computeYTranslation(container,subView, (float)y);
	    currentPoint.setLocation(x,y);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return " (pos "
			+ dx
			+ (xAnchor == PREVIOUS ? "" : "r")
			+ ", "
			+ dy
			+ (yAnchor == PREVIOUS ? "" : "r")
			+ ")";
	}

}
