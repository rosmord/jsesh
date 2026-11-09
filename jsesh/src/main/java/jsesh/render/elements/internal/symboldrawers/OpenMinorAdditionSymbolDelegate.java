package jsesh.render.elements.internal.symboldrawers;

import java.awt.geom.Path2D;

public class OpenMinorAdditionSymbolDelegate extends AbstractSymbolDrawer {

	protected CombinedPath buildShapeForDrawing(double width, double height) {
		double halfStroke= strokeWidth/2.0;
		Path2D.Double pol = new Path2D.Double();
		pol.moveTo(width-halfStroke, halfStroke);			
		pol.curveTo(0, 0,
				0, height-halfStroke, 
				width-halfStroke, height-halfStroke);
		return new CombinedPath(pol);
	}

}
