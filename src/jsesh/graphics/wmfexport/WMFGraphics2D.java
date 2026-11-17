package jsesh.graphics.wmfexport;

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
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import jsesh.graphics.generic.BaseGraphics2D;
import jsesh.graphics.generic.RandomAccessFileAdapter;
import jsesh.graphics.generic.RandomAccessStream;
import jsesh.graphics.generic.StreamGraphicsConfiguration;
import jsesh.utils.GeometryUtils;

/**
 * Windows Metafile Format Graphics 2D driver.
 * 
 * <p>
 * Unit : Originally, a unit of 1.0 (1 point) is worth 20 twips (or 1/72 inch).
 * <p>
 * IMPORTANT : improve pen handling. Pen should be as shared as possible. Store
 * them in an array, and delete those who don't survive a gc restore.
 * 
 * @author S. Rosmorduc
 */
public class WMFGraphics2D extends BaseGraphics2D {

	private float currentLineWidth;

	WMFPen currentPen;

	/**
	 * Picture dimensions, in TWIPS.
	 */
	private Dimension dimensions;

	/**
	 * Curves maximal error.
	 */
	private double precision = 0.05;

	private WMFDeviceContext wmfOut;

	/**
	 * True if the graphic context's disposal should close the wmfOut file.
	 * (when copies are made with create(), their disposal should not close the
	 * wmf file).
	 */
	private boolean closeOnDispose;

	/**
	 * Creates a WMFGraphics2D in the file f.
	 * 
	 * @param f
	 * @param dims : graphics dimensions, in points.
	 * @throws IOException
	 */

	public WMFGraphics2D(File f, Dimension dims) throws IOException {
		this(new RandomAccessFileAdapter(new RandomAccessFile(f,"rw")), dims);
	}

	/**
	 * Creates a WMFGraphics2D in the file fname.
	 * 
	 * @param fname path of the file
	 * @param dims dimensions, expressed in points
	 * @throws IOException
	 */
	public WMFGraphics2D(String fname, Dimension dims) throws IOException {
		this(new File(fname), dims);
	}

	/**
	 * Create a WMF graphics which will be stored in stream.
	 * Stream may encapsulate a RandomAccessFile or a simple byte array.
	 * @param stream
	 * @param dims dimensions, expressed in points
	 */
	public WMFGraphics2D(RandomAccessStream stream, Dimension dims) throws IOException {
		closeOnDispose = true;
		dimensions = new Dimension(dims.width * 20, dims.height * 20);
		wmfOut = new WMFDeviceContext(stream, dimensions);
		scale(20, 20);
		setFont(new Font("SansSerif", Font.PLAIN, 12));
		writeHeader();		
	}
	
