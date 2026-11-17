package jsesh.mdcDisplayer.draw;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

/**
 * DEPRECATED INTERFACE. NOW A CLASS.
 * Represent all information needed to draw a text, save the text itself :
 * the various lines width, spacing, the fonts to use, etc.
 * 
 * Some more attention should be given to the units used ?!?
 * 
 * <p>This file is free Software 
 * under the GNU LESSER GENERAL PUBLIC LICENCE.
 * 
 * <p>(c) Serge Rosmorduc
 * @author Serge Rosmorduc
 *
 * TODO : improve the drawing specifications system. There should be an easy way to copy the specifications in order to create small variants.
 * 
 */
public interface CopyOfDrawingSpecifications {

	/**
	 * The class used for actual hieroglyphic drawing.
	 */
	HieroglyphsDrawer getHieroglyphsDrawer();

	/*
	 * We delegate the actual drawing of any element to this object
	 */

	//ElementDrawer getElementDrawer();

	/**
	* returns the dimenstions of the text in String.
	* The font to use is specified by ToggleType.
	* 
	* @param scriptCode : a code from jsesh.mdc.constants.ScriptCodes
	* @param text
	* @return Dimension2D
	*/
	
	Rectangle2D getTextDimensions(char scriptCode, String text);

	/**
	 * Method getCadratWidth.
	 * returns the expected width for a cadrat.
	 */
	float getCadratWidth();

	/**
	 *
	 * the stroke used for wide lines, like cartouches parts.
	 * @return Stroke
	 */
	Stroke getWideStroke();

	// Stroke getNullStroke();

	/**
	 * Constant for horizontal text.
	 */
	static final int HORIZONTAL= 0;
	
	/**
	 * Constant for vertical text.
	 */
	static final int VERTICAL= 1;
	
	/**
	 * Text orientation. 
	 * One of HORIZONTAL or VERTICAL.
	 * @return
	 */
	public int getOrientation();

	/**
	 * The class used to layout the text.
	 * I feel it should not be here. I have to rethink the UML.
	 * @return
	 */
	// IMPORTANT : suppress this
	// public Layout getLayout();
	
	/**
	 * Method getPageWidth : returns the page width.
	 * @return float
	 */
	float getPageWidth();
	
	/**
	 * Method getRuleWidth.
	 * @return float
	 */
	float getRuleWidth();

	/**
	 * @return float
	 */
	float getLineSkip();

	
	/**
	 * the minimal amount of space between two signs in a group.
	 * @return int
	 */
	float getSmallSkip();

	/**
	 * @return
	 */
	float getMaxCadratHeight();

	/**
	 * The width of a cartouche line.
	 * @return
	 */
	float getCartoucheLineWidth();

	/**
	 * @return
	 */
	Color getCursorColor();

	/**
	 * Gets the font to use for a particular script.
	 * 
	 * @param scriptCode : a code from ScriptCodes
	 * @return the font to use
	 * @see jsesh.mdc.constants.ScriptCodes
	 */
	Font getFont(char scriptCode);

	/**
	 * Returns the color used for red stuff.
	 * Might be replaced by a setRed() method at some point.
	 * (this would allow different strategies, as shading).
	 * @return
	 */
	Color getRedColor();

	/**
	 * @return
	 */
	Color getGrayColor();

	/**
	 * @return
	 */
	Color getBlackColor();

	/**
	 * Get line width for a cartouche
	 * @param cartoucheType one of 'c', 's', 'h', 'f'.
	 * @return
	 */
	Stroke getCartoucheStroke(int cartoucheType);

	Stroke getFineStroke();
		
	/**
	 * @param i
	 * @param j
	 * @return
	 */
	float getCartoucheStartWidth(int type, int part);

	/**
	 * @param i
	 * @param j
	 * @return
	 */
	float getCartoucheEndWidth(int type, int part);

	/**
	 * @param i
	 * @return
	 */
	float getCartoucheTopMargin(int type);

	/**
	 * @return
	 */
	boolean isSmallSignsCentered();

	/**
	 * Gets the width of a particular type of philological parenthesis.
	 * @param philologyType the type of parenthesis, a constant from  jsesh.mdc.constants.SymbolCodes
	 * @return the width of the symbol.
	 * @see jsesh.mdc.constants.SymbolCodes
	 */
	float getPhilologyWidth(int philologyType);

	/**
	 * The height of an A1 sign.
	 * @return
	 */
	float getBaseLength();

	/**
	 * @param text
	 * @return
	 */
	Dimension2D getSuperScriptDimensions(String text);

	/**
	 * @return
	 */
	Font getSuperScriptFont();

	/**
	 * @return
	 */
	float getLeftMargin();

	/**
	 * @return
	 */
	float getTopMargin();
}
