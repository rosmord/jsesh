package org.qenherkhopeshef.viewToolKit.drawing.event;

import org.qenherkhopeshef.viewToolKit.drawing.element.CompositeElement;

/**
 * An event occurring <em>in</em> a composite.
 * @author rosmord
 *
 */
public class CompositeEvent extends DrawingEvent {
	
	private DrawingEvent childEvent;

	/**
	 * Build the event.
	 * @param composite : the container element affected by the event.
	 * @param childEvent : the inner event.
	 */
	public CompositeEvent(CompositeElement composite, DrawingEvent childEvent) {
		super(composite);
		this.childEvent = childEvent;
	}
	
	public DrawingEvent getChildEvent() {
		return childEvent;
	}
	
	@Override
	public String toString() {
		return "[ev: "+ "composite" + getSource()+ " "+ getChildEvent() +  "]";
	}

	@Override
	public void accept(DrawingEventVisitor drawingEventVisitor) {
		drawingEventVisitor.visitCompositeEvent(this);
	}
}
