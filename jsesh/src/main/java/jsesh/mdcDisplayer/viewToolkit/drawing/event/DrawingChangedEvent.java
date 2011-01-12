package jsesh.mdcDisplayer.viewToolkit.drawing.event;


/**
 * An event which means that the drawing has been drastically modified.
 * @author rosmord
 *
 */
public class DrawingChangedEvent extends DrawingEvent {

	public DrawingChangedEvent() {
		super(null);
	}

	@Override
	public void accept(EventVisitor eventVisitor) {
		eventVisitor.visitDrawingChangedEvent(this);
	}

}
