package org.qenherkhopeshef.graphics.pict;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.qenherkhopeshef.graphics.generic.BaseGraphics2D;
import org.qenherkhopeshef.graphics.generic.SortablePolygon;
import org.qenherkhopeshef.graphics.pict.MacPictDeviceContext.MPPoint;
import org.qenherkhopeshef.graphics.utils.GeometryUtils;

/**
 * A Graphics device which draws into pictures in MacPict format.
 * It can be scaled through affine transforms, but its default scale is
 * 1 unit = 1 point.
 * TODO: generalize this class to fit any vector format.
 * 
 * @author rosmord
 *
 */
public class MacPictGraphics2D extends BaseGraphics2D {

	private MacPictDeviceContext deviceContext;

	/**
	 * Scale from Graphics 2D resolution to mac pict resolution.
	 */
	private double deviceScale= MacPictDeviceContext.DEFAULT_RESOLUTION / 72.0;
	
	/**
	 * Embedding level, so that we can safely clone MacPictGraphics2D.
	 */
	
	private int level= 0;
	/**
	 * Curves maximal error.
	 */
	private double precision = 0.05;

	/**
	 * Create a picture which without margin.
	 */
	public MacPictGraphics2D() {
		deviceContext= new MacPictDeviceContext();
	}
	
	/**
	 * Create a picture which with a given bounding box.
	 */
	public MacPictGraphics2D(double minx, double miny, double maxx, double maxy) {
		MPPoint p0 = deviceCoords(new Point2D.Double(minx, miny));
		MPPoint p1 = deviceCoords(new Point2D.Double(maxx, maxy));
		
		deviceContext= new MacPictDeviceContext(p0.x, p0.y, p1.x, p1.y);
	}
	
	
	public MacPictGraphics2D(MacPictGraphics2D graphics2D) {
		super(graphics2D);
		deviceContext= graphics2D.deviceContext;
		level= graphics2D.level+ 1;
	}

	
	/**
	 * Gets the picture DPI resolution.
	 * @return
	 * @see org.qenherkhopeshef.graphics.pict.MacPictDeviceContext#getDpi()
	 */
	public int getDpi() {
		return deviceContext.getDpi();
	}


	/**
	 * Sets the picture DPI resolution.
	 * @param dpi
	 * @see org.qenherkhopeshef.graphics.pict.MacPictDeviceContext#setDpi(int)
	 */
	public void setDpi(int dpi) {
		deviceContext.setDpi(dpi);
	}


