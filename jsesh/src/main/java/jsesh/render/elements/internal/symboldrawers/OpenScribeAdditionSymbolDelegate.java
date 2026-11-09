package jsesh.render.elements.internal.symboldrawers;

import java.awt.geom.Path2D;

public class OpenScribeAdditionSymbolDelegate extends AbstractSymbolDrawer {

	protected CombinedPath buildShapeForDrawing(double width, double height) {
		Path2D.Double pol = new Path2D.Double();
		double halfStroke = strokeWidth / 2.0;
		pol.moveTo(width / 2.0, halfStroke);
		pol.lineTo(width / 2.0, height / 3);
		
		return new CombinedPath(pol, buildRectangle(width, height));
	}

}
