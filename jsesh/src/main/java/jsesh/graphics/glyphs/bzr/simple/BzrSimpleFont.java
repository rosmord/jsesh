package jsesh.graphics.glyphs.bzr.simple;

import jsesh.hieroglyphs.graphics.ShapeChar;

/**
 * BzrSimpleFont represent a BZR font loaded in memory
 * 
 * 
 * Created: Fri Jun 7 12:27:21 2002
 * 
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * 
 */

public class BzrSimpleFont {

	private ShapeChar rep[];

	public BzrSimpleFont() {
		rep = new ShapeChar[256];
	}

	public ShapeChar getChar(int i) {
		return rep[i];
	}

	public void setChar(int i, ShapeChar s) {
		rep[i] = s;
	}
}// BzrSimpleFont
