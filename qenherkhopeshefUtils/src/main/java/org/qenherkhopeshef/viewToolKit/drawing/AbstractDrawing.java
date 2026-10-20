package org.qenherkhopeshef.viewToolKit.drawing;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingChangedEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingListener;
import org.qenherkhopeshef.viewToolKit.drawing.event.GraphicalElementManager;
import org.qenherkhopeshef.viewToolKit.drawing.tabularDrawing.TabularDrawing;

/// Base class for creating drawing classes.
///
/// Provides element management.
///
/// ## Points to take into account in actual classes
///
/// Implementations of [AbstractDrawing] should respect a few points:
///
/// - all methods which add an element should take care of calling "manage",
///   so that this element forwards its information to the drawing.
/// - when events occur in elements, they are managed by the
///   [#eventOccurredInDrawing(DrawingEvent)] method, which you should provide.
///   A typical implementation for simple cases would be:
///
///   ```java
///   protected void eventOccurredInDrawing(DrawingEvent ev) {
///      // a custom method to compute the new size of this drawing (if needed)
///      computeNewDrawingSize(ev);
///      // pass the event to our listeners.
///      fireDrawingEvent(ev);
///   }
///   ```
///
///   In many cases (for instance if the drawing is a text), a change in a
///   drawing element will cause a re-computation of the drawing layout, which
///   will, in turn, modify the position of other elements. A typical
///   [#eventOccurredInDrawing(DrawingEvent)] method in this case would be:
///
///   ```java
///   protected void eventOccurredInDrawing(DrawingEvent ev) {
///      // Avoid being called when in layout (else, infinite recursion !)
///      if (!beingChanged) {
///        beingChanged = true;
///        // Find the culprit, and re-layout...
///        relayout(ev.getSource());
///        beingChanged = false;
///        fireDrawingEvent(ev);
///      }
///   }
///   ```
///
///   (taken from the code of [TabularDrawing]; note that `beingChanged` and
///   `relayout()` are not provided by [AbstractDrawing])
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public abstract class AbstractDrawing implements Drawing {

	private LinkedHashSet<DrawingListener> listeners= new LinkedHashSet<DrawingListener>();

	/**
	 * Decorations set by {@link #setDecorations(Collection)}.
	 */
	private List<GraphicalElement> decorations = List.of();
	
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
		// Iterate on a copy: a listener might remove itself.
		for (DrawingListener l: new ArrayList<DrawingListener>(listeners)) {
			l.drawingChanged(e);
		}
	}
	
	public Collection<GraphicalElement> getElementsAt(Point2D point2D) {
		Rectangle2D r= new Rectangle2D.Double(point2D.getX(), point2D.getY(), 1,1);
		return getElementsInZone(r);
	}

    public Collection<GraphicalElement> getDecorations() {
        return decorations;
    }

    /// Sets "meaningless" decoration elements (selection handles, rubber bands...).
    ///
    /// They are drawn over the drawing elements, are not managed by the
    /// drawing and are not returned by [#getElementsInZone(Rectangle2D)].
    ///
    /// @param newDecorations the new decorations, in drawing order.
    public void setDecorations(Collection<? extends GraphicalElement> newDecorations) {
        decorations = List.copyOf(newDecorations);
        fireDrawingEvent(new DrawingChangedEvent());
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
