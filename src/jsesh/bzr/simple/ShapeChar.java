package jsesh.bzr.simple;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import jsesh.utils.DoubleUtils;

/**
 * A glyph in a BzrSimpleFont (basically a shape which can be painted).
 * 
 * 
 * Created: Mon Jun 10 10:49:26 2002
 * 
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC
 *         </a>
 * 
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

	/**
	 * fixShape ensures the shapeChar is correct, using the shape as starting point. 
	 * <p>Fixes both the bounding box and the sign origin.
	 */
	public void fixShape() {
		// Just to be sure of the bounding box...
		PathIterator it= shape.getPathIterator(null, 0.01);
		GeneralPath p= new GeneralPath();
		p.append(it, false);
		Rectangle2D r= p.getBounds2D();
		shape= AffineTransform.getTranslateInstance(-r.getMinX(), -r.getMinY()).createTransformedShape(shape);
		setBBox(0, r.getMaxY() - r.getMinY(), r.getMaxX() - r.getMinX(), 0);
	}

	
	/**
	 * Modify a shape char in order to get an height of exactly h units.
	 * Should be used only in the context of sign importation and/or modifications.
	 * @param h : the height of the sign.
	 */
	public void scaleToHeight(double h) {
		double s = h / getBbox().getHeight();
		setShape(AffineTransform.getScaleInstance(s, s)
				.createTransformedShape(getShape()));
		fixShape();	
	}
	/**
	 * Saves the drawing of this sign as an SVG file.
	 * Note that the output should be in ASCII (or UTF-8) encoding for best portability.
	 * @param out
	 * @param encoding the name of the encoding to use. We suggest "UTF-8".
	 */
	public void exportToSVG(OutputStream o, String encoding) throws IOException {
		Writer out= new OutputStreamWriter(o, encoding);
		out.write("<?xml version=\"1.0\" ");
		if (! "".equals(encoding)) {
			out.write("encoding =\""+ encoding+ "\" ");
		}
		out.write("standalone=\"yes\"?>\n");
		out.write("<svg width=\"");
		out.write(DoubleUtils.writeInDecimal(bbox.getWidth()));
		out.write("\" height=\"");
		out.write(DoubleUtils.writeInDecimal(bbox.getHeight()));
		out.write("\">\n");
		out.write("<path style=\"fill:#000000; stroke:none\" d=\"");
		PathIterator iter= shape.getPathIterator(null);
		while (! iter.isDone()) {
			float coords[] = new float[6];
            int type = iter.currentSegment(coords);
            switch (type) {
            case PathIterator.SEG_CLOSE:
            	            out.write(" Z ");	
                break;
            case PathIterator.SEG_CUBICTO:
                out.write(" C ");
                outputNumbers(out, coords, 6);
                break;
            case PathIterator.SEG_LINETO:
            	out.write(" L ");
            	outputNumbers(out, coords, 2);
                break;
            case PathIterator.SEG_MOVETO:    
            	out.write(" M ");
            	outputNumbers(out, coords, 2);
                break;
            case PathIterator.SEG_QUADTO:
            	out.write(" Q ");
            	outputNumbers(out, coords, 4);
                break;
            }
            iter.next();
		}
		out.write("\"/>");
		out.write("</svg>\n");
		out.close();
	}

	private void outputNumbers(Writer out, float[] coords, int max) throws IOException {
		for (int i=0; i< max; i++) {
			out.write(DoubleUtils.writeInDecimal(coords[i]));
			out.write(' ');
		}
		
	}
} // ShapeChar
