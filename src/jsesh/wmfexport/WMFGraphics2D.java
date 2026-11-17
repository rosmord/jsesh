package jsesh.wmfexport;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.freehep.graphicsio.AbstractVectorGraphicsIO;

/**
 * Windows Metafile Format Graphics 2D driver.
 * 
 * IMPORTANT : improve pen handling. Pen should be as shared as possible. Store
 * them in an array, and delete those who don't survive a gc restore.
 * 
 * @author S. Rosmorduc
 */
public class WMFGraphics2D extends AbstractVectorGraphicsIO {

	WMFDeviceContext wmfOut;

	WMFPen currentPen;

	Stack transformations;

	// AffineTransform currentTransform;
	private Dimension dimensions;

	/**
	 * Curves maximal error.
	 */
	private double precision = 0.05;

	/**
	 * Creates a WMFGraphics2D in the file f.
	 * 
	 * @param f
	 * @param dims :
	 *            graphics dimensions, in points.
	 * @throws IOException
	 */
	public WMFGraphics2D(File f, Dimension dims) throws IOException {
		// automated restore
		super(dims, false);
		dimensions = new Dimension(dims.width * 20, dims.height * 20);
		wmfOut = new WMFDeviceContext(f, dimensions);
		transformations = new Stack();
		scale(20, 20);
		setFont(new Font("SansSerif", Font.PLAIN, 12));
		// scale(1440,1440);
		//currentTransform= new AffineTransform();
	}

	public WMFGraphics2D(String fname, Dimension dims) throws IOException {
		this(new File(fname), dims);
	}

