package jsesh.mdcDisplayer.preferences;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;

public interface DrawingPreferences {
	
	// Methods about sign dimensions


	float getTabUnitWidth();
	
	void setTabUnitWidth(float f);
	
	/**
	 * The desired height of a standard "high" sign (like A1), in points.
	 * 
	 * @return the standard sign height.
	 */

	float getStandardSignHeight();

	/**
	 * Sets the desired height (in points) of a standard "high" sign like A1.
	 * 
	 * @param standardSignHeight
	 *            The standardSignHeight to set.
	 */
	void setStandardSignHeight(float standardSignHeight);	

	/**
	 * @return max cadrat height.
	 */
	float getMaxCadratHeight();

	void setMaxCadratHeight(float f);

	/**
	 * @return the maximum width for a cadrat in columns.
	 */
	float getMaxCadratWidth();

	void setMaxCadratWidth(float maxCadratWidth);

	Stroke getFineStroke();

	Stroke getWideStroke();

	/**
	 * Returns the width of fine lines.
	 * 
	 * @return
	 */
	float getFineLineWidth();

	/**
	 * @param fineLineWidth
	 *            The fineLineWidth to set.
	 */
	void setFineLineWidth(float fineLineWidth);

	/**
	 * The width of wide lines.
	 * 
	 * @return
	 */
	float getWideLineWidth();

	/**
	 * @param wideLineWidth
	 *            The wideLineWidth to set.
	 */
	void setWideLineWidth(float wideLineWidth);

	float getPhilologyWidth(int philologyType);

	/**
	 * @return true if small signs are centered.
	 */
	boolean isSmallSignsCentered();

	/**
	 * @param b
	 */
	void setSmallSignsCentered(boolean b);

	

	/**
	 * return the ratio to base size a sign should have to be considered a
	 * "large" sign.
	 * 
	 * @return the largeSignSize
	 */
	double getLargeSignSizeRatio();

	/**
	 * @param largeSignSize
	 *            the largeSignSize to set
	 */
	void setLargeSignSize(double largeSignSize);

	/**
	 * Return the ratio to base size a sign should have to be considered a
	 * "small" sign. i.e. if a sign size is smaller than getBaseLangth() *
	 * getSmallSize(), this sign is small.
	 * 
	 * @return a ratio.
	 */
	double getSmallSignSize();

	// text layout

	float getLineSkip();

	void setLineSkip(float f);

	float getSmallSkip();

	void setSmallSkip(float smallSkip);

	float getColumnSkip();

	void setColumnSkip(float columnSkip);

	/**
	 * Returns informations about the page dimensions and margins.
	 * @return a copy of the current page layout.
	 */
	PageLayout getPageLayout();
	
	void setPageLayout(PageLayout pageLayout);
		
	
	/**
	 * @return Returns the textDirection (left to right or right to left).
	 */
	TextDirection getTextDirection();

	/**
	 * @param textDirection
	 *            The textDirection to set.
	 */
	void setTextDirection(TextDirection textDirection);

	/**
	 * Return the text textOrientation, either horizontal or vertical.
	 * 
	 * @return one of DrawingSpecification.HORIZONTAL or
	 *         DrawingSpecification.VERTICAL.
	 */
	TextOrientation getTextOrientation();

	/**
	 * Sets the text textOrientation, either horizontal or vertical.
	 * 
	 * @param i
	 *            one of DrawingSpecification.HORIZONTAL or
	 *            DrawingSpecification.VERTICAL.
	 */
	void setTextOrientation(TextOrientation i);

	// Methods about colours.

	Color getBlackColor();

	Color getCursorColor();

	Color getGrayColor();

	Color getRedColor();

	Color getBackgroundColor();

	void setBlackColor(Color black);

	void setRedColor(Color color);

	void setGrayColor(Color color);

	void setCursorColor(Color color);

	void setBackgroundColor(Color backgroundColor);
	
	/**
	 * Returns the width of a cartouche knot.
	 * Currently it's the same as cartoucheLineWidth.
	 * TODO CHECK THIS. It's used in CartoucheHelper. What for ?
	 * @deprecated
	 * @return
	 */
	float getCartoucheknotLength();

	/**
	 * @return Returns the cartoucheLineWidth.
	 */
	float getCartoucheLineWidth();

	float getCartoucheLoopLength();

	float getCartoucheMargin();

	float getEnclosureBastionDepth();

	float getEnclosureBastionLength();

	float getEnclosureBastionSkip();

	float getHwtSmallMargin();

	float getHwtSquareSize();

	float getSerekhDoorSize();

	void setSerekhDoorSize(float serekhDoorSize);
	
	void setCartoucheLineWidth(float cartoucheLineWidth);

	void setCartoucheLoopLength(float cartoucheLoopLength);

	void setCartoucheMargin(float cartoucheMargin);

	void setEnclosureBastionDepth(float enclosureBastionDepth);

	void setEnclosureBastionLength(float enclosureBastionLength);

	void setEnclosureBastionSkip(float enclosureBastionSkip);

	void setHutSmallMargin(float hutSmallMargin);

	void setHutSquareSize(float hutSquareSize);

	// Methods about non hieroglyphic fonts
	Font getFont(char code);

	Font getSuperScriptFont();
	
	/**
	 * Returns the size (in points) below which we will start using small body fonts.
	 * For instance, if we set it to 10, any sign drawn with a scale such that the A1 sign height would be 
	 * below the said limit will be drawn in the small body (bold) font.
	 * @return 
	 */
	double getSmallBodyScaleLimit();

	/**
	 * sets the size (in points) below which we will start using small body fonts. 
	 */

	void setSmallBodyScaleLimit(double limit);
	
	/**
	 * Returns the scale of the graphic device, in graphic units per typographical point.
	 * This is the scale used by the device if g.getXScale() returns 1.0, not the current scale.
	 * Note that we could be lying. In the case of a screen zoom, for instance, we will still provide the original scale.
	 */
	double getGraphicDeviceScale() ;

	/**
	 * Gets the scale of the graphic device, in graphic units per typographical point. 
	 */
	void setGraphicDeviceScale(double graphicDeviceScale);
	
	/**
	 * Should the drawing respect page break specifications or draw lines instead ?
	 * @return
	 */
	boolean isPaged();

	/**
	 * Should the drawing respect page break specifications or draw lines instead ?
	 * @param paged
	 */
	void setPaged(boolean paged);
	
	ShadingStyle getShadingStyle();
	
	void setShadingStyle(ShadingStyle style);
	
	
}
