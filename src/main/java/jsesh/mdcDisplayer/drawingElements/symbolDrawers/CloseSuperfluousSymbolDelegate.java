package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import jsesh.mdcDisplayer.drawingElements.ViewBox;

public class CloseSuperfluousSymbolDelegate implements SymbolDrawerDelegate {

	public void draw(Graphics2D g2d, int angle, ViewBox viewBox,
			float strokeWidth) {
		new SymbolTopDrawer().draw(g2d, angle, viewBox, strokeWidth);
		new SymbolBottomDrawer().draw(g2d, angle, viewBox, strokeWidth);
	}

	public float getBaseWidth() {
		return 3;
	}

	private static class SymbolTopDrawer extends AbstractSymbolDrawer {
		protected CombinedPath buildShapeForDrawing(float width, float height) {
			float centerY = height / 2f;
			float halfStroke = strokeWidth / 2f;
			GeneralPath pol = new GeneralPath();

			pol.moveTo(halfStroke, halfStroke);
			pol.curveTo(width - halfStroke, halfStroke, halfStroke, centerY,
					width - halfStroke, centerY);
			
			return new CombinedPath(pol,buildMyPath(width, height, strokeWidth));
		}
	}

	private static class SymbolBottomDrawer extends AbstractSymbolDrawer {

		protected CombinedPath buildShapeForDrawing(float width, float height) {
			float halfStroke = strokeWidth / 2f;
			float centerY = height / 2f;
			GeneralPath pol = new GeneralPath();

			pol.moveTo(width - halfStroke, centerY);
			pol.curveTo(halfStroke, centerY, width - halfStroke, height
					- halfStroke, halfStroke, height - halfStroke);
			
			
			return new CombinedPath(pol,buildMyPath(width, height, strokeWidth));
		}

	}
	
	private static GeneralPath buildMyPath(float width, float height, float strokeWidth) {
		float halfStroke = strokeWidth / 2f;
		float centerY = (height) / 2f;
		GeneralPath pol = new GeneralPath();
		final float pw = width - halfStroke;

		pol.moveTo(halfStroke, halfStroke);
		pol.curveTo(width - halfStroke, halfStroke, halfStroke, centerY,
				width - halfStroke, centerY);
		pol.moveTo(width - halfStroke, centerY);
		pol.curveTo(halfStroke, centerY, width - halfStroke, height
				- halfStroke, halfStroke, height - halfStroke);
		
		return pol;
	}

}
