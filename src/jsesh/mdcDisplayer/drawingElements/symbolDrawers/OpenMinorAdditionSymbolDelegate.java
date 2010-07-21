package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.geom.GeneralPath;

public class OpenMinorAdditionSymbolDelegate extends AbstractSymbolDrawer {

	protected CombinedPath buildShapeForDrawing(float width, float height) {
		float halfStroke= strokeWidth/2f;
		GeneralPath pol = new GeneralPath();
		pol.moveTo(width-halfStroke, halfStroke);			
		pol.curveTo(0, 0,
				0, height-halfStroke, 
				width-halfStroke, height-halfStroke);
		return new CombinedPath(pol);
	}

}
