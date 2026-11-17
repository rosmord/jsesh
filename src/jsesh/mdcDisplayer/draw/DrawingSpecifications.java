/*
 * Created on 28 mai 2004
 *
 * This code is published under the LGPL.
 */
package jsesh.mdcDisplayer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.resources.ResourcesManager;
import jsesh.utils.DoubleDimensions;

/**
 * Informations about line width, height...
 * 
 * Good candidate for the prototype <em>design pattern</em>.
 * 
 * @author rosmord
 *
 *This code is published under the GNU LGPL.
 */
public class DrawingSpecifications implements Cloneable
{
	/**
	 * Constant for horizontal text.
	 */
	public static final int HORIZONTAL= 0;
	
	/**
	 * Constant for vertical text.
	 */
	public static final int VERTICAL= 1;
	
	private float baseLength= -1;
	private float cadratWidth;

	private FontRenderContext fontRenderContext;
	private Font[] fontMap;
	private HieroglyphsDrawer hieroglyphsDrawer;
	private float lineSkip;
	private float maxCadratHeight;
	
	private int orientation;
	private float pageWidth;
	private float ruleWidth;
	private boolean smallSignsCentered;
	private float smallSkip;
	private Font superScriptFont;
	private Stroke wideStroke;
	private float topMargin;
	private float leftMargin;
	private Color cursorColor;

	public DrawingSpecifications()
	{
		fontRenderContext= new FontRenderContext(new AffineTransform(), true, true);
		smallSignsCentered= false;
		pageWidth= 80 * 14;
		smallSkip= 2;
		lineSkip= 6;
		ruleWidth= 2f;
		orientation= HORIZONTAL;
		hieroglyphsDrawer= new SimpleHieroglyphicDrawer();
		cadratWidth= 16;
		wideStroke= new BasicStroke(1.5f * ruleWidth);
		maxCadratHeight= 15f;
		topMargin= smallSkip + getCartoucheLineWidth() + getCartoucheTopMargin('c');
		leftMargin= 3*smallSkip;

		//		TODO : ensure system independance for font ?.
		//latinFont= Font.createFont(Font.TRUETYPE_FONT, in);
		// IMPORTANT : Load fonts only once !
		buildFontMap();
		
		superScriptFont= new Font(null, Font.PLAIN, 5);
		cursorColor= new Color(0,0,255,100);

	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		DrawingSpecifications result;
		result= (DrawingSpecifications) super.clone();
		result.buildFontMap();
		return result;
	}

