package jsesh.render.elements.internal.symboldrawers;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;

import jsesh.render.view.ViewBox;

public class ClosePreviouslyReadableSymbolDelegate implements SymbolDrawerDelegate {

	public void draw(Graphics2D g2d, int angle, ViewBox viewBox, double strokeWidth) {
		new CloseErasedSymbolDelegate().draw(g2d, angle, viewBox, strokeWidth);
		double smallWidth = strokeWidth * 0.75;
		Stroke smallStroke= new BasicStroke((float) smallWidth);
		Stroke oldStroke= g2d.getStroke();
		g2d.setStroke(smallStroke);
		new SmallLineDrawer(strokeWidth).draw(g2d, angle, viewBox, smallWidth);
		g2d.setStroke(oldStroke);
	}

	public double getBaseWidth() {
		return 3;
	}

	private static class SmallLineDrawer extends AbstractSymbolDrawer {
		private double parentStrokeWidth;
		
		public SmallLineDrawer(double parentStrokeWidth) {
			this.parentStrokeWidth = parentStrokeWidth;
		}

		protected CombinedPath buildShapeForDrawing(double width, double height) {
			double hStart= width /2 + parentStrokeWidth;
			double vStart= 3* parentStrokeWidth;
			Path2D.Double path= new Path2D.Double();
			path.moveTo(width - hStart, vStart);
			path.lineTo(width - hStart, height-vStart);
			return new CombinedPath(path, buildRectangle(width, height));
		}
		
	}
	
}
