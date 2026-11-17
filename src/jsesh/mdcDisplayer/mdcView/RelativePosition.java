/*
 * Created on 7 juin 2004
 *
 * This code is published under the LGPL.
 */
package jsesh.mdcDisplayer.mdcView;

import java.awt.geom.Point2D;

/**
 * RelativePosition represent position of a view relatively to either the
 * previous view or its parent view.
 * 
 * @author rosmord
 * 
 * This code is published under the GNU LGPL.
 */
public class RelativePosition {
    //	CONSTANT DECLARATIONS

    /**
     * The view's (x,y) is relative to the right side of the containing view.
     */

    public static final byte EAST = 4;

    /**
     * The view's (x,y) is relative to the upper side of the containing view.
     */

    public static final byte NORTH = 1;

    /**
     * The view's (x,y) is relative to the previous reference point (default)
     */

    public static final byte PREVIOUS = 0;

    /**
     * The view's (x,y) is relative to the lower side of the containing view.
     */

    public static final byte SOUTH = 2;

    /**
     * The view's (x,y) is relative to the left side of the containing view.
     */

    public static final byte WEST = 8;

    // FIELDS :

    private float dx, dy;

    private int xAnchor, yAnchor;

    /**
     * Build a RelativePosition.
     * 
     * <p>
     * if anchors are set to "PREVIOUS", the corresponding dimension is relative
     * to the previous view. The other value correspond to positions relative to
     * the parent view.
     * 
     * <p>
     * The coordinate system is always oriented in the usual way : x axis
     * rightward and y axis oriented downward.
     * 
     * @param dx
     *            horizontal coordinate.
     * @param xAnchor
     *            one of WEST, EAST or PREVIOUS
     * @param dy
     *            vertical coordinate.
     * @param yAnchor
     *            one of NORTH, SOUTH or PREVIOUS
     */
    public RelativePosition(float dx, byte xAnchor, float dy, byte yAnchor) {
        setValues(dx, xAnchor, dy, yAnchor);
    }

    /**
     * Sets position to 0,0 relative to the current position.
     */
    public void clear() {
        setValues(0, PREVIOUS, 0, PREVIOUS);
    }

    /**
     * @return dx
     */
    public float getDx() {
        return dx;
    }

    /**
     * @return dy
     */
    public float getDy() {
        return dy;
    }

    /**
     * @return the XAnchor code.
     * @see RelativePosition#RelativePosition(float, byte, float, byte)
     */
    public int getXAnchor() {
        return xAnchor;
    }

    /**
     * @return the YAnchor code.
     * @see RelativePosition#RelativePosition(float, byte, float, byte)
     */
    public int getYAnchor() {
        return yAnchor;
    }

    /**
     * @param f
     */
    public void setDx(float f) {
        dx = f;
    }

    /**
     * @param f
     */
    public void setDy(float f) {
        dy = f;
    }

    /**
     * @param anchor
     *            The xAnchor to set.
     * @see RelativePosition#RelativePosition(float, byte, float, byte)
     */
    public void setXAnchor(int anchor) {
        xAnchor = anchor;
    }

    /**
     * @param anchor
     *            The yAnchor to set.
     * @see RelativePosition#RelativePosition(float, byte, float, byte)
     */
    public void setYAnchor(int anchor) {
        yAnchor = anchor;
    }

    /**
     * Sets all values for this position.
     * 
     * @param x
     * @param xAnchor
     * @param y
     * @param yAnchor
     * @see RelativePosition#RelativePosition(float, byte, float, byte)
     */
    public void setValues(float x, int xAnchor, float y, int yAnchor) {
        dx = x;
        dy = y;
        this.xAnchor = xAnchor;
        this.yAnchor = yAnchor;
    }

    /**
     * Compute the actual horizontal translation to apply to
     * <code>subView</code>.
     * <p>
     * The coordinates are relative to the <em>container view</em>.
     * @param container :
     *            the container view
     * @param currentX
     * 
     * @return new horizontal position.
     */
    public float computeXTranslation(MDCView container, float currentX) {
        switch (getXAnchor()) {
        case WEST:
            currentX = 0;
            break;
        case EAST:
            currentX = container.getInternalWidth();
            // - subView.getWidth();
            break;
        case PREVIOUS:
            // DO NOTHING
            break;
        }
        return currentX + getDx();
    }

    /**
     * Compute the actual vertical translation to apply to <code>subView</code>.
     * <p>
     * The coordinates are relative to the <em>container view</em>.
     * @param container :
     *            the container view
     * @param currentY
     * 
     * @return new vertical position.
     */

    public float computeYTranslation(MDCView container, float currentY) {
        switch (getYAnchor()) {
        case NORTH:
            currentY = 0;
            break;
        case SOUTH:
            currentY = container.getInternalHeight();
            break;
        case PREVIOUS:
            // DO NOTHING
            break;
        }
        return currentY + getDy();
    }

    /**
     * Moves currentPoint according to RelativePosition specifications.
     * @param container
     * @param currentPoint
     */

    public void translate(MDCView container, Point2D currentPoint) {
        double x = currentPoint.getX();
        double y = currentPoint.getY();
        x = computeXTranslation(container, (float) x);
        y = computeYTranslation(container, (float) y);
        currentPoint.setLocation(x, y);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return " (pos " + dx + (xAnchor == PREVIOUS ? "" : "r") + ", " + dy
                + (yAnchor == PREVIOUS ? "" : "r") + ")";
    }

    /**
     * Modify the current relativePosition, composing it with a second one.
     * @param p : a translation applied to the current relative position. p will not be modified.
     */
    public void composeWith(RelativePosition p) {
        // dx :
        switch (p.getXAnchor())
        {
        case WEST:
        case EAST:
            dx= p.dx;
            // xAnchor= p.xAnchor;
            break;            
        case PREVIOUS:
            dx+= p.dx;            
        }
        // dy :
        switch (p.getYAnchor())
        {
        case NORTH:
        case SOUTH:
            dy= p.dy;
            //yAnchor= p.yAnchor;
            break;            
        case PREVIOUS:
            dy+= p.dy;            
        }        
    }

}
