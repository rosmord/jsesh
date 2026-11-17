/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 12 févr. 2005
 *
 */
package jsesh.wmfexport;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * Basic class for creating customs Graphics2D.
 * <p>Currently unused. 
 * <p> We don't use much of the freehep library, and if we decide to use IText for pdf export (and rtf !), 
 * we will use it even lesser, so this class will allow the wmf export to be stand alone. We took some of the principles from
 * freehep.
 * 
 * <p> This class defines most of Graphics2D methods in terms of a few selected basic ones.
 * It's basically very simple and straightforward. If your target has good specific methods for 
 * a particular operation, feel free to overwrite the corresponding methods.
 * 
 * <p> As a rule, data is defined to support Graphics2D operations, and the simpler operations call the more
 * advanced one. For instance, translate(int,int) calls translate(double,double) which calls transform() with
 * the appropriate transformation.
 *  
 * @author Serge Rosmorduc
 */
public abstract class BaseGraphics2D extends Graphics2D {

    private AffineTransform currentTransform;
    private Stroke currentStroke;
    private Paint currentPaint;
    private Color currentBackground;
    private Color currentForeground;
    private Font currentFont;
    private Shape currentClip;
    
    /* (non-Javadoc)
     * @see java.awt.Graphics2D#draw(java.awt.Shape)
     */
    public void draw(Shape s) {
        
        // TODO Auto-generated method stub        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawImage(java.awt.Image, java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawImage(java.awt.image.BufferedImage, java.awt.image.BufferedImageOp, int, int)
     */
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawRenderedImage(java.awt.image.RenderedImage, java.awt.geom.AffineTransform)
     */
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawRenderableImage(java.awt.image.renderable.RenderableImage, java.awt.geom.AffineTransform)
     */
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawString(java.lang.String, int, int)
     */
    public void drawString(String str, int x, int y) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawString(java.lang.String, float, float)
     */
    public void drawString(String s, float x, float y) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, int, int)
     */
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, float, float)
     */
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawGlyphVector(java.awt.font.GlyphVector, float, float)
     */
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#fill(java.awt.Shape)
     */
    public void fill(Shape s) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#hit(java.awt.Rectangle, java.awt.Shape, boolean)
     */
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getDeviceConfiguration()
     */
    public GraphicsConfiguration getDeviceConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setComposite(java.awt.Composite)
     */
    public void setComposite(Composite comp) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setPaint(java.awt.Paint)
     */
    public void setPaint(Paint paint) {
        currentPaint= paint;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setStroke(java.awt.Stroke)
     */
    public void setStroke(Stroke s) {
        currentStroke= s;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setRenderingHint(java.awt.RenderingHints.Key, java.lang.Object)
     */
    public void setRenderingHint(Key hintKey, Object hintValue) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getRenderingHint(java.awt.RenderingHints.Key)
     */
    public Object getRenderingHint(Key hintKey) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setRenderingHints(java.util.Map)
     */
    public void setRenderingHints(Map hints) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#addRenderingHints(java.util.Map)
     */
    public void addRenderingHints(Map hints) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getRenderingHints()
     */
    public RenderingHints getRenderingHints() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#translate(int, int)
     */
    public void translate(int x, int y) {
        translate((double)x, (double)y);
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#translate(double, double)
     */
    public void translate(double tx, double ty) {
        transform(AffineTransform.getTranslateInstance(tx,ty));
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#rotate(double)
     */
    public void rotate(double theta) {
        rotate(theta,0,0);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#rotate(double, double, double)
     */
    public void rotate(double theta, double x, double y) {
        transform(AffineTransform.getRotateInstance(theta,x,y));
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#scale(double, double)
     */
    public void scale(double sx, double sy) {
        transform(AffineTransform.getScaleInstance(sx,sy));
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#shear(double, double)
     */
    public void shear(double shx, double shy) {
        transform(AffineTransform.getShearInstance(shx,shy));
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#transform(java.awt.geom.AffineTransform)
     */
    public void transform(AffineTransform Tx) {
        currentTransform.concatenate(Tx);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setTransform(java.awt.geom.AffineTransform)
     */
    public void setTransform(AffineTransform Tx) {      
        currentTransform= (AffineTransform) Tx.clone();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getTransform()
     */
    public AffineTransform getTransform() {       
        return currentTransform;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getPaint()
     */
    public Paint getPaint() {    
        return currentPaint;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getComposite()
     */
    public Composite getComposite() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setBackground(java.awt.Color)
     */
    public void setBackground(Color color) {
        currentBackground= color;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getBackground()
     */
    public Color getBackground() {
        return currentBackground;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getStroke()
     */
    public Stroke getStroke() {
      return currentStroke;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#clip(java.awt.Shape)
     */
    public void clip(Shape s) {
        // TODO Auto-generated method stub     
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getFontRenderContext()
     */
    public FontRenderContext getFontRenderContext() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#create()
     */
    abstract public Graphics create();

    /* (non-Javadoc)
     * @see java.awt.Graphics#getColor()
     */
    public Color getColor() {
        return currentForeground;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setColor(java.awt.Color)
     */
    public void setColor(Color c) {
        currentForeground= c;
    }

    /**
     * Switches back to paint mode, disabling Xor mode.
     * <p> May usually be empty.
     */
    abstract public void setPaintMode();

    /**
     * Switches to Xor mode.
     * @see java.awt.Graphics#setXORMode(java.awt.Color)
     */
    abstract public void setXORMode(Color c1);

    /* (non-Javadoc)
     * @see java.awt.Graphics#getFont()
     */
    public Font getFont() {
        return currentFont;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setFont(java.awt.Font)
     */
    public void setFont(Font font) {
        currentFont= font;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getFontMetrics(java.awt.Font)
     */
    public FontMetrics getFontMetrics(Font f) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getClipBounds()
     */
    public Rectangle getClipBounds() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#clipRect(int, int, int, int)
     */
    public void clipRect(int x, int y, int width, int height) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setClip(int, int, int, int)
     */
    public void setClip(int x, int y, int width, int height) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getClip()
     */
    public Shape getClip() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setClip(java.awt.Shape)
     */
    public void setClip(Shape clip) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
     */
    abstract public void copyArea(int x, int y, int width, int height, int dx, int dy);

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawLine(int, int, int, int)
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        draw(new Line2D.Float(x1,y1,x2,y2));
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillRect(int, int, int, int)
     */
    public void fillRect(int x, int y, int width, int height) {
        fill(new Rectangle2D.Float(x,y,width, height));
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#clearRect(int, int, int, int)
     */
    public void clearRect(int x, int y, int width, int height) {
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawRoundRect(int, int, int, int, int, int)
     */
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        draw(new RoundRectangle2D.Float(x,y,width, height, arcWidth, arcHeight));
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillRoundRect(int, int, int, int, int, int)
     */
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {     
        fill(new RoundRectangle2D.Float(x,y,width, height, arcWidth, arcHeight));
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawOval(int, int, int, int)
     */
    public void drawOval(int x, int y, int width, int height) {
        draw(new Ellipse2D.Float(x,y,width,height));
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillOval(int, int, int, int)
     */
    public void fillOval(int x, int y, int width, int height) {
        draw(new Ellipse2D.Float(x,y,width,height));
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawArc(int, int, int, int, int, int)
     */
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        draw(new Arc2D.Float(x,y,width, height, startAngle, arcAngle, Arc2D.OPEN));
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillArc(int, int, int, int, int, int)
     */
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        fill(new Arc2D.Float(x,y,width, height, startAngle, arcAngle, Arc2D.PIE));      
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawPolyline(int[], int[], int)
     */
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawPolygon(int[], int[], int)
     */
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillPolygon(int[], int[], int)
     */
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#dispose()
     */
    public void dispose() {        
    }
}
