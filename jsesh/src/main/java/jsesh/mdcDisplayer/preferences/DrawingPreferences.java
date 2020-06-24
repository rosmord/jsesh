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
package jsesh.mdcDisplayer.preferences;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.Optional;

import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.utils.YODChoice;

public interface DrawingPreferences {

	// Methods about sign dimensions
    /**
     * get the width of the unit used in MdC tabulation (in points).
     */
    float getTabUnitWidth();

    /**
     * sets the width of the unit used in MdC tabulation (in points).
     *
     * @param f the width to set.
     */
    void setTabUnitWidth(float f);

    /**
     * The desired height of a standard "high" sign (like A1), in points.
     * Defaults to 18.
     * @return the standard sign height.
     */
    float getStandardSignHeight();

    /**
     * Sets the desired height (in points) of a standard "high" sign like A1.
     *
     * @param standardSignHeight The standardSignHeight to set.
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
     * Returns the width of fine lines. Those are lines created with the MdC
     * {l200,800} construct
     *
     * @return
     */
    float getFineLineWidth();

    /**
     * Sets the width of fine lines
     *
     * @see #getFineLineWidth()
     * @param fineLineWidth The fineLineWidth to set.
     */
    void setFineLineWidth(float fineLineWidth);

    /**
     * Gets the width of wide lines. Those are horizontal lines created with the
     * MdC {L200,800} constructs.
     *
     * @return a width in points
     */
    float getWideLineWidth();

    /**
     * sets the width of wide lines.
     *
     * @see #getWideLineWidth()
     * @param wideLineWidth The wideLineWidth to set.
     */
    void setWideLineWidth(float wideLineWidth);

    /**
     * Get the space to use when drawing ecdotic signs like accolades.
     *
     * @param philologyType
     * @return
     */
    float getPhilologyWidth(int philologyType);

    /**
     * Are small signs vertically centered ?
     *
     * @return true if small signs are centered.
     */
    boolean isSmallSignsCentered();

    /**
     * Choose if quadrants made of a "low sign" like "X1" should be vertically
     * centered.
     *
     * @param centered
     */
    void setSmallSignsCentered(boolean centered);

    /**
     * return the ratio to base size a sign should have to be considered a
     * "large" sign.
     *
     * @return the largeSignSize
     */
    double getLargeSignSizeRatio();

    /**
     * Sets the ratio used to say if a sign is a "large" sign.
     * <p>
     * The ratio is a comparizon with the A1 base size. For instance, if the
     * ration is 0.8, any sign at least as tall as 0.8*height of A1 will be
     * considered large.
     *
     * @param largeSignSize the largeSignSize to set
     */
    void setLargeSignSizeRatio(double largeSignSize);

    /**
     * Return the ratio to base size a sign should have to be considered a
     * "small" sign. i.e. if a sign size is smaller than getBaseLangth() *
     * getSmallSize(), this sign is small.
     *
     * @return a ratio.
     */
    double getSmallSignSizeRatio();
    
    float getLineSkip();

    void setLineSkip(float f);

    /**
     * gets the separation between adjacent quadrants.
     *
     * @return the separation, in points.
     */
    float getSmallSkip();

    /**
     * sets the separation between adjacent quadrants.
     *
     * @param smallSkip the skip, in points.
     */
    void setSmallSkip(float smallSkip);

    float getColumnSkip();

    void setColumnSkip(float columnSkip);

    /**
     * Returns informations about the page dimensions and margins.
     *
     * @return a copy of the current page layout.
     */
    PageLayout getPageLayout();

    void setPageLayout(PageLayout pageLayout);

    /**
     * @return Returns the textDirection (left to right or right to left).
     */
    TextDirection getTextDirection();

    /**
     * @param textDirection The textDirection to set.
     */
    void setTextDirection(TextDirection textDirection);

    /**
     * Return the text textOrientation, either horizontal or vertical.
     *
     * @return one of DrawingSpecification.HORIZONTAL or
     * DrawingSpecification.VERTICAL.
     */
    TextOrientation getTextOrientation();

