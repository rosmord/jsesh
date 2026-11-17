package jsesh.hieroglyphs;

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
 * @version */

import java.util.Set;

import jsesh.bzr.simple.ShapeChar;

abstract public class HieroglyphicFontManager {

	static HieroglyphicFontManager instance = null;

	abstract public ShapeChar get(String code);
	
	public static HieroglyphicFontManager getInstance() {
		if (instance == null)
			//try {
				instance = new MemoryHieroglyphicFontManager();
				//instance= new HieroglyphicDB();
			//} catch (SQLException e) {
			//					e.printStackTrace();
			//}
		return instance;
	}
	
	abstract public Set getCodes();
	
} // HieroglyphicFontManager
