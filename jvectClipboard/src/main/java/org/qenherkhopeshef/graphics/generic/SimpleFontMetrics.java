/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 24 juin 2005
 *
 */
package org.qenherkhopeshef.graphics.generic;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 * @author Serge Rosmorduc
 */
public class SimpleFontMetrics extends FontMetrics {
    /*
    *  getAscent()
    * getLeading()
    * getMaxAdvance()
    * charWidth(char)
    * charsWidth(char[], int, int)
    */
    
    /**
     * @param font
     */
    protected SimpleFontMetrics(Font font) {
        super(font);       
    }

    /* (non-Javadoc)
     * @see java.awt.FontMetrics#getAscent()
     */
    public int getAscent() {
    	// parent calls the font size.
        return super.getAscent();
    }
    
    /* (non-Javadoc)
     * @see java.awt.FontMetrics#getLeading()
     */
    public int getLeading() {
        return super.getLeading();
    }
    
    /* (non-Javadoc)
     * @see java.awt.FontMetrics#getMaxAdvance()
     */
    public int getMaxAdvance() {
        return super.getMaxAdvance();
    }
    
    /* (non-Javadoc)
     * @see java.awt.FontMetrics#charsWidth(char[], int, int)
     */
    public int charsWidth(char[] data, int off, int len) {
    	// LineMetrics metrics = font.getLineMetrics(data, off, off+len, getFontRenderContext());
    	String s= new String(data);
    	FontRenderContext context;
    	// getFontRenderContext would be nice, but we want to keep 1.5
    	// compatibility.
    	context= new FontRenderContext(new AffineTransform(), false, true);
		TextLayout layout = new TextLayout(s, font, context);
    	return (int) Math.ceil(layout.getAdvance());
    }
    
    /* (non-Javadoc)
     * @see java.awt.FontMetrics#charWidth(char)
     */
    public int charWidth(char ch) {
      return charsWidth(new char[]{ch}, 0, 1);
    }
}
