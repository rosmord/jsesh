package jsesh.drawingspecifications;

import java.awt.Font;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.utils.TransliterationEncoding;
import jsesh.mdc.utils.YODChoice;
import jsesh.mdcDisplayer.context.JSeshTechRenderContext;
import jsesh.resources.ResourcesManager;
import jsesh.utils.DoubleDimensions;

/**
 * Specifications related to fonts and non-hieroglyphic text.
 * 
 * <p>
 * This class is not perfect. Some information
 * relates only to the case of Unicode
 * 
 * <p>
 * An important change from the previous system: it
 * is the programmer's responsability to provide a
 * correct transliteration font,
 * i.e. a MdC font if translitUnicode is false, a
 * Unicode font if it is true.
 * 
 * @param translitUnicode      true if transliteration font uses Unicode, false
 *                             if
 *                             it is in classical ASCII Manuel de Codage.
 * @param yodChoice            how to render yod in transliteration (only used
 *                             if the font is a Unicode Font)
 * @param gardinerQofUsed      true if "q" is rendered as k with dot below (like
 *                             in Gardiner's grammar) or as a plain "q" (if
 *                             false).
 * @param plainFont            the font to use for normal text (+l in MdC)
 * @param boldFont             the font to use for bold text (+b in MdC)
 * @param italicFont           the font to use for italic text (+i in MdC)
 * @param superScriptFont      the font to use for so-called "small text", i.e.
 *                             texts like "sic" or note references.
 * @param translitterationFont the font to use for transliteration.
 * 
 * @see ScriptCodes (will probably change)
 */
public record FontSpecification(
		boolean translitUnicode,
		YODChoice yodChoice,
		boolean gardinerQofUsed,
		Font plainFont,
		Font boldFont,
		Font italicFont,
		Font superScriptFont,
		Font translitterationFont) {

	/**
	 * A reasonnable default.
	 */
	public static final FontSpecification DEFAULT = new FontSpecification(
			true,
			YODChoice.UA7BD,
			true,
			new Font("Serif", Font.PLAIN, 12),
			new Font("Serif", Font.BOLD, 12),
			new Font("Serif", Font.ITALIC, 12),
			new Font("Serif", Font.ITALIC, 5),
			ResourcesManager.getInstance().getUnicodeTransliterationFont());

	public TransliterationEncoding transliterationEncoding() {
		return new TransliterationEncoding(translitUnicode, yodChoice, gardinerQofUsed);
	}

	/**
	 * Returns the font for a given script code.
	 * 
	 * @param scriptCode
	 * @return
	 */
	public Font getFont(char scriptCode) {
		return switch (scriptCode) {
			case ScriptCodes.LATIN ->
				plainFont;
			case ScriptCodes.BOLD ->
				boldFont;
			case ScriptCodes.ITALIC ->
				italicFont;
			case ScriptCodes.TRANSLITERATION ->
				translitterationFont;
			default ->
				plainFont;
		};
	}

	/**
	 * Computes actual dimensions for a given superScript text.
	 * 
	 * NOTE: this method might move to a helper class if we want to avoid adding a
	 * dependency
	 * between this class and AWT (but it's aready there because of Font), or if we
	 * want to avoid putting logic
	 * in a record.
	 * 
	 * @param JSeshRenderContext the rendering context to use.
	 * @param text               the text to measure
	 * @return the dimensions of the text in superScript font.
	 */
	public Dimension2D superScriptDimensions(JSeshTechRenderContext techRenderContext, String text) {
		Rectangle2D r = superScriptFont.getStringBounds(text,
				techRenderContext.fontRenderContext());
		return new DoubleDimensions(r.getWidth(), r.getHeight());
	}

	/**
	 * Get the dimensions of a text in a given script.
	 * @param techRenderContext
	 * @param scriptCode
	 * @param text
	 * @return
	 */
	public Rectangle2D textDimensions(JSeshTechRenderContext techRenderContext, char scriptCode, String text) {
		Rectangle2D r = getFont(scriptCode).getStringBounds(text,
				techRenderContext.fontRenderContext());
		return r;
	}

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
		private Font superScriptFont;
		private Font translitterationFont;

		public Builder(FontSpecification specs) {
			this.translitUnicode = specs.translitUnicode;
			this.yodChoice = specs.yodChoice;
			this.gardinerQofUsed = specs.gardinerQofUsed;
			this.plainFont = specs.plainFont;
			this.boldFont = specs.boldFont;
			this.italicFont = specs.italicFont;
			this.superScriptFont = specs.superScriptFont;
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

		public Builder superScriptFont(Font superScriptFont) {
			this.superScriptFont = superScriptFont;
			return this;
		}

		public Builder translitterationFont(Font translitterationFont) {
			this.translitterationFont = translitterationFont;
			return this;
		}

		public FontSpecification build() {
			return new FontSpecification(translitUnicode, yodChoice, gardinerQofUsed, plainFont, boldFont, italicFont,
					superScriptFont,
					translitterationFont);
		}
	}
}
