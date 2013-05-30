package org.qenherkhopeshef.viewToolKit.drawing.event;

public interface DrawingEventVisitor {
	void visitCompositeEvent(CompositeEvent ev);
	void visitPropertyChangedEvent(PropertyChangedEvent ev);
	void visitElementAddedEvent(ElementAdded elementAdded);
	void visitElementRemoved(ElementRemoved elementRemoved);
	void visitDrawingChangedEvent(DrawingChangedEvent drawingChangedEvent);
	void visitCursorChangedEvent(CursorChangedEvent CursorChangedEvent);
}