	/**
	 * Type-aware clone method.
	 * @return
	 */
	public DrawingSpecifications copy() {
		try {
			return (DrawingSpecifications) clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);			
		}
	}
	
	/**
	 * 
	 */
	private void buildFontMap() {
		fontMap= new Font[256];
		fontMap[ScriptCodes.LATIN]= new Font("Serif", Font.PLAIN, 12);
		fontMap[ScriptCodes.BOLD]= new Font("Serif", Font.BOLD, 12);
		fontMap[ScriptCodes.ITALIC]= new Font("Serif", Font.ITALIC, 12);
		fontMap[ScriptCodes.TRANSLITERATION]=
			ResourcesManager.getInstance().getTransliterationFont();
	}
	
	private void doCopy(DrawingSpecifications specs) {
		
	}
	
	
	/**
	 * The height of an A1 sign.
	 * @return
	 */
	public float getBaseLength() {
		if (baseLength == -1) {
			Rectangle2D r = hieroglyphsDrawer.getBBox("A1");
			baseLength=  (float) r.getHeight();
		}
		return baseLength;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getBlackColor()
	 */
	public Color getBlackColor()
	{
		return Color.BLACK;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getCadratWidth()
	 */
	public float getCadratWidth()
	{
		return cadratWidth;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getCartoucheEndWidth(int, int)
	 */
	public float getCartoucheEndWidth(int cartoucheType, int part)
	{
		return getCartouchePartWidth(cartoucheType, part);
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getCartoucheLineWidth()
	 */
	public float getCartoucheLineWidth()
	{
		return 2.0f;
	}


	private float getCartouchePartWidth(int cartoucheType, int part) {
		if (part == 0)
					return 0;
				else
					if (cartoucheType == 's' && part == 2)
						return 20;
				else if ((cartoucheType == 'h' || cartoucheType == 's') && part == 1)
					return 2;
				else
					return 5;
	}
	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getCartoucheStartWidth(int, int)
	 */
	public float getCartoucheStartWidth(int cartoucheType, int part)
	{
		return getCartouchePartWidth(cartoucheType, part);
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getCartoucheStroke(jsesh.mdc.model.Cartouche)
	 */
	public Stroke getCartoucheStroke(int cartoucheType)
	{
		return new BasicStroke(1.5f);
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getCartoucheTopMargin(int)
	 */
	public float getCartoucheTopMargin(int cartoucheType)
	{
		return 2f;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getCursorColor()
	 */
	public Color getCursorColor()
	{
		return cursorColor;
	}

	/**
	 * @return
	 */
	public FontRenderContext getFontRenderContext() {
		return fontRenderContext;
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getFineStroke()
	 */
	public Stroke getFineStroke()
	{
		return new BasicStroke(0.5f);
	}

	public Font getFont(char code)
	{
		if (code < 0 || code > 255)
			code= 'l';
		Font result= fontMap[code];
		if (result == null)
			result= fontMap['l'];
		return result;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getGrayColor()
	 */
	public Color getGrayColor()
	{
		Color gray= new Color(0.5f, 0.5f, 0.5f, 0.5f);
		return gray;
	}

	
	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getHieroglyphsDrawer()
	 */
	public HieroglyphsDrawer getHieroglyphsDrawer()
	{
		return hieroglyphsDrawer;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getLineSkip()
	 */
	public float getLineSkip()
	{
		return lineSkip;
	}

	/**
	 * @return
	 */
	public float getMaxCadratHeight()
	{
		return maxCadratHeight;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getOrientation()
	 */
	public int getOrientation()
	{
		return orientation;
	}


	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getTextWidth()
	 */
	public float getPageWidth()
	{
		return pageWidth;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getPhilologyWidth(int)
	 */
	public float getPhilologyWidth(int philologyType)
	{
		// IMPORTANT : this is a hack. We should make it cleaner.
		if(philologyType >= 100)
			philologyType= philologyType/2;
		float width=0;
		switch (philologyType) {
			case SymbolCodes.EDITORADDITION:
			case SymbolCodes.EDITORSUPERFLUOUS:
			case SymbolCodes.ERASEDSIGNS:
			case SymbolCodes.SCRIBEADDITION:
			case SymbolCodes.PREVIOUSLYREADABLE:
				width= 4;
			break;
		}
		return width;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getRedColor()
	 */
	public Color getRedColor()
	{
		return Color.RED;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getRuleWidth()
	 */
	public float getRuleWidth()
	{
		return ruleWidth;
	}


	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getSmallSkip()
	 */
	public float getSmallSkip()
	{
		return smallSkip;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getSuperScriptDimensions(java.lang.String)
	 */
	public Dimension2D getSuperScriptDimensions(String text) {
		Rectangle2D r=
			superScriptFont.getStringBounds(text.toString(), fontRenderContext);
		return new DoubleDimensions(r.getWidth(), r.getHeight());
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getSuperScriptFont()
	 */
	public Font getSuperScriptFont() {
		return superScriptFont;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getTextDimensions(char, java.lang.String)
	 */
	public Rectangle2D getTextDimensions(char scriptCode, String text)
	{
		Rectangle2D r=
			getFont(scriptCode).getStringBounds(text.toString(), fontRenderContext);
		return r;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getWideStroke()
	 */
	public Stroke getWideStroke()
	{
		return wideStroke;
	}

	/**
	 * @return
	 */
	public boolean isSmallSignsCentered()
	{
		return smallSignsCentered;
	}

	/**
	 * @param f
	 */
	public void setCadratWidth(float f) {
		cadratWidth = f;
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

	/**
	 * @param i
	 */
	public void setOrientation(int i) {
		orientation = i;
	}

	/**
	 * @param f
	 */
	public void setPageWidth(float f) {
		pageWidth = f;
	}

	/**
	 * @param f
	 */
	public void setRuleWidth(float f) {
		ruleWidth = f;
	}

	/**
	 * @param b
	 */
	public void setSmallSignsCentered(boolean b)
	{
		smallSignsCentered= b;
	}

	/**
	 * @param f
	 */
	public void setSmallSkip(float f) {
		smallSkip = f;
	}

	/**
	 * @param stroke
	 */
	public void setWideStroke(Stroke stroke) {
		wideStroke = stroke;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getLeftMargin()
	 */
	public float getLeftMargin() {
		return leftMargin;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.DrawingSpecifications#getTopMargin()
	 */
	public float getTopMargin() {
		return topMargin;
	}
	
	
	public void setBaseLength(float baseLength) {
		this.baseLength = baseLength;
	}
	
	public void setLeftMargin(float leftMargin) {
		this.leftMargin = leftMargin;
	}
	
	public void setTopMargin(float topMargin) {
		this.topMargin = topMargin;
	}
	
}
