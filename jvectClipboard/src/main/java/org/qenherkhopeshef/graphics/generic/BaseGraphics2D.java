/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 12 fevr. 2005
 *
 */
package org.qenherkhopeshef.graphics.generic;

import java.awt.BasicStroke;
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
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Basic class for creating customs Graphics2D.
 * <p>
 * We now use IText for pdf export, so the freehep library wasn't very useful
 * anymore. We have decided to write this class to allow the wmf export to be
 * stand alone. We took some of the principles from freehep.
 * 
 * <p>
 * This class defines most of Graphics2D methods in terms of a few selected
 * basic ones. It's basically very simple and straightforward. If your target
 * has good specific methods for a particular operation, feel free to overwrite
 * the corresponding methods.
 * 
 * <p>
 * As a rule, data is defined to support Graphics2D operations, and the simpler
 * operations call the more advanced one. For instance, translate(int,int) calls
 * translate(double,double) which calls transform() with the appropriate
 * transformation. Hence, defining the abstract method should give usable
 * results, but redefining more specific methods should give better results. As
 * an example, every drawing will be done though the Shape interface, but it
 * will probably be more efficient to redefine specific rectangle, etc...
 * 
 * <p>
 * A few design decisions :
 * <ul>
 * <li>transformations are handled in a straightforward way. Drawing operations
 * should <em>not</em> modify the current transformation.
 * </ul>
 * 
 * 
 * @author Serge Rosmorduc
 */
public abstract class BaseGraphics2D extends Graphics2D {

	private Composite composite;

	private Color currentBackground;

	private Shape currentClip;

	private Font currentFont;

	private Color currentForeground;

	private Paint currentPaint;

	private Stroke currentStroke;

	private AffineTransform currentTransform;

	private HashMap renderingHints = new HashMap();

	protected BaseGraphics2D() {
		currentBackground = Color.WHITE;
		currentForeground = Color.BLACK;
		currentFont = new Font("Serif", Font.PLAIN, 12);
		currentTransform = new AffineTransform();
		currentStroke = new BasicStroke();
	}

