package jsesh.search.simpleSearch;

/**
 * Base interface for wildcards.
 */
interface WildcardToken {

	
	boolean match(HieroglyphOccurrence token);

	/**
	 * Is this pattern repeated 0 or n times.
	 * (notice the '0') !
	 * @return
	 */
	boolean isRepeat();
}
