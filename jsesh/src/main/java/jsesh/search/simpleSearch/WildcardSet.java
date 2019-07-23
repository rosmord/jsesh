package jsesh.search.simpleSearch;

/**
 * A set of possible matching values.
 */
public class WildcardSet implements WildcardToken {
	@Override
	public boolean match(HieroglyphOccurrence token) {
		return false;
	}

	@Override
	public boolean isRepeat() {
		return false;
	}
}
