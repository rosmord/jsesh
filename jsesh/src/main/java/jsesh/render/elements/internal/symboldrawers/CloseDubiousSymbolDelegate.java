package jsesh.render.elements.internal.symboldrawers;

import java.awt.geom.Path2D;

public class CloseDubiousSymbolDelegate extends AbstractSymbolDrawer {
	
	// done
	protected CombinedPath buildShapeForDrawing(double width, double height) {
		Path2D.Double pol = new Path2D.Double();
		pol.moveTo(strokeWidth/2.0, strokeWidth/2.0);
		pol.lineTo(width-strokeWidth/2.0, strokeWidth/2.0);
		pol.lineTo(width-strokeWidth/2.0, height / 2);
		return new CombinedPath(pol);
	}

}
