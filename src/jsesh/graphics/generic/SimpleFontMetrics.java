/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 24 juin 2005
 *
 */
package jsesh.graphics.generic;

import java.awt.Font;
import java.awt.FontMetrics;

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
        // TODO Auto-generated method stub
        return super.getAscent();
    }
    
    /* (non-Javadoc)
     * @see java.awt.FontMetrics#getLeading()
     */
    public int getLeading() {
        // TODO Auto-generated method stub
        return super.getLeading();
    }
    
    /* (non-Javadoc)
     * @see java.awt.FontMetrics#getMaxAdvance()
     */
    public int getMaxAdvance() {
        // TODO Auto-generated method stub
        return super.getMaxAdvance();
    }
    
    /* (non-Javadoc)
     * @see java.awt.FontMetrics#charsWidth(char[], int, int)
     */
    public int charsWidth(char[] data, int off, int len) {
        // TODO Auto-generated method stub
        return super.charsWidth(data, off, len);
    }
    
    /* (non-Javadoc)
     * @see java.awt.FontMetrics#charWidth(char)
     */
    public int charWidth(char ch) {
        // TODO Auto-generated method stub
        return super.charWidth(ch);
    }
}
