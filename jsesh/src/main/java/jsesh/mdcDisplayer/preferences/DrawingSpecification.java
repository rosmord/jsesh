package jsesh.mdcDisplayer.preferences;

import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import jsesh.mdc.file.DocumentPreferences;
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
	 * The height of an A1 sign in the current fonts (in font units). DEPRECATED
	 * 
	 * @return The height of an A1 sign.
	 */

	// float getBaseLength();

	/**
	 * Returns the scale which should be applied to the current font to obtain
	 * the desired sign height.
	 * 
	 * @return the scale to apply to the current glyph font.
	 */
	float getSignScale();

	/**
	 * Compute the stroke which correspond to cartouche lines. Utility method.
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

	// HieroglyphsDrawer getHieroglyphsDrawer();

	/**
	 * Compute the actual size of a given string.
	 */
	Rectangle2D getTextDimensions(char scriptCode, String text);

	boolean isGardinerQofUsed();

	void setGardinerQofUsed(boolean useGardinerQof);

	/**
	 * Gets the document preferences which corresponds to those drawing specifications.
	 * @return
	 */
	DocumentPreferences extractDocumentPreferences();

	void applyDocumentPreferences(DocumentPreferences prefs);
}