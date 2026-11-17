/*
 * Created on 5 juil. 2005 by rosmord
 *
 * TODO document the file BitmapStreamGraphics.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.graphics.bitmaps;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.imageio.ImageIO;

import jsesh.swingUtils.GraphicsUtils;

/**
 * A StreamGraphics2D for bitmaps files.
 * @author rosmord
 */
public class BitmapStreamGraphics extends StreamGraphics2DDelegate {

    OutputStream out;
    String format;
    BufferedImage image;
    
    
    public BitmapStreamGraphics(OutputStream out, Dimension dim, String format, boolean transparency) {
        this.format= format;
        this.out= out;
        int imageType;
        
        if (transparency)
            imageType= BufferedImage.TYPE_INT_ARGB;
        else
            imageType= BufferedImage.TYPE_INT_RGB;        
        image= new BufferedImage(dim.width, dim.height, imageType);
        proxy= (Graphics2D) image.getGraphics();
        GraphicsUtils.antialias(proxy);
    }
    
    
    public void dispose()  {
        proxy.dispose();
        proxy= null;        
        try {
            ImageIO.write(image,format,out);
            out.close();
        } catch (IOException e) {           
            e.printStackTrace();
        }
        image= null;
        out= null;
    }

    /* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#setProperties(java.util.Properties)
     */
    public void setProperties(Properties properties) {
        
    }
}
