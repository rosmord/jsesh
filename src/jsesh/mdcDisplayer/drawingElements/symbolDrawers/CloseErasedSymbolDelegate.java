package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.geom.GeneralPath;

public class CloseErasedSymbolDelegate extends AbstractSymbolDrawer {

	// done
	protected CombinedPath buildShapeForDrawing(float width, float height) {
		GeneralPath pol = new GeneralPath();
		float halfStroke= strokeWidth/2f;
		pol.moveTo(halfStroke, halfStroke);
		pol.lineTo(width-halfStroke, halfStroke);
		pol.lineTo(width-halfStroke, height-halfStroke);
		pol.lineTo(halfStroke, height-halfStroke);
		return new CombinedPath(pol);
	}

}