	/**
	 * Create a duplicate of this graphic context.
	 * <p>
	 * To be used when writting copy constructor for subclasses.
	 * 
	 * @param g
	 */
	public BaseGraphics2D(BaseGraphics2D g) {
		// We should ge sure that shapes are immutable ?
		// TODO : copy the shape.
		if (g.currentClip != null)
			currentClip = g.currentClip;
		if (g.composite != null)
			composite = g.composite;

		currentBackground = g.currentBackground;

		currentFont = g.currentFont;

		currentForeground = g.currentForeground;

		currentPaint = g.currentPaint;

		currentStroke = g.currentStroke;

		currentTransform = (AffineTransform) g.currentTransform.clone();

		renderingHints = g.renderingHints;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#addRenderingHints(java.util.Map)
	 */
	public void addRenderingHints(Map hints) {
		renderingHints.putAll(hints);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#clearRect(int, int, int, int)
	 */
	public void clearRect(int x, int y, int width, int height) {
		Color c = getColor();
		setColor(getBackground());
		fillRect(x, y, width, height);
		setColor(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#clip(java.awt.Shape)
	 */
	public void clip(Shape s) {
		if (currentClip == null)
			currentClip = s;
		else {
			Area a;
			if (currentClip instanceof Area)
				a = (Area) currentClip;
			else
				a = new Area(currentClip);
			a.intersect(new Area(s));
			currentClip = a;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#clipRect(int, int, int, int)
	 */
	public void clipRect(int x, int y, int width, int height) {
		Area clip;
		if (currentClip instanceof Area) {
			clip = (Area) currentClip;
		} else {
			clip = new Area(currentClip);
		}
		clip.intersect(new Area(new Rectangle(x, y, width, height)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
	 */
	abstract public void copyArea(int x, int y, int width, int height, int dx,
			int dy);

	/**
	 * Create a clone of this graphics2d, rendering into the same picture. Keep
	 * different states informations however. Suggested implementation: create a
	 * copy constructor and use
	 * 
	 * <pre>
	 * return new MyGraphicsType(this);
	 * </pre>
	 * 
	 * as implementation.
	 * 
	 * @see java.awt.Graphics#create()
	 */
	abstract public Graphics create();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#dispose()
	 */
	abstract public void dispose();

	/**
	 * Draws a shape.
	 * <p>
	 * A basic, probably very suboptimal implementation of draw, which calls
	 * fill(shape). For the sake of efficiency, you should probably rewrite it.
	 * 
	 * @see Graphics2D#draw(Shape)
	 */
	public void draw(Shape s) {
		fill(getStroke().createStrokedShape(s));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#drawArc(int, int, int, int, int, int)
	 */
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		draw(new Arc2D.Float(x, y, width, height, startAngle, arcAngle,
				Arc2D.OPEN));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#drawGlyphVector(java.awt.font.GlyphVector,
	 * float, float)
	 */
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		// To use fonts in our output, we need quite a lot of work.
		// For now, we use fonts as sources for drawings !
		// wmfOut.textOut(p.x, p.y, txt);
		translate(x, y);
		for (int i = 0; i < g.getNumGlyphs(); i++)
			// wmfOut.SetPolyFillMode(WMFConstants.ALTERNATE);
			fill(g.getGlyphOutline(i));
		// draw(vect.getOutline());
		// wmfOut.SetPolyFillMode(WMFConstants.WINDING);
		translate(-x, -y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#drawImage(java.awt.image.BufferedImage,
	 * java.awt.image.BufferedImageOp, int, int)
	 */
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		// Code taken from the javadoc !
		Image img1 = op.filter(img, null);
		drawImage(img1, AffineTransform.getTranslateInstance(x, y), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#drawImage(java.awt.Image,
	 * java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
	 */
	abstract public boolean drawImage(Image img, AffineTransform xform,
			ImageObserver obs);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int,
	 * java.awt.Color, java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		if (w != -1 && h != -1) {
			Color col = getColor();
			this.setColor(bgcolor);
			fillRect(x, y, w, h);
			drawImage(img, x, y, observer);
			setColor(col);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int,
	 * java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		return drawImage(img, AffineTransform.getTranslateInstance(x, y),
				observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int,
	 * java.awt.Color, java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) {
		// Compute the transform
		AffineTransform tr;
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		if (w == -1)
			w = width;
		if (h == -1)
			h = height;
		float xscale = width / (float) w;
		float yscale = height / (float) h;
		Color col = getColor();
		if (w != -1 && h != -1) {
			this.setColor(bgcolor);
			fillRect(x, y, width, height);
		}
		tr = new AffineTransform(xscale, 0, x, 0, yscale, y);
		boolean result = drawImage(img, tr, observer);
		setColor(col);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int,
	 * java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		return drawImage(img, x, y, width, height, getBackground(), observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int,
	 * int, int, int, java.awt.Color, java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, Color bgcolor,
			ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int,
	 * int, int, int, java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#drawLine(int, int, int, int)
	 */
	public void drawLine(int x1, int y1, int x2, int y2) {
		draw(new Line2D.Float(x1, y1, x2, y2));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#drawOval(int, int, int, int)
	 */
	public void drawOval(int x, int y, int width, int height) {
		draw(new Ellipse2D.Float(x, y, width, height));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#drawPolygon(int[], int[], int)
	 */
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		GeneralPath s = getPathFor(xPoints, yPoints, nPoints);
		if (s != null) {
			s.closePath();
			draw(s);
		}
	}

	/**
	 * Draws a polygon.
	 * <p>
	 * The default implementation uses shapes. You can probably get something
	 * more efficient.
	 * 
	 * @see java.awt.Graphics#drawPolyline(int[], int[], int)
	 */
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		GeneralPath s = getPathFor(xPoints, yPoints, nPoints);
		draw(s);
	}

	/**
	 * A simple and probably false interpretation.
	 * 
	 * @see java.awt.Graphics2D#drawRenderableImage(java.awt.image.renderable.RenderableImage,
	 *      java.awt.geom.AffineTransform)
	 */
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		RenderedImage img1 = img.createDefaultRendering();
		drawRenderedImage(img1, xform);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#drawRenderedImage(java.awt.image.RenderedImage,
	 * java.awt.geom.AffineTransform)
	 */
	abstract public void drawRenderedImage(RenderedImage img,
			AffineTransform xform);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#drawRoundRect(int, int, int, int, int, int)
	 */
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		draw(new RoundRectangle2D.Float(x, y, width, height, arcWidth,
				arcHeight));

	}

	/*
	 * Uses drawGlyphVector.
	 * 
	 * @see
	 * java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator,
	 * float, float)
	 */
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		GlyphVector vect = getFont().createGlyphVector(getFontRenderContext(),
				iterator);
		drawGlyphVector(vect, x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator,
	 * int, int)
	 */
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		drawString(iterator, (float) x, (float) y);
	}

	/*
	 * Uses drawGlyphVector.
	 * 
	 * @see java.awt.Graphics2D#drawString(java.lang.String, float, float)
	 */
	public void drawString(String s, float x, float y) {
		GlyphVector vect = getFont().createGlyphVector(getFontRenderContext(),
				s);
		drawGlyphVector(vect, x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#drawString(java.lang.String, int, int)
	 */
	public void drawString(String str, int x, int y) {
		drawString(str, (float) x, (float) y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#fill(java.awt.Shape)
	 */
	abstract public void fill(Shape s);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#fillArc(int, int, int, int, int, int)
	 */
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		fill(new Arc2D.Float(x, y, width, height, startAngle, arcAngle,
				Arc2D.PIE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#fillOval(int, int, int, int)
	 */
	public void fillOval(int x, int y, int width, int height) {
		draw(new Ellipse2D.Float(x, y, width, height));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#fillPolygon(int[], int[], int)
	 */
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		GeneralPath s = getPathFor(xPoints, yPoints, nPoints);
		if (s != null) {
			s.closePath();
			fill(s);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#fillRect(int, int, int, int)
	 */
	public void fillRect(int x, int y, int width, int height) {
		fill(new Rectangle2D.Float(x, y, width, height));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#fillRoundRect(int, int, int, int, int, int)
	 */
	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		fill(new RoundRectangle2D.Float(x, y, width, height, arcWidth,
				arcHeight));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#getBackground()
	 */
	public Color getBackground() {
		return currentBackground;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#getClip()
	 */
	public Shape getClip() {
		return currentClip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#getClipBounds()
	 */
	public Rectangle getClipBounds() {
		return currentClip.getBounds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#getColor()
	 */
	public Color getColor() {
		return currentForeground;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#getComposite()
	 */
	public Composite getComposite() {
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#getFont()
	 */
	public Font getFont() {
		return currentFont;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#getFontMetrics(java.awt.Font)
	 */
	public FontMetrics getFontMetrics(Font f) {
		return new SimpleFontMetrics(f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#getPaint()
	 */
	public Paint getPaint() {
		if (currentPaint == null)
			return currentForeground;
		else
			return currentPaint;
	}

	/**
	 * Builds a path for a polyline.
	 * <p>
	 * This command can be used to create the path needed by most polygon
	 * oriented commands.
	 * <p>
	 * The path is not closed, but a simple closePath command will do.
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 * @return a path, or null if nPoints is 0.
	 */
	protected GeneralPath getPathFor(int[] xPoints, int[] yPoints, int nPoints) {
		GeneralPath s = null;
		if (nPoints != 0) {
			s = new GeneralPath();
			s.moveTo(xPoints[0], yPoints[0]);
			for (int i = 1; i < nPoints; i++) {
				s.lineTo(xPoints[i], yPoints[i]);
			}
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#getRenderingHint(java.awt.RenderingHints.Key)
	 */
	public Object getRenderingHint(Key hintKey) {
		return renderingHints.get(hintKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#getRenderingHints()
	 */
	public RenderingHints getRenderingHints() {
		return new RenderingHints(renderingHints);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#getStroke()
	 */
	public Stroke getStroke() {
		return currentStroke;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#getTransform()
	 */
	public AffineTransform getTransform() {
		// Must return a *copy* of current transform.
		return (AffineTransform) currentTransform.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#hit(java.awt.Rectangle, java.awt.Shape, boolean)
	 */
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#rotate(double)
	 */
	public void rotate(double theta) {
		rotate(theta, 0, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#rotate(double, double, double)
	 */
	public void rotate(double theta, double x, double y) {
		transform(AffineTransform.getRotateInstance(theta, x, y));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#scale(double, double)
	 */
	public void scale(double sx, double sy) {
		transform(AffineTransform.getScaleInstance(sx, sy));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#setBackground(java.awt.Color)
	 */
	public void setBackground(Color color) {
		currentBackground = color;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#setClip(int, int, int, int)
	 */
	public void setClip(int x, int y, int width, int height) {
		setClip(new Rectangle(x, y, width, height));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#setClip(java.awt.Shape)
	 */
	public void setClip(Shape clip) {
		currentClip = clip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#setColor(java.awt.Color)
	 */
	public void setColor(Color c) {
		currentForeground = c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#setComposite(java.awt.Composite)
	 */
	public void setComposite(Composite comp) {
		composite = comp;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics#setFont(java.awt.Font)
	 */
	public void setFont(Font font) {
		currentFont = font;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#setPaint(java.awt.Paint)
	 */
	public void setPaint(Paint paint) {
		if (paint != null) {
			if (paint instanceof Color) {
				currentForeground = (Color) paint;
				currentPaint = null;
			} else
				currentPaint = paint;
		}
	}

	/*
	 * Switches back to paint mode, disabling Xor mode. <p> May usually be
	 * empty.
	 */
	abstract public void setPaintMode();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#setRenderingHint(java.awt.RenderingHints.Key,
	 * java.lang.Object)
	 */
	public void setRenderingHint(Key hintKey, Object hintValue) {
		renderingHints.put(hintKey, hintValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#setRenderingHints(java.util.Map)
	 */
	public void setRenderingHints(Map hints) {
		renderingHints.clear();
		renderingHints.putAll(hints);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#setStroke(java.awt.Stroke)
	 */
	public void setStroke(Stroke s) {
		currentStroke = s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#setTransform(java.awt.geom.AffineTransform)
	 */
	public void setTransform(AffineTransform Tx) {
		currentTransform = (AffineTransform) Tx.clone();
	}

	/**
	 * Switches to Xor mode.
	 * 
	 * @see java.awt.Graphics#setXORMode(java.awt.Color)
	 */
	abstract public void setXORMode(Color c1);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#shear(double, double)
	 */
	public void shear(double shx, double shy) {
		transform(AffineTransform.getShearInstance(shx, shy));
	}

	/**
	 * Transform the current system reference according to Tx.
	 * <p>
	 * All scaling and assorted transformations in BaseGraphics2D use this
	 * method. Hence, if you want to modify the transformation system, simply
	 * redefine it.
	 * 
	 * @see java.awt.Graphics2D#transform(java.awt.geom.AffineTransform)
	 */
	public void transform(AffineTransform Tx) {
		currentTransform.concatenate(Tx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#translate(double, double)
	 */
	public void translate(double tx, double ty) {
		transform(AffineTransform.getTranslateInstance(tx, ty));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Graphics2D#translate(int, int)
	 */
	public void translate(int x, int y) {
		translate((double) x, (double) y);
	}

	/**
	 * Communicates properties (which can be used as a generic system for
	 * parameters) to the graphic system.
	 * 
	 * @param properties
	 */
	public abstract void setProperties(Properties properties);


	public GraphicsConfiguration getDeviceConfiguration() {
		return new StreamGraphicsConfiguration(
				new Rectangle(0, 0, 10000, 10000), new AffineTransform());
	}

	public FontRenderContext getFontRenderContext() {
		// IMPORTANT :
		return new FontRenderContext(getTransform(), false, true);
	}
}