	/**
	 * Sets an explicit clip rectangle for the mac picture.
	 * @param dimension
	 */
	public void setPictureClip(double minx, double miny, double maxx, double maxy) {
		Point2D p1a= new  Point2D.Double(minx,miny);
		Point2D p2a= new  Point2D.Double(maxx,maxy);
		MacPictDeviceContext.MPPoint p1= deviceCoords(p1a);
		MacPictDeviceContext.MPPoint p2= deviceCoords(p2a);
		
		deviceContext.setClipBox(p1.x, p1.y, p2.x, p2.y);
	}
	
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		// DO NOTHING
	}

	/**
	 * Build a clone of this context, rendering into the same picture.
	 */
	public Graphics create() {
		return new MacPictGraphics2D(this);
	}

	/**
	 * dispose and close the graphic context, if it's the top-level one.
	 */
	public void dispose() {
		if (level==0) {
			deviceContext.closePicture();
		}
	}

	public void draw(Shape shape) {
		// Probably not the best solution, but always work :
		fill(getStroke().createStrokedShape(shape));
		/*
		if (shape instanceof Line2D) {
			Line2D l = (Line2D) shape;

			MacPictDeviceContext.MPPoint p1, p2;
			p1 = deviceCoords(l.getP1());
			p2 = deviceCoords(l.getP2());
			deviceContext.line(p1, p2);
		} else {
			PathIterator iter = new FlatteningPathIterator(shape
					.getPathIterator(null), precision);

			MacPictDeviceContext.MPPoint first = null;
			while (!iter.isDone()) {
				double coords[] = new double[6];
				int type = iter.currentSegment(coords);
				switch (type) {
				case PathIterator.SEG_CLOSE:
					deviceContext.lineFrom(first);
					break;
				case PathIterator.SEG_CUBICTO:
					// Normally impossible (flattened !)
					deviceContext.lineFrom(deviceCoords(coords[0], coords[1]));
					deviceContext.lineFrom(deviceCoords(coords[2], coords[3]));
					deviceContext.lineFrom(deviceCoords(coords[4], coords[5]));
					break;
				case PathIterator.SEG_LINETO:
					deviceContext.lineFrom(deviceCoords(coords[0], coords[1]));
					break;
				case PathIterator.SEG_MOVETO:
					first = deviceCoords(coords[0], coords[1]);
					deviceContext.line(first, first);
					break;
				case PathIterator.SEG_QUADTO:
					deviceContext.lineFrom(deviceCoords(coords[0], coords[1]));
					deviceContext.lineFrom(deviceCoords(coords[2], coords[3]));
					break;
				default:
					throw new RuntimeException(
							"unexpected constant in path iterator");
				}
				iter.next();
			}
		}*/
	}

	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		// DO NOTHING
		return false;
	}

	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		// DO NOTHING
	}

	public void fill(Shape shape) {
		double coords[] = new double[6];
		Color paintColor = getColor();
		if (getPaint() instanceof Color) {
			paintColor = (Color) getPaint();
		}
		// wmfOut.SelectObject((short) 1);
		// Default case
		if (shape instanceof Rectangle2D) {
			Rectangle2D r = (Rectangle2D) shape;
			MacPictDeviceContext.MPPoint topLeft = deviceCoords(r.getMinX(), r
					.getMinY());
			MacPictDeviceContext.MPPoint bottomRight = deviceCoords(r.getMaxX(),
					r.getMaxY());
			deviceContext.setForegroundColor(build48bitColor(getColor()));
			// deviceContext.paintRectangle(r.getMinX(), r.getMinY(),
			// r.getMaxX(),r.getMaxY());
			deviceContext.paintRectangle(topLeft, bottomRight);
		} else {
			PathIterator iter;

			// We may use areas to suppress the problems caused by shape
			// auto-intersection.
			Area shapeAsArea = new Area(shape);

			iter = shapeAsArea.getPathIterator(null, precision);

			// iter = new FlatteningPathIterator(iter, precision);

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
						points
								.add(new Point2D.Double(coords[i],
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
						points
								.add(new Point2D.Double(coords[i],
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
				SortablePolygon sortablePolygons[] = new SortablePolygon[polygons
						.size()];

				for (int i = 0; i < polygons.size(); i++) {
					List pts = (List) polygons.get(i);

					sortablePolygons[i] = new SortablePolygon(pts);
					area += GeometryUtils.algebricArea(pts);

				}

				Arrays.sort(sortablePolygons);

				for (int k = 0; k < sortablePolygons.length; k++)
					polygons.set(k, sortablePolygons[k].points);

				// Inverse the two filling colors if necessary.
				MacPictDeviceContext.MPColor fillColor = build48bitColor(paintColor);
				MacPictDeviceContext.MPColor unfillColor = build48bitColor(getBackground());
				if (area < 0) {
					fillColor = build48bitColor(getBackground());
					unfillColor = build48bitColor(paintColor);
				}
				// paint the content.
				for (Iterator i = polygons.iterator(); i.hasNext();) {
					List pts = (List) i.next();
					if (GeometryUtils.algebricArea(pts) > 0) {
						fillPolygon(pts, fillColor);
					} else {
						fillPolygon(pts, unfillColor);

					}

				}
			}
		}

	}

	private void fillPolygon(List pts,
			org.qenherkhopeshef.graphics.pict.MacPictDeviceContext.MPColor fillColor) {
		if (pts.isEmpty())
			return;
		MacPictDeviceContext.MPPoint poly[] = new MacPictDeviceContext.MPPoint[pts.size()];
		for (int i = 0; i < poly.length; i++) {
			poly[i] = deviceCoords((Point2D) pts.get(i));
		}
		deviceContext.setForegroundColor(fillColor);
		deviceContext.fillPoly(poly);
	}

	public void setPaintMode() {
		// TODO Auto-generated method stub

	}

	public void setProperties(Properties properties) {
		// TODO Auto-generated method stub

	}

	public void setXORMode(Color c1) {
		// TODO Auto-generated method stub

	}

	// Compute the coordinates of a point in device space.

	private MacPictDeviceContext.MPPoint deviceCoords(double x, double y) {
		return deviceCoords(new Point2D.Double(x, y));
	}

	private MacPictDeviceContext.MPPoint deviceCoords(Point2D p) {
		Point2D res = new Point2D.Double();
		getTransform().transform(p, res);
		return new MacPictDeviceContext.MPPoint((short) (res.getX()*deviceScale), (short) (res
				.getY()*deviceScale));
	}

	/**
	 * Convert 24-bit color to 48 bits.
	 * 
	 * @param color
	 * @return
	 */
	private static MacPictDeviceContext.MPColor build48bitColor(Color color) {
		return new MacPictDeviceContext.MPColor(color.getRed() << 8, color
				.getGreen() << 8, color.getBlue() << 8);
	}
	
	/**
	 * Write the resulting picture to a stream.
	 * @param outputStream
	 * @throws IOException
	 */
	public void writeToStream(OutputStream outputStream) throws IOException {
		deviceContext.writeToStream(outputStream);
	}

	/**
	 * Return the resulting picture as a byte array.
	 * @return the content of the resulting picture.
	 * @see org.qenherkhopeshef.graphics.pict.MacPictDeviceContext#getAsArray()
	 */
	public byte[] getAsArray() {
		return deviceContext.getAsArray();
	}

	/**
	 * Return an array suitable for inclusion in RTF files.
	 * In RTF files, the first 256 null bytes are not included. 
	 * @return
	 */
	public byte[] getAsArrayForRTF() {
		return deviceContext.getAsArrayForRTF();
	}

	
}
