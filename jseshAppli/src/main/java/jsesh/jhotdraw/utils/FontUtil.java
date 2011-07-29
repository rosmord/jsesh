package jsesh.jhotdraw.utils;

import java.awt.Font;

public class FontUtil {

	/**
	 * Reverse of Font.decode operation.
	 * 
	 * @param font
	 * @return an string such that font and
	 *         Font.decode(FontUtil.fontEncode(font)) are the same font.
	 */
	public static String fontEncode(Font font) {
		String style;
		switch (font.getStyle()) {
		case Font.BOLD:
			style= "BOLD";
			break;
		case Font.ITALIC:
			style= "ITALIC";
			break;
		case com.lowagie.text.Font.BOLDITALIC:
			style= "BOLDITALIC";
			break;
		default:
			style = "PLAIN";
			break;
		}
		return font.getFamily() + " " + style + " " + font.getSize();
	}
}
