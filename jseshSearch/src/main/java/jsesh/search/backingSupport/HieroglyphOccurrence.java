package jsesh.search.backingSupport;

import java.util.Set;

import jsesh.model.MDCPosition;

/// A record of the occurrence of a sign at a certain position in a text.
///
/// An embedded sign (`sign1`) may have a Gardiner equivalent (its declared
/// "variant of" code): the occurrence then matches both codes.
///
/// @author rosmord
public class HieroglyphOccurrence {
	private final String code;
	private final String gardinerEquivalent;
	private final MDCPosition position;

	public HieroglyphOccurrence(String code, MDCPosition position) {
		this(code, null, position);
	}

	/// @param code               the canonical code of the sign.
	/// @param gardinerEquivalent the canonical Gardiner equivalent of an
	///                           embedded sign, or `null`.
	/// @param position           the position of the sign in the text.
	public HieroglyphOccurrence(String code, String gardinerEquivalent, MDCPosition position) {
		this.code = code;
		this.gardinerEquivalent = gardinerEquivalent;
		this.position = position;
	}

	public String getCode() {
		return code;
	}

	/// The Gardiner equivalent of an embedded sign, or `null`.
	public String getGardinerEquivalent() {
		return gardinerEquivalent;
	}

	public MDCPosition getPosition() {
		return position;
	}

	/// Is this occurrence's code, or its Gardiner equivalent, `code`.
	public boolean hasCode(String code) {
		return this.code.equals(code) || code.equals(gardinerEquivalent);
	}

	/// Is this occurrence's code, or its Gardiner equivalent, in `codes`.
	public boolean matchesAny(Set<String> codes) {
		return codes.contains(code) || (gardinerEquivalent != null && codes.contains(gardinerEquivalent));
	}

	@Override
	public String toString() {
		String equivalent = gardinerEquivalent == null ? "" : "=" + gardinerEquivalent;
		return "(" + code + equivalent + ", " + position.getIndex() + ')';
	}
}
