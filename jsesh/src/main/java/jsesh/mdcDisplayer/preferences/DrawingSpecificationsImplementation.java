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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
 * This code is published under the GNU LGPL.
 */
public class DrawingSpecificationsImplementation implements Cloneable,
        DrawingSpecification {

    private Map<String, Color> propertyColors = new HashMap<>();
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

    private float enclosureBastionDepth = 3;

    /*
	 * ------------------------- Enclosure
     */
    private float enclosureBastionLength = 4;

    private Font[] fontMap;

    private YODChoice yodChoice;

    private boolean translitUnicode;

    private FontRenderContext fontRenderContext;


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
     * Temporary system, while waiting for something better. Ask for text
     * justification.
     */
    private boolean justified = false;

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
    private double largeSignSizeRatio = 0.8;

    /**
     * Ratio to base size a sign should have to be considered a "small" sign.
     * i.e. if a sign size is smaller than getBaseLangth() * getSmallSize(),
     * this sign is small.
     */
    private double smallSignSizeRatio = 0.4;

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

    private boolean gardinerQofUsed = true;

    private HashMap<String, Color> tagColors = new HashMap<>();

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
        //tagColors.put("blue", Color.BLUE);
    }

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

    @Override
    public Object clone() throws CloneNotSupportedException {
        DrawingSpecificationsImplementation result;
        result = (DrawingSpecificationsImplementation) super.clone();
        result.fontMap = this.fontMap.clone();
        result.tagColors = new HashMap<>(this.tagColors);
        result.propertyColors = new HashMap<>(propertyColors);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawingSpecification copy() {
        try {
            return (DrawingSpecification) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getStandardSignHeight() {
        return standardSignHeight;
    }

    @Override
    public void setStandardSignHeight(float standardSignHeight) {
        this.standardSignHeight = standardSignHeight;
    }

    @Override
    public float getTabUnitWidth() {
        return tabUnitWidth;
    }

    @Override
    public void setTabUnitWidth(float tabUnitWidth) {
        this.tabUnitWidth = tabUnitWidth;
    }

    @Override
    public Color getBlackColor() {
        return blackColor;
    }

    public float getCartoucheknotLength() {
        // return cartoucheknotLength;
        return cartoucheLineWidth;
    }

    @Override
    public float getCartoucheLineWidth() {
        return cartoucheLineWidth;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public float getCartoucheLoopLength() {
        return cartoucheLoopLength;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public float getCartoucheMargin() {
        return cartoucheMargin;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Stroke buildCartoucheStroke(int cartoucheType) {
        return new BasicStroke(getCartoucheLineWidth());
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Color getCursorColor() {
        return cursorColor;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public float getEnclosureBastionDepth() {
        return enclosureBastionDepth;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public float getEnclosureBastionLength() {
        return enclosureBastionLength;
    }

    @Override
    public Stroke getFineStroke() {
        return new BasicStroke(fineLineWidth);
    }

    /**
     * {@inheritDoc}
     *
     *
     */
    @Override
    public Font getFont(char code) {
        if (code < 0 || code > 255) {
            code = 'l';
        }
        Font result = fontMap[code];
        if (result == null) {
            result = fontMap['l'];
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    public YODChoice getYodChoice() {
        return yodChoice;
    }

    /**
     * @param yodChoice the yodChoice to set
     */
    public void setYodChoice(YODChoice yodChoice) {
        this.yodChoice = yodChoice;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTranslitUnicode() {
        return translitUnicode;
    }

    /**
     * @param translitUnicode the translitUnicode to set
     */
    public void setTranslitUnicode(boolean translitUnicode) {
        this.translitUnicode = translitUnicode;
    }

    public boolean isGardinerQofUsed() {
        return this.gardinerQofUsed;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public Color getGrayColor() {
        return grayColor;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public float getHwtSmallMargin() {
        return hutSmallMargin;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public float getHwtSquareSize() {
        return hutSquareSize;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public float getLineSkip() {
        return lineSkip;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public float getMaxCadratHeight() {
        return maxCadratHeight;
    }

    /**
     * {@inheritDoc}
     *
     * @return
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
    @Override
    public float getPhilologyWidth(int philologyType) {
        // IMPORTANT : this is a hack. We should make it cleaner.
        if (philologyType >= 100) {
            philologyType = philologyType / 2;
        }
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

    public Color getRedColor() {
        return redColor;
    }

    @Override
    public float getSerekhDoorSize() {
        return serekhDoorSize;
    }

    @Override
    public float getSmallSkip() {
        return smallSkip;
    }

    @Override
    public Dimension2D getSuperScriptDimensions(String text) {
        Rectangle2D r = superScriptFont.getStringBounds(text,
                fontRenderContext);
        return new DoubleDimensions(r.getWidth(), r.getHeight());
    }

    @Override
    public Font getSuperScriptFont() {
        return superScriptFont;
    }

    @Override
    public Rectangle2D getTextDimensions(char scriptCode, String text) {
        Rectangle2D r = getFont(scriptCode).getStringBounds(text,
                fontRenderContext);
        return r;
    }

    @Override
    public TextDirection getTextDirection() {
        return textDirection;
    }

    @Override
    public TextOrientation getTextOrientation() {
        return textOrientation;
    }

    @Override
    public Stroke getWideStroke() {
        return new BasicStroke(wideLineWidth);
    }

    @Override
    public boolean isSmallSignsCentered() {
        return smallSignsCentered;
    }

    @Override
    public void setCartoucheLineWidth(float cartoucheLineWidth) {
        this.cartoucheLineWidth = cartoucheLineWidth;
    }

    @Override
    public void setCartoucheLoopLength(float cartoucheLoopLength) {
        this.cartoucheLoopLength = cartoucheLoopLength;
    }

    @Override
    public void setCartoucheMargin(float cartoucheMargin) {
        this.cartoucheMargin = cartoucheMargin;
    }

    @Override
    public void setEnclosureBastionDepth(float enclosureBastionDepth) {
        this.enclosureBastionDepth = enclosureBastionDepth;
    }

    @Override
    public void setEnclosureBastionLength(float enclosureBastionLength) {
        this.enclosureBastionLength = enclosureBastionLength;
    }


    @Override
    public void setFontRenderContext(FontRenderContext context) {
        fontRenderContext = context;
    }

    @Override
    public void setHutSmallMargin(float hutSmallMargin) {
        this.hutSmallMargin = hutSmallMargin;
    }

    @Override
    public void setHutSquareSize(float hutSquareSize) {
        this.hutSquareSize = hutSquareSize;
    }

    @Override
    public void setLineSkip(float f) {
        lineSkip = f;
    }

    @Override
    public void setMaxCadratHeight(float f) {
        maxCadratHeight = f;
    }

    @Override
    public void setMaxCadratWidth(float maxCadratWidth) {
        this.maxCadratWidth = maxCadratWidth;
    }

    @Override
    public void setSerekhDoorSize(float serekhDoorSize) {
        this.serekhDoorSize = serekhDoorSize;
    }

    @Override
    public void setSmallSignsCentered(boolean b) {
        smallSignsCentered = b;
    }

    @Override
    public void setSmallSkip(float f) {
        smallSkip = f;
    }

    @Override
    public void setTextDirection(TextDirection textDirection) {
        this.textDirection = textDirection;
    }

    @Override
    public void setTextOrientation(TextOrientation i) {
        textOrientation = i;
    }

    @Override
    public float getColumnSkip() {
        return columnSkip;
    }

    @Override
    public void setColumnSkip(float columnSkip) {
        this.columnSkip = columnSkip;
    }

    @Override
    public float getFineLineWidth() {
        return fineLineWidth;
    }

    @Override
    public void setFineLineWidth(float fineLineWidth) {
        this.fineLineWidth = fineLineWidth;
    }

    @Override
    public float getWideLineWidth() {
        return wideLineWidth;
    }

    @Override
    public void setWideLineWidth(float wideLineWidth) {
        this.wideLineWidth = wideLineWidth;
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void setBlackColor(Color black) {
        this.blackColor = black;
    }

    @Override
    public void setCursorColor(Color color) {
        this.cursorColor = color;
    }

    @Override
    public void setGrayColor(Color color) {
        this.grayColor = color;
    }

    @Override
    public void setRedColor(Color color) {
        this.redColor = color;
    }

    @Override
    public float getSignScale() {
        return (float) (getStandardSignHeight() / getHieroglyphsDrawer()
                .getHeightOfA1());
    }

    @Override
    public double getLargeSignSizeRatio() {
        return largeSignSizeRatio;
    }

    @Override
    public void setLargeSignSizeRatio(double largeSignSize) {
        this.largeSignSizeRatio = largeSignSize;
    }

    @Override
    public double getSmallSignSizeRatio() {
        return smallSignSizeRatio;
    }

    @Override
    public HieroglyphsDrawer getHieroglyphsDrawer() {
        return hieroglyphsDrawer;
    }

    @Override
    public double getSmallBodyScaleLimit() {
        return smallBodyScaleLimit;
    }

    @Override
    public void setSmallBodyScaleLimit(double limit) {
        this.smallBodyScaleLimit = limit;
    }

    @Override
    public double getGraphicDeviceScale() {
        return graphicDeviceScale;
    }

    @Override
    public void setGraphicDeviceScale(double graphicDeviceScale) {
        this.graphicDeviceScale = graphicDeviceScale;
    }

    @Override
    public boolean isPaged() {
        return paged;
    }

    @Override
    public void setPaged(boolean paged) {
        this.paged = paged;
    }

    @Override
    public ShadingStyle getShadingStyle() {
        return shadingStyle;
    }

    @Override
    public void setShadingStyle(ShadingStyle shadingStyle) {
        this.shadingStyle = shadingStyle;
    }

    /**
     * Returns a copy of the current page layout. To actually change the page
     * layout, one needs to modify this copy and set a new layout.
     */
    @Override
    public PageLayout getPageLayout() {
        return (PageLayout) pageLayout.clone();
    }

    @Override
    public void setPageLayout(PageLayout pageLayout) {
        this.pageLayout = pageLayout;
    }

    /**
     * Gets the parts of those specifications which corresponds to document
     * preferences.
     */
    @Override
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
    @Override
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
        if (prefs.isUseLinesForShading()) {
            setShadingStyle(ShadingStyle.LINE_HATCHING);
        } else {
            setShadingStyle(ShadingStyle.GRAY_SHADING);
        }
    }

    @Override
    public TransliterationEncoding getTransliterationEncoding() {
        return new TransliterationEncoding(isTranslitUnicode(), getYodChoice(), isGardinerQofUsed());
    }

    /**
     * Are the lines (all of them) the same width ? (temporary measure)
     *
     * @return true if lines are justified.
     */
    @Override
    public boolean isJustified() {
        return justified;
    }

    /**
     * Change the lines justification (all of them).
     *
     * @param justified the justified to set
     */
    @Override
    public void setJustified(boolean justified) {
        this.justified = justified;
    }

    @Override
    public Color getColorForProperty(String propertyName) {
        return propertyColors.get(propertyName);
    }

    @Override
    public void setColorForProperty(String propertyName, Color color) {
        propertyColors.put(propertyName, color);
    }

    @Override
    public Optional<Color> getTagColor(String tag) {
        Color c = tagColors.get(tag);
        if (c == null) {
            return Optional.empty();
        } else {
            return Optional.of(c);
        }
    }

    @Override
    public void setTagColor(String tag, Color color) {
        tagColors.put(tag, color);
    }

}
