package jsesh.mdcDisplayer.viewToolkit.drawing.event;

import jsesh.mdcDisplayer.viewToolkit.elements.GraphicalElement;
import jsesh.mdcDisplayer.viewToolkit.elements.properties.Property;

public class PropertyChangedEvent extends DrawingEvent {

	private Property property;

	public PropertyChangedEvent(GraphicalElement source, Property property) {
		super(source);
		this.property= property;
	}

	public Property getProperty() {
		return property;
	}
	
	@Override
	public String toString() {
		return "[ev: "+ "property_change " + getSource()+ " " + property+"]";
	}
	
	@Override
	public void accept(EventVisitor eventVisitor) {
			eventVisitor.visitPropertyChangedEvent(this);
	}

}
