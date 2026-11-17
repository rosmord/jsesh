/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.graphicExport;

import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import java.util.Properties;

import org.freehep.graphics2d.TagString;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.ImageGraphics2D;
import org.freehep.graphicsio.MultiPageDocument;

/**
 * A Multipage Vector graphics which creates one file for each page.
 * 
 * @author Serge Rosmorduc
 * 
 * This file is free software under the Gnu Lesser Public License.
 */
public class MultiFileGraphics extends VectorGraphics implements
		MultiPageDocument {

	private Properties properties;

	private boolean multiPage = true;

	private VectorGraphics g;

	//private Graphics2D g;
	private int pageNumber = 0;

	private VectorGraphicsFactory factory;

	private File directory;

	private String base;

	private String extension;

	public interface VectorGraphicsFactory {
		VectorGraphics createGraphics(File f, int pageNumber,
				Dimension dimension, String type) throws FileNotFoundException;
	}

	public MultiFileGraphics(VectorGraphicsFactory factory, File directory,
			String base, String extension) {
		this.factory = factory;
		this.base = base;
		this.directory = directory;
		this.extension = extension;
	}

	/**
	 * @param arg0
	 * @return
	 */
	public static VectorGraphics create(Graphics arg0) {
		return VectorGraphics.create(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public static int getSymbol(String arg0) {
		return VectorGraphics.getSymbol(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public static int getTextAlignment(String arg0) {
		return VectorGraphics.getTextAlignment(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	public static double getXalignment(double arg0, double arg1, int arg2) {
		return VectorGraphics.getXalignment(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 */
	public static double getYalignment(double arg0, double arg1, double arg2,
			int arg3) {
		return VectorGraphics.getYalignment(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param hints
	 */
	public void addRenderingHints(Map hints) {
		g.addRenderingHints(hints);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void clearRect(double arg0, double arg1, double arg2, double arg3) {
		g.clearRect(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void clearRect(int x, int y, int width, int height) {
		g.clearRect(x, y, width, height);
	}

	/**
	 * @param s
	 */
	public void clip(Shape s) {
		g.clip(s);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void clipRect(double arg0, double arg1, double arg2, double arg3) {
		g.clipRect(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void clipRect(int x, int y, int width, int height) {
		g.clipRect(x, y, width, height);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param dx
	 * @param dy
	 */
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		g.copyArea(x, y, width, height, dx, dy);
	}

	/**
	 * @return
	 */
	public Graphics create() {
		return g.create();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 */
	public Graphics create(double arg0, double arg1, double arg2, double arg3) {
		return g.create(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public Graphics create(int x, int y, int width, int height) {
		return g.create(x, y, width, height);
	}

	/**
	 *  
	 */
	public void dispose() {
		g.dispose();
	}

	/**
	 * @param s
	 */
	public void draw(Shape s) {
		g.draw(s);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param raised
	 */
	public void draw3DRect(int x, int y, int width, int height, boolean raised) {
		g.draw3DRect(x, y, width, height, raised);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public void drawArc(double arg0, double arg1, double arg2, double arg3,
			double arg4, double arg5) {
		g.drawArc(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param startAngle
	 * @param arcAngle
	 */
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		g.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	/**
	 * @param data
	 * @param offset
	 * @param length
	 * @param x
	 * @param y
	 */
	public void drawBytes(byte[] data, int offset, int length, int x, int y) {
		g.drawBytes(data, offset, length, x, y);
	}

	/**
	 * @param data
	 * @param offset
	 * @param length
	 * @param x
	 * @param y
	 */
	public void drawChars(char[] data, int offset, int length, int x, int y) {
		g.drawChars(data, offset, length, x, y);
	}

	/**
	 * @param v
	 * @param x
	 * @param y
	 */
	public void drawGlyphVector(GlyphVector v, float x, float y) {
		g.drawGlyphVector(v, x, y);
	}

	/**
	 * @param img
	 * @param dx1
	 * @param dy1
	 * @param dx2
	 * @param dy2
	 * @param sx1
	 * @param sy1
	 * @param sx2
	 * @param sy2
	 * @param bgcolor
	 * @param observer
	 * @return
	 */
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, Color bgcolor,
			ImageObserver observer) {
		return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
				bgcolor, observer);
	}

	/**
	 * @param img
	 * @param dx1
	 * @param dy1
	 * @param dx2
	 * @param dy2
	 * @param sx1
	 * @param sy1
	 * @param sx2
	 * @param sy2
	 * @param observer
	 * @return
	 */
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
				observer);
	}

	/**
	 * @param img
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param bgcolor
	 * @param observer
	 * @return
	 */
	public boolean drawImage(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) {
		return g.drawImage(img, x, y, width, height, bgcolor, observer);
	}

	/**
	 * @param img
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param observer
	 * @return
	 */
	public boolean drawImage(Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		return g.drawImage(img, x, y, width, height, observer);
	}

	/**
	 * @param img
	 * @param x
	 * @param y
	 * @param bgcolor
	 * @param observer
	 * @return
	 */
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		return g.drawImage(img, x, y, bgcolor, observer);
	}

	/**
	 * @param img
	 * @param x
	 * @param y
	 * @param observer
	 * @return
	 */
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		return g.drawImage(img, x, y, observer);
	}

	/**
	 * @param img
	 * @param xform
	 * @param obs
	 * @return
	 */
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		return g.drawImage(img, xform, obs);
	}

	/**
	 * @param img
	 * @param op
	 * @param x
	 * @param y
	 */
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		g.drawImage(img, op, x, y);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void drawLine(double arg0, double arg1, double arg2, double arg3) {
		g.drawLine(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void drawLine(int x1, int y1, int x2, int y2) {
		g.drawLine(x1, y1, x2, y2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void drawOval(double arg0, double arg1, double arg2, double arg3) {
		g.drawOval(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void drawOval(int x, int y, int width, int height) {
		g.drawOval(x, y, width, height);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public void drawPolygon(double[] arg0, double[] arg1, int arg2) {
		g.drawPolygon(arg0, arg1, arg2);
	}

	/**
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		g.drawPolygon(xPoints, yPoints, nPoints);
	}

	/**
	 * @param p
	 */
	public void drawPolygon(Polygon p) {
		g.drawPolygon(p);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public void drawPolyline(double[] arg0, double[] arg1, int arg2) {
		g.drawPolyline(arg0, arg1, arg2);
	}

	/**
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		g.drawPolyline(xPoints, yPoints, nPoints);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void drawRect(double arg0, double arg1, double arg2, double arg3) {
		g.drawRect(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void drawRect(int x, int y, int width, int height) {
		g.drawRect(x, y, width, height);
	}

	/**
	 * @param img
	 * @param xform
	 */
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		g.drawRenderableImage(img, xform);
	}

	/**
	 * @param img
	 * @param xform
	 */
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		g.drawRenderedImage(img, xform);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public void drawRoundRect(double arg0, double arg1, double arg2,
			double arg3, double arg4, double arg5) {
		g.drawRoundRect(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param arcWidth
	 * @param arcHeight
	 */
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public void drawString(String arg0, double arg1, double arg2) {
		g.drawString(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 */
	public void drawString(String arg0, double arg1, double arg2, int arg3,
			int arg4) {
		g.drawString(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @param arg6
	 * @param arg7
	 * @param arg8
	 * @param arg9
	 */
	public void drawString(String arg0, double arg1, double arg2, int arg3,
			int arg4, boolean arg5, Color arg6, double arg7, boolean arg8,
			Color arg9) {
		g
				.drawString(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
						arg8, arg9);
	}

	/**
	 * @param s
	 * @param x
	 * @param y
	 */
	public void drawString(String s, float x, float y) {
		g.drawString(s, x, y);
	}

	/**
	 * @param str
	 * @param x
	 * @param y
	 */
	public void drawString(String str, int x, int y) {
		g.drawString(str, x, y);
	}

	/**
	 * @param iterator
	 * @param x
	 * @param y
	 */
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		g.drawString(iterator, x, y);
	}

	/**
	 * @param iterator
	 * @param x
	 * @param y
	 */
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		g.drawString(iterator, x, y);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public void drawString(TagString arg0, double arg1, double arg2) {
		g.drawString(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 */
	public void drawString(TagString arg0, double arg1, double arg2, int arg3,
			int arg4) {
		g.drawString(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @param arg6
	 * @param arg7
	 * @param arg8
	 * @param arg9
	 */
	public void drawString(TagString arg0, double arg1, double arg2, int arg3,
			int arg4, boolean arg5, Color arg6, double arg7, boolean arg8,
			Color arg9) {
		g
				.drawString(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
						arg8, arg9);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void drawSymbol(double arg0, double arg1, double arg2, int arg3) {
		g.drawSymbol(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void drawSymbol(int arg0, int arg1, int arg2, int arg3) {
		g.drawSymbol(arg0, arg1, arg2, arg3);
	}

	/**
	 *  
	 */
	public void endExport() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return g.equals(obj);
	}

	/**
	 * @param s
	 */
	public void fill(Shape s) {
		g.fill(s);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param raised
	 */
	public void fill3DRect(int x, int y, int width, int height, boolean raised) {
		g.fill3DRect(x, y, width, height, raised);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public void fillAndDraw(Shape arg0, Color arg1) {
		g.fillAndDraw(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public void fillArc(double arg0, double arg1, double arg2, double arg3,
			double arg4, double arg5) {
		g.fillArc(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param startAngle
	 * @param arcAngle
	 */
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		g.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void fillOval(double arg0, double arg1, double arg2, double arg3) {
		g.fillOval(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void fillOval(int x, int y, int width, int height) {
		g.fillOval(x, y, width, height);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public void fillPolygon(double[] arg0, double[] arg1, int arg2) {
		g.fillPolygon(arg0, arg1, arg2);
	}

	/**
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		g.fillPolygon(xPoints, yPoints, nPoints);
	}

	/**
	 * @param p
	 */
	public void fillPolygon(Polygon p) {
		g.fillPolygon(p);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void fillRect(double arg0, double arg1, double arg2, double arg3) {
		g.fillRect(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void fillRect(int x, int y, int width, int height) {
		g.fillRect(x, y, width, height);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public void fillRoundRect(double arg0, double arg1, double arg2,
			double arg3, double arg4, double arg5) {
		g.fillRoundRect(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param arcWidth
	 * @param arcHeight
	 */
	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void fillSymbol(double arg0, double arg1, double arg2, int arg3) {
		g.fillSymbol(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void fillSymbol(int arg0, int arg1, int arg2, int arg3) {
		g.fillSymbol(arg0, arg1, arg2, arg3);
	}

	/**
	 * @return
	 */
	public Color getBackground() {
		return g.getBackground();
	}

	/**
	 * @return
	 */
	public Shape getClip() {
		return g.getClip();
	}

	/**
	 * @return
	 */
	public Rectangle getClipBounds() {
		return g.getClipBounds();
	}

	/**
	 * @param r
	 * @return
	 */
	public Rectangle getClipBounds(Rectangle r) {
		return g.getClipBounds(r);
	}

	/**
	 * @deprecated As of JDK version 1.1,
	 * @return
	 */
	public Rectangle getClipRect() {
		return g.getClipRect();
	}

	/**
	 * @return
	 */
	public Color getColor() {
		return g.getColor();
	}

	/**
	 * @return
	 */
	public int getColorMode() {
		return g.getColorMode();
	}

	/**
	 * @return
	 */
	public Composite getComposite() {
		return g.getComposite();
	}

	/**
	 * @return
	 */
	public String getCreator() {
		return g.getCreator();
	}

	/**
	 * @return
	 */
	public GraphicsConfiguration getDeviceConfiguration() {
		return g.getDeviceConfiguration();
	}

	/**
	 * @return
	 */
	public Font getFont() {
		return g.getFont();
	}

	/**
	 * @return
	 */
	public FontMetrics getFontMetrics() {
		return g.getFontMetrics();
	}

	/**
	 * @param f
	 * @return
	 */
	public FontMetrics getFontMetrics(Font f) {
		return g.getFontMetrics(f);
	}

	/**
	 * @return
	 */
	public FontRenderContext getFontRenderContext() {
		return g.getFontRenderContext();
	}

	/**
	 * @return
	 */
	public Paint getPaint() {
		return g.getPaint();
	}

	/**
	 * @param arg0
	 * @return
	 */
	public String getProperty(String arg0) {
		return g.getProperty(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Color getPropertyColor(String arg0) {
		return g.getPropertyColor(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Dimension getPropertyDimension(String arg0) {
		return g.getPropertyDimension(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public double getPropertyDouble(String arg0) {
		return g.getPropertyDouble(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public int getPropertyInt(String arg0) {
		return g.getPropertyInt(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Rectangle getPropertyRectangle(String arg0) {
		return g.getPropertyRectangle(arg0);
	}

	/**
	 * @param hintKey
	 * @return
	 */
	public Object getRenderingHint(Key hintKey) {
		return g.getRenderingHint(hintKey);
	}

	/**
	 * @return
	 */
	public RenderingHints getRenderingHints() {
		return g.getRenderingHints();
	}

	/**
	 * @return
	 */
	public Stroke getStroke() {
		return g.getStroke();
	}

	/**
	 * @return
	 */
	public AffineTransform getTransform() {
		return g.getTransform();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return g.hashCode();
	}

	/**
	 * @param rect
	 * @param s
	 * @param onStroke
	 * @return
	 */
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		return g.hit(rect, s, onStroke);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public boolean hitClip(int x, int y, int width, int height) {
		return g.hitClip(x, y, width, height);
	}

	/**
	 * @return
	 */
	public boolean isDeviceIndependent() {
		return g.isDeviceIndependent();
	}

	/**
	 * @param arg0
	 * @return
	 */
	public boolean isProperty(String arg0) {
		return g.isProperty(arg0);
	}

	/**
	 * @param arg0
	 */
	public void printComment(String arg0) {
		g.printComment(arg0);
	}

	/**
	 * @param theta
	 */
	public void rotate(double theta) {
		g.rotate(theta);
	}

	/**
	 * @param theta
	 * @param x
	 * @param y
	 */
	public void rotate(double theta, double x, double y) {
		g.rotate(theta, x, y);
	}

	/**
	 * @param sx
	 * @param sy
	 */
	public void scale(double sx, double sy) {
		g.scale(sx, sy);
	}

	/**
	 * @param color
	 */
	public void setBackground(Color color) {
		g.setBackground(color);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void setClip(double arg0, double arg1, double arg2, double arg3) {
		g.setClip(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setClip(int x, int y, int width, int height) {
		g.setClip(x, y, width, height);
	}

	/**
	 * @param clip
	 */
	public void setClip(Shape clip) {
		g.setClip(clip);
	}

	/**
	 * @param c
	 */
	public void setColor(Color c) {
		g.setColor(c);
	}

	/**
	 * @param arg0
	 */
	public void setColorMode(int arg0) {
		g.setColorMode(arg0);
	}

	/**
	 * @param comp
	 */
	public void setComposite(Composite comp) {
		g.setComposite(comp);
	}

	/**
	 * @param arg0
	 */
	public void setCreator(String arg0) {
		g.setCreator(arg0);
	}

	/**
	 * @param arg0
	 */
	public void setDeviceIndependent(boolean arg0) {
		g.setDeviceIndependent(arg0);
	}

	/**
	 * @param font
	 */
	public void setFont(Font font) {
		g.setFont(font);
	}

	/**
	 * @param arg0
	 */
	public void setLineWidth(double arg0) {
		g.setLineWidth(arg0);
	}

	/**
	 * @param arg0
	 */
	public void setLineWidth(int arg0) {
		g.setLineWidth(arg0);
	}

	/**
	 * @param paint
	 */
	public void setPaint(Paint paint) {
		g.setPaint(paint);
	}

	/**
	 *  
	 */
	public void setPaintMode() {
		g.setPaintMode();
	}

	/**
	 * @param arg0
	 */
	public void setProperties(Properties arg0) {
		properties = arg0;
	}

	/**
	 * @param hintKey
	 * @param hintValue
	 */
	public void setRenderingHint(Key hintKey, Object hintValue) {
		g.setRenderingHint(hintKey, hintValue);
	}

	/**
	 * @param hints
	 */
	public void setRenderingHints(Map hints) {
		g.setRenderingHints(hints);
	}

	/**
	 * @param s
	 */
	public void setStroke(Stroke s) {
		g.setStroke(s);
	}

	/**
	 * @param Tx
	 */
	public void setTransform(AffineTransform Tx) {
		g.setTransform(Tx);
	}

	/**
	 * @param c1
	 */
	public void setXORMode(Color c1) {
		g.setXORMode(c1);
	}

	/**
	 * @param shx
	 * @param shy
	 */
	public void shear(double shx, double shy) {
		g.shear(shx, shy);
	}

	/**
	 *  
	 */
	public void startExport() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return g.toString();
	}

	/**
	 * @param Tx
	 */
	public void transform(AffineTransform Tx) {
		g.transform(Tx);
	}

	/**
	 * @param tx
	 * @param ty
	 */
	public void translate(double tx, double ty) {
		g.translate(tx, ty);
	}

	/**
	 * @param x
	 * @param y
	 */
	public void translate(int x, int y) {
		g.translate(x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.MultiPageDocument#setMultiPage(boolean)
	 */

	public void setMultiPage(boolean arg0) {
		multiPage = arg0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.MultiPageDocument#isMultiPage()
	 */
	public boolean isMultiPage() {
		return multiPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.MultiPageDocument#setHeader(java.awt.Font,
	 *      org.freehep.graphics2d.TagString, org.freehep.graphics2d.TagString,
	 *      org.freehep.graphics2d.TagString, int)
	 */
	public void setHeader(Font arg0, TagString arg1, TagString arg2,
			TagString arg3, int arg4) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.MultiPageDocument#setFooter(java.awt.Font,
	 *      org.freehep.graphics2d.TagString, org.freehep.graphics2d.TagString,
	 *      org.freehep.graphics2d.TagString, int)
	 */
	public void setFooter(Font arg0, TagString arg1, TagString arg2,
			TagString arg3, int arg4) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.MultiPageDocument#openPage(java.awt.Component)
	 */
	public void openPage(Component arg0) throws IOException {
		openPage(arg0.getSize(), arg0.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.MultiPageDocument#openPage(java.awt.Dimension,
	 *      java.lang.String)
	 */
	public void openPage(Dimension dimensions, String title) throws IOException {
		String num = stringFormat(pageNumber + 1);
		File f = new File(directory, base + num + "." + extension);
		// TODO : Choose a better system for extensions.
		g= null;
		//End of the bug fix.
		g = factory.createGraphics(f, pageNumber, dimensions, extension);
		g.setProperties(properties);
		pageNumber++;
		g.startExport();
	}

	private String stringFormat(int i) {
		// TODO : improve stringFormat
		return "" + i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.MultiPageDocument#closePage()
	 */
	public void closePage() throws IOException {
		if (g != null) {
			g.endExport();
			g.dispose();
		}
		g = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#initProperties(java.util.Properties)
	 */
	protected void initProperties(Properties properties) {
		this.properties = properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#getProperties()
	 */
	protected Properties getProperties() {
		return properties;
	}

	public static MultiFileGraphics getExportForJPG(File directory,
			String base, String extension) {
		VectorGraphicsFactory factory = new VectorGraphicsFactory() {

			public VectorGraphics createGraphics(File f, int pageNumber,
					Dimension dimension, String type)
					throws FileNotFoundException {
				VectorGraphics result = null;
				result = new ImageGraphics2D(f, dimension, type);
				return result;
			}
		};

		MultiFileGraphics result = new MultiFileGraphics(factory,
				directory, base, extension);
		return result;
	}
}