package jsesh.render.elements.internal.symboldrawers;

import java.awt.geom.Path2D;

public class CloseErasedSymbolDelegate extends AbstractSymbolDrawer {

	// done
	protected CombinedPath buildShapeForDrawing(double width, double height) {
		Path2D.Double pol = new Path2D.Double();
		double halfStroke= strokeWidth/2.0;
		pol.moveTo(halfStroke, halfStroke);
		pol.lineTo(width-halfStroke, halfStroke);
		pol.lineTo(width-halfStroke, height-halfStroke);
		pol.lineTo(halfStroke, height-halfStroke);
		return new CombinedPath(pol);
	}

}
