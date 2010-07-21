package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.geom.GeneralPath;

public class OpenScribeAdditionSymbolDelegate extends AbstractSymbolDrawer {

	protected CombinedPath buildShapeForDrawing(float width, float height) {
		GeneralPath pol = new GeneralPath();
		float halfStroke = strokeWidth / 2f;
		pol.moveTo(width / 2f, halfStroke);
		pol.lineTo(width / 2f, height / 3);
		
		return new CombinedPath(pol, buildRectangle(width, height));
	}

}
