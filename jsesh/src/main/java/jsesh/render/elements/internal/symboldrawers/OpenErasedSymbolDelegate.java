package jsesh.render.elements.internal.symboldrawers;

import java.awt.geom.Path2D;


public class OpenErasedSymbolDelegate extends AbstractSymbolDrawer  {

	/**
	 * Produce a drawing of the sign. 
	 * The drawing is done with an angle of 0,
	 * and is supposed to fill the proposed width and height.
	 * 
	 * <p> The drawing origin is 0,0
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	protected CombinedPath buildShapeForDrawing(double width, double height) {
		Path2D.Double pol = new Path2D.Double();
		double halfStroke= strokeWidth/2.0;
		pol.moveTo(width-halfStroke, halfStroke);
		pol.lineTo(halfStroke, halfStroke);
		pol.lineTo(halfStroke, height-halfStroke);
		pol.lineTo(width-halfStroke, height-halfStroke);
		return new CombinedPath(pol);
	}

}
