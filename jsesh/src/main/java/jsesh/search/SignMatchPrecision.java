package jsesh.search;

/**
 * Precision with which signs are matched.
 * Other terms will probably be added.
 */
public enum SignMatchPrecision {
	/**
	 * A Gardiner code should exactly match the sign.
	 */
	EXACT,
	/**
	 * FULL Variants should be matched (e.g. w and W).
	 * This could be the default value.
	 */
	FULL_VARIANT,
	/**
	 * Any kind of variant will match.
	 */
	ANY_VARIANT

}
