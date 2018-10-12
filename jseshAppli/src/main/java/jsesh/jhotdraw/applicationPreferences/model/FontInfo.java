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

import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.utils.YODChoice;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.resources.ResourcesManager;

/**
 * Font-related information to set (Immutable object).
 * <p>
 * This will be transformed into a parameter for DrawingSpecification at some
 * point, which is the reason for this class to be immutable.
 *
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class FontInfo {

    private static final String YOD_CHOICE = "yodChoice";
    private static final String USE_EMBEDDED_TRANSLIT_FONT = "useEmbeddedTranslitFont";
    private static final String TRANSLIT_UNICODE = "translitUnicode";
    private static final String TRANSLITERATION_FONT = "transliterationFont";
    private static final String TRANSLITERATION_FONT_SIZE = "transliterationFontSize";
    private static final String BASE_FONT = "baseFont";
    private static final String BASE_FONT_SIZE = "baseFontSize";
    private static final String CURRENT_HIEROGLYPHS_SOURCE = "CURRENT_HIEROGLYPHS_SOURCE";

    private File hieroglyphsFolder;
    private Font baseFont, transliterationFont;
    /**
     * Should we use the embedded ASCII MdC Font ?
     */
    private boolean useEmbeddedFont = true;
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
        this.useEmbeddedFont = fontInfo.useEmbeddedFont;
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
        if (hieroglyphsFolder == null) {
            preferences.remove(CURRENT_HIEROGLYPHS_SOURCE);
        } else {
            preferences.put(CURRENT_HIEROGLYPHS_SOURCE,
                    hieroglyphsFolder.getAbsolutePath());
        }

        preferences.put(BASE_FONT, baseFont.getName());
        preferences.putInt(BASE_FONT_SIZE, baseFont.getSize());
        preferences.put(TRANSLITERATION_FONT, transliterationFont.getName());
        preferences.putInt(TRANSLITERATION_FONT_SIZE,
                transliterationFont.getSize());

        preferences.putBoolean(TRANSLIT_UNICODE, translitUnicode);
        preferences.putBoolean(USE_EMBEDDED_TRANSLIT_FONT, useEmbeddedFont);
        preferences.put(YOD_CHOICE, yodChoice.name());
    }

    public static FontInfo getFromPreferences(Preferences preferences) {
        FontInfo fontInfo;

        File hieroglyphsFolder;
        Font baseFont, transliterationFont;

        String currentHieroglyphicPath = preferences.get(
                CURRENT_HIEROGLYPHS_SOURCE, null);

        if (currentHieroglyphicPath != null) {
            hieroglyphsFolder = new File(currentHieroglyphicPath);
        } else {
            hieroglyphsFolder = null;
        }

        baseFont = new Font(preferences.get(BASE_FONT, "Serif"), Font.PLAIN,
                preferences.getInt(BASE_FONT_SIZE, 12));
        transliterationFont = new Font(preferences.get(TRANSLITERATION_FONT,
                "Serif"), Font.PLAIN, preferences.getInt(
                        TRANSLITERATION_FONT_SIZE, 12));
        fontInfo = new FontInfo(hieroglyphsFolder, baseFont,
                transliterationFont);
        fontInfo = fontInfo
                .withTranslitUnicode(
                        preferences.getBoolean(TRANSLIT_UNICODE, true))
                .withYodChoice(
                        YODChoice.valueOf(preferences.get(YOD_CHOICE,
                                YODChoice.U0313.name())))
                .withUseEmbeddedFont(
                        preferences
                                .getBoolean(USE_EMBEDDED_TRANSLIT_FONT, true));
        return fontInfo;
    }

    /**
     * Should we use the embedded ASCII MdC Font ?
     *
     * @param useEmbeddedFont
     * @return
     */
    public FontInfo withUseEmbeddedFont(boolean useEmbeddedFont) {
        FontInfo result = new FontInfo(this);
        result.useEmbeddedFont = useEmbeddedFont;
        if (useEmbeddedFont) {
            result.translitUnicode = false;
            result.transliterationFont = ResourcesManager.getInstance().getTransliterationFont();
        }
        return result;
    }

    /**
     * Should we use the embedded ASCII MdC Font ?
     * @return true if we use the embedded font.
     */
    public boolean isUseEmbeddedFont() {
        return useEmbeddedFont;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FontInfo [hieroglyphsFolder=" + hieroglyphsFolder
                + ", baseFont=" + baseFont + ", transliterationFont="
                + transliterationFont + ", useEmbeddedFont=" + useEmbeddedFont
                + ", translitUnicode=" + translitUnicode + ", yodChoice="
                + yodChoice + "]";
    }

    /**
     * Apply those font information to a given drawing specification object.
     * Temporary method. In fact, font information should be part of the drawing
     * specifications.
     *
     * @param drawingSpecification
     */
    public void applyToDrawingSpecifications(
            DrawingSpecification drawingSpecification) {
        drawingSpecification.setFont('*', getBaseFont());
        drawingSpecification.setFont(ScriptCodes.TRANSLITERATION,
                getTransliterationFont());
        drawingSpecification.setTranslitUnicode(isTranslitUnicode());
        drawingSpecification.setYodChoice(getYodChoice());
    }

}
