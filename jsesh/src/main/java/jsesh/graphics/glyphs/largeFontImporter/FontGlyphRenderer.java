package jsesh.graphics.glyphs.largeFontImporter;

import javax.swing.table.DefaultTableCellRenderer;

public class FontGlyphRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6774222968907392445L;

	protected void setValue(Object value) {
		if (value instanceof FontGlyph) {
			FontGlyph glyph = (FontGlyph) value;
			setFont(glyph.getFont());
			char []s= new char[] {(char)glyph.getSignPos()};
			if (! glyph.getFont().canDisplay((char)glyph.getSignPos()))
				System.out.println(Messages.getString("FontGlyphRenderer.CANT_DISPLAY")+ glyph.getSignPos()); //$NON-NLS-1$
			setText(new String(s));
		}
	}

}
