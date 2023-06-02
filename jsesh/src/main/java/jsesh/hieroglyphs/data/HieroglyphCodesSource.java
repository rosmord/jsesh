package jsesh.hieroglyphs.data;

import java.util.Set;

/**
 * Something which knows about a collection of hieroglyph codes.
 * 
 * @author rosmord
 *
 */
public interface HieroglyphCodesSource {

	/**
	 * Does this source know about a certain code.
	 * 
	 * @param code the code to search
	 * @return if the code is known by the source.
	 */
	boolean hasCode(String code);
	

	/**
	 * Gets the set of the codes for the signs defined by this hieroglyph codes.
	 * @return a immutable set of codes.
	 */
	Set<String> getCodes();
}
