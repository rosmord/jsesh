package org.qenherkhopeshef.viewtoolkit1.drawing.event;


public class CursorChangedEvent extends DrawingEvent {


	public CursorChangedEvent() {
		super(null);
	}

	@Override
	public void accept(DrawingEventVisitor drawingEventVisitor) {
		drawingEventVisitor.visitCursorChangedEvent(this);
	}
		

}
