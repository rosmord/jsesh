package org.qenherkhopeshef.graphics.pdf;

import com.lowagie.text.pdf.CMYKColor;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.util.Map;
import java.util.Properties;

import org.qenherkhopeshef.graphics.generic.BaseGraphics2D;
import org.qenherkhopeshef.graphics.generic.StreamGraphicsConfiguration;

/**
 * Front-end for the IText pdf, in order to avoid the many context save/restore
 * IText performs.
 *
 * In fact, it's a plain and simple delegate.
 *
 * @author Serge Rosmorduc
 */
public class QenherPDFGraphics2D extends BaseGraphics2D {

    /**
     * The delegate.
     */
    Graphics2D delegate;

    int depth = 0;

    public static final CMYKColor CMYK_BLACK = new CMYKColor(0, 0, 0, 255);
    public static final CMYKColor CMYK_WHITE = new CMYKColor(0, 0, 0, 0);

    /**
     * Create a graphics.
     *
     * @param g2d the PDF graphics 2D from IText. dimensions, expressed in pica
     * points (1/72 inch or 0.03528cm)
     */
    public QenherPDFGraphics2D(Graphics2D g2d) {
        delegate = g2d;
//        delegate.setColor(CMYK_BLACK);
//        delegate.setBackground(CMYK_WHITE);
        setFont(new Font("SansSerif", Font.PLAIN, 12));
//        setColor(CMYK_BLACK);
//        setBackground(CMYK_WHITE);
    }

    /**
     * Copy constructor.
     *
     * @param g
     */
    private QenherPDFGraphics2D(QenherPDFGraphics2D g) {
        super(g);
        this.delegate = g.delegate;
        this.depth = g.depth + 1;
    }

    public void draw(Shape s) {
        Shape transformed
                = getStroke().createStrokedShape(s);
        fill(transformed);
        // delegate.draw(getTransform().createTransformedShape(shape));
    }

    @Override
    public void fill(Shape s) {
        // The code below was not completely correct
        // Color.equals supposes two colors are equals if they have 
        // the same RGB rendering... which is incorrect,
        // ask printing shops !
        if (!(delegate.getColor().getClass().equals(getColor().getClass())
                && delegate.getColor().equals(getColor()))) {
            delegate.setColor(getColor());
        }
        delegate.fill(getTransform().createTransformedShape(s));
    }

    public Graphics create() {
        return new QenherPDFGraphics2D(this);
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return new StreamGraphicsConfiguration(
                new Rectangle(0, 0, 10000, 10000), new AffineTransform());
    }

    public FontRenderContext getFontRenderContext() {
        // IMPORTANT :
        return new FontRenderContext(getTransform(), false, true);
    }

    @Override
    public void dispose() {
        if (depth == 0) {
            delegate.dispose();
        }
    }

    @Override
    public void addRenderingHints(Map hints) {
        delegate.addRenderingHints(hints);
    }

    @Override
    public void setRenderingHints(Map hints) {
        delegate.setRenderingHints(hints);
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return false;
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
    }

    @Override
    public void setPaintMode() {
    }

    @Override
    public void setXORMode(Color c1) {
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
