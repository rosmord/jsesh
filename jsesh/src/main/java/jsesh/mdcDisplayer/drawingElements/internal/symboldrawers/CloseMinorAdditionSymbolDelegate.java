package jsesh.mdcDisplayer.drawingElements.internal.symboldrawers;

import java.awt.geom.GeneralPath;

// Done
public class CloseMinorAdditionSymbolDelegate extends AbstractSymbolDrawer {

	protected CombinedPath buildShapeForDrawing(float width, float height) {
		GeneralPath pol = new GeneralPath();
		float halfStroke= strokeWidth/2f;
		
		pol = new GeneralPath();
		pol.moveTo(halfStroke, halfStroke);
		// the width parameter should be modified a little, methink. later.
		pol.curveTo(width,
				halfStroke, width,
				height-halfStroke, halfStroke, height-halfStroke);
		
		return new CombinedPath(pol);
	}

}
