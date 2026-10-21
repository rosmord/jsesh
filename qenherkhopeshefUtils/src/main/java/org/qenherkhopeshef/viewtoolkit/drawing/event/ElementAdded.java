package org.qenherkhopeshef.viewtoolkit.drawing.event;

import org.qenherkhopeshef.viewtoolkit.drawing.element.GraphicalElement;

public class ElementAdded extends DrawingEvent {

	public ElementAdded(GraphicalElement source) {
		super(source);
	}

	@Override
	public String toString() {
		return "[ev: "+ "added" + getSource()+ "]";
	}
	
	@Override
	public void accept(DrawingEventVisitor drawingEventVisitor) {
			drawingEventVisitor.visitElementAddedEvent(this);
	}

}
