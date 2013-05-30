package org.qenherkhopeshef.viewToolKit.drawing.event;

public abstract class DrawingEventAdapter implements DrawingEventVisitor {

	/**
	 * Default (no-op) method called by all other visitor methods.
	 * <p> If you want a default behaviour for the cases you don't handle, just
	 * redefine this method.
	 * @param ev
	 */
	public void visitDefault(DrawingEvent ev) {
		// NO-OP
	}
	
	@Override
	public void visitCompositeEvent(CompositeEvent ev) {
		visitDefault(ev);
	}

	@Override
	public void visitPropertyChangedEvent(PropertyChangedEvent ev) {
		visitDefault(ev);
	}

	@Override
	public void visitElementAddedEvent(ElementAdded elementAdded) {
		visitDefault(elementAdded);
	}

	@Override
	public void visitElementRemoved(ElementRemoved elementRemoved) {
		visitDefault(elementRemoved);
	}

	@Override
	public void visitDrawingChangedEvent(DrawingChangedEvent drawingChangedEvent) {
		visitDefault(drawingChangedEvent);
	}

	@Override
	public void visitCursorChangedEvent(CursorChangedEvent cursorChangedEvent) {
		visitDefault(cursorChangedEvent);
	}

}