	private WMFGraphics2D(WMFGraphics2D g) {
		super(g, false);
		wmfOut = g.wmfOut;
		transformations = (Stack) g.transformations.clone();
		for (int i = 0; i < transformations.size(); i++) {
			transformations.set(i, ((AffineTransform) g.transformations.get(i))
					.clone());
		}
		// g.setTransform((AffineTransform) getTransform().clone());
		// currentTransform= (AffineTransform) g.currentTransform.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writeHeader()
	 */
	public void writeHeader() throws IOException {
		wmfOut.setWindowOrg((short) 0, (short) 0);
		wmfOut.setWindowExt((short) dimensions.getWidth(), (short) dimensions
				.getHeight());
		wmfOut.SaveDC();
		wmfOut.SetROP2(WMFConstants.R2_COPYPEN);
		wmfOut.SetBKColor(0x000000);
		wmfOut.SetBKMode((short) WMFConstants.TRANSPARENT);
		wmfOut.createPenIndirect(WMFConstants.PS_SOLID, (short) 20,
				WMFDeviceContext.buildColor(0, 0, 0));
		wmfOut.SelectObject((short) 0);
		wmfOut.createBrushIndirect((short) 0, 0, WMFConstants.BS_SOLID);
		wmfOut.SelectObject((short) 1);
		wmfOut.SetPolyFillMode(WMFConstants.WINDING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writeBackground()
	 */
	public void writeBackground() throws IOException {
		// NO OP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writeTrailer()
	 */
	public void writeTrailer() throws IOException {
		wmfOut.RestoreDC((short) -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#closeStream()
	 */
	public void closeStream() throws IOException {
		wmfOut.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writeComment(java.lang.String)
	 */
	public void writeComment(String arg0) throws IOException {
		// NO OP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writeGraphicsSave()
	 */
	protected void writeGraphicsSave() throws IOException {
		wmfOut.SaveDC();
		//transformations.push(currentTransform.clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writeGraphicsRestore()
	 */
	protected void writeGraphicsRestore() throws IOException {
		wmfOut.RestoreDC((short) 1);
		/*
		 * if (transformations.isEmpty()) currentTransform= new
		 * AffineTransform(); else currentTransform= (AffineTransform)
		 * transformations.pop();
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writeImage(java.awt.image.RenderedImage,
	 *      java.awt.geom.AffineTransform, java.awt.Color)
	 */
	protected void writeImage(RenderedImage arg0, AffineTransform arg1,
			Color arg2) throws IOException {
		// NO OP CURRENTLY.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writeString(java.lang.String,
	 *      double, double)
	 */
	protected void writeString(String txt, double x, double y)
			throws IOException {
		WMFPoint p = deviceCoords(x, y);
		// To use fonts in our output, we need quite a lot of work.
		// For now, we use fonts as sources for drawings !
		//wmfOut.textOut(p.x, p.y, txt);
		translate(x, y);
		GlyphVector vect = getFont().createGlyphVector(getFontRenderContext(),
				txt);
		for (int i=0; i < vect.getNumGlyphs(); i++) 
		//wmfOut.SetPolyFillMode(WMFConstants.ALTERNATE);
			fill(vect.getGlyphOutline(i));
		//draw(vect.getOutline());
		//wmfOut.SetPolyFillMode(WMFConstants.WINDING);
		translate(-x, -y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writeTransform(java.awt.geom.AffineTransform)
	 */
	protected void writeTransform(AffineTransform transform) throws IOException {
		// currentTransform= (AffineTransform) transform.clone();
		// DO nothing ????
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writeClip(java.awt.geom.Rectangle2D)
	 */
	protected void writeClip(Rectangle2D arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writeClip(java.awt.Shape)
	 */
	protected void writeClip(Shape arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writePaint(java.awt.Color)
	 */
	protected void writePaint(Color arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writePaint(java.awt.GradientPaint)
	 */
	protected void writePaint(GradientPaint arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writePaint(java.awt.TexturePaint)
	 */
	protected void writePaint(TexturePaint arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#writePaint(java.awt.Paint)
	 */
	protected void writePaint(Paint arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#copyArea(int, int, int, int,
	 *      int, int)
	 */
	public void copyArea(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#create()
	 */
	public Graphics create() {
		return new WMFGraphics2D(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#setPaintMode()
	 */
	public void setPaintMode() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#setXORMode(java.awt.Color)
	 */
	public void setXORMode(Color arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#toString()
	 */
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#draw(java.awt.Shape)
	 */
	public void draw(Shape shape) {
		try {
			WMFPen p = wmfOut.createDrawPen(getWMFLineWidth(), WMFDeviceContext
					.buildColor(getColor()));
			wmfOut.selectPen(p);
			if (shape instanceof Line2D) {
				Line2D l = (Line2D) shape;
				WMFPoint p1 = deviceCoords(l.getP1());
				WMFPoint p2 = deviceCoords(l.getP2());

				wmfOut.MoveTo(p1.x, p1.y);
				wmfOut.LineTo(p2.x, p2.y);
			} else {

				//wmfOut.SelectObject((short) 1);
				// Default case
				PathIterator iter = new FlatteningPathIterator(shape
						.getPathIterator(null), precision);
				WMFPoint first = null;
				while (!iter.isDone()) {
					double coords[] = new double[6];
					int type = iter.currentSegment(coords);
					switch (type) {
					case PathIterator.SEG_CLOSE:
						wmfOut.LineTo(first);
						break;
					case PathIterator.SEG_CUBICTO:
						wmfOut.LineTo(deviceCoords(coords[0], coords[1]));
						wmfOut.LineTo(deviceCoords(coords[2], coords[3]));
						wmfOut.LineTo(deviceCoords(coords[4], coords[5]));
						break;
					case PathIterator.SEG_LINETO:
						wmfOut.LineTo(deviceCoords(coords[0], coords[1]));
						break;
					case PathIterator.SEG_MOVETO:
						first = deviceCoords(coords[0], coords[1]);
						wmfOut.MoveTo(first);
						break;
					case PathIterator.SEG_QUADTO:
						wmfOut.LineTo(deviceCoords(coords[0], coords[1]));
						wmfOut.LineTo(deviceCoords(coords[2], coords[3]));
						break;
					default:
						throw new RuntimeException(
								"unexpected constant in path iterator");
					}
					iter.next();
				}
			}
			wmfOut.freePen(p);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#drawGlyphVector(java.awt.font.GlyphVector,
	 *      float, float)
	 */
	public void drawGlyphVector(GlyphVector arg0, float x, float y) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#drawString(java.text.AttributedCharacterIterator,
	 *      float, float)
	 */
	public void drawString(AttributedCharacterIterator arg0, float x, float y) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#fill(java.awt.Shape)
	 */
	public void fill(Shape shape) {
		try {
			double coords[] = new double[6];
			Color fillColor = getColor();
			if (getPaint() instanceof Color) {
				fillColor = (Color) getPaint();
			}
			WMFPen fillPen = wmfOut.createFillPen(WMFDeviceContext
					.buildColor(fillColor));
			WMFPen unfillPen = wmfOut.createFillPen(WMFDeviceContext
					.buildColor(getBackground()));
			//wmfOut.SelectObject((short) 1);
			// Default case
			if (shape instanceof Rectangle2D) {
				Rectangle2D r = (Rectangle2D) shape;
				WMFPoint topLeft = deviceCoords(r.getMinX(), r.getMinY());
				WMFPoint bottomRight = deviceCoords(r.getMaxX(), r.getMaxY());
				wmfOut.selectPen(fillPen);
				wmfOut.rectangle(topLeft.x, topLeft.y, bottomRight.x,
						bottomRight.y);
			} else {
				PathIterator iter = new FlatteningPathIterator(shape
						.getPathIterator(null), precision);
				WMFPoint first = null;
				List polygons= new LinkedList();
				ArrayList points = new ArrayList();
				while (!iter.isDone()) {
					int type = iter.currentSegment(coords);
					switch (type) {
					case PathIterator.SEG_CLOSE:
						polygons.add(points);
						points= new ArrayList();
						break;
					case PathIterator.SEG_CUBICTO:
						for (int i = 0; i < 6; i += 2)
							points.add(new Point2D.Double(coords[i],
									coords[i + 1]));
						break;
					case PathIterator.SEG_LINETO:
						points.add(new Point2D.Double(coords[0], coords[1]));
						break;
					case PathIterator.SEG_MOVETO:
						if (!points.isEmpty()) {
							polygons.add(points);
							points= new ArrayList();
						}
						//first = deviceCoords(coords[0], coords[1]);
						points.add(new Point2D.Double(coords[0], coords[1]));
						//wmfOut.MoveTo(first);
						break;
					case PathIterator.SEG_QUADTO:
						for (int i = 0; i < 4; i += 2)
							points.add(new Point2D.Double(coords[i],
									coords[i + 1]));
						break;
					default:
						throw new RuntimeException(
								"unexpected constant in path iterator");
					}
					iter.next();
				}
				if (!points.isEmpty()) {
					polygons.add(points);
					points= null;
				}
				
				if (! polygons.isEmpty()) {
					// THIS ALGORITHM IS FALSE !!! It works more or less for us, 
					// because of the conventions we use, but it's false.
					// "true" system : 
					// for each component, take a normal vector. compute the intersections with the others components.
					// the sum can be computed simply by component. 
					// IMPORTANT : deal with WIND_EVEN_ODD one day.
					// The painting is probably based on WIND_NON_ZERO
					// the polygon system is too simple for that.
					// So we compute the geometric area of the whole shape.
					// if it's positive, sub-polygons which have a positive area are drawn in foreground colour,
					// and the other in the background colour.
					// else, we reverse.
					double area= 0;
					for (Iterator i= polygons.iterator(); i.hasNext();) {
						List pts= (List) i.next();
						area+= algebricArea(pts);
					}
					// Inverse the two filling pens if necessary.
					if (area < 0) {
						WMFPen p= fillPen;
						fillPen= unfillPen;
						unfillPen= p;
					}
					// paint the content.
					for (Iterator i= polygons.iterator(); i.hasNext();) {
						List pts= (List) i.next();
						if (algebricArea(pts) > 0) {
							wmfFillPolygon(pts, fillPen);
						} else {
							wmfFillPolygon(pts, unfillPen);
						}
						
					}
				}
			}
			wmfOut.freePen(fillPen);
			wmfOut.freePen(unfillPen);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private WMFPen selectedPen; // Temporary. A better system should be used

	// later.

	private float currentLineWidth;

	/**
	 * Compute the algebric area of a polygon.
	 * 
	 * @param points
	 * @return
	 */
	private double algebricArea(List points) {
		double area = 0;
		if (points.size() != 0) {
			Point2D first = (Point2D) points.get(0);
			Point2D old = first;
			for (int i = 1; i < points.size(); i++) {
				Point2D curr = (Point2D) points.get(i);
				area += (curr.getX() - old.getX()) * (curr.getY() + old.getY());
				old = curr;
			}
			area += (first.getX() - old.getX()) * (first.getY() + old.getY());
		}
		return area;
	}

	/**
	 * Fills a polygon.
	 * <p>
	 * This function is currently basically false ! The filling system I assume
	 * is the one used by TeX's MetaFonts. But the one really used is different,
	 * because it takes into account the whole shape, not just the connex
	 * compounds of its frontier.
	 * <p> 
	 * 
	 * <p>
	 * A global solution: a bit longer, but usable. With our rules, the
	 * geometric area of shape should be positive. If we get a negative global
	 * area, we only need to inverse it.
	 * 
	 * 
	 * <P>
	 * WMF, when in winding mode, has the following meaning : the number of time
	 * we turn around a point, taking into account the drawing direction.
	 * <p>
	 * This is exactly Sun's WIND_NON_ZERO. However, polygons in WMF are connex.
	 * 
	 * @param points :
	 *            a list of Point2D
	 * @throws IOException
	 */

	private void wmfFillPolygon(List points, WMFPen fill)
			throws IOException {
		if (points.isEmpty())
			return;
		WMFPoint pts[] = new WMFPoint[points.size()];
		for (int i = 0; i < pts.length; i++) {
			pts[i] = deviceCoords((Point2D) points.get(i));
		}
		wmfOut.selectPen(fill);
		wmfOut.Polygon(pts, (short) pts.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#getDeviceConfiguration()
	 */
	public GraphicsConfiguration getDeviceConfiguration() {
		// TODO : implements this ?
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#hit(java.awt.Rectangle,
	 *      java.awt.Shape, boolean)
	 */
	public boolean hit(Rectangle arg0, Shape arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#create(double, double, double,
	 *      double)
	 */
	public Graphics create(double arg0, double arg1, double arg2, double arg3) {
		return create();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphics2d.VectorGraphics#fillAndDraw(java.awt.Shape,
	 *      java.awt.Color)
	 */
	public void fillAndDraw(Shape shape, Color color) {
		Color c = getColor();
		setColor(color);
		fill(shape);
		draw(shape);
		setColor(c);
	}

	// Compute the coordinates of a point in device space.

	private WMFPoint deviceCoords(double x, double y) {
		return deviceCoords(new Point2D.Double(x, y));
	}

	private WMFPoint deviceCoords(Point2D p) {
		Point2D res = new Point2D.Double();
		getTransform().transform(p, res);
		return new WMFPoint((short) res.getX(), (short) res.getY());
	}

	/**
	 * @return Returns the dimensions.
	 */
	public Dimension getDimensions() {
		return dimensions;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.freehep.graphicsio.AbstractVectorGraphicsIO#setStroke(java.awt.Stroke)
	 */
	public void setStroke(Stroke stroke) {
		super.setStroke(stroke);
		if (stroke instanceof BasicStroke) {
			BasicStroke bs = (BasicStroke) stroke;
			currentLineWidth = bs.getLineWidth();
		}
	}

	public short getWMFLineWidth() {
		return (short) (currentLineWidth * getTransform().getScaleY());
	}
}