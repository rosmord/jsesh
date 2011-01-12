package jsesh.mdcDisplayer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class Shader {
	
	/**
	 * Shade an area. Utility method.
	 * To function correctly, this method needs the area to be expressed in page coordinates.
	 * That is, all areas should be drawn using the same reference system.
	 * If it isn't the case, they won't align correctly.
	 * TODO : to avoid too many copies of the area, we might decide that the area will be modified
	 * by the method.
	 * @param area
	 */
	public void shadeArea(Graphics2D g, Area area) {
		// Easy way: paint the area in grey...
		// Complex way : intersect the area with line hatching...

		// Prevent us from messing with the area (not that important currently,
		// but who knows ?)

		area = (Area) area.clone();
		// Let's work in a protected graphic environment.
		Graphics2D tempG = (Graphics2D) g.create();

		// Now we can work in page coordinate for everything...
		// Let's take a rectangle large enough
		Rectangle2D r = area.getBounds2D();

		// Now, we want to draw lines in this rectangle
		// Those lines will be added to an area
		Area shadingLines = new Area();

		// the minimal possible starting point for a shading line which
		// intersects our area.
		double startx = r.getMinX() - r.getHeight();

		double spacing = 3; // Spacing between lines (move it to
		// drawingSpecification)

		double lineWidth = 0.5; // Normal line width (idem).

		BasicStroke stroke = new BasicStroke((float) lineWidth);

		// Actual start...
		double n = Math.ceil((startx + r.getMaxY()) / spacing);

		// This is the abscissa of the first line we will draw

		double x = n * spacing - r.getMaxY();

		// Add all relevant line segments to the area.
		while (x < r.getMaxX()) {
			Line2D l = new Line2D.Double(x, r.getMaxY(), x + r.getHeight(), r
					.getMinY());

			shadingLines.add(new Area(stroke.createStrokedShape(l)));
			x += spacing;
		}
		// The area is ready. Intersect it with the original area
		area.intersect(shadingLines);
		// Draw...
		tempG.setColor(Color.BLACK);
		tempG.fill(area);

		tempG.dispose();
	}
}
