package org.qenherkhopeshef.graphics.emf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Properties;
import java.util.logging.Logger;

import org.qenherkhopeshef.graphics.generic.BaseGraphics2D;
import org.qenherkhopeshef.graphics.generic.RandomAccessFileAdapter;
import org.qenherkhopeshef.graphics.generic.RandomAccessStream;

/**
 * A Graphics device which draws into pictures in EMF format.
 * 
 * Default scale: in the default scale, 1 unit corresponds to 1 point.
 * 
 * @author rosmord
 * 
 */
public class EMFGraphics2D extends BaseGraphics2D {

	private EMFDeviceContext deviceContext = null;

	/**
	 * Scale from Graphics 2D resolution to emf resolution. Default inner EMF
	 * resolution is 1/100 mm. We want the default unit to be 1 point.
	 */

	private double deviceScale = 100.0 * 0.353;

	/**
	 * Embedding level, so that we can safely clone MacPictGraphics2D.
	 */

	private int level = 0;

	/**
	 * Create an EMFGraphics2D for writing to a file.
	 * 
	 * @param file
	 *            the file to write data to.
	 * @param dims
	 *            the picture dimensions, in <strong>points</strong>
	 * @param creator
	 *            software creating the picture (may be null)
	 * @param comment
	 *            free text comment included in the picture (may be null too).
	 * 
	 * @throws java.io.IOException
	 */
	public EMFGraphics2D(File file, Dimension2D dims, String creator,
			String comment) throws IOException {
		if (file.exists())
			file.delete();
		RandomAccessStream stream = new RandomAccessFileAdapter(
				new RandomAccessFile(file, "rw"));
		int w = (int) Math.ceil(dims.getWidth());
		int h = (int) Math.ceil(dims.getHeight());
		initEMFGraphics2D(stream, w, h, creator, comment);
	}

	/**
	 * Create a picture which without margin.
	 * 
	 * @param out
	 * @param dims pictures dimensions, in <strong>points</strong>
	 * @param creator
	 *            software creating the picture (may be null)
	 * @param comment
	 *            free text comment included in the picture (may be null too).
	 * 
	 * @throws IOException
	 */
	public EMFGraphics2D(RandomAccessStream stream, Dimension2D dims,
			String creator, String comment) throws IOException {
		int w = (int) Math.ceil(dims.getWidth());
		int h = (int) Math.ceil(dims.getHeight());
		initEMFGraphics2D(stream, w, h, creator, comment);
	}

	/**
	 * Create a picture which with a given bounding box.
	 * 
	 * @param creator
	 *            software creating the picture (may be null)
	 * @param comment
	 *            free text comment included in the picture (may be null too).
	 * 
	 * @param out
	 * @throws IOException
	 */
	public EMFGraphics2D(RandomAccessStream out, long maxx, long maxy,
			String creator, String comment) throws IOException {
		initEMFGraphics2D(out, maxx, maxy, creator, comment);
	}

	public EMFGraphics2D(EMFGraphics2D graphics2D) {
		super(graphics2D);
		this.deviceContext = graphics2D.deviceContext;
		this.deviceScale = graphics2D.deviceScale;

		level = graphics2D.level + 1;
	}

	/**
	 * Method to initialize an EMFGraphics 2D. (more convenient internally than
	 * a constructor, as it can be called from any part of a constructor).
	 * 
	 * @param out
	 * @param maxx
	 * @param maxy
	 * @param creator
	 *            software creating the picture (may be null)
	 * @param comment
	 *            free text comment included in the picture (may be null too).
	 * @throws java.io.IOException
	 */
	private void initEMFGraphics2D(RandomAccessStream out, long maxx,
			long maxy, String creator, String comment) throws IOException {
		int w = (int) Math.ceil(maxx * deviceScale);
		int h = (int) Math.ceil(maxy * deviceScale);
		deviceContext = new EMFDeviceContext(out, w, h, creator, comment);
		// Device context header.

		// R0003: [017] EMR_SETMAPMODE (s=12) {iMode(8=MM_ANISOTROPIC)}
		// R0004: [011] EMR_SETVIEWPORTEXTEX (s=16) {szlExtent(546,366)}
		// R0005: [009] EMR_SETWINDOWEXTEX (s=16) {szlExtent(546,366)}
		// R0006: [009] EMR_SETWINDOWEXTEX (s=16) {szlExtent(806,506)}
		// R0007: [010] EMR_SETWINDOWORGEX (s=16) {ptlOrigin(0,0)}
		// deviceContext.setMapMode(WMFConstants.MM_ANISOTROPIC);
		deviceContext.setWindowOrg(0, 0);
		deviceContext.setWindowExt(w, h);
		// Needed ???
		// deviceContext.createFillPen(0);
		// EMFPen emfPen = deviceContext.createDrawPen((short)0, 1l);
		// deviceContext.selectPen(emfPen);

		// 0: [025] EMR_SETBKCOLOR (s=12) {0x00FFFFFF}
		// R0011: [018] EMR_SETBKMODE (s=12) {iMode(2=OPAQUE)}

		// deviceContext.setBkColor(0);
		// EMFPen emfPen = deviceContext.createDrawPen((short)0, 1l);
		// deviceContext.selectPen(emfPen);

		// short a = deviceContext.createBrush(0, 0xFF, WMFConstants.BS_SOLID);
		// deviceContext.selectObject(a);
	}