	private WMFGraphics2D(WMFGraphics2D g) {
		super(g);
		dimensions = g.dimensions;
		wmfOut = g.wmfOut;
		closeOnDispose = false;
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

				// wmfOut.SelectObject((short) 1);
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


	/**
	 * fill a shape. In the general case, will fill a shape assuming an even-odd rule.
	 * In order to get a reasonably robust result, as java paths are not sensitive to the order of their subpaths, but WMF is, 
	 * we order the subpathes according to their minimal abscisse. Thus, the outerest subpathes are drawn first. This works only if pathes
	 * do not overlap. We are rather lucky in this case, as the Area class makes it trivial to transform a complex shape whith overlapping pathes into
	 * one without overlaps.
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
			// wmfOut.SelectObject((short) 1);
			// Default case
			if (shape instanceof Rectangle2D) {
				Rectangle2D r = (Rectangle2D) shape;
				WMFPoint topLeft = deviceCoords(r.getMinX(), r.getMinY());
				WMFPoint bottomRight = deviceCoords(r.getMaxX(), r.getMaxY());
				wmfOut.selectPen(fillPen);
				wmfOut.rectangle(topLeft.x, topLeft.y, bottomRight.x,
						bottomRight.y);
			} else {
				PathIterator iter;

				// We may use areas to suppress the problems caused by shape auto-intersection.
				Area shapeAsArea= new Area(shape);
				
				 
				iter = shapeAsArea.getPathIterator(null, precision);

				//iter = new FlatteningPathIterator(iter, precision); 

				List polygons = new ArrayList();
				ArrayList points = new ArrayList();
				while (!iter.isDone()) {
					int type = iter.currentSegment(coords);
					switch (type) {
					case PathIterator.SEG_CLOSE:
						polygons.add(points);
						points = new ArrayList();
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
							points = new ArrayList();
						}
						// first = deviceCoords(coords[0], coords[1]);
						points.add(new Point2D.Double(coords[0], coords[1]));
						// wmfOut.MoveTo(first);
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
					points = null;
				}

				if (!polygons.isEmpty()) {
					// THIS ALGORITHM IS FALSE !!! It works more or less for us,
					// because of the conventions we use, but it's false.
					// "true" system :
					// for each component, take a normal vector. compute the
					// intersections with the others components.
					// the sum can be computed simply by component.
					// IMPORTANT : deal with WIND_EVEN_ODD one day.
					// The painting is probably based on WIND_NON_ZERO
					// the polygon system is too simple for that.
					// So we compute the geometric area of the whole shape.
					// if it's positive, sub-polygons which have a positive area
					// are drawn in foreground colour,
					// and the other in the background colour.
					// else, we reverse.
					double area = 0;
					// When we create path using the Area class,
					// (and in general theory), the order of the subpathes is
					// not significant.
					// hence we try a quick fix here : we sort the polygons
					// according to their minimum x.

					// We compute a list
					SortablePolygons sortablePolygons[] = new SortablePolygons[polygons
							.size()];

					for (int i = 0; i < polygons.size(); i++) {
						List pts = (List) polygons.get(i);

						sortablePolygons[i] = new SortablePolygons(pts);
						area += GeometryUtils.algebricArea(pts);

					}

					Arrays.sort(sortablePolygons);

					for (int k = 0; k < sortablePolygons.length; k++)
						polygons.set(k, sortablePolygons[k].points);

					// Inverse the two filling pens if necessary.
					if (area < 0) {
						WMFPen p = fillPen;
						fillPen = unfillPen;
						unfillPen = p;
					}
					// paint the content.
					for (Iterator i = polygons.iterator(); i.hasNext();) {
						List pts = (List) i.next();
						if (GeometryUtils.algebricArea(pts) > 0) {
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

	public double getPrecision() {
		return precision;
	}

	public short getWMFLineWidth() {
		return (short) (currentLineWidth * getTransform().getScaleY());
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
			BasicStroke bs = (BasicStroke) stroke;
			currentLineWidth = bs.getLineWidth();
		}
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
	 * @param points
	 *            a list of Point2D
	 * @param fill
	 *            the fill pen.
	 * @throws IOException
	 */

	private void wmfFillPolygon(List points, WMFPen fill) throws IOException {
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
	 * 
	 */
	private void writeHeader() throws IOException {
		/* without these lines, the actual drawing tries to fill the window.*/
		wmfOut.setWindowOrg((short) 0, (short) 0);
		wmfOut.setWindowExt((short) dimensions.getWidth(), (short) dimensions
				.getHeight());
		
		//wmfOut.SetMapMode((short) WMFConstants.MM_ANISOTROPIC);
		//wmfOut.SetMapMode((short) WMFConstants.MM_TWIPS);
		wmfOut.SaveDC();
		
		/*
		 * Prepare the drawing commands...
		 */
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

	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		// TODO Auto-generated method stub

	}

	public Graphics create() {
		return new WMFGraphics2D(this);
	}

	public void dispose() {
		if (closeOnDispose && wmfOut != null)
			try {
				// we should get sure that dispose can be called more than once
				// without harm.
				wmfOut.RestoreDC((short) -1);
				wmfOut.close();
				wmfOut = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		else
			wmfOut = null;
	}

	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		// TODO Auto-generated method stub
		return false;
	}

	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	public void setXORMode(Color c1) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.graphics.wmfexport.BaseGraphics2D#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties properties) {
		// TODO Auto-generated method stub

	}
}

/**
 * Polygons sortables by minimal abscisse.
 * 
 * @author rosmord
 * 
 */
class SortablePolygons implements Comparable {
	public double minx;

	public List points;

	public SortablePolygons(List points) {
		this.points = points;
		if (points.size() > 0) {
			minx = ((Point2D) points.get(0)).getX();
			for (int k = 1; k < points.size(); k++) {
				double x = ((Point2D) points.get(k)).getX();
				if (x < minx)
					minx = x;
			}
		}
	}

	public int compareTo(Object arg0) {
		SortablePolygons pol2 = (SortablePolygons) arg0;
		double diff = (minx - pol2.minx);
		int result;
		if (diff < 0)
			result = -1;
		else if (diff > 0)
			result = 1;
		else
			result = 0;
		return result;
	}
}