    /**
     * Sets the text textOrientation, either horizontal or vertical.
     *
     * @param i one of DrawingSpecification.HORIZONTAL or
     * DrawingSpecification.VERTICAL.
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
     * Returns the color associated with a given tag (like A1\red, or
     * A1\det).
     * @param tag
     * @return a color, if any is specified.
     */
    Optional<Color> getTagColor(String tag);
    
    void setTagColor(String tag, Color color);
    
    /**
     * Returns the width of a cartouche knot. Currently it's the same as
     * cartoucheLineWidth. TODO CHECK THIS. It's used in CartoucheHelper. What
     * for ?
     *
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

    float getHwtSmallMargin();

    float getHwtSquareSize();

    float getSerekhDoorSize();

    void setSerekhDoorSize(float serekhDoorSize);

    void setCartoucheLineWidth(float cartoucheLineWidth);

    void setCartoucheLoopLength(float cartoucheLoopLength);

    void setCartoucheMargin(float cartoucheMargin);

    void setEnclosureBastionDepth(float enclosureBastionDepth);

    void setEnclosureBastionLength(float enclosureBastionLength);

    void setHutSmallMargin(float hutSmallMargin);

    void setHutSquareSize(float hutSquareSize);

	// Methods about non hieroglyphic fonts
    /**
     * Sets the font for a particular type of text. The character code are
     * described by the ScriptCodes class.
     *
     * @param code the code for the font to use ('l' for latin, 'i' for italic...)
     * @return the font to use.
     * @see ScriptCodes
     */
    Font getFont(char code);

    /**
     * Gets the font to use for superscript text.
     *
     * @return
     */
    Font getSuperScriptFont();

    /**
     * Sets the font to use for a particular kind of code.
     * <p>
     * Note that if code is '*', all latin fonts will be affected. The parameter
     * font will be used as base, and the other ones will be derived from it.
     *
     * @param code
     * @param font
     * @see ScriptCodes for all codes.
     */
    void setFont(char code, Font font);

    /**
     * States if the transliteration font uses unicode or the manuel de codage.
     * If the font is unicode, more information is given by
     * {@link #getYodChoice()}
     *
     * @param translitUnicode
     */
    void setTranslitUnicode(boolean translitUnicode);

    /**
     * States if the transliteration font uses unicode or the manuel de codage.
     *
     * @return 
     */
    boolean isTranslitUnicode();

    /**
     * Choose the system to use to draw the yod sign (only for unicode font).
     *
     * @param yodChoice
     */
    void setYodChoice(YODChoice yodChoice);

    /**
     * Returns the system to use to draw the yod sign (only for unicode font).
     * @return 
     */
    YODChoice getYodChoice();

    /**
     * Returns the size (in points) below which we will start using small body
     * fonts. For instance, if we set it to 10, any sign drawn with a scale such
     * that the A1 sign height would be below the said limit will be drawn in
     * the small body (bold) font.
     *
     * @return
     */
    double getSmallBodyScaleLimit();

    /**
     * sets the size (in points) below which we will start using small body
     * fonts.
     * @param limit
     */
    void setSmallBodyScaleLimit(double limit);

    /**
     * Returns the scale of the graphic device, in graphic units per
     * typographical point. This is the scale used by the device if
     * g.getXScale() returns 1.0, not the current scale. Note that we could be
     * lying. In the case of a screen zoom, for instance, we will still provide
     * the original scale.
     * @return 
     */
    double getGraphicDeviceScale();

    /**
     * Gets the scale of the graphic device, in graphic units per typographical
     * point.
     * @param graphicDeviceScale
     */
    void setGraphicDeviceScale(double graphicDeviceScale);

    /**
     * Should the drawing respect page break specifications or draw lines
     * instead ?
     *
     * @return
     */
    boolean isPaged();

    /**
     * Should the drawing respect page break specifications or draw lines
     * instead ?
     *
     * @param paged
     */
    void setPaged(boolean paged);

    ShadingStyle getShadingStyle();

    void setShadingStyle(ShadingStyle style);

    /**
     * (TEMPORARY) should text be justified. (Here, justified means 'all lines
     * will have the same width'.
     *
     * @return true if text is justified
     */
    boolean isJustified();

    /**
     * Ask for justification of the text.
     *
     * @param justified
     */
    void setJustified(boolean justified);
}
