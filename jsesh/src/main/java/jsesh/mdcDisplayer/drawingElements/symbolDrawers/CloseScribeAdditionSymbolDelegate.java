package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.geom.GeneralPath;

public class CloseScribeAdditionSymbolDelegate extends AbstractSymbolDrawer {

	protected CombinedPath buildShapeForDrawing(float width, float height) {
		GeneralPath pol = new GeneralPath();
		pol.moveTo(width/2f, strokeWidth/2f);
		pol.lineTo(width/2f, height / 3);

		return new CombinedPath(pol, buildRectangle(width, height));
	}

}
