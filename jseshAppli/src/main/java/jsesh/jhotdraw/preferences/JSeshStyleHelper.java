package jsesh.jhotdraw.preferences;

import java.util.prefs.Preferences;

import jsesh.render.style.DocumentPreferencesStyleConverter;
import jsesh.render.style.JSeshStyle;
import jsesh.render.style.ShadingMode;
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


     /**
     * Initialize a JSeshStyle from Java preferences. 
     *
     * @param preferences standard java preferences.
     * @return the corresponding JSeshStyle.
     */
    public static JSeshStyle javaPreferencesToJSeshStyle(Preferences preferences, JSeshStyle original) {
        JSeshStyle result = original.copy()
                .geometry(g -> g.cartoucheLineWidth((float) preferences.getDouble(JSeshInfoConstants.JSESH_CARTOUCHE_LINE_WIDTH, 1.0)) // ok
                        .maxCadratWidth((float) preferences.getDouble(JSeshInfoConstants.JSESH_MAX_QUADRANT_WIDTH, 22)) // ok
                        .maxCadratHeight((float) preferences.getDouble(JSeshInfoConstants.JSESH_MAX_QUADRANT_HEIGHT, 18)) // ok
                        .lineSkip((float) preferences.getDouble(JSeshInfoConstants.JSESH_LINE_SKIP, 6)) // ok
                        .columnSkip((float) preferences.getDouble(JSeshInfoConstants.JSESH_COLUMN_SKIP, 10)) // ok
                        .standardSignHeight((float) preferences.getDouble(JSeshInfoConstants.JSESH_STANDARD_SIGN_HEIGHT, 18.0)) // ok
                        .smallBodyScaleLimit((float) preferences.getDouble(JSeshInfoConstants.JSESH_SMALL_BODY_SCALE_LIMIT, 12.0)) // ok
                        .smallSkip((float) preferences.getDouble(JSeshInfoConstants.JSESH_SMALL_SKIP, 4)))
                .options(o -> o.smallSignCentered(preferences.getBoolean(JSeshInfoConstants.JSESH_SMALL_SIGNS_CENTRED, false)))
                .painting(c -> c
                        .shadingStyle(preferences.getBoolean(JSeshInfoConstants.JSESH_USE_LINES_FOR_SHADING, true)
                                ? ShadingMode.LINE_HATCHING
                                : ShadingMode.GRAY_SHADING))
                .build();
        
        return result;
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

    /**
     * Saves data from a JSeshStyle into Java preferences.
     * @param jseshStyle
     * @param preferences
     */
    public static void saveJSeshStyleToPreferences(JSeshStyle jseshStyle, Preferences preferences) {
        // From default document...
        preferences.putDouble(JSeshInfoConstants.JSESH_CARTOUCHE_LINE_WIDTH, jseshStyle.geometry().cartoucheLineWidth());	//ok
        preferences.putDouble(JSeshInfoConstants.JSESH_MAX_QUADRANT_WIDTH, jseshStyle.geometry().maxCadratWidth());//ok
        preferences.putDouble(JSeshInfoConstants.JSESH_MAX_QUADRANT_HEIGHT, jseshStyle.geometry().maxCadratHeight());//ok
        preferences.putDouble(JSeshInfoConstants.JSESH_LINE_SKIP, jseshStyle.geometry().lineSkip());//ok
        preferences.putDouble(JSeshInfoConstants.JSESH_COLUMN_SKIP, jseshStyle.geometry().columnSkip());//ok
        preferences.putDouble(JSeshInfoConstants.JSESH_STANDARD_SIGN_HEIGHT, jseshStyle.geometry().standardSignHeight()); //ok
        preferences.putDouble(JSeshInfoConstants.JSESH_SMALL_BODY_SCALE_LIMIT, jseshStyle.geometry().smallBodyScaleLimit());//ok
        preferences.putDouble(JSeshInfoConstants.JSESH_SMALL_SKIP, jseshStyle.geometry().smallSkip());//ok        
        preferences.putBoolean(JSeshInfoConstants.JSESH_SMALL_SIGNS_CENTRED, jseshStyle.options().smallSignCentered());//ok
        preferences.putBoolean(JSeshInfoConstants.JSESH_USE_LINES_FOR_SHADING, jseshStyle.painting().shadingStyle().equals(ShadingMode.LINE_HATCHING));//ok
        
    }

}
