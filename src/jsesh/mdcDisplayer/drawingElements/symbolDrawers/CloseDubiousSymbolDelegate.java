package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.geom.GeneralPath;

public class CloseDubiousSymbolDelegate extends AbstractSymbolDrawer {
	
	// done
	protected CombinedPath buildShapeForDrawing(float width, float height) {
		GeneralPath pol = new GeneralPath();
		pol.moveTo(strokeWidth/2f, strokeWidth/2f);
		pol.lineTo(width-strokeWidth/2f, strokeWidth/2f);
		pol.lineTo(width-strokeWidth/2f, height / 2);
		return new CombinedPath(pol);
	}

}
