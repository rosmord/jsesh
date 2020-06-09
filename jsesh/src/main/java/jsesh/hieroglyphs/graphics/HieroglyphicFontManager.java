package jsesh.hieroglyphs.graphics;

/**
 * HieroglyphicFontManager associates glyphs with codes. Test version
 * only. The real one will be an interface, and the real
 * implementation will take the glyphs from a database (or maybe for
 * usual ones from files, but this should be quite hidden).
 *
 *
 * Created: Mon Jun 10 17:59:58 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 */

import java.util.Set;


public interface HieroglyphicFontManager {


	/**
	 * Returns the ShapeChar for the given Manuel de Codage code, or null if the code is unknown.
	 * @param code
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
	 * Gets the set of the codes for the signs defined by this manager.
	 * @return a immutable set of codes.
	 */
	Set<String> getCodes();
	
	/**
	 * Returns true if new signs are known to have been added since the last call to getCodes.
	 * If the manager is immutable or if one wants to keep things simple, returning false is an option.
	 * @return true if new signs are known to have been added since the last call to getCodes.
	 */
	boolean hasNewSigns();
	
} // HieroglyphicFontManager
