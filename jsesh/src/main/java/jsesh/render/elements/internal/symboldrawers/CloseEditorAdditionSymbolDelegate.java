package jsesh.render.elements.internal.symboldrawers;

import java.awt.geom.Path2D;

public class CloseEditorAdditionSymbolDelegate extends AbstractSymbolDrawer {

	// done
	protected CombinedPath buildShapeForDrawing(double width, double height) {
		double centerY = height / 2.0;
		double halfStroke= strokeWidth/2.0;
		Path2D.Double pol = new Path2D.Double();
		
		pol.moveTo(halfStroke, halfStroke);
		pol.lineTo(width-halfStroke, centerY);
		pol.lineTo(halfStroke, height-halfStroke);
		
		return new CombinedPath(pol);
	}
	
	public double getBaseWidth() {
		return 3;
	}
}
