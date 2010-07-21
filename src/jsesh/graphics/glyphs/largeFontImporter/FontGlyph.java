package jsesh.graphics.glyphs.largeFontImporter;

import java.awt.Font;

public class FontGlyph {

	private Font font;
	private int signPos;
	public FontGlyph(Font font, int pos) {
		this.font= font;
		this.signPos= pos;
	}

	public Font getFont() {
		return font;
	}
	
	public int getSignPos() {
		return signPos;
	}
}
