/*
 * Created on 5 juil. 2005 by rosmord
 *
 * TODO document the file StreamGraphics2DDelegate.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package org.qenherkhopeshef.graphics.bitmaps;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import org.qenherkhopeshef.graphics.generic.BaseGraphics2D;



/**
 * A wrapper to encapsulate a "normal" Graphics2D in a StreamGraphics2D.
 * Currently used for bitmaps.
 * 
 * <p> Implementations should give a value to <code>proxy</code>.
 * @author rosmord
 */
abstract public class StreamGraphics2DDelegate extends BaseGraphics2D {
    /**
     * The actual Graphics2D we are drawing to.
     * A value should be given to it before actual drawing takes place.
     */
    protected Graphics2D proxy;
    
    
    public void addRenderingHints(Map hints) {
        proxy.addRenderingHints(hints);
    }
    
    public void clearRect(int x, int y, int width, int height) {
        proxy.clearRect(x, y, width, height);
    }
    
    public void clip(Shape s) {
        proxy.clip(s);
    }
    
    public void clipRect(int x, int y, int width, int height) {
        proxy.clipRect(x, y, width, height);
    }
    
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        proxy.copyArea(x, y, width, height, dx, dy);
    }
    
    public Graphics create() {
        return proxy.create();
    }
    
    public Graphics create(int x, int y, int width, int height) {
        return proxy.create(x, y, width, height);
    }
    public void dispose() {
        proxy.dispose();
    }
    public void draw(Shape s) {
        proxy.draw(s);
    }
    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        proxy.draw3DRect(x, y, width, height, raised);
    }
    public void drawArc(int x, int y, int width, int height, int startAngle,
            int arcAngle) {
        proxy.drawArc(x, y, width, height, startAngle, arcAngle);
    }
    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        proxy.drawBytes(data, offset, length, x, y);
    }
    public void drawChars(char[] data, int offset, int length, int x, int y) {
        proxy.drawChars(data, offset, length, x, y);
    }
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        proxy.drawGlyphVector(g, x, y);
    }
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, Color bgcolor,
            ImageObserver observer) {
        return proxy.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
                bgcolor, observer);
    }
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return proxy.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
                observer);
    }
    public boolean drawImage(Image img, int x, int y, int width, int height,
            Color bgcolor, ImageObserver observer) {
        return proxy.drawImage(img, x, y, width, height, bgcolor, observer);
    }
    public boolean drawImage(Image img, int x, int y, int width, int height,
            ImageObserver observer) {
        return proxy.drawImage(img, x, y, width, height, observer);
    }
    public boolean drawImage(Image img, int x, int y, Color bgcolor,
            ImageObserver observer) {
        return proxy.drawImage(img, x, y, bgcolor, observer);
    }
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return proxy.drawImage(img, x, y, observer);
    }
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return proxy.drawImage(img, xform, obs);
    }
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        proxy.drawImage(img, op, x, y);
    }
    public void drawLine(int x1, int y1, int x2, int y2) {
        proxy.drawLine(x1, y1, x2, y2);
    }
    public void drawOval(int x, int y, int width, int height) {
        proxy.drawOval(x, y, width, height);
    }
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        proxy.drawPolygon(xPoints, yPoints, nPoints);
    }
    public void drawPolygon(Polygon p) {
        proxy.drawPolygon(p);
    }
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        proxy.drawPolyline(xPoints, yPoints, nPoints);
    }
    public void drawRect(int x, int y, int width, int height) {
        proxy.drawRect(x, y, width, height);
    }
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        proxy.drawRenderableImage(img, xform);
    }
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        proxy.drawRenderedImage(img, xform);
    }
    public void drawRoundRect(int x, int y, int width, int height,
            int arcWidth, int arcHeight) {
        proxy.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }
    public void drawString(String s, float x, float y) {
        proxy.drawString(s, x, y);
    }
    public void drawString(String str, int x, int y) {
        proxy.drawString(str, x, y);
    }
    public void drawString(AttributedCharacterIterator iterator, float x,
            float y) {
        proxy.drawString(iterator, x, y);
    }
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        proxy.drawString(iterator, x, y);
    }
    public boolean equals(Object obj) {
        return proxy.equals(obj);
    }
    public void fill(Shape s) {
        proxy.fill(s);
    }
    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        proxy.fill3DRect(x, y, width, height, raised);
    }
    public void fillArc(int x, int y, int width, int height, int startAngle,
            int arcAngle) {
        proxy.fillArc(x, y, width, height, startAngle, arcAngle);
    }
    public void fillOval(int x, int y, int width, int height) {
        proxy.fillOval(x, y, width, height);
    }
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        proxy.fillPolygon(xPoints, yPoints, nPoints);
    }
    public void fillPolygon(Polygon p) {
        proxy.fillPolygon(p);
    }
    public void fillRect(int x, int y, int width, int height) {
        proxy.fillRect(x, y, width, height);
    }
    public void fillRoundRect(int x, int y, int width, int height,
            int arcWidth, int arcHeight) {
        proxy.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }
    public Color getBackground() {
        return proxy.getBackground();
    }
    public Shape getClip() {
        return proxy.getClip();
    }
    public Rectangle getClipBounds() {
        return proxy.getClipBounds();
    }
    public Rectangle getClipBounds(Rectangle r) {
        return proxy.getClipBounds(r);
    }
    
    /**
     * @deprecated
     */
    public Rectangle getClipRect() {
        return proxy.getClipRect();
    }
    public Color getColor() {
        return proxy.getColor();
    }
    public Composite getComposite() {
        return proxy.getComposite();
    }
    public GraphicsConfiguration getDeviceConfiguration() {
        return proxy.getDeviceConfiguration();
    }
    public Font getFont() {
        return proxy.getFont();
    }
    public FontMetrics getFontMetrics() {
        return proxy.getFontMetrics();
    }
    public FontMetrics getFontMetrics(Font f) {
        return proxy.getFontMetrics(f);
    }
    public FontRenderContext getFontRenderContext() {
        return proxy.getFontRenderContext();
    }
    public Paint getPaint() {
        return proxy.getPaint();
    }
    public Object getRenderingHint(Key hintKey) {
        return proxy.getRenderingHint(hintKey);
    }
    public RenderingHints getRenderingHints() {
        return proxy.getRenderingHints();
    }
    public Stroke getStroke() {
        return proxy.getStroke();
    }
    public AffineTransform getTransform() {
        return proxy.getTransform();
    }
    public int hashCode() {
        return proxy.hashCode();
    }
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return proxy.hit(rect, s, onStroke);
    }
    public boolean hitClip(int x, int y, int width, int height) {
        return proxy.hitClip(x, y, width, height);
    }
    public void rotate(double theta) {
        proxy.rotate(theta);
    }
    public void rotate(double theta, double x, double y) {
        proxy.rotate(theta, x, y);
    }
    public void scale(double sx, double sy) {
        proxy.scale(sx, sy);
    }
    public void setBackground(Color color) {
        proxy.setBackground(color);
    }
    public void setClip(int x, int y, int width, int height) {
        proxy.setClip(x, y, width, height);
    }
    public void setClip(Shape clip) {
        proxy.setClip(clip);
    }
    public void setColor(Color c) {
        proxy.setColor(c);
    }
    public void setComposite(Composite comp) {
        proxy.setComposite(comp);
    }
    public void setFont(Font font) {
        proxy.setFont(font);
    }
    public void setPaint(Paint paint) {
        proxy.setPaint(paint);
    }
    public void setPaintMode() {
        proxy.setPaintMode();
    }
    public void setRenderingHint(Key hintKey, Object hintValue) {
        proxy.setRenderingHint(hintKey, hintValue);
    }
    public void setRenderingHints(Map hints) {
        proxy.setRenderingHints(hints);
    }
    public void setStroke(Stroke s) {
        proxy.setStroke(s);
    }
    public void setTransform(AffineTransform Tx) {
        proxy.setTransform(Tx);
    }
    public void setXORMode(Color c1) {
        proxy.setXORMode(c1);
    }
    public void shear(double shx, double shy) {
        proxy.shear(shx, shy);
    }
    public String toString() {
        return proxy.toString();
    }
    public void transform(AffineTransform Tx) {
        proxy.transform(Tx);
    }
    public void translate(double tx, double ty) {
        proxy.translate(tx, ty);
    }
    public void translate(int x, int y) {
        proxy.translate(x, y);
    }
}
