package org.qenherkhopeshef.viewtoolkit.drawing.event;

import org.qenherkhopeshef.viewtoolkit.drawing.element.GraphicalElement;

public abstract class DrawingEvent {
	
	private GraphicalElement source;

	
	public DrawingEvent(GraphicalElement source) {
		super();
		this.source = source;
	}

	public GraphicalElement getSource() {
		return source;
	}
	
	abstract public void accept(DrawingEventVisitor drawingEventVisitor);

	
}
