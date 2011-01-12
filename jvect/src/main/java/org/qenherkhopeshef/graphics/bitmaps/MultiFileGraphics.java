/*
 * Created on 17 oct. 2004
 *
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package org.qenherkhopeshef.graphics.bitmaps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.qenherkhopeshef.graphics.generic.BaseGraphics2D;
import org.qenherkhopeshef.graphics.generic.BaseGraphics2DException;



/**
 * A Multipage Vector graphics which creates one file for each page.
 * 
 * @author Serge Rosmorduc
 * 
 * This file is free software under the Gnu Lesser Public License.
 */
public class MultiFileGraphics extends BaseGraphics2D   {

	private Properties properties;

	private BaseGraphics2D g;

	//private Graphics2D g;
	private int pageNumber = 0;

	private Graphics2DFactory factory;

	private File directory;

	private String base;

	private String extension;

	public interface Graphics2DFactory {
		BaseGraphics2D createGraphics(File f, int pageNumber,
				Dimension dimension) throws FileNotFoundException;
	}

	public MultiFileGraphics(Graphics2DFactory factory, File directory,
			String base, String extension) {
		this.factory = factory;
		this.base = base;
		this.directory = directory;
		this.extension = extension;
	}

    /* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#copyArea(int, int, int, int, int, int)
     */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        g.copyArea(x, y,  width,height, dx, dy);
        
    }

    /* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#create()
     */
    public Graphics create() {
        // TODO : Improve this one.
        if (g != null)
            return g.create();
        else
            return null;
    }

    /* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#dispose()
     */
    public void dispose() {
        if (g!= null)
            g.dispose();
    }

    /* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#draw(java.awt.Shape)
     */
    public void draw(Shape s) {
        g.draw(s);
    }

    /* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#drawImage(java.awt.Image, java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {       
        return false;
    }

    /* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#drawRenderedImage(java.awt.image.RenderedImage, java.awt.geom.AffineTransform)
     */
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        // TODO Auto-generated method stub        
    }

    /* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#fill(java.awt.Shape)
     */
    public void fill(Shape s) {
        g.fill(s);        
    }

    /* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#getDeviceConfiguration()
     */
    public GraphicsConfiguration getDeviceConfiguration() {        
        return g.getDeviceConfiguration();
    }

    /* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#getFontRenderContext()
     */
    public FontRenderContext getFontRenderContext() {
        return g.getFontRenderContext();
    }

    /* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#setPaintMode()
     */
    public void setPaintMode() {
        g.setPaintMode();
    }

    /* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#setXORMode(java.awt.Color)
     */
    public void setXORMode(Color c1) {
        g.setXORMode(c1);
    }

   

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    

	
	public void openPage(Dimension dimensions, String title) throws BaseGraphics2DException {
		try {
            String num = stringFormat(pageNumber + 1);
            File f = new File(directory, base + num + "." + extension);
            // TODO : Choose a better system for extensions.
            g= null;
            //End of the bug fix.
            g = factory.createGraphics(f, pageNumber, dimensions);
            g.setProperties(properties);
            g.transform(getTransform());
            pageNumber++;
        } catch (FileNotFoundException e) {
            throw new BaseGraphics2DException(e);
        }
	}

	
	/* (non-Javadoc)
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#transform(java.awt.geom.AffineTransform)
     */
    public void transform(AffineTransform Tx) {
        // Keep track of the current transform.
        super.transform(Tx);
        // if there is a current graphic, update it.
        if (g!= null)
            g.transform(Tx);
    }
    
	private String stringFormat(int i) {
		// TODO : improve stringFormat
		return "" + i;
	}

}