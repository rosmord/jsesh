/*
 * Created on 7 juil. 2005 by rosmord
 *
 * TODO document the file BitmapGraphicsFactory.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.graphics.export;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Transparency;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.qenherkhopeshef.graphics.bitmaps.BitmapStreamGraphics;
import org.qenherkhopeshef.graphics.bitmaps.MultiFileGraphics;
import org.qenherkhopeshef.graphics.generic.BaseGraphics2D;


/**
 * Utility file for creating graphics for multi-file exportation.
 * <p> Too specific to this task, hence not a public class.
 * <p>
 * TODO : consider merging BaseGraphics2DFactory with MultiFileGraphics.Graphics2DFactory.
 * @author rosmord
 */
class BitmapGraphicsFactory implements MultiFileGraphics.Graphics2DFactory {
    String format;
    Color background;
    
    /**
     * Create a graphic factory usable for multiple files.
     * @param format : one of "png", "jpg".
     * @param background ; the background color for all pages. If null : no background.
     */
    
    public BitmapGraphicsFactory(String format, Color background) {
        super();
        this.format = format;
        this.background = background;
        
    }
    /* (non-Javadoc)
     * @see jsesh.graphics.export.MultiFileGraphics.Graphics2DFactory#createGraphics(java.io.File, int, java.awt.Dimension, java.lang.String)
     */
    public BaseGraphics2D createGraphics(File f, int pageNumber, Dimension dimension) throws FileNotFoundException {
        BitmapStreamGraphics result;
        FileOutputStream out= new FileOutputStream(f);
        result= new BitmapStreamGraphics(out,dimension,format, background.getTransparency() == Transparency.TRANSLUCENT);
        if (background.getTransparency() != Transparency.TRANSLUCENT)
        	result.fillWith(background);
        if (background != null) {            
            result.setBackground(background);
        }                
        result.clearRect(0,0,dimension.width,dimension.height);
        return result;
    }

    
    public Color getBackground() {
        return background;
    }
    
    public void setBackground(Color background) {
        this.background = background;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
}
