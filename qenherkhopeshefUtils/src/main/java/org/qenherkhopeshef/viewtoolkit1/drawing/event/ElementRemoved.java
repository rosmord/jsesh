package org.qenherkhopeshef.viewtoolkit.drawing.event;

import org.qenherkhopeshef.viewtoolkit.drawing.element.GraphicalElement;

public class ElementRemoved extends DrawingEvent {

	public ElementRemoved(GraphicalElement source) {
		super(source);
	}

	@Override
	public void accept(DrawingEventVisitor drawingEventVisitor) {
		drawingEventVisitor.visitElementRemoved(this);
	}

}
