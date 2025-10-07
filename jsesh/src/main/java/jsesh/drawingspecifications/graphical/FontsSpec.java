package jsesh.drawingspecifications.graphical;

import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.utils.YODChoice;
import jsesh.resources.ResourcesManager;

import java.awt.Font;

/**
 * Specifications related to fonts and non-hieroglyphic text.
 * 
 * @param translitUnicode true if transliteration is in Unicode, false if it is in classical ASCII Manuel de Codage.
 * @param yodChoice how to render yod in transliteration (only used if the font is a Unicode Font)
 * @param gardinerQofUsed true if "q" is rendered as k with dot below (like in Gardiner's grammar) or as a plain "q" (if false).
 * @param plainFont the font to use for normal text.
 * @param boldFont the font to use for bold text.
 * @param italicFont the font to use for italic text.
 * @param translitterationFont the font to use for transliteration.
 * 
 * This class is not perfect. Some information relates only to the case of Unicode transliteration fonts.
 * 
 * <p>An important change from the previous system: it is the programmer responsability to provide a correct transliteration font,
 * i.e. a MdC font if translitUnicode is false, a Unicode font if it is true.
 * @see ScriptCodes (will probably change)
 */
public record FontsSpec(		
		boolean translitUnicode,
		YODChoice yodChoice,
		boolean gardinerQofUsed,
		Font plainFont,
		Font boldFont,
		Font italicFont,
		Font translitterationFont) {

	/**
	 * A reasonnable default.
	 */
	public static final FontsSpec DEFAULT = new FontsSpec(
			true,
			YODChoice.UA7BD,
			true,
			new Font("Serif", Font.PLAIN, 12),
			new Font("Serif", Font.BOLD, 12),
			new Font("Serif", Font.ITALIC, 12),
			ResourcesManager.getInstance().getUnicodeTransliterationFont()
			);

	public Builder copy() {
		return new Builder(this);
	}
	
	/** copy builder class */
	public static class Builder {
		private boolean translitUnicode;
		private YODChoice yodChoice;
		private boolean gardinerQofUsed;
		private Font plainFont;
		private Font boldFont;
		private Font italicFont;
		private Font translitterationFont;

		public Builder(FontsSpec specs) {
			this.translitUnicode = specs.translitUnicode;
			this.yodChoice = specs.yodChoice;
			this.gardinerQofUsed = specs.gardinerQofUsed;
			this.plainFont = specs.plainFont;
			this.boldFont = specs.boldFont;
			this.italicFont = specs.italicFont;
			this.translitterationFont = specs.translitterationFont;
		}

		public Builder translitUnicode(boolean translitUnicode) {
			this.translitUnicode = translitUnicode;
			return this;
		}

		public Builder yodChoice(YODChoice yodChoice) {
			this.yodChoice = yodChoice;
			return this;
		}

		public Builder gardinerQofUsed(boolean gardinerQofUsed) {
			this.gardinerQofUsed = gardinerQofUsed;
			return this;
		}

		public Builder plainFont(Font plainFont) {
			this.plainFont = plainFont;
			return this;
		}

		public Builder boldFont(Font boldFont) {
			this.boldFont = boldFont;
			return this;
		}

		public Builder italicFont(Font italicFont) {
			this.italicFont = italicFont;
			return this;
		}

		public Builder translitterationFont(Font translitterationFont) {
			this.translitterationFont = translitterationFont;
			return this;
		}

		public FontsSpec build() {
			return new FontsSpec(translitUnicode, yodChoice, gardinerQofUsed, plainFont, boldFont, italicFont,
					translitterationFont);
		}
	}
}
