package org.qenherkhopeshef.graphics.eps;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import org.qenherkhopeshef.graphics.generic.BaseGraphics2D;
import org.qenherkhopeshef.graphics.generic.StreamGraphicsConfiguration;


/**
 * Quick hack for producing EPS graphics. Should probably be improved and
 * cleaned up, as it is mainly a cut and paste from SVGGraphics2D, which is in
 * turn a cut and paste from WMFGraphics2D.
 * 
 * Some kind of "vector graphics" base class would be welcome.
 * 
 * @author Serge Rosmorduc
 */
public class EncapsulatedPostscriptGraphics2D extends BaseGraphics2D {

	/**
	 * The EPS device.
	 */

	EPSLowLevel epsOut;

	/**
	 * Picture dimensions, in px.
	 */
	private Dimension dimensions;

	/**
	 * Curves maximal error.
	 */
	private double precision = 0.05;

	/**
	 * True if the graphic context's disposal should close the file. (when
	 * copies are made with create(), their disposal should not close the wmf
	 * file).
	 */
	private boolean closeOnDispose;

	/**
	 * Creates a SVGGraphics2D in the file f.
	 * 
	 * @param f
	 * @param dims :
	 *            graphics dimensions, in points.
	 * @throws IOException
	 */

	public EncapsulatedPostscriptGraphics2D(File f, Dimension2D dims)
			throws IOException {
		this(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"), dims,"");
	}

	public EncapsulatedPostscriptGraphics2D(File f, Dimension2D dims, String comment)
		throws IOException {
		this(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"), dims, comment);
	}
	/**
	 * Creates a SVGGraphics2D in the file fname.
	 * 
	 * @param fname
	 *            path of the file
	 * @param dims
	 *            dimensions, expressed in points
	 * @throws IOException
	 */
	public EncapsulatedPostscriptGraphics2D(String fname, Dimension2D dims)
			throws IOException {
		this(new File(fname), dims);
	}

	/**
	 * Create a SVGGraphics2D graphics which will be stored in stream. Stream
	 * may encapsulate a RandomAccessFile or a simple byte array. Note that the
	 * Writer encoding <b>must</b> be UTF-8.
	 * 
	 * @param writer
	 * @param dims
	 *            dimensions, expressed in pica points (1/72 inch or 0.03528cm)
	 */
	public EncapsulatedPostscriptGraphics2D(Writer writer, Dimension2D dims, String comment)
			throws IOException {
		closeOnDispose = true;
		epsOut = new EPSLowLevel(writer, dims,comment);
		setFont(new Font("SansSerif", Font.PLAIN, 12));
		
	}

	private EncapsulatedPostscriptGraphics2D(EncapsulatedPostscriptGraphics2D g) {
		super(g);
		dimensions = g.dimensions;
		epsOut = g.epsOut;
		closeOnDispose = false;
	}

	public void draw(Shape shape) {
		if (shape instanceof Line2D) {
			Line2D l = (Line2D) shape;
			Point2D a = this.deviceCoords(l.getP1());
			Point2D b = this.deviceCoords(l.getP2());

			epsOut.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
		} else {
			epsOut.gsave();
			addPath(shape);
			epsOut.setColor(getColor().getRed(), getColor().getGreen(),
					getColor().getBlue());
			epsOut.strokePath();
			epsOut.grestore();
		}
	}

	/**
	 * Adds a new path to the drawing surface.
	 * 
	 * @param shape
	 */
	private void addPath(Shape shape) {
		epsOut.startPath();

		PathIterator iter = shape.getPathIterator(null);

		Point2D first = null;

		while (!iter.isDone()) {
			double coords[] = new double[6];
			int type = iter.currentSegment(coords);
			Point2D points[] = getPointsInDeviceSpace(coords); // create new
			// points
			switch (type) {
			case PathIterator.SEG_CLOSE:
				epsOut.closePath();
				break;
			case PathIterator.SEG_CUBICTO:
				epsOut.cubicTo(points[0].getX(), points[0].getY(), points[1]
						.getX(), points[1].getY(), points[2].getX(), points[2]
						.getY());
				break;
			case PathIterator.SEG_LINETO:
				epsOut.lineTo(points[0].getX(), points[0].getY());
				break;
			case PathIterator.SEG_MOVETO:
				first = points[0];
				epsOut.moveTo(first.getX(), first.getY());
				break;
			case PathIterator.SEG_QUADTO:
				epsOut.quadTo(points[0].getX(), points[0].getY(), points[1]
						.getX(), points[1].getY());
				break;
			default:
				throw new RuntimeException(
						"unexpected constant in path iterator");
			}
			iter.next();
		}
	}

	/**
	 * fill a shape.
	 */
	public void fill(Shape shape) {
		epsOut.gsave();
		addPath(shape);
		epsOut.setColor(getColor().getRed(), getColor().getGreen(), getColor()
				.getBlue());
		epsOut.fillPath();
		epsOut.grestore();

	}

	public double getPrecision() {
		return precision;
	}

	/**
	 * Sets the maximal error between theoretical curves and what is drawn. The
	 * greater the precision, the nicer the curves... but the larger the file.
	 * 
	 * @param precision
	 */
	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public void setStroke(Stroke stroke) {
		super.setStroke(stroke);
		if (stroke instanceof BasicStroke) {
			// We should attempt to use "standard" postscript strokes in this case...
			//BasicStroke bs = (BasicStroke) stroke;
		}
	}

	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		// NO OP
	}

	public Graphics create() {
		return new EncapsulatedPostscriptGraphics2D(this);
	}

	public void dispose() {
		if (closeOnDispose && epsOut != null) {
			epsOut.close();
			epsOut = null;
		} else
			epsOut = null;
	}

	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		// NO OP
		return false;
	}

	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		// NO OP

	}

	public GraphicsConfiguration getDeviceConfiguration() {
		return new StreamGraphicsConfiguration(
				new Rectangle(0, 0, 10000, 10000), new AffineTransform());
	}

	public FontRenderContext getFontRenderContext() {
		// IMPORTANT :
		return new FontRenderContext(getTransform(), false, true);
	}

	public void setPaintMode() {
		// NO OP

	}

	public void setXORMode(Color c1) {
		// NO OP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.graphics.wmfexport.BaseGraphics2D#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties properties) {
		// NO OP

	}

	/**
	 * Takes coordinates in the current space, and transform them to device
	 * space. device space is constant for a specific device.
	 * 
	 * @param p
	 * @return
	 */

	private Point2D deviceCoords(Point2D p) {
		Point2D res = new Point2D.Double();
		getTransform().transform(p, res);
		return res;
	}

	private Point2D[] getPointsInDeviceSpace(double coords[]) {
		Point2D points[] = new Point2D[coords.length / 2];
		for (int i = 0; i < points.length; i++) {
			Point2D.Double p = new Point2D.Double(coords[i * 2],
					coords[i * 2 + 1]);
			points[i] = deviceCoords(p);
		}
		return points;
	}

}
