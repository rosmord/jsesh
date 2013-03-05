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
 *  Created on 28 mai 2004
 */
package jsesh.mdcDisplayer.preferences;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.file.DocumentPreferences;
import jsesh.mdc.utils.TransliterationEncoding;
import jsesh.mdc.utils.YODChoice;
import jsesh.mdcDisplayer.drawingElements.HieroglyphicDrawerDispatcher;
import jsesh.mdcDisplayer.drawingElements.HieroglyphsDrawer;
import jsesh.resources.ResourcesManager;
import jsesh.utils.DoubleDimensions;

/**
 * Informations about line width, height... Should probably be simpler (and cut
 * in two : one part for user customization, and one part for more technical
 * stuff). Good candidate for the prototype <em>design pattern</em>.
 * 
 * <p>
 * We intend to move any computation <em>out</em> of this class to simplify its
 * use.
 * <p>
 * In particular, the elaborate code for cartouche contained generic methods for
 * all kinds of cartouches. This worked more or less, but proved difficult to
 * extend. Hence a more basic version now.
 * 
 * <h2>Cartouche related dimensions (add "cartouche" to get the real name of the
 * variables) :</h2>
 * <p align="center">
 * <img src= "../../../images/cartoucheDims.png">
 * </p>
 * <p>
 * Note that a standard horizontal cartouche total length is : lineWidth*2 +
 * loopLength*2+ size of the inner text + knotLength
 * <p>
 * An horizontal cartouche height is : linewidth*2+ margin*2 + maxCadratHeight
 * 
 * <p>
 * The principles are the same for the other kinds of "cartouche"-like elements.
 * 
 * @author rosmord
 * 
 *         This code is published under the GNU LGPL.
 */
