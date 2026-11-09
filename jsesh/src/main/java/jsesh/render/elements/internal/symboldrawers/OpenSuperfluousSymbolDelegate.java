package jsesh.render.elements.internal.symboldrawers;

import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import jsesh.render.view.ViewBox;

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
			double strokeWidth) {
		new SymbolTopDrawer().draw(g2d, angle, viewBox, strokeWidth);
		new SymbolBottomDrawer().draw(g2d, angle, viewBox, strokeWidth);
	}

	public double getBaseWidth() {
		return 3;
	}

	private static class SymbolTopDrawer extends AbstractSymbolDrawer {
		protected CombinedPath buildShapeForDrawing(double width, double height) {
			double halfStroke = strokeWidth / 2.0;
			double centerY = (height) / 2.0;

			Path2D.Double pol = new Path2D.Double();

			final double pw = width - halfStroke;

			pol.moveTo(pw, halfStroke);

			pol.curveTo(halfStroke, halfStroke, pw, centerY, halfStroke,
					centerY);
			return new CombinedPath(pol, buildMyPath(width, height, strokeWidth));
		}
	}

	private static class SymbolBottomDrawer extends AbstractSymbolDrawer {

		protected CombinedPath buildShapeForDrawing(double width, double height) {
			double halfStroke = strokeWidth / 2.0;
			double centerY = (height) / 2.0;
			Path2D.Double pol = new Path2D.Double();

			final double pw = width - halfStroke;

			pol.moveTo(halfStroke, centerY);

			pol.curveTo(pw, centerY, halfStroke, height - halfStroke, pw,
					height - halfStroke);

			return new CombinedPath(pol, buildMyPath(width, height, strokeWidth));
		}
	}

	private static Path2D.Double buildMyPath(double width, double height, double strokeWidth) {
		double halfStroke = strokeWidth / 2.0;
		double centerY = (height) / 2.0;

		Path2D.Double pol = new Path2D.Double();

		final double pw = width - halfStroke;

		pol.moveTo(pw, halfStroke);

		pol.curveTo(halfStroke, halfStroke, pw, centerY, halfStroke, centerY);

		pol.moveTo(halfStroke, centerY);

		pol.curveTo(pw, centerY, halfStroke, height - halfStroke, pw, height
				- halfStroke);

		return pol;
	}
}
