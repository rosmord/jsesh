package jsesh.utils;

import java.util.prefs.Preferences;

import jsesh.drawingspecifications.JSeshStyle;
import jsesh.drawingspecifications.ShadingMode;
import jsesh.mdc.constants.JSeshInfoConstants;
import jsesh.mdc.file.DocumentPreferences;

/**
 * Utility class to convert document preferences to JSesh styles.
 * 
 * Could be in of of the two classes DocumentPreferences and JSeshStyle, but I
 * think the coupling
 * is lower if I put it here.
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
        return original.copy()
                .geometry(g -> g.cartoucheLineWidth((float) preferences.getCartoucheLineWidth())
                        .maxCadratWidth((float) preferences.getMaxQuadratWidth())
                        .maxCadratHeight((float) preferences.getMaxQuadratHeight())
                        .lineSkip((float) preferences.getLineSkip())
                        .columnSkip((float) preferences.getColumnSkip())
                        .standardSignHeight((float) preferences.getStandardSignHeight())
                        .smallBodyScaleLimit((float) preferences.getSmallBodyScaleLimit())
                        .smallSkip((float) preferences.getSmallSkip()))
                .options(o -> o.smallSignCentered(preferences.isSmallSignCentered())
                        .textOrientation(preferences.getTextOrientation())
                        .textDirection(preferences.getTextDirection()))
                .colors(c -> c
                        .shadingStyle(preferences.isUseLinesForShading() ? ShadingMode.LINE_HATCHING
                                : ShadingMode.GRAY_SHADING))
                .build();
    }


     /**
     * Initialize a JSeshStyle from Java preferences. 
     *
     * @param preferences standard java preferences.
     * @return the corresponding JSeshStyle.
     */
    public static JSeshStyle javaPreferencesToJSeshStyle(Preferences preferences) {
        JSeshStyle result = JSeshStyle.DEFAULT.copy()
                .geometry(g -> g.cartoucheLineWidth((float) preferences.getDouble(JSeshInfoConstants.JSESH_CARTOUCHE_LINE_WIDTH, 1.0)) // ok
                        .maxCadratWidth((float) preferences.getDouble(JSeshInfoConstants.JSESH_MAX_QUADRANT_WIDTH, 22)) // ok
                        .maxCadratHeight((float) preferences.getDouble(JSeshInfoConstants.JSESH_MAX_QUADRANT_HEIGHT, 18)) // ok
                        .lineSkip((float) preferences.getDouble(JSeshInfoConstants.JSESH_LINE_SKIP, 6)) // ok
                        .columnSkip((float) preferences.getDouble(JSeshInfoConstants.JSESH_COLUMN_SKIP, 10)) // ok
                        .standardSignHeight((float) preferences.getDouble(JSeshInfoConstants.JSESH_STANDARD_SIGN_HEIGHT, 18.0)) // ok
                        .smallBodyScaleLimit((float) preferences.getDouble(JSeshInfoConstants.JSESH_SMALL_BODY_SCALE_LIMIT, 12.0)) // ok
                        .smallSkip((float) preferences.getDouble(JSeshInfoConstants.JSESH_SMALL_SKIP, 4)))
                .options(o -> o.smallSignCentered(preferences.getBoolean(JSeshInfoConstants.JSESH_SMALL_SIGNS_CENTRED, false)))
                .colors(c -> c
                        .shadingStyle(preferences.getBoolean(JSeshInfoConstants.JSESH_USE_LINES_FOR_SHADING, true)
                                ? ShadingMode.LINE_HATCHING
                                : ShadingMode.GRAY_SHADING))
                .build();
        
        return result;
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
