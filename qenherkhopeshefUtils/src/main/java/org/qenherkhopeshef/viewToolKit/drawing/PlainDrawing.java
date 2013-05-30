package org.qenherkhopeshef.viewToolKit.drawing;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingEvent;

/**
 * A very plain and not optimized drawing class.
 * <p>
 * Use for trying ideas and get something running as soon as possible. Not
 * suited for anything but very very simple drawings.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 * 
 */
public class PlainDrawing extends AbstractDrawing {

	private List<GraphicalElement> elements = new ArrayList<GraphicalElement>();
	private Rectangle2D drawingSize;

	@Override
	public Collection<GraphicalElement> getElementsInZone(Rectangle2D rectangle) {
		List<GraphicalElement> result = new ArrayList<GraphicalElement>();
		for (GraphicalElement elt : elements) {
			if (elt.getDecoratedBounds().intersects(rectangle))
				result.add(elt);
		}
		return result;
	}

	@Override
	public Rectangle2D getPreferredSize() {
		return getDrawingSize();
	}

	private Rectangle2D getDrawingSize() {
		if (drawingSize == null) {
			if (elements.isEmpty()) {
				// We should do something more intelligent there.
				drawingSize = new Rectangle2D.Double();
			} else {
				for (GraphicalElement elt : elements) {
					if (drawingSize == null)
						drawingSize = elt.getDecoratedBounds();
					else
						drawingSize.add(elt.getDecoratedBounds());
				}
			}
		}
		return drawingSize;
	}

	@Override
	public Rectangle2D getCursorBounds() {
		return null;
	}

	@Override
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	public void addElement(GraphicalElement elt) {
		elements.add(elt);
		this.manage(elt);
	}
	
	@Override
	protected void eventOccurredInDrawing(DrawingEvent ev) {
		drawingSize= null;
		fireDrawingEvent(ev);
	}

}
