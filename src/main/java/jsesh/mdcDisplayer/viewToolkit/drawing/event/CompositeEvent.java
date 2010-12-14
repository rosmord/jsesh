package jsesh.mdcDisplayer.viewToolkit.drawing.event;

import jsesh.mdcDisplayer.viewToolkit.elements.CompositeElement;


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
	public void accept(EventVisitor eventVisitor) {
		eventVisitor.visitCompositeEvent(this);
	}
}
