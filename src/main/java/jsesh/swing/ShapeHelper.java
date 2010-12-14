package jsesh.swing;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class ShapeHelper {

	public static Shape transformShape(double x, double y, double xscale,
			double yscale, double angle, Shape shape) {
		Shape result= shape;
		if (angle != 0) {
			AffineTransform tr0 = AffineTransform.getRotateInstance(angle);
			result = tr0.createTransformedShape(result);
			// Move the top-left of this shape to 0,0.
			Rectangle2D r = result.getBounds2D();
			x = x - r.getMinX() * xscale;
			y = y - r.getMinY() * yscale;
		}
		AffineTransform tr = AffineTransform.getTranslateInstance(x, y);
		// scale and turn.
		tr.concatenate(AffineTransform.getScaleInstance(xscale, yscale));
		result = tr.createTransformedShape(result);
		return result;
	}

}
