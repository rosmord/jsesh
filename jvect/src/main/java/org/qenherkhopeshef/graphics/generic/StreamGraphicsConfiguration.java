/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 28 juin 2005
 *
 */
package org.qenherkhopeshef.graphics.generic;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;

/**
 * Graphics configuration for on file images.
 * @author Serge Rosmorduc
 */
public class StreamGraphicsConfiguration extends GraphicsConfiguration {

    private Rectangle bounds;
    
    private AffineTransform normalizingTransform;
    
    
    /**
     * @param bounds
     * @param normalizingTransform
     */
    public StreamGraphicsConfiguration(Rectangle bounds,
            AffineTransform normalizingTransform) {
        super();
        this.bounds = bounds;
        this.normalizingTransform = normalizingTransform;
    }
    
    /* (non-Javadoc)
     * @see java.awt.GraphicsConfiguration#getDevice()
     */
    public GraphicsDevice getDevice() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int)
     */
    public BufferedImage createCompatibleImage(int width, int height) {       
        return new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
    }

    /* (non-Javadoc)
     * @see java.awt.GraphicsConfiguration#createCompatibleVolatileImage(int, int)
     */
    public VolatileImage createCompatibleVolatileImage(int width, int height) {        
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.GraphicsConfiguration#createCompatibleVolatileImage(int, int, int)
     */
    public VolatileImage createCompatibleVolatileImage(int width, int height,
            int transparency) {        
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int, int)
     */
    public BufferedImage createCompatibleImage(int width, int height,
            int transparency) {
        return new BufferedImage(width, height,BufferedImage.TYPE_INT_ARGB);
    }

    /* (non-Javadoc)
     * @see java.awt.GraphicsConfiguration#getColorModel()
     */
    public ColorModel getColorModel() {       
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.GraphicsConfiguration#getColorModel(int)
     */
    public ColorModel getColorModel(int transparency) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.GraphicsConfiguration#getDefaultTransform()
     */
    public AffineTransform getDefaultTransform() {
        return new AffineTransform();
    }

    /* (non-Javadoc)
     * @see java.awt.GraphicsConfiguration#getNormalizingTransform()
     */
    public AffineTransform getNormalizingTransform() {
        return normalizingTransform;
    }

    /* (non-Javadoc)
     * @see java.awt.GraphicsConfiguration#getBounds()
     */
    public Rectangle getBounds() {
        return bounds;
    }

}
