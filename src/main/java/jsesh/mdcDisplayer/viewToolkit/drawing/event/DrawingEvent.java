package jsesh.mdcDisplayer.viewToolkit.drawing.event;

import jsesh.mdcDisplayer.viewToolkit.elements.GraphicalElement;


public abstract class DrawingEvent {
	
	private GraphicalElement source;

	
	public DrawingEvent(GraphicalElement source) {
		super();
		this.source = source;
	}

	public GraphicalElement getSource() {
		return source;
	}
	
	abstract public void accept(EventVisitor eventVisitor);

	
}
