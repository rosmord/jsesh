package jsesh.mdcDisplayer.viewToolkit.drawing.event;

import jsesh.mdcDisplayer.viewToolkit.elements.GraphicalElement;

public class ElementRemoved extends DrawingEvent {

	public ElementRemoved(GraphicalElement source) {
		super(source);
	}

	@Override
	public void accept(EventVisitor eventVisitor) {
		eventVisitor.visitElementRemoved(this);
	}

}
