package jsesh.render.elements.internal.symboldrawers;

import java.awt.geom.Path2D;

public class OpenEditorAdditionSymbolDelegate extends AbstractSymbolDrawer {

	protected CombinedPath buildShapeForDrawing(double width, double height) {
		double centerY = height / 2.0;
		double halfStroke= strokeWidth/2.0;
		Path2D.Double pol = new Path2D.Double();
		
		pol.moveTo(width-halfStroke, halfStroke);
		pol.lineTo(halfStroke, centerY);
		pol.lineTo(width-halfStroke, height-halfStroke);
		return new CombinedPath(pol);
	}
	
	public double getBaseWidth() {
		return 3;
	}

}
