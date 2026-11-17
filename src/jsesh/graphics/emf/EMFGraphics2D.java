package jsesh.graphics.emf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Properties;

import jsesh.graphics.generic.BaseGraphics2D;
import jsesh.graphics.generic.RandomAccessStream;

/**
 * A Graphics device which draws into pictures in MacPict format. It can be
 * scaled through affine transforms, but its default scale is 1 unit = 1 point.
 * TODO: generalize this class to fit any vector format.
 * 
 * @author rosmord
 * 
 */
public class EMFGraphics2D extends BaseGraphics2D {

	private EMFDeviceContext deviceContext = null;

	/**
	 * Scale from Graphics 2D resolution to emf resolution.
	 * With default values, 1 point in G2D is 1mm on emf, and 100 device points.
	 */

	private double deviceScale = 10;

	/**
	 * Embedding level, so that we can safely clone MacPictGraphics2D.
	 */

	private int level = 0;

	/**
	 * Curves maximal error.
	 */
	private double precision = 0.05;

	/**
	 * Create a picture which without margin.
	 * 
	 * @param out
	 * @param dims
	 * @throws IOException
	 */
	public EMFGraphics2D(RandomAccessStream out, Dimension dims)
			throws IOException {
		this(out, dims.width,dims.height);		
	}

	/**
	 * Create a picture which with a given bounding box.
	 * 
	 * @param out
	 * @throws IOException
	 */
	public EMFGraphics2D(RandomAccessStream out, long maxx, long maxy)
			throws IOException {
		int w=(int)Math.ceil(maxx*deviceScale);
		int h= (int)Math.ceil(maxy*deviceScale);
		deviceContext = new EMFDeviceContext(out, w,h);

		deviceContext.setWindowOrg(0, 0);
		deviceContext.setWindowExt(w,h);
		deviceContext.saveDC();
		//short a = deviceContext.createBrush(0, 0xFF, WMFConstants.BS_SOLID);
		//deviceContext.selectObject(a);
	}

	public EMFGraphics2D(EMFGraphics2D graphics2D) {
		super(graphics2D);
		this.deviceContext = graphics2D.deviceContext;
		this.deviceScale = graphics2D.deviceScale;

		level = graphics2D.level + 1;
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
				deviceContext.restoreDC();
				deviceContext.closeMetafile();
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
			EMFPoint lastPoint= deviceCoords(0,0);
			EMFPoint firstPoint= deviceCoords(0,0);
			boolean mustClose= false;
			
			while (!iter.isDone()) {
				int type = iter.currentSegment(coords);
				switch (type) {
				case PathIterator.SEG_CLOSE:
					deviceContext.closeFigure(); // TODO : is this correct ?
					mustClose= false;
					lastPoint= firstPoint;
					break;
				case PathIterator.SEG_CUBICTO: {
					EMFPoint[] controls = new EMFPoint[3];
					controls[0] = deviceCoords(coords[0], coords[1]);
					controls[1] = deviceCoords(coords[2], coords[3]);
					controls[2] = deviceCoords(coords[4], coords[5]);

					deviceContext.polyBezierTo(null, controls);
					lastPoint= controls[2];
					mustClose= true;
				}
					break;
				case PathIterator.SEG_LINETO:
					lastPoint= deviceCoords(coords[0], coords[1]);
					deviceContext.lineTo(lastPoint);
					mustClose= true;
					break;
				case PathIterator.SEG_MOVETO:
					if (mustClose) {
						deviceContext.closeFigure();
					}
					mustClose= false;
					firstPoint= deviceCoords(coords[0], coords[1]);
					lastPoint= firstPoint;
					deviceContext.moveToEx(firstPoint);
					break;
				case PathIterator.SEG_QUADTO: {
					// To get an equivalent cubic spline from a quadric one,
					// the control points are 2/3 of the original one.
					// So, we need to know the last control available.
					EMFPoint[] controls = new EMFPoint[3];
					
					EMFPoint quadricControl= deviceCoords(coords[0], coords[1]); 
					EMFPoint endControl= deviceCoords(coords[2], coords[3]); 
					
					controls[0] = new EMFPoint(
							lastPoint.getX() + (2*(quadricControl.getX() - lastPoint.getX()))/3,
							lastPoint.getY() + (2*(quadricControl.getY() - lastPoint.getY()))/3
					);
					
					controls[1] = new EMFPoint(
							endControl.getX() + (2*(quadricControl.getX() - endControl.getX()))/3,
							endControl.getY() + (2*(quadricControl.getY() - endControl.getY()))/3
					);
					
					controls[2] = endControl;
					lastPoint= endControl;

					deviceContext.polyBezierTo(null, controls);
					mustClose= true;
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
	 * Sets the device scale.
	 * If equals to 1, 1 point will be 1 emf point, which is 1/100 of a mm.
	 * This is a bad idea. We suggest larger scales.
	 * @param deviceScale the deviceScale to set
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
		return new EMFPoint((short) (res.getX() * deviceScale), (short) (res
				.getY() * deviceScale));
	}
}
