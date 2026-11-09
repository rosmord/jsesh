package jsesh.render.elements.internal.symboldrawers;

import java.awt.geom.Path2D;

// Done
public class CloseMinorAdditionSymbolDelegate extends AbstractSymbolDrawer {

	protected CombinedPath buildShapeForDrawing(double width, double height) {
		Path2D.Double pol = new Path2D.Double();
		double halfStroke= strokeWidth/2.0;
		
		pol = new Path2D.Double();
		pol.moveTo(halfStroke, halfStroke);
		// the width parameter should be modified a little, methink. later.
		pol.curveTo(width,
				halfStroke, width,
				height-halfStroke, halfStroke, height-halfStroke);
		
		return new CombinedPath(pol);
	}

}
