package jsesh.utilitySoftwares.signInfoEditor.events;

/**
 * Event on tags.
 * @author rosmord
 */
public class TagEvent extends SignInfoModelEvent {

	public TagEvent(Object source) {
		super(source);
	}

	public void accept(SignInfoModelEventVisitor signInfoModelEventVisitor) {
		signInfoModelEventVisitor.visitTagEvent(this);
	}
	
}
