package jsesh.search.backingSupport;

import jsesh.model.MDCPosition;

/**
 * A record of the occurrence of a sign at a certain position in a text.
 * @author rosmord
 */
public class HieroglyphOccurrence {
	private final String code;
	private final MDCPosition position;

	public HieroglyphOccurrence(String code, MDCPosition position) {
		this.code = code;
		this.position = position;
	}

	public String getCode() {
		return code;
	}

	public MDCPosition getPosition() {
		return position;
	}

	public boolean hasCode(String code) {
		return this.code.equals(code);
	}

	@Override
	public String toString() {
		return "(" + code + ", " + position.getIndex() + ')';
	}
}
