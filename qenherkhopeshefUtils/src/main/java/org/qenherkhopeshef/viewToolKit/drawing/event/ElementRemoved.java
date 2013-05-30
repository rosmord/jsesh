package org.qenherkhopeshef.viewToolKit.drawing.event;

import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;

public class ElementRemoved extends DrawingEvent {

	public ElementRemoved(GraphicalElement source) {
		super(source);
	}

	@Override
	public void accept(DrawingEventVisitor drawingEventVisitor) {
		drawingEventVisitor.visitElementRemoved(this);
	}

}
