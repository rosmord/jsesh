package jsesh.utilitySoftwares.signInfoEditor.events;

import java.util.EventObject;

/**
 * Parent class for all events which occur on a SignInfoModel.
 * Currently only implemented for creation of tags.
 * @author rosmord
 */
public abstract class SignInfoModelEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	public SignInfoModelEvent(Object source) {
		super(source);
	}

	public abstract void accept(SignInfoModelEventVisitor signInfoModelEventVisitor);

}
