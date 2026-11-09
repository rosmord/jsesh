package jsesh.render.elements.internal.symboldrawers;

import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import jsesh.render.view.ViewBox;

public class CloseSuperfluousSymbolDelegate implements SymbolDrawerDelegate {

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
			double centerY = height / 2.0;
			double halfStroke = strokeWidth / 2.0;
			Path2D.Double pol = new Path2D.Double();

			pol.moveTo(halfStroke, halfStroke);
			pol.curveTo(width - halfStroke, halfStroke, halfStroke, centerY,
					width - halfStroke, centerY);
			
			return new CombinedPath(pol,buildMyPath(width, height, strokeWidth));
		}
	}

	private static class SymbolBottomDrawer extends AbstractSymbolDrawer {

		protected CombinedPath buildShapeForDrawing(double width, double height) {
			double halfStroke = strokeWidth / 2.0;
			double centerY = height / 2.0;
			Path2D.Double pol = new Path2D.Double();

			pol.moveTo(width - halfStroke, centerY);
			pol.curveTo(halfStroke, centerY, width - halfStroke, height
					- halfStroke, halfStroke, height - halfStroke);
			
			
			return new CombinedPath(pol,buildMyPath(width, height, strokeWidth));
		}

	}
	
	private static Path2D.Double buildMyPath(double width, double height, double strokeWidth) {
		double halfStroke = strokeWidth / 2.0;
		double centerY = (height) / 2.0;
		Path2D.Double pol = new Path2D.Double();

		pol.moveTo(halfStroke, halfStroke);
		pol.curveTo(width - halfStroke, halfStroke, halfStroke, centerY,
				width - halfStroke, centerY);
		pol.moveTo(width - halfStroke, centerY);
		pol.curveTo(halfStroke, centerY, width - halfStroke, height
				- halfStroke, halfStroke, height - halfStroke);
		
		return pol;
	}

}
