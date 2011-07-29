/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.jhotdraw.applicationPreferences.model;

import java.awt.Font;
import java.io.File;
import java.util.prefs.Preferences;

import jsesh.jhotdraw.utils.FontUtil;
import jsesh.mdcDisplayer.preferences.YODChoice;

/**
 * Font-related information to set (Immutable object).
 * <p> This will be transformed into a parameter for DrawingSpecification at some point.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */

public class FontInfo {
	private static final String TRANSLITERATION_FONT = "transliterationFont";
	private static final String BASE_FONT = "baseFont";
	private static final String CURRENT_HIEROGLYPHS_SOURCE = "CURRENT_HIEROGLYPHS_SOURCE";
	private File hieroglyphsFolder;
	private Font baseFont, transliterationFont;
	private boolean translitUnicode = true;
	private YODChoice yodChoice = YODChoice.U0313;

	public FontInfo(File hieroglyphsFolder, Font baseFont,
			Font transliterationFont) {
		super();
		this.hieroglyphsFolder = hieroglyphsFolder;
		this.baseFont = baseFont;
		this.transliterationFont = transliterationFont;
	}

	public FontInfo(FontInfo fontInfo) {
		this(fontInfo.hieroglyphsFolder, fontInfo.baseFont,
				fontInfo.transliterationFont);
		this.translitUnicode = fontInfo.translitUnicode;
		this.yodChoice = fontInfo.yodChoice;
	}

	public File getHieroglyphsFolder() {
		return hieroglyphsFolder;
	}

	public Font getBaseFont() {
		return baseFont;
	}

	public Font getTransliterationFont() {
		return transliterationFont;
	}

	public FontInfo withTranslitUnicode(boolean useUnicode) {
		FontInfo result = new FontInfo(this);
		result.translitUnicode = useUnicode;
		return result;
	}

	public FontInfo withYodChoice(YODChoice yodChoice) {
		FontInfo result = new FontInfo(this);
		result.yodChoice = yodChoice;
		return result;
	}

	public boolean isTranslitUnicode() {
		return translitUnicode;
	}

	public YODChoice getYodChoice() {
		return yodChoice;
	}

	public void savetoPrefs(Preferences preferences) {
		if (hieroglyphsFolder == null)
			preferences.remove(CURRENT_HIEROGLYPHS_SOURCE);
		else {
			preferences.put(CURRENT_HIEROGLYPHS_SOURCE,
					hieroglyphsFolder.getAbsolutePath());
		}

		//preferences.put(BASE_FONT, FontUtil.fontEncode(baseFont) );
		//preferences.put(TRANSLITERATION_FONT, FontUtil.fontEncode(transliterationFont));
		preferences.putBoolean("translitUnicode", translitUnicode);
		preferences.put("yodChoice", yodChoice.name());
	}

	public static FontInfo getFromPreferences(Preferences preferences) {
		FontInfo fontInfo;
		
		File hieroglyphsFolder;
		Font baseFont, transliterationFont;

		String currentHieroglyphicPath = preferences.get(
				CURRENT_HIEROGLYPHS_SOURCE, null);

		if (currentHieroglyphicPath != null)
			hieroglyphsFolder = new File(currentHieroglyphicPath);
		else
			hieroglyphsFolder= null;
		
		baseFont= Font.getFont(preferences.get(BASE_FONT, "Serif PLAIN 12"), Font.decode(null));
		transliterationFont= Font.getFont(preferences.get(TRANSLITERATION_FONT, "Serif ITALIC 12"), Font.decode(null));

		fontInfo= new FontInfo(hieroglyphsFolder, baseFont, transliterationFont);
		fontInfo = fontInfo.withTranslitUnicode(
				preferences.getBoolean("translitUnicode", true)).withYodChoice(
				YODChoice.valueOf(preferences.get("yodChoice",
						YODChoice.U0313.name())));
		return fontInfo;
	}
}
