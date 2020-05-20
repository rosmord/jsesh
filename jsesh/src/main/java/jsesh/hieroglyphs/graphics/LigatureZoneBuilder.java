/**
 * A automated ligature system, very loosely inspired from M-J. Nederhoff's RES.
 * 
 * (RES is actually much more precise, as it use all signs to compute the "ligature",
 *  and no bounding box at all).
 * @author Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.hieroglyphs.graphics;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/**
 * 
 * @author rosmord
 */
public class LigatureZoneBuilder {

	private ShapeChar signShape;

	private Area signArea;

	private LigatureZone zones[];

	static final double MARGIN = 1;

	// Should be taken from drawingSpecifications!!!
	
	static final double MAXHEIGHT = 18;

	/**
	 * @param shape
	 */
	public LigatureZoneBuilder(ShapeChar signShape) {
		zones = new LigatureZone[3];
		this.signShape = signShape;
		signArea = signShape.getSignArea(0, 0, 1, 1, 0);
	}

	/**
	 * Compute the "down" ligature area (usable for "D", "ns", and the like).
	 */
	private void computeThirdArea() {
		// Principle : we try to fit a rectangular area, whose start edge is
		// always 0, whose height is the sign's height(PREVIOUSLY : max cadrat
		// height)
		// the end edge moves along the diagonal from
		// (sign.width/2,sign.height)--(sign.width,0)

		double signHeight = signShape.getBbox().getHeight();
		double signWidth = signShape.getBbox().getWidth();

		Point2D.Double outer, inner;
		outer = new Point2D.Double(signWidth / 2, signHeight);
		inner = new Point2D.Double(signWidth, 0);

		double t = findBestZonePosition(outer, inner, -signWidth, signHeight);
		if (t > 0.2) {
			Point2D.Double p = computePosition(outer, inner, t);
			//zones[2] = new LigatureZone(0, p.y, p.x, signHeight - p.y + 1);
			zones[2] = new LigatureZone(0, p.y, p.x, MAXHEIGHT - p.y + 1);
		} else {
			// Try along a more oblique line: 
			outer.x=0;
			t = findBestZonePosition(outer, inner, -signWidth, signHeight);
			if (t > 0.2) {
				Point2D.Double p = computePosition(outer, inner, t);
				//zones[2] = new LigatureZone(0, p.y, p.x, signHeight  - p.y+1);
				zones[2] = new LigatureZone(0, p.y, p.x, MAXHEIGHT  - p.y+1);
				
			}
			zones[2] = null;
		}
		if (zones[2] != null) {
			zones[2].setVerticalGravity(VerticalGravity.BOTTOM);
		}
	}

	/**
	 * @param outer
	 * @param inner
	 * @param t
	 * @return
	 */
	private Point2D.Double computePosition(Point2D.Double outer,
			Point2D.Double inner, double t) {
		double x = outer.x + t * (inner.x - outer.x);
		double y = outer.y + t * (inner.y - outer.y);
		return new Point2D.Double(x, y);
	}

	private void computeFirstArea() {
		// Principle for this area : we try to fit a rectangular area, whose
		// size is one third of the size of the sign.
		// the area r is placed at coordinates x= -1/2 r.width, y= 1/2
		// sign.height.
		// Then, if the sign and the rectangle overlap, we move the rectangle
		// horizontally until it does not.
		// For this, we use a dichotomic method. We know there is no overlapping
		// when x= -r.width !

		double y = 0.5 * signShape.getBbox().getHeight();

		double height = signShape.getBbox().getHeight() / 3.0;
		double width = signShape.getBbox().getWidth() / 2;

		Point2D.Double outer = new Point2D.Double(-width, y);
		Point2D.Double inner = new Point2D.Double(0, y);

		double t = findBestZonePosition(outer, inner, width, height);

		if (t < 0.2) {
			// Too near the edge... try with a smaller box :
			height = signShape.getBbox().getHeight() / 4.0;
			width = signShape.getBbox().getWidth() / 4.0;
			// y = 0.5 * signShape.getBbox().getHeight();
			t = findBestZonePosition(outer, inner, width, height);
			if (t < 0.1) {
				zones[0] = null;
			} else {
				Point2D.Double p = computePosition(outer, inner, t);
				zones[0] = new LigatureZone(p.x, p.y, width, height);
			}
		} else {
			Point2D.Double p = computePosition(outer, inner, t);
			zones[0] = new LigatureZone(p.x, p.y, width, height);
		}
		if (zones[0]!= null) {
			zones[0].setHorizontalGravity(HorizontalGravity.END);
		}
		// TODO We may now enlarge the box vertically
	}

	/**
	 * Return the largest barycentric coordinate on the segment outer-inner such
	 * that the rectangle doesn't intersect the sign.
	 */
	private double findBestZonePosition(Point2D.Double outer,
			Point2D.Double inner, double width, double height) {
		double innerT = 1;
		double outerT = 0;
		while (Math.abs(innerT - outerT) > 0.01) {
			double middleT = (innerT + outerT) / 2.0;

			Point2D.Double p = computePosition(outer, inner, middleT);

			double minx = p.x - MARGIN;
			double miny = p.y - MARGIN;
			if (width < 0) {
				minx = minx + width;
			}
			if (height < 0) {
				miny = miny + height;
			}

			Area intersectArea = new Area(new Rectangle2D.Double(minx, miny,
					Math.abs(width) + 2 * MARGIN, Math.abs(height) + 2 * MARGIN));
			intersectArea.intersect(signArea);
			if (intersectArea.isEmpty()) {
				outerT = middleT;
			} else {
				innerT = middleT;
			}
		}
		return outerT;
	}

	/**
	 * @param shape
	 */
	private void computeSecondArea() {
		// Principle : we start with an element whose size is one third of the
		// size of the sign, placed
		// at x= sign.width/2, y 0. We then move it horizontally.
		double width = signShape.getBbox().getWidth() / 2;
		double height = signShape.getBbox().getHeight() / 2.5;

		Point2D.Double inner, outer;

		outer = new Point2D.Double(signShape.getBbox().getWidth(), 0);
		inner = new Point2D.Double(signShape.getBbox().getWidth() / 2, 0);
		double t = findBestZonePosition(outer, inner, width, height);
		if (t < 0.2) {
			// Try same thing, but with y a bit highter :
			height = signShape.getBbox().getHeight() / 3;
			// y= -0.3*height;
			t = findBestZonePosition(outer, inner, width, height);
			if (t < 0.1) {
				zones[1] = null;
			} else {
				Point2D.Double p = computePosition(outer, inner, t);
				zones[1] = new LigatureZone(p.x, p.y, width, height);
			}
		} else {
			Point2D.Double p = computePosition(outer, inner, t);

			zones[1] = new LigatureZone(p.x, p.y, width, height);
		}
		if (zones[1]!= null) {
			zones[1].setHorizontalGravity(HorizontalGravity.START);
			zones[1].setVerticalGravity(VerticalGravity.TOP);
		}
	}

	/**
	 * Return one of the ligature areas : left, right, and down.
	 * 
	 * @param i
	 */
	public LigatureZone getLigatureArea(int i) {
		switch (i) {
		case 0:
			computeFirstArea();
			break;
		case 1:
			computeSecondArea();
			break;
		case 2:
			computeThirdArea();
			break;
		default:
			break;
		}
		return zones[i];
	}

}
