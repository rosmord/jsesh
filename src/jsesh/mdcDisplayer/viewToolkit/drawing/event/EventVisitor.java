package jsesh.mdcDisplayer.viewToolkit.drawing.event;

public interface EventVisitor {
	void visitCompositeEvent(CompositeEvent ev);
	void visitPropertyChangedEvent(PropertyChangedEvent ev);
	void visitElementAddedEvent(ElementAdded elementAdded);
	void visitElementRemoved(ElementRemoved elementRemoved);
	void visitDrawingChangedEvent(DrawingChangedEvent drawingChangedEvent);
}
