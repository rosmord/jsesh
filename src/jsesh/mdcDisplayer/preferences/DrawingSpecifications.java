/*
 * Created on 28 mai 2004
 *
 * This code is published under the LGPL.
 */
package jsesh.mdcDisplayer.preferences;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;

import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdcDisplayer.mdcView.HieroglyphsDrawer;
import jsesh.mdcDisplayer.mdcView.SimpleHieroglyphicDrawer;
import jsesh.resources.ResourcesManager;
import jsesh.utils.DoubleDimensions;

/**
 * Informations about line width, height... Should probably be simpler (and cut
 * in two : one part for user customization, and one part for more technical
 * stuff). Good candidate for the prototype <em>design pattern</em>.
 * 
 * 
 * <p>
 * We intend to move any computation <em>out</em> of this class to simplify
 * its use.
 * <p>
 * In particular, the elaborate code for cartouche contained generic methods for
 * all kinds of cartouches. This worked more or less, but proved difficult to
 * extend. Hence a more basic version now.
 * 
 * <h2>Cartouche related dimensions (add "cartouche" to get the real name of
 * the variables) :</h2>
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
public class DrawingSpecifications implements Cloneable {
    private float baseLength = -1;

    private float cadratWidth;

    private float cartoucheknotLength = 2;

    /*------------------------
     * Cartouche-related variables.
     *
     */

    private float cartoucheLineWidth = 2;

    private float cartoucheLoopLength = 10;

    private float cartoucheMargin = 2;

    private Color cursorColor;

    private float enclosureBastionDepth = 5;

    /*
     * ------------------------- Enclosure
     * 
     *  
     */

    private float enclosureBastionLength = 10;
    
    private float enclosureBastionSkip= 5;

    private Font[] fontMap;

    private FontRenderContext fontRenderContext;

    private HieroglyphsDrawer hieroglyphsDrawer;

    /*
     * ------------------------- Hut-related variables
     *  
     */

    /**
     * The margin between the hut side and the first sign, when there is no
     * square. Note that the start point is the internal side of the contour
     * line.
     */
    private float hutSmallMargin = 3;

    private float hutSquareSize = 10;

    private float leftMargin;

    private float lineSkip;
    
    private float columnSkip;

    private float maxCadratHeight;
    
    private float maxCadratWidth;

    private float pageWidth;

    /*
     * ------------------------- Serekh-related variables
     *  
     */

    private float serekhDoorSize = 20;

    private boolean smallSignsCentered;

    private float smallSkip;

    private Font superScriptFont;

    private TextDirection textDirection;
    
    private TextOrientation textOrientation;

    /**
     * Top text page margin.
     */
    private float topMargin;

    /**
     * The expected size for a page. 
     * If null, the size of the generated pictures will depend on the contents of the pages.
     * if not, the text rendering will accomodate the page sizes.
     */
    private PageFormat pageFormat;

	private float fineLineWidth;

	private float wideLineWidth;

	private Color backgroundColor= Color.WHITE;

	/**
     * The desired height of a standard "high" sign (like A1), in points.
     */
	private float standardSignHeight;

	/**
	 * When a hight sign is alone in a column (resp. a wide sign alone in a row),
	 * it will be enlarged so as to fill the entire height (resp. width).
	 */
	private boolean enlargeLargeLoneSigns;
	
	/**
	 * Ratio to base size a sign should have to be considered a "large" sign.
	 */
	private double largeSignSize= 0.8;

	/**
	 * Ratio to base size a sign should have to be considered a "small" sign.
	 * i.e. if a sign size is smaller than getBaseLangth() * getSmallSize(), 
	 * this sign is small.
	 */
	private double smallSignSize= 0.4;

    //private float
    public DrawingSpecifications() {
        fontRenderContext = new FontRenderContext(new AffineTransform(), true,
                true);
        smallSignsCentered = false;
        pageWidth = 80 * 14;
        smallSkip = 2;
        lineSkip = 6;
        columnSkip= 10;
        fineLineWidth= 0.5f;
        wideLineWidth= 3f;
        textOrientation = TextOrientation.HORIZONTAL;
        textDirection = TextDirection.LEFT_TO_RIGHT;
        hieroglyphsDrawer = new SimpleHieroglyphicDrawer();
        cadratWidth = 16;
        maxCadratHeight = 18f;
        maxCadratWidth= 22f;
        topMargin = smallSkip + getCartoucheLineWidth() + getCartoucheMargin();
        leftMargin = 3 * smallSkip;

        standardSignHeight= getBaseLength();
        //		TODO : ensure system independance for font ?.
        //latinFont= Font.createFont(Font.TRUETYPE_FONT, in);
        // IMPORTANT : Load fonts only once !
        buildFontMap();

        superScriptFont = new Font(null, Font.PLAIN, 5);
        cursorColor = new Color(0, 0, 255, 100);       
        pageFormat= null;
        enlargeLargeLoneSigns= true;
    }

    /**
     *  
     */
    private void buildFontMap() {
        fontMap = new Font[256];
        //fontMap[ScriptCodes.LATIN] = new Font("Serif", Font.PLAIN, 12);
        fontMap[ScriptCodes.LATIN] = ResourcesManager.getInstance().getUnicodeFont();
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
    public Object clone() throws CloneNotSupportedException {
        DrawingSpecifications result;
        result = (DrawingSpecifications) super.clone();
        result.buildFontMap();
        return result;
    }

    /**
     * Compute the size needed for cartouches starts and ends along the main
     * axis. Use the basic sizes values declared in this class.
     * 
     * @param type
     *            the type of cartouche ('c', 's', 'h', 'f')
     * @param element :
     *            0,1,2,3, depending on the type of cartouche.
     * @return a size.
     */

    public float computeCartouchePartLength(int type, int element) {
        float result = 0;
        switch (type) {
        case 'c':
            switch (element) {
            case 0:
                result = 0;
                break;
            case 1:
                result = getCartoucheLoopLength();
                break;
            case 2:
                result = getCartoucheLoopLength() + getCartoucheknotLength();
                break;
            default:
                throw new RuntimeException("bad value for element " + element);
            }
            break;
        case 's':
            switch (element) {
            case 1:
                result = getHutSmallMargin()
                        + getCartoucheLineWidth();
                break;
            case 2:
                result = getHutSmallMargin()
                        + getCartoucheLineWidth()
                        + getSerekhDoorSize();
                break;
            }
            break;
        case 'h':
            switch (element) {
            case 0:
                break;
            case 1:
                result = getHutSmallMargin() + getCartoucheLineWidth();
                break;
            case 2:
            case 3:
                result = getHutSmallMargin() + getCartoucheLineWidth()
                        + getHutSquareSize();
                break;
            default:
                throw new RuntimeException("bad value for element " + element);
            }
            break;
        case 'f':
            if (element != 0) {
                result = getHutSmallMargin()
                        + getEnclosureBastionDepth()
                        + getCartoucheLineWidth();
            }
            break;
        }
        return result;
    }

    /**
     * length along the secondary axis of a cartouche from the cartouche
     * external side to the text inside.
     * 
     * @param type
     * @return the length along the secondary axis of a cartouche from the
     *         cartouche external side to the text inside.
     */
    public float computeCartoucheSecondaryLength(int type) {
        float result = 0;
        switch (type) {
        case 'c':
        case 's':
        case 'h':
            result = getCartoucheMargin() + getCartoucheLineWidth();
            break;
        case 'f':
            result = getCartoucheMargin() + getCartoucheLineWidth()
                    + getEnclosureBastionDepth();
            break;
        }
        return result;
    }

    /**
     * Type-aware clone method.
     * 
     * @return a copy of DrawingSpecifications
     */
    public DrawingSpecifications copy() {
        try {
            return (DrawingSpecifications) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

   
    /**
     * The height of an A1 sign in the current fonts (in font units).
     * @return The height of an A1 sign.
     */
    public float getBaseLength() {
        if (baseLength == -1) {
            Rectangle2D r = hieroglyphsDrawer.getBBox("A1");
            baseLength = (float) r.getHeight();
        }
        return baseLength;
    }

    
    /**
     * The desired height of a standard "high" sign (like A1), in points.
     * @return the standard sign height.
     */
    
    public float getStandardSignHeight() {
    	return standardSignHeight;
    }
    
    /**
     * Sets the desired height (in points) of a standard "high" sign like A1.
	 * @param standardSignHeight The standardSignHeight to set.
	 */
	public void setStandardSignHeight(float standardSignHeight) {
		this.standardSignHeight = standardSignHeight;
	}
    
    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getBlackColor()
     */
    public Color getBlackColor() {
        return Color.BLACK;
    }

    /**
     * the desired cadrat width.
     * 
     * @return the desired width.
     * @see #setCadratWidth
     */
    public float getCadratWidth() {
        return cadratWidth;
    }
    
    public float getCartoucheknotLength() {
        return cartoucheknotLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getCartoucheEndWidth(int,
     *      int)
     * 
     * public float getCartoucheEndWidth(int cartoucheType, int part) { return
     * getCartouchePartWidth(cartoucheType, part); }
     */

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getCartoucheLineWidth()
     * 
     * public float getCartoucheLineWidth() { return 2.0f; }
     */

    /**
     * @return Returns the cartoucheLineWidth.
     */
    public float getCartoucheLineWidth() {
        return cartoucheLineWidth;
    }

    public float getCartoucheLoopLength() {
        return cartoucheLoopLength;
    }

    public float getCartoucheMargin() {
        return cartoucheMargin;
    }


    /**
     * Compute the stroke which correspond to cartouche lines.
     * Utility method.
     */
    public Stroke buildCartoucheStroke(int cartoucheType) {
        return new BasicStroke(getCartoucheLineWidth());
    }

    public Color getCursorColor() {
        return cursorColor;
    }

    public float getEnclosureBastionDepth() {
        return enclosureBastionDepth;
    }

    public float getEnclosureBastionLength() {
        return enclosureBastionLength;
    }

    public float getEnclosureBastionSkip() {
        return enclosureBastionSkip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getFineStroke()
     */
    public Stroke getFineStroke() {
        return new BasicStroke(fineLineWidth);
    }

    public Font getFont(char code) {
        if (code < 0 || code > 255)
            code = 'l';
        Font result = fontMap[code];
        if (result == null)
            result = fontMap['l'];
        return result;
    }

    /**
     * @return the font render context.
     */
    public FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getGrayColor()
     */
    public Color getGrayColor() {
        Color gray = new Color(0.5f, 0.5f, 0.5f, 0.5f);
        return gray;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getHieroglyphsDrawer()
     */
    public HieroglyphsDrawer getHieroglyphsDrawer() {
        return hieroglyphsDrawer;
    }

    public float getHutSmallMargin() {
        return hutSmallMargin;
    }

    public float getHutSquareSize() {
        return hutSquareSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getLeftMargin()
     */
    public float getLeftMargin() {
        return leftMargin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getLineSkip()
     */
    public float getLineSkip() {
        return lineSkip;
    }

    /**
     * @return max cadrat height.
     */
    public float getMaxCadratHeight() {
        return maxCadratHeight;
    }

    /**
     * @return the maximum width for a cadrat in columns.
     */
    public float getMaxCadratWidth() {     
        return maxCadratWidth;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getTextWidth()
     */
    public float getPageWidth() {
        return pageWidth;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getPhilologyWidth(int)
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
    public Color getRedColor() {
        return Color.RED;
    }

   
    public float getSerekhDoorSize() {
        return serekhDoorSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getSmallSkip()
     */
    public float getSmallSkip() {
        return smallSkip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getSuperScriptDimensions(java.lang.String)
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
    public Font getSuperScriptFont() {
        return superScriptFont;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getTextDimensions(char,
     *      java.lang.String)
     */
    public Rectangle2D getTextDimensions(char scriptCode, String text) {
        Rectangle2D r = getFont(scriptCode).getStringBounds(text.toString(),
                fontRenderContext);
        return r;
    }

    /**
     * @return Returns the textDirection.
     */
    public TextDirection getTextDirection() {
        return textDirection;
    }

    /**
     * Return the text textOrientation, either horizontal or verical.
     * 
     * @return one of DrawingSpecification.HORIZONTAL or
     *         DrawingSpecification.VERTICAL.
     */
    public TextOrientation getTextOrientation() {
        return textOrientation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getTopMargin()
     */
    public float getTopMargin() {
        return topMargin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getWideStroke()
     */
    public Stroke getWideStroke() {
    	return  new BasicStroke(wideLineWidth);
    }

    /**
     * @return true if small signs are centered.
     */
    public boolean isSmallSignsCentered() {
        return smallSignsCentered;
    }

    public void setBaseLength(float baseLength) {
        this.baseLength = baseLength;
    }

    /**
     * Sets the desired width for a cadrat. This width is used as a maximal
     * width in horizontal mode (except for cadrats made only of one line, which
     * are not reduced) ; In column mode, this width is in fact the column
     * width.
     * 
     * @param f
     */
    public void setCadratWidth(float f) {
        cadratWidth = f;
    }

    public void setCartoucheknotLength(float cartoucheknotLength) {
        this.cartoucheknotLength = cartoucheknotLength;
    }

    public void setCartoucheLineWidth(float cartoucheLineWidth) {
        this.cartoucheLineWidth = cartoucheLineWidth;
    }

    public void setCartoucheLoopLength(float cartoucheLoopLength) {
        this.cartoucheLoopLength = cartoucheLoopLength;
    }

    public void setCartoucheMargin(float cartoucheMargin) {
        this.cartoucheMargin = cartoucheMargin;
    }

    public void setEnclosureBastionDepth(float enclosureBastionDepth) {
        this.enclosureBastionDepth = enclosureBastionDepth;
    }

    public void setEnclosureBastionLength(float enclosureBastionLength) {
        this.enclosureBastionLength = enclosureBastionLength;
    }
    
    public void setEnclosureBastionSkip(float enclosureBastionSkip) {
        this.enclosureBastionSkip = enclosureBastionSkip;
    }

    /**
     * @param context
     */
    public void setFontRenderContext(FontRenderContext context) {
        fontRenderContext = context;
    }

    /**
     * @param drawer
     */
    public void setHieroglyphsDrawer(HieroglyphsDrawer drawer) {
        hieroglyphsDrawer = drawer;
    }

    public void setHutSmallMargin(float hutSmallMargin) {
        this.hutSmallMargin = hutSmallMargin;
    }

    public void setHutSquareSize(float hutSquareSize) {
        this.hutSquareSize = hutSquareSize;
    }

    public void setLeftMargin(float leftMargin) {
        this.leftMargin = leftMargin;
    }

    /**
     * @param f
     */
    public void setLineSkip(float f) {
        lineSkip = f;
    }

    /**
     * @param f
     */
    public void setMaxCadratHeight(float f) {
        maxCadratHeight = f;
    }

    public void setMaxCadratWidth(float maxCadratWidth) {
        this.maxCadratWidth = maxCadratWidth;
    }

    /**
     * @param f
     */
    public void setPageWidth(float f) {
        pageWidth = f;
    }


    public void setSerekhDoorSize(float serekhDoorSize) {
        this.serekhDoorSize = serekhDoorSize;
    }

    /**
     * @param b
     */
    public void setSmallSignsCentered(boolean b) {
        smallSignsCentered = b;
    }

    /**
     * @param f
     */
    public void setSmallSkip(float f) {
        smallSkip = f;
    }

    /**
     * @param textDirection
     *            The textDirection to set.
     */
    public void setTextDirection(TextDirection textDirection) {
        this.textDirection = textDirection;
    }

    /**
     * Sets the text textOrientation, either horizontal or vertical.
     * 
     * @param i
     *            one of DrawingSpecification.HORIZONTAL or
     *            DrawingSpecification.VERTICAL.
     */
    public void setTextOrientation(TextOrientation i) {
        textOrientation = i;
    }

    public void setTopMargin(float topMargin) {
        this.topMargin = topMargin;
    }

   
    public float getColumnSkip() {
        return columnSkip;
    }
    public void setColumnSkip(float columnSkip) {
        this.columnSkip = columnSkip;
    }

	/**
	 * Get The expected size for a page. 
     * If null, the size of the generated pictures will depend on the contents of the pages.
     * if not, the text rendering will accomodate the page sizes.	
	 * @return Returns the pageFormat.
	 */
	public PageFormat getPageFormat() {
		return pageFormat;
	}

	/**
	 * @param pageFormat The pageFormat to set.
	 */
	public void setPageFormat(PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}

	/**
	 * Returns the width of fine lines.
	 * @return
	 */
	public float getFineLineWidth() {
		return fineLineWidth;
	}
	
	/**
	 * @param fineLineWidth The fineLineWidth to set.
	 */
	public void setFineLineWidth(float fineLineWidth) {
		this.fineLineWidth = fineLineWidth;
	}
    
	/**
	 * The width of wide lines.
	 * @return
	 */
	public float getWideLineWidth() {
		return wideLineWidth;
	}
	
	/**
	 * @param wideLineWidth The wideLineWidth to set.
	 */
	public void setWideLineWidth(float wideLineWidth) {
		this.wideLineWidth = wideLineWidth;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}
	
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	/**
	 * Returns the scale which should be applied to the current font to obtain the desired sign height.
	 * @return the scale to apply to the current glyph font.
	 */
	public float getSignScale() {
		return getStandardSignHeight() / getBaseLength();
	}

	/**
	 * @return the enlargeLargeLoneSigns
	 */
	public boolean isEnlargeLargeLoneSigns() {
		return enlargeLargeLoneSigns;
	}

	/**
	 * @param enlargeLargeLoneSigns the enlargeLargeLoneSigns to set
	 */
	public void setEnlargeLargeLoneSigns(boolean enlargeLargeLoneSigns) {
		this.enlargeLargeLoneSigns = enlargeLargeLoneSigns;
	}

	/**
	 * return the ratio to base size a sign should have to be considered a "large" sign.
	 * @return the largeSignSize
	 */
	public double getLargeSignSize() {
		return largeSignSize;
	}

	/**
	 * @param largeSignSize the largeSignSize to set
	 */
	public void setLargeSignSize(double largeSignSize) {
		this.largeSignSize = largeSignSize;
	}

	/**
	 * Return the ratio to base size a sign should have to be considered a "small" sign.
	 * i.e. if a sign size is smaller than getBaseLangth() * getSmallSize(), 
	 * this sign is small.
	 * @return a ratio.
	 */
	public double getSmallSignSize() {
		return smallSignSize;
	}
	
	
}
