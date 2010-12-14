package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.geom.GeneralPath;

public class CloseEditorAdditionSymbolDelegate extends AbstractSymbolDrawer {

	// done
	protected CombinedPath buildShapeForDrawing(float width, float height) {
		float centerY = height / 2f;
		float halfStroke= strokeWidth/2f;
		GeneralPath pol = new GeneralPath();
		
		pol.moveTo(halfStroke, halfStroke);
		pol.lineTo(width-halfStroke, centerY);
		pol.lineTo(halfStroke, height-halfStroke);
		
		return new CombinedPath(pol);
	}
	
	public float getBaseWidth() {
		return 3;
	}
}
