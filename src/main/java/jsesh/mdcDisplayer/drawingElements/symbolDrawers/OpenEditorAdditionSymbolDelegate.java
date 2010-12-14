package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.geom.GeneralPath;

public class OpenEditorAdditionSymbolDelegate extends AbstractSymbolDrawer {

	protected CombinedPath buildShapeForDrawing(float width, float height) {
		float centerY = height / 2f;
		float halfStroke= strokeWidth/2f;
		GeneralPath pol = new GeneralPath();
		
		pol.moveTo(width-halfStroke, halfStroke);
		pol.lineTo(halfStroke, centerY);
		pol.lineTo(width-halfStroke, height-halfStroke);
		return new CombinedPath(pol);
	}
	
	public float getBaseWidth() {
		return 3;
	}

}
