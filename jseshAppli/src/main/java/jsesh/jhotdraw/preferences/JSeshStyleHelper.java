package jsesh.jhotdraw.preferences;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import jsesh.io.document.DocumentHeaderCodec;
import jsesh.model.LayoutSetting;

import jsesh.render.style.DocumentPreferencesStyleConverter;
import jsesh.render.style.JSeshStyle;
import jsesh.model.constants.JSeshInfoConstants;
import jsesh.document.DocumentPreferences;

/**
 * Utility class to extract JSeshStyle objects from various sources.
 * 
 * <p> We could also create a specific module for all the preferences 
 * a third party software might want to use.
 * 
 */
public class JSeshStyleHelper {

    /**
     * Apply a specific document preferences to an original JSesh style, and return
     * the new style.
     * 
     * @param preferences the document preferences to apply.
     * @param original    the original style to use as a basis for the new one.
     * @return a new style with the document preferences applied.
     */
    public static JSeshStyle applyDocumentPreferences(DocumentPreferences preferences, JSeshStyle original) {
        return DocumentPreferencesStyleConverter.applyDocumentPreferences(preferences, original);
    }


    /// Initialises a JSeshStyle from Java preferences.
    ///
    /// The preferences use the format of document headers (see
    /// [DocumentHeaderCodec]), so the preferences saved by older versions are
    /// read too. Only the layout of the text is kept there: the orientation,
    /// the direction and the page of the original style are not changed.
    ///
    /// @param preferences standard java preferences.
    /// @param original the style used as a basis.
    /// @return the corresponding JSeshStyle.
    public static JSeshStyle javaPreferencesToJSeshStyle(Preferences preferences, JSeshStyle original) {
        Map<String, String> map = new HashMap<>();
        try {
            for (String key : preferences.keys()) {
                if (key.startsWith(JSeshInfoConstants.JSESH_INFO_PREFIX)) {
                    map.put(key, preferences.get(key, ""));
                }
            }
        } catch (BackingStoreException e) {
            return original;
        }
        DocumentPreferences saved = DocumentHeaderCodec.fromHeader(map);
        DocumentPreferences current = DocumentPreferencesStyleConverter.toDocumentPreferences(original);
        DocumentPreferences merged = current
                .withUnit(saved.unit())
                .withSettings(saved.settings()
                        .with(LayoutSetting.LAYOUT, current.textOrientation())
                        .with(LayoutSetting.DIRECTION, current.textDirection()))
                .withSmallBodyScaleLimit(saved.smallBodyScaleLimit())
                .withUseLinesForShading(saved.useLinesForShading())
                .withUseUnderlineForRed(saved.useUnderlineForRed());
        return DocumentPreferencesStyleConverter.applyDocumentPreferences(merged, original);
    }

    /**
     * Creates a DocumentPreferences from a JSeshStyle, converting all fields.
     *
     * @param jseshStyle the style to convert
     * @return a new DocumentPreferences with values from the style
     */
    public static DocumentPreferences jseshStyleToDocumentPreferences(JSeshStyle jseshStyle) {
        return DocumentPreferencesStyleConverter.toDocumentPreferences(jseshStyle);
    }

    /// Saves the layout of a JSeshStyle into Java preferences, in the format
    /// read by [#javaPreferencesToJSeshStyle(Preferences, JSeshStyle)]. The
    /// orientation, the direction and the page are not saved.
    ///
    /// @param jseshStyle the style.
    /// @param preferences where to save it.
    public static void saveJSeshStyleToPreferences(JSeshStyle jseshStyle, Preferences preferences) {
        DocumentPreferences prefs = DocumentPreferencesStyleConverter.toDocumentPreferences(jseshStyle)
                .withPage(0, 0, 0, 0, 0, 0);
        Map<String, String> header = DocumentHeaderCodec.toHeader(prefs);
        header.remove(DocumentHeaderCodec.headerKey(LayoutSetting.LAYOUT));
        header.remove(DocumentHeaderCodec.headerKey(LayoutSetting.DIRECTION));
        header.remove(JSeshInfoConstants.JSESH_PAGE_ORIENTATION);
        header.remove(JSeshInfoConstants.JSESH_PAGE_DIRECTION);
        header.forEach(preferences::put);
    }

}
