package jsesh.render.elements.internal.symboldrawers;

import java.awt.geom.Path2D;

public class CloseScribeAdditionSymbolDelegate extends AbstractSymbolDrawer {

	protected CombinedPath buildShapeForDrawing(double width, double height) {
		Path2D.Double pol = new Path2D.Double();
		pol.moveTo(width/2.0, strokeWidth/2.0);
		pol.lineTo(width/2.0, height / 3);

		return new CombinedPath(pol, buildRectangle(width, height));
	}

}
