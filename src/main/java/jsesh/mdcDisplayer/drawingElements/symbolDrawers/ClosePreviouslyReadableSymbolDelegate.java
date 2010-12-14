package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;

import jsesh.mdcDisplayer.drawingElements.ViewBox;

public class ClosePreviouslyReadableSymbolDelegate implements SymbolDrawerDelegate {

	public void draw(Graphics2D g2d, int angle, ViewBox viewBox, float strokeWidth) {
		new CloseErasedSymbolDelegate().draw(g2d, angle, viewBox, strokeWidth);
		float smallWidth = strokeWidth * 0.75f;
		Stroke smallStroke= new BasicStroke(smallWidth);
		Stroke oldStroke= g2d.getStroke();
		g2d.setStroke(smallStroke);
		new SmallLineDrawer(strokeWidth).draw(g2d, angle, viewBox, smallWidth);
		g2d.setStroke(oldStroke);
	}

	public float getBaseWidth() {
		return 3;
	}

	private static class SmallLineDrawer extends AbstractSymbolDrawer {
		private float parentStrokeWidth;
		
		public SmallLineDrawer(float parentStrokeWidth) {
			this.parentStrokeWidth = parentStrokeWidth;
		}

		protected CombinedPath buildShapeForDrawing(float width, float height) {
			float hStart= width /2 + parentStrokeWidth;
			float vStart= 3* parentStrokeWidth;
			GeneralPath path= new GeneralPath();
			path.moveTo(width - hStart, vStart);
			path.lineTo(width - hStart, height-vStart);
			return new CombinedPath(path, buildRectangle(width, height));
		}
		
	}
	
}
