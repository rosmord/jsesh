package org.qenherkhopeshef.viewToolKit.drawing.event;

import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;

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
