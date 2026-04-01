package jsesh.hieroglyphs.fonts;

/**
 * HieroglyphicFontManager associates glyphs with codes. Test version
 * only. The real one will be an interface, and the real
 * implementation will take the glyphs from a database (or maybe for
 * usual ones from files, but this should be quite hidden).
 *
 *
 * Created: Mon Jun 10 17:59:58 2002
 *
 * <ul>
 * <li> !! this is probably one of the oldest java files in JSesh. 24 years old today !!
 * <li> JSesh was not even called JSesh at that time, and was simply an experiment to see if Java 2.0 was fast enough to
 * draw a hieroglyphic text.
 * <li> there are probably some older codes, but they were translated from tksesh and HieroTeX.
 * </ul>
 * @author <a href="mailto:serge.rosmorduc@qenherkhopeshef.org">Serge ROSMORDUC</a>
 */

import jsesh.hieroglyphs.data.HieroglyphCodesSource;
import jsesh.hieroglyphs.signshape.ShapeChar;


public interface HieroglyphicFontManager extends HieroglyphCodesSource {


	/**
	 * Does this font manager know about a certain code.
	 * @param code the code to search
	 * @return if the code is known by the font manager.
	 */
	default boolean hasCode(String code) {
		return get(code) != null;
	}
	
	/**
	 * Returns the ShapeChar for the given Manuel de Codage code, or null if the code is unknown.
	 * @param code : the <em>canonical code</em> for a sign. 
	 * @return the ShapeChar for the given Manuel de Codage code, or null if the code is unknown.
	 */
	ShapeChar get(String code);

	/**
	 * Returns the small body (bolder) shapeChar for a given manuel de codage code, or null if there is none available.
	 * @param code
	 * @return
	 */
	ShapeChar getSmallBody(String code);

	
	/**
	 * Returns true if new signs are known to have been added since the last call to getCodes.
	 * If the manager is immutable or if one wants to keep things simple, returning false is an option.
	 * @return true if new signs are known to have been added since the last call to getCodes.
	 */
	boolean hasNewSigns();
	
}
