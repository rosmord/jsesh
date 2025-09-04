package jsesh.drawingspecifications;

import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.utils.YODChoice;
import java.awt.Font;

/**
 * Specifications related to fonts.
 * 
 * @see ScriptCodes (will probably change)
 */
public record FontsSpecifications(
		/**
		 * Use Unicode for translitteration or MdC 256 char font ?
		 */
		boolean translitUnicode,
		YODChoice yodChoice,

		/**
		 * Is "q" rendered as k with dot below (like in Gardiner's grammar) or as a
		 * plain "q" (if false).
		 */
		boolean gardinerQofUsed,
		Font plainFont,
		Font boldFont,
		Font italicFont,
		Font translitterationFont) {
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

		public Builder(FontsSpecifications specs) {
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

		public FontsSpecifications build() {
			return new FontsSpecifications(translitUnicode, yodChoice, gardinerQofUsed, plainFont, boldFont, italicFont,
					translitterationFont);
		}
	}
}
