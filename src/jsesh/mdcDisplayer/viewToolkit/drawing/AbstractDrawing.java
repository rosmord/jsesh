package jsesh.mdcDisplayer.viewToolkit.drawing;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;

import jsesh.mdcDisplayer.viewToolkit.drawing.event.DrawingEvent;
import jsesh.mdcDisplayer.viewToolkit.drawing.event.DrawingListener;
import jsesh.mdcDisplayer.viewToolkit.drawing.event.GraphicalElementManager;
import jsesh.mdcDisplayer.viewToolkit.elements.GraphicalElement;


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
	protected abstract void eventOccurrentInDrawing(DrawingEvent ev);
	
	private class DrawingElementManager implements GraphicalElementManager {

		public void eventOccurred(DrawingEvent ev) {
			eventOccurrentInDrawing(ev);
		}
	}
}
