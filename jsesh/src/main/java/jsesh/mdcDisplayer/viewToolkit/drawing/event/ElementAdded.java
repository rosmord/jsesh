package jsesh.mdcDisplayer.viewToolkit.drawing.event;

import jsesh.mdcDisplayer.viewToolkit.elements.GraphicalElement;

public class ElementAdded extends DrawingEvent {

	public ElementAdded(GraphicalElement source) {
		super(source);
	}

	@Override
	public String toString() {
		return "[ev: "+ "added" + getSource()+ "]";
	}
	
	@Override
	public void accept(EventVisitor eventVisitor) {
			eventVisitor.visitElementAddedEvent(this);
	}

}
