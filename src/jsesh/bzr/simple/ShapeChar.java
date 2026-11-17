package jsesh.bzr.simple;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

/**
 * ShapeChar.java
 * 
 * 
 * Created: Mon Jun 10 10:49:26 2002
 * 
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC
 *         </a>
 * @version
 */

public class ShapeChar {
	private Shape shape;

	public ShapeChar() {

	}

	Rectangle2D bbox;

	/**
	 * Get the value of bbox.
	 * 
	 * @return value of bbox.
	 */
	public Rectangle2D getBbox() {
		return bbox;
	}

	/**
	 * Set the value of bbox.
	 * 
	 * @param v
	 *            Value to assign to bbox.
	 */
	public void setBbox(Rectangle2D v) {
		this.bbox = v;
	}

	/**
	 * sets this char's bounding box from coordinates
	 * 
	 * @param llx
	 *            a <code>double</code> value
	 * @param lly
	 *            a <code>double</code> value
	 * @param urx
	 *            a <code>double</code> value
	 * @param ury
	 *            a <code>double</code> value
	 *  
	 */
	public void setBBox(double llx, double lly, double urx, double ury) {
		this.bbox = new Rectangle2D.Double(llx, ury, urx - llx, lly - ury);
	}

	/**
	 * Get the value of shape.
	 * 
	 * @return value of shape.
	 */
	public Shape getShape() {
		return shape;
	}

	/**
	 * Set the value of shape.
	 * 
	 * @param v
	 *            Value to assign to shape.
	 */
	public void setShape(Shape v) {
		this.shape = v;
	}

	/**
	 * 
	 * @param g
	 * @param x
	 * @param y
	 * @param xscale
	 * @param yscale
	 * @param angle
	 *            rotation angle for this glyph, in radians.
	 */
	public void draw(Graphics2D g, double x, double y, double xscale,
			double yscale, float angle) {
		//AffineTransform tr;
		//tr= g.getTransform();
		Graphics2D tmpG = (Graphics2D) g.create();
		tmpG.translate(x, y);
		Shape s = getShape();
		tmpG.scale(xscale, yscale);
		if (angle != 0) {
			// In case of rotation, compute the rotated shape (we need it to
			// know the sign placement).
			s = AffineTransform.getRotateInstance(angle)
					.createTransformedShape(s);
			// Now, we move the top-left of this shape to 0,0
			Rectangle2D r = s.getBounds2D();
			tmpG.translate(-r.getMinX(), -r.getMinY());
		}
		// The following line doesn't do what it's supposed to do.
		tmpG.fill(s);
		//g.setTransform(tr);
		tmpG.dispose();
	}

	
	public Shape getTransformedShape(double x, double y, double xscale,
			double yscale, double angle) {
	    Shape result= getShape();
	    if (angle != 0) {
	        AffineTransform tr0= AffineTransform.getRotateInstance(angle);
	        result= tr0.createTransformedShape(getShape());
	        // Move the top-left of this shape to 0,0.
	        Rectangle2D r = result.getBounds2D();
	        x= x - r.getMinX()*xscale;
	        y= y - r.getMinY()*yscale;
	    } 
	    AffineTransform tr= AffineTransform.getTranslateInstance(x,y);
	    // scale and turn.
	    tr.concatenate(AffineTransform.getScaleInstance(xscale, yscale));
	    result= tr.createTransformedShape(result);
	    return result;
	}
	
	public Area getSignArea(double x, double y, double xscale,
			double yscale, double angle) {
	     	Area s1 = new Area();
	     	Shape s= getTransformedShape(x,y,xscale, yscale, angle);
	        PathIterator iter = s.getPathIterator(null);
	        GeneralPath current = null;
	        while (!iter.isDone()) {
	            float coords[] = new float[6];
	            int type = iter.currentSegment(coords);
	            switch (type) {
	            case PathIterator.SEG_CLOSE:
	                current.closePath();
	            	s1.add(new Area(current));
	            	current= null;
	                break;
	            case PathIterator.SEG_CUBICTO:
	                current.curveTo(coords[0], coords[1], coords[2], coords[3],
	                        coords[4], coords[5]);
	                break;
	            case PathIterator.SEG_LINETO:
	                current.lineTo(coords[0], coords[1]);
	                break;
	            case PathIterator.SEG_MOVETO:
	                if (current != null)
	                    s1.add(new Area(current));
	                current= new GeneralPath(iter.getWindingRule());
	            	current.moveTo(coords[0], coords[1]);
	                break;
	            case PathIterator.SEG_QUADTO:
	                current.quadTo(coords[0], coords[1], coords[2], coords[3]);
	                break;
	            }
	            iter.next();
	        }
	        if (current != null)
	            s1.add(new Area(current));
	        return s1;
	}
} // ShapeChar
