package jsesh.mdcDisplayer.preferences;

import com.lowagie.text.pdf.CMYKColor;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import jsesh.mdc.file.DocumentPreferences;
import jsesh.mdc.utils.TransliterationEncoding;
import jsesh.mdcDisplayer.drawingElements.HieroglyphsDrawer;

/**
 * Parameters for drawing. Contains both changeable values and a number of
 * utility methods. Should probably be separated in two parts, with the utility
 * methods used as a decorator.
 *
 * @author rosmord
 *
 */
public interface DrawingSpecification extends DrawingPreferences {

    HieroglyphsDrawer getHieroglyphsDrawer();

    /**
     * Type-aware clone method.
     *
     * @return a copy of DrawingSpecifications
     */
    DrawingSpecification copy();

  
    /**
     * Returns the scale which should be applied to the current font to obtain
     * the desired sign height.
     *
     * @return the scale to apply to the current glyph font.
     */
    float getSignScale();

    /**
     * Compute the stroke which correspond to cartouche lines. Utility method.
     *
     * @param cartoucheType
     * @return
     */
    Stroke buildCartoucheStroke(int cartoucheType);

    /**
     * @return the font render context.
     */
    FontRenderContext getFontRenderContext();

    /**
     * @param context
     */
    void setFontRenderContext(FontRenderContext context);

    Dimension2D getSuperScriptDimensions(String text);

    /**
     * Compute the actual size of a given string.
     * @param scriptCode a code ('i', 'l'...) for a latin/coptic... font system
     * @param text a text to render
     * @return  the size of the text.
     */
    Rectangle2D getTextDimensions(char scriptCode, String text);

    boolean isGardinerQofUsed();

    void setGardinerQofUsed(boolean useGardinerQof);

    /**
     * Gets the document preferences which corresponds to those drawing
     * specifications.
     *
     * @return
     */
    DocumentPreferences extractDocumentPreferences();

    void applyDocumentPreferences(DocumentPreferences prefs);

    /**
     * gets the way transliteration should be encoded in the display font.
     *
     * @return
     */
    TransliterationEncoding getTransliterationEncoding();

    /**
     * Returns the color associated with a given property.
     * <p>
     * For instance, one can define that glyphs annotated with \det are
     * displayed in blue.
     *
     * @param propertyName
     * @return
     */
    Color getColorForProperty(String propertyName);

    /**
     * Defines the color used for glyphs having a certain property.
     *
     * @param propertyName
     * @param color
     */
    void setColorForProperty(String propertyName, Color color);
}
