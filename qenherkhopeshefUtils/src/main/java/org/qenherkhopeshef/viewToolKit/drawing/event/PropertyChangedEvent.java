package org.qenherkhopeshef.viewToolKit.drawing.event;

import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.property.Property;

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
	public void accept(DrawingEventVisitor drawingEventVisitor) {
			drawingEventVisitor.visitPropertyChangedEvent(this);
	}

}
