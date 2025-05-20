package jsesh.mdcDisplayer.preferences;

import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.utils.YODChoice;
import java.awt.Font;

/**
 * Specifications related to fonts.
 * @see ScriptCodes (will probably change)
 */
public record FontsSpecifications(
		/**
		 * Use Unicode for translitteration or MdC 256 char font ?
		 */
		boolean translitUnicode,
		YODChoice yodChoice,

	    /**
	     * Is "q" rendered as k with dot below (like in Gardiner's grammar) or as a plain "q" (if false).
	     */
		boolean gardinerQofUsed,
		Font plainFont, 
		Font boldFont,
		Font italicFont,
		Font translitterationFont
		) {
	

}