public class DrawingSpecificationsImplementation implements Cloneable,
		DrawingSpecification {

	private Color redColor = Color.RED;

	private Color blackColor = Color.BLACK;

	/**
	 * TO DO: add the possibility to choose one's favourite colours.
	 */
	private Color grayColor = new Color(0.6f, 0.6f, 0.6f, 0.3f);

	/*------------------------
	 * Cartouche-related variables.
	 *
	 */

	private float cartoucheLineWidth = 1;

	private float cartoucheLoopLength = 10;

	private float cartoucheMargin = 2;

	private Color cursorColor;

	private float enclosureBastionDepth = 5;

	/*
	 * ------------------------- Enclosure
	 */

	private float enclosureBastionLength = 10;

	private float enclosureBastionSkip = 5;

	private Font[] fontMap;

	private YODChoice yodChoice;

	private boolean translitUnicode;

	private FontRenderContext fontRenderContext;

	// private HieroglyphsDrawer hieroglyphsDrawer;

	/*
	 * ------------------------- Hut-related variables
	 */

	/**
	 * The margin between the hut side and the first sign, when there is no
	 * square. Note that the start point is the internal side of the contour
	 * line.
	 */
	private float hutSmallMargin = 3;

	private float hutSquareSize = 10;

	/**
	 * ------------------------- Serekh-related variables
	 * 
	 */

	private float serekhDoorSize = 20;

	private float lineSkip;

	private float columnSkip;

	private float maxCadratHeight;

	private float maxCadratWidth;

	private boolean smallSignsCentered;
	
	/**
	 * Temporary system, while waiting for something better.
	 * Ask for text justification.
	 */
	private boolean justified= false;

	/**
	 * Skip between adjacent quadrants.
	 */
	private float smallSkip;

	/**
	 * Size of a tab unit. Typically 1/200 of the size of a cadrat.
	 */
	private float tabUnitWidth = 18f / 200f;

	private Font superScriptFont;

	private TextDirection textDirection;

	private TextOrientation textOrientation;

	private PageLayout pageLayout;

	private float fineLineWidth;

	private float wideLineWidth;

	private Color backgroundColor = Color.WHITE;

	/**
	 * The desired height of a standard "high" sign (like A1), in points.
	 */
	private float standardSignHeight;

	/**
	 * Ratio to base size a sign should have to be considered a "large" sign.
	 */
	private double largeSignSize = 0.8;

	/**
	 * Ratio to base size a sign should have to be considered a "small" sign.
	 * i.e. if a sign size is smaller than getBaseLangth() * getSmallSize(),
	 * this sign is small.
	 */
	private double smallSignSize = 0.4;

	private HieroglyphsDrawer hieroglyphsDrawer;

	private double smallBodyScaleLimit = 12;

	/**
	 * Default scale = 1 graphic point for 1 typographical point. (in order to
	 * keep it simple, we will try to reuse this scale in our Graphics2D
	 * implementations).
	 */

	private double graphicDeviceScale = 1;

	private boolean paged;

	private ShadingStyle shadingStyle = ShadingStyle.LINE_HATCHING;

	private boolean gardinerQofUsed = true;;

	// private float
	public DrawingSpecificationsImplementation() {
		fontRenderContext = new FontRenderContext(null, true, true);
		smallSignsCentered = false;
		smallSkip = 2;
		lineSkip = 6;
		columnSkip = 10;
		fineLineWidth = 0.5f;
		wideLineWidth = 3f;
		textOrientation = TextOrientation.HORIZONTAL;
		textDirection = TextDirection.LEFT_TO_RIGHT;
		// hieroglyphsDrawer = new SVGFontHieroglyphicDrawer();
		maxCadratHeight = 18f;
		maxCadratWidth = 22f;

		// Sets sensible defaults for page layout.
		pageLayout = new PageLayout();
		// pageLayout.setTextWidth(80 * 14); // What does this mean ?
		pageLayout.setTopMargin(smallSkip + getCartoucheLineWidth()
				+ getCartoucheMargin());
		pageLayout.setLeftMargin(3 * smallSkip);
		//pageLayout.setLeftMargin(0);
		
		pageLayout.setTextWidth(538);
		pageLayout.setTextHeight(760);

		standardSignHeight = 18f;
		// TODO : ensure system independance for font ?.
		// latinFont= Font.createFont(Font.TRUETYPE_FONT, in);
		// IMPORTANT : Load fonts only once !
		buildFontMap();

		superScriptFont = new Font(null, Font.PLAIN, 5);
		cursorColor = new Color(0, 0, 255, 100);
		hieroglyphsDrawer = new HieroglyphicDrawerDispatcher();
	}

	/**
     *  
     */
	private void buildFontMap() {
		fontMap = new Font[256];
		// fontMap[ScriptCodes.LATIN] = new Font("Serif", Font.PLAIN, 12);
		fontMap[ScriptCodes.LATIN] = ResourcesManager.getInstance()
				.getUnicodeFont();
		fontMap[ScriptCodes.BOLD] = new Font("Serif", Font.BOLD, 12);
		fontMap[ScriptCodes.ITALIC] = new Font("Serif", Font.ITALIC, 12);
		fontMap[ScriptCodes.TRANSLITERATION] = ResourcesManager.getInstance()
				.getTransliterationFont();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		DrawingSpecificationsImplementation result;
		result = (DrawingSpecificationsImplementation) super.clone();
		result.fontMap = this.fontMap.clone();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#copy()
	 */
	public DrawingSpecification copy() {
		try {
			return (DrawingSpecification) clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getStandardSignHeight()
	 */

	public float getStandardSignHeight() {
		return standardSignHeight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setStandardSignHeight(float)
	 */
	public void setStandardSignHeight(float standardSignHeight) {
		this.standardSignHeight = standardSignHeight;
	}

	/**
	 * @return the tabUnitWidth
	 */
	public float getTabUnitWidth() {
		return tabUnitWidth;
	}

	/**
	 * @param tabUnitWidth
	 *            the tabUnitWidth to set
	 */
	public void setTabUnitWidth(float tabUnitWidth) {
		this.tabUnitWidth = tabUnitWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getBlackColor()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getBlackColor
	 * ()
	 */
	public Color getBlackColor() {
		return blackColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getCartoucheknotLength()
	 */
	public float getCartoucheknotLength() {
		// return cartoucheknotLength;
		return cartoucheLineWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.draw.DrawingSpecifications#getCartoucheEndWidth(int,
	 * int)
	 * 
	 * public float getCartoucheEndWidth(int cartoucheType, int part) { return
	 * getCartouchePartWidth(cartoucheType, part); }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.draw.DrawingSpecifications#getCartoucheLineWidth()
	 * 
	 * public float getCartoucheLineWidth() { return 2.0f; }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getCartoucheLineWidth()
	 */
	public float getCartoucheLineWidth() {
		return cartoucheLineWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getCartoucheLoopLength()
	 */
	public float getCartoucheLoopLength() {
		return cartoucheLoopLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getCartoucheMargin()
	 */
	public float getCartoucheMargin() {
		return cartoucheMargin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * buildCartoucheStroke(int)
	 */
	public Stroke buildCartoucheStroke(int cartoucheType) {
		return new BasicStroke(getCartoucheLineWidth());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getCursorColor
	 * ()
	 */
	public Color getCursorColor() {
		return cursorColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getEnclosureBastionDepth()
	 */
	public float getEnclosureBastionDepth() {
		return enclosureBastionDepth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getEnclosureBastionLength()
	 */
	public float getEnclosureBastionLength() {
		return enclosureBastionLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getEnclosureBastionSkip()
	 */
	public float getEnclosureBastionSkip() {
		return enclosureBastionSkip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getFineStroke()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getFineStroke
	 * ()
	 */
	public Stroke getFineStroke() {
		return new BasicStroke(fineLineWidth);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getFont(
	 * char)
	 */
	public Font getFont(char code) {
		if (code < 0 || code > 255)
			code = 'l';
		Font result = fontMap[code];
		if (result == null)
			result = fontMap['l'];
		return result;
	}

	/**
	 * @see DrawingPreferences#setFont(char, Font)
	 */
	public void setFont(char code, Font font) {
		if (code == '*') {
			this.fontMap[ScriptCodes.LATIN] = font;
			this.fontMap[ScriptCodes.BOLD] = font.deriveFont(Font.BOLD);
			this.fontMap[ScriptCodes.ITALIC] = font.deriveFont(Font.ITALIC);
			this.fontMap[ScriptCodes.TRANSLITERATION] = font
					.deriveFont(Font.ITALIC);
		} else {
			this.fontMap[code] = font;
		}
	}

	/**
	 * @return the yodChoice
	 */
	public YODChoice getYodChoice() {
		return yodChoice;
	}

	/**
	 * @param yodChoice
	 *            the yodChoice to set
	 */
	public void setYodChoice(YODChoice yodChoice) {
		this.yodChoice = yodChoice;
	}

	/**
	 * @return the translitUnicode
	 */
	public boolean isTranslitUnicode() {
		return translitUnicode;
	}

	/**
	 * @param translitUnicode
	 *            the translitUnicode to set
	 */
	public void setTranslitUnicode(boolean translitUnicode) {
		this.translitUnicode = translitUnicode;
	}

	public boolean isGardinerQofUsed() {
		return this.gardinerQofUsed;
	}

	/**
	 * @param gardinerQofUsed
	 *            the gardinerQofUsed to set
	 */
	public void setGardinerQofUsed(boolean gardinerQofUsed) {
		this.gardinerQofUsed = gardinerQofUsed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getFontRenderContext()
	 */
	public FontRenderContext getFontRenderContext() {
		return fontRenderContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getGrayColor()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getGrayColor
	 * ()
	 */
	public Color getGrayColor() {
		return grayColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getHieroglyphsDrawer()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getHieroglyphsDrawer()
	 */
	// public HieroglyphsDrawer getHieroglyphsDrawer() {
	// return hieroglyphsDrawer;
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getHutSmallMargin()
	 */
	public float getHwtSmallMargin() {
		return hutSmallMargin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getHutSquareSize
	 * ()
	 */
	public float getHwtSquareSize() {
		return hutSquareSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getLineSkip()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getLineSkip
	 * ()
	 */
	public float getLineSkip() {
		return lineSkip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getMaxCadratHeight()
	 */
	public float getMaxCadratHeight() {
		return maxCadratHeight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getMaxCadratWidth()
	 */
	public float getMaxCadratWidth() {
		return maxCadratWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getPhilologyWidth(int)
	 */
	public float getPhilologyWidth(int philologyType) {
		// IMPORTANT : this is a hack. We should make it cleaner.
		if (philologyType >= 100)
			philologyType = philologyType / 2;
		float width = 0;
		switch (philologyType) {
		case SymbolCodes.EDITORADDITION:
		case SymbolCodes.EDITORSUPERFLUOUS:
		case SymbolCodes.ERASEDSIGNS:
		case SymbolCodes.SCRIBEADDITION:
		case SymbolCodes.PREVIOUSLYREADABLE:
		case SymbolCodes.MINORADDITION:
		case SymbolCodes.DUBIOUS:
			width = 4;
			break;
		}
		return width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getRedColor()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getRedColor
	 * ()
	 */
	public Color getRedColor() {
		return redColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getSerekhDoorSize()
	 */
	public float getSerekhDoorSize() {
		return serekhDoorSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getSmallSkip()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getSmallSkip
	 * ()
	 */
	public float getSmallSkip() {
		return smallSkip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.draw.DrawingSpecifications#getSuperScriptDimensions
	 * (java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getSuperScriptDimensions(java.lang.String)
	 */
	public Dimension2D getSuperScriptDimensions(String text) {
		Rectangle2D r = superScriptFont.getStringBounds(text.toString(),
				fontRenderContext);
		return new DoubleDimensions(r.getWidth(), r.getHeight());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getSuperScriptFont()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getSuperScriptFont()
	 */
	public Font getSuperScriptFont() {
		return superScriptFont;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.draw.DrawingSpecifications#getTextDimensions(char,
	 * java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getTextDimensions(char, java.lang.String)
	 */
	public Rectangle2D getTextDimensions(char scriptCode, String text) {
		Rectangle2D r = getFont(scriptCode).getStringBounds(text.toString(),
				fontRenderContext);
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getTextDirection
	 * ()
	 */
	public TextDirection getTextDirection() {
		return textDirection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getTextOrientation()
	 */
	public TextOrientation getTextOrientation() {
		return textOrientation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getWideStroke()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getWideStroke
	 * ()
	 */
	public Stroke getWideStroke() {
		return new BasicStroke(wideLineWidth);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * isSmallSignsCentered()
	 */
	public boolean isSmallSignsCentered() {
		return smallSignsCentered;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setCartoucheLineWidth(float)
	 */
	public void setCartoucheLineWidth(float cartoucheLineWidth) {
		this.cartoucheLineWidth = cartoucheLineWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setCartoucheLoopLength(float)
	 */
	public void setCartoucheLoopLength(float cartoucheLoopLength) {
		this.cartoucheLoopLength = cartoucheLoopLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setCartoucheMargin(float)
	 */
	public void setCartoucheMargin(float cartoucheMargin) {
		this.cartoucheMargin = cartoucheMargin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setEnclosureBastionDepth(float)
	 */
	public void setEnclosureBastionDepth(float enclosureBastionDepth) {
		this.enclosureBastionDepth = enclosureBastionDepth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setEnclosureBastionLength(float)
	 */
	public void setEnclosureBastionLength(float enclosureBastionLength) {
		this.enclosureBastionLength = enclosureBastionLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setEnclosureBastionSkip(float)
	 */
	public void setEnclosureBastionSkip(float enclosureBastionSkip) {
		this.enclosureBastionSkip = enclosureBastionSkip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setFontRenderContext(java.awt.font.FontRenderContext)
	 */
	public void setFontRenderContext(FontRenderContext context) {
		fontRenderContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setHieroglyphsDrawer(jsesh.mdcDisplayer.mdcView.HieroglyphsDrawer)
	 */
	// public void setHieroglyphsDrawer(HieroglyphsDrawer drawer) {
	// hieroglyphsDrawer = drawer;
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setHutSmallMargin(float)
	 */
	public void setHutSmallMargin(float hutSmallMargin) {
		this.hutSmallMargin = hutSmallMargin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#setHutSquareSize
	 * (float)
	 */
	public void setHutSquareSize(float hutSquareSize) {
		this.hutSquareSize = hutSquareSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#setLineSkip
	 * (float)
	 */
	public void setLineSkip(float f) {
		lineSkip = f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setMaxCadratHeight(float)
	 */
	public void setMaxCadratHeight(float f) {
		maxCadratHeight = f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setMaxCadratWidth(float)
	 */
	public void setMaxCadratWidth(float maxCadratWidth) {
		this.maxCadratWidth = maxCadratWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setSerekhDoorSize(float)
	 */
	public void setSerekhDoorSize(float serekhDoorSize) {
		this.serekhDoorSize = serekhDoorSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setSmallSignsCentered(boolean)
	 */
	public void setSmallSignsCentered(boolean b) {
		smallSignsCentered = b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#setSmallSkip
	 * (float)
	 */
	public void setSmallSkip(float f) {
		smallSkip = f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#setTextDirection
	 * (jsesh.mdc.constants.TextDirection)
	 */
	public void setTextDirection(TextDirection textDirection) {
		this.textDirection = textDirection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setTextOrientation(jsesh.mdc.constants.TextOrientation)
	 */
	public void setTextOrientation(TextOrientation i) {
		textOrientation = i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getColumnSkip
	 * ()
	 */
	public float getColumnSkip() {
		return columnSkip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#setColumnSkip
	 * (float)
	 */
	public void setColumnSkip(float columnSkip) {
		this.columnSkip = columnSkip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getFineLineWidth
	 * ()
	 */
	public float getFineLineWidth() {
		return fineLineWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#setFineLineWidth
	 * (float)
	 */
	public void setFineLineWidth(float fineLineWidth) {
		this.fineLineWidth = fineLineWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getWideLineWidth
	 * ()
	 */
	public float getWideLineWidth() {
		return wideLineWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#setWideLineWidth
	 * (float)
	 */
	public void setWideLineWidth(float wideLineWidth) {
		this.wideLineWidth = wideLineWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * getBackgroundColor()
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#
	 * setBackgroundColor(java.awt.Color)
	 */
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingPreferences#setBlackColor(java.
	 * awt.Color)
	 */
	public void setBlackColor(Color black) {
		this.blackColor = black;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingPreferences#setCursorColor(java
	 * .awt.Color)
	 */
	public void setCursorColor(Color color) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingPreferences#setGrayColor(java.awt
	 * .Color)
	 */
	public void setGrayColor(Color color) {
		this.grayColor = color;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingPreferences#setRedColor(java.awt
	 * .Color)
	 */
	public void setRedColor(Color color) {
		this.redColor = color;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getSignScale
	 * ()
	 */
	public float getSignScale() {
		return (float) (getStandardSignHeight() / getHieroglyphsDrawer()
				.getHeightOfA1());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getLargeSignSize
	 * ()
	 */
	public double getLargeSignSizeRatio() {
		return largeSignSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#setLargeSignSize
	 * (double)
	 */
	public void setLargeSignSize(double largeSignSize) {
		this.largeSignSize = largeSignSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.preferences.DrawingSpecificationInterface#getSmallSignSize
	 * ()
	 */
	public double getSmallSignSize() {
		return smallSignSize;
	}

	public HieroglyphsDrawer getHieroglyphsDrawer() {
		return hieroglyphsDrawer;
	}

	public double getSmallBodyScaleLimit() {
		return smallBodyScaleLimit;
	}

	public void setSmallBodyScaleLimit(double limit) {
		this.smallBodyScaleLimit = limit;
	}

	public double getGraphicDeviceScale() {
		return graphicDeviceScale;
	}

	public void setGraphicDeviceScale(double graphicDeviceScale) {
		this.graphicDeviceScale = graphicDeviceScale;
	}

	public boolean isPaged() {
		return paged;
	}

	public void setPaged(boolean paged) {
		this.paged = paged;
	}

	public ShadingStyle getShadingStyle() {
		return shadingStyle;
	}

	public void setShadingStyle(ShadingStyle shadingStyle) {
		this.shadingStyle = shadingStyle;
	}

	
	
	/**
	 * Returns a copy of the current page layout. To actually change the page
	 * layout, one needs to modify this copy and set a new layout.
	 */
	public PageLayout getPageLayout() {
		return (PageLayout) pageLayout.clone();
	}

	public void setPageLayout(PageLayout pageLayout) {
		this.pageLayout = pageLayout;
	}

	/**
	 * Gets the parts of those specifications which corresponds to document
	 * preferences.
	 */
	public DocumentPreferences extractDocumentPreferences() {
		DocumentPreferences prefs = new DocumentPreferences()
				.withCartoucheLineWidth(cartoucheLineWidth)
				.withColumnSkip(columnSkip)
				.withLineSkip(lineSkip)
				.withMaxQuadrantHeight(getMaxCadratHeight())
				.withMaxQuadrantWidth(getMaxCadratWidth())
				.withSmallBodyScaleLimit(smallBodyScaleLimit)
				.withSmallSignCentered(isSmallSignsCentered())
				.withStandardSignHeight(standardSignHeight)
				.withTextDirection(textDirection)
				.withTextOrientation(textOrientation)
				.withSmallSkip(smallSkip)
				.withUseLinesForShading(
						getShadingStyle().equals(ShadingStyle.LINE_HATCHING));
		return prefs;
	}

	/**
	 * modify the parts of those specifications which corresponds to document
	 * preferences.
	 */
	public void applyDocumentPreferences(DocumentPreferences prefs) {
		setTextDirection(prefs.getTextDirection());
		setTextOrientation(prefs.getTextOrientation());
		setCartoucheLineWidth((float) prefs.getCartoucheLineWidth());
		setColumnSkip((float) prefs.getColumnSkip());
		setLineSkip((float) prefs.getLineSkip());
		setMaxCadratHeight((float) prefs.getMaxQuadrantHeight());
		setMaxCadratWidth((float) prefs.getMaxQuadrantWidth());
		setSmallBodyScaleLimit(prefs.getSmallBodyScaleLimit());
		setStandardSignHeight((float) prefs.getStandardSignHeight());
		setSmallSignsCentered(prefs.isSmallSignCentered());
		setSmallSkip((float) prefs.getSmallSkip());
		if( prefs.isUseLinesForShading()) {
			setShadingStyle(ShadingStyle.LINE_HATCHING);
		} else
			setShadingStyle(ShadingStyle.GRAY_SHADING);		
	}
	
	
	public TransliterationEncoding getTransliterationEncoding() {
		return new TransliterationEncoding(isTranslitUnicode(), getYodChoice(), isGardinerQofUsed());
	}

	/**
	 * Are the lines (all of them) the same width ?
	 * (temporary measure)
	 * @return true if lines are justified.
	 */
	public boolean isJustified() {
		return justified;
	}

	/**
	 * Change the lines justification (all of them).
	 * @param justified the justified to set
	 */
	public void setJustified(boolean justified) {
		this.justified = justified;
	}
}
