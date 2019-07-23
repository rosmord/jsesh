package jsesh.search.simpleSearch;

/**
 * A skip.
 * Matches anything any number of times.
 */
public class WildcardSkip implements WildcardToken{
	@Override
	public boolean match(HieroglyphOccurrence token) {
		return true;
	}

	@Override
	public boolean isRepeat() {
		return true;
	}
}
