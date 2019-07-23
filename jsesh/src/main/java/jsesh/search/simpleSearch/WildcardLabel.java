package jsesh.search.simpleSearch;

/**
 * A simple label, for exact match.
 */
public class WildcardLabel implements WildcardToken {
	@Override
	public boolean match(HieroglyphOccurrence token) {
		return false;
	}

	@Override
	public boolean isRepeat() {
		return false;
	}
}
