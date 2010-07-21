package jsesh.mdcDisplayer.viewToolkit.drawing;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import jsesh.mdcDisplayer.viewToolkit.drawing.event.DrawingEvent;
import jsesh.mdcDisplayer.viewToolkit.drawing.event.DrawingListener;
import jsesh.mdcDisplayer.viewToolkit.elements.GraphicalElement;

public interface Drawing {
	public Collection<GraphicalElement> getElementsInZone(Rectangle2D rectangle);

	public Collection<GraphicalElement> getElementsAt(Point2D point2D);

	public Rectangle2D getPreferredSize();

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