	/**
	 * 
	 * Sets an explicit clip rectangle for the mac picture.
	 * 
	 * @param dimension
	 */
	public void setPictureClip(double minx, double miny, double maxx,
			double maxy) {
	}

	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		// DO NOTHING
	}

	/**
	 * Build a clone of this context, rendering into the same picture.
	 */
	public Graphics create() {
		return new EMFGraphics2D(this);
	}

	/**
	 * dispose and close the graphic context, if it's the top-level one.
	 */
	public void dispose() {
		if (level == 0) {
			try {
				if (deviceContext != null) {
					// deviceContext.restoreDC();
					deviceContext.closeMetafile();
					deviceContext = null;
					Logger.getLogger("jsesh.graphics").fine("Closing EMF file");
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void draw(Shape shape) {
		// Probably not the best solution, but always work :
		fill(getStroke().createStrokedShape(shape));
	}

	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		// DO NOTHING
		return false;
	}

	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		// DO NOTHING
	}

	public void fill(Shape shape) {
		try {
			double coords[] = new double[6];
			Color paintColor = getColor();
			if (getPaint() instanceof Color) {
				paintColor = (Color) getPaint();
			}

			EMFPen pen = deviceContext.createFillPen(paintColor.getRGB());
			deviceContext.selectPen(pen);
			PathIterator iter;
			iter = shape.getPathIterator(new AffineTransform());
			deviceContext.beginPath();
			EMFPoint lastPoint = deviceCoords(0, 0);
			EMFPoint firstPoint = deviceCoords(0, 0);
			boolean mustClose = false;

			while (!iter.isDone()) {
				int type = iter.currentSegment(coords);
				switch (type) {
				case PathIterator.SEG_CLOSE:
					deviceContext.closeFigure(); // TODO : is this correct ?
					mustClose = false;
					lastPoint = firstPoint;
					break;
				case PathIterator.SEG_CUBICTO: {
					EMFPoint[] controls = new EMFPoint[3];
					controls[0] = deviceCoords(coords[0], coords[1]);
					controls[1] = deviceCoords(coords[2], coords[3]);
					controls[2] = deviceCoords(coords[4], coords[5]);

					deviceContext.polyBezierTo(null, controls);
					lastPoint = controls[2];
					mustClose = true;
				}
					break;
				case PathIterator.SEG_LINETO:
					lastPoint = deviceCoords(coords[0], coords[1]);
					deviceContext.lineTo(lastPoint);
					mustClose = true;
					break;
				case PathIterator.SEG_MOVETO:
					if (mustClose) {
						deviceContext.closeFigure();
					}
					mustClose = false;
					firstPoint = deviceCoords(coords[0], coords[1]);
					lastPoint = firstPoint;
					deviceContext.moveToEx(firstPoint);
					break;
				case PathIterator.SEG_QUADTO: {
					// To get an equivalent cubic spline from a quadric one,
					// the control points are 2/3 of the original one.
					// So, we need to know the last control available.
					EMFPoint[] controls = new EMFPoint[3];

					EMFPoint quadricControl = deviceCoords(coords[0], coords[1]);
					EMFPoint endControl = deviceCoords(coords[2], coords[3]);

					controls[0] = new EMFPoint(lastPoint.getX()
							+ (2 * (quadricControl.getX() - lastPoint.getX()))
							/ 3, lastPoint.getY()
							+ (2 * (quadricControl.getY() - lastPoint.getY()))
							/ 3);

					controls[1] = new EMFPoint(endControl.getX()
							+ (2 * (quadricControl.getX() - endControl.getX()))
							/ 3, endControl.getY()
							+ (2 * (quadricControl.getY() - endControl.getY()))
							/ 3);

					controls[2] = endControl;
					lastPoint = endControl;

					deviceContext.polyBezierTo(null, controls);
					mustClose = true;
				}
					break;
				default:
					throw new RuntimeException(
							"unexpected constant in path iterator");
				}
				iter.next();
			}

			deviceContext.endPath();
			deviceContext.fillPath();
			deviceContext.freePen(pen);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	/**
	 * Sets the device scale. If equals to 1, 1 point will be 1 emf point, which
	 * is 1/100 of a mm. This is a bad idea. We suggest larger scales.
	 * 
	 * @param deviceScale
	 *            the deviceScale to set
	 */
	public void setDeviceScale(double deviceScale) {
		this.deviceScale = deviceScale;
	}

	/**
	 * @return the deviceScale
	 */
	public double getDeviceScale() {
		return deviceScale;
	}

	// Compute the coordinates of a point in device space.

	private EMFPoint deviceCoords(double x, double y) {
		return deviceCoords(new Point2D.Double(x, y));
	}

	private EMFPoint deviceCoords(Point2D p) {
		Point2D res = new Point2D.Double();
		getTransform().transform(p, res);
		return new EMFPoint((short) (res.getX() * deviceScale),
				(short) (res.getY() * deviceScale));
	}

}
