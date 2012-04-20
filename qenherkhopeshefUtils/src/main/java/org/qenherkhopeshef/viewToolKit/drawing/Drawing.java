package org.qenherkhopeshef.viewToolKit.drawing;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingListener;

public interface Drawing {

    /**
     * Returns all the elements in a given zone (for drawing update typically).
     * @param rectangle
     * @return
     */
	public Collection<GraphicalElement> getElementsInZone(Rectangle2D rectangle);

    /**
     * Returns the elements at a given point (for mouse click processing typically).
     * @param point2D
     * @return
     */
	public Collection<GraphicalElement> getElementsAt(Point2D point2D);

    /**
     * Returns "meaningless" decoration elements like cursors and the like.
     * (will be improved later).
     * @return
     */
    public Collection<GraphicalElement> getDecorations();

	public Rectangle2D getPreferredSize();

	/**
	 * If the drawing supports the notion of cursor, this method will return the area where the cursor is drawn.
	 * <p> This is used to move the display to the cursor in large drawings.
	 * <p> It is possible to return null if the display is supposed to be small or if there is no cursor.
	 * @return the cursor bounds, or null if no cursor is set.
	 */
	public Rectangle2D getCursorBounds();
	
	public void addDrawingListener(DrawingListener listener);

	public void remove(DrawingListener listener);

	public boolean isEmpty();
	/**
	 * Warns the listeners for this drawing that it changed. Normally called
	 * either by the drawing itself or by the modified GraphicalElements.
	 * 
	 * @param e
	 */
	public void fireDrawingEvent(DrawingEvent e);
}
