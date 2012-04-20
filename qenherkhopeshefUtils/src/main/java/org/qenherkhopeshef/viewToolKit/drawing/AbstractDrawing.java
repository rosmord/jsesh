package org.qenherkhopeshef.viewToolKit.drawing;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingListener;
import org.qenherkhopeshef.viewToolKit.drawing.event.GraphicalElementManager;
import org.qenherkhopeshef.viewToolKit.drawing.tabularDrawing.TabularDrawing;

/**
 * Base class for creating drawing classes.
 * <p> Provides element management.
 * <h2> Points to take into account in actual classes</h2>
 * Implementations of AbstractDrawing should respect a few points :
 * <ul>
 * <li> all method which add an element should take care of calling "manage", so that this element forwards its informations to the drawing.
 * <li> when events occur in elements, they are managed by the  {@link #eventOccurredInDrawing(DrawingEvent)} method, which you should provide.
 * A typical implementation for simple cases would be :
 * <pre>
 *protected void eventOccurredInDrawing(DrawingEvent ev) {
 *   // a custom method to compute the new size of this drawing (if needed)
 *   computeNewDrawingSize(ev);
 *   // pass the event to our listeners.
 *   fireDrawingEvent(ev);
 *}
 * </pre>
 * In many cases (for instance if the drawing is a text), a change in a drawing element will cause a re-computation of the drawing layout,
 * which will, in turn, modify the position of other elements. A typical {@link #eventOccurredInDrawing(DrawingEvent)} method in this case
 * would be : 
 * <pre>
 *protected void eventOccurredInDrawing(DrawingEvent ev) {
 *   // Avoid being called when in layout (else, infinite recursion !)
 *   if (!beingChanged) {
 *     beingChanged = true;
 *     // Find the culprit, and re-layout...
 *     relayout(ev.getSource());
 *     beingChanged = false;
 *     fireDrawingEvent(ev);
 *   }
 *}
 * </pre>
 * (taken from the code of {@link TabularDrawing})
 * (note that <code>beingChanged</code> and <code>relayout()</code> are not provided by {@link AbstractDrawing})
 * </ul>
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public abstract class AbstractDrawing implements Drawing {

	private HashSet<DrawingListener> listeners= new HashSet<DrawingListener>();
	
	/**
	 * The element which will receive and transmit all information from drawings.
	 * 
	 */
	private GraphicalElementManager graphicalElementManager= new DrawingElementManager();
	
	public void addDrawingListener(DrawingListener listener) {
		listeners.add(listener);
	}

	public void remove(DrawingListener listener) {
		listeners.remove(listener);
	}

	public void fireDrawingEvent(DrawingEvent e) {
		for (DrawingListener l: listeners) {
			l.drawingChanged(e);
		}
	}
	
	public Collection<GraphicalElement> getElementsAt(Point2D point2D) {
		Rectangle2D r= new Rectangle2D.Double(point2D.getX(), point2D.getY(), 1,1);
		return getElementsInZone(r);
	}

    public Collection<GraphicalElement> getDecorations() {
        return Collections.emptySet();
    }

    /**
	 * Links the drawing and the graphical element.
	 * Should be called by all "add" method.
	 * @param elt
	 */
	protected void manage(GraphicalElement elt) {
		elt.setManager(graphicalElementManager);
	}
	
	/**
	 * An event occurred in one of our elements.
	 * React to the event (maybe change the layout, and probably fire a drawing-level event).
	 * @param ev
	 */
	protected abstract void eventOccurredInDrawing(DrawingEvent ev);
	
	private class DrawingElementManager implements GraphicalElementManager {

		public void eventOccurred(DrawingEvent ev) {
			eventOccurredInDrawing(ev);
		}
	}
}
