package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import jsesh.mdcDisplayer.drawingElements.ViewBox;

/**
 * Draw the curly bracket.
 * 
 * @author rosmord
 * 
 *        <p>
 *         Implementation note : we used to draw them as one curve, but it
 *         doesn't render well in EMF.
 *         </p>
 *        <p>
 *         Implementation note (bis) : take the rotation into account.
 */
public class OpenSuperfluousSymbolDelegate implements SymbolDrawerDelegate {

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
			float halfStroke = strokeWidth / 2f;
			float centerY = (height) / 2f;

			GeneralPath pol = new GeneralPath();

			final float pw = width - halfStroke;

			pol.moveTo(pw, halfStroke);

			pol.curveTo(halfStroke, halfStroke, pw, centerY, halfStroke,
					centerY);
			return new CombinedPath(pol, buildMyPath(width, height, strokeWidth));
		}
	}

	private static class SymbolBottomDrawer extends AbstractSymbolDrawer {

		protected CombinedPath buildShapeForDrawing(float width, float height) {
			float halfStroke = strokeWidth / 2f;
			float centerY = (height) / 2f;
			GeneralPath pol = new GeneralPath();

			final float pw = width - halfStroke;

			pol.moveTo(halfStroke, centerY);

			pol.curveTo(pw, centerY, halfStroke, height - halfStroke, pw,
					height - halfStroke);

			return new CombinedPath(pol, buildMyPath(width, height, strokeWidth));
		}
	}

	private static GeneralPath buildMyPath(float width, float height, float strokeWidth) {
		float halfStroke = strokeWidth / 2f;
		float centerY = (height) / 2f;

		GeneralPath pol = new GeneralPath();

		final float pw = width - halfStroke;

		pol.moveTo(pw, halfStroke);

		pol.curveTo(halfStroke, halfStroke, pw, centerY, halfStroke, centerY);

		pol.moveTo(halfStroke, centerY);

		pol.curveTo(pw, centerY, halfStroke, height - halfStroke, pw, height
				- halfStroke);

		return pol;
	}
}
