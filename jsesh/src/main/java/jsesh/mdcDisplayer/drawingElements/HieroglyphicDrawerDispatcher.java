package jsesh.mdcDisplayer.drawingElements;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import jsesh.hieroglyphs.LigatureZone;
import jsesh.mdcDisplayer.drawingElements.symbolDrawers.SpecialSymbolDrawer;

/**
 * This hieroglyphic drawer is able to dispatch the drawings of signs according
 * to their codes and other features.
 * 
 * @author rosmord
 * 
 */
public class HieroglyphicDrawerDispatcher implements HieroglyphsDrawer {

	private SVGFontHieroglyphicDrawer svgFontHieroglyphicDrawer = new SVGFontHieroglyphicDrawer();

	private HieroglyphsDrawer getDrawer(String code) {
		if (SpecialSymbolDrawer.getInstance().isSpecial(code)) {
			return SpecialSymbolDrawer.getInstance();
		} else {
			return svgFontHieroglyphicDrawer;
		}
	}

	public void draw(Graphics2D g, String code, int angle, ViewBox view) {
		getDrawer(code).draw(g, code, angle, view);
	}

	public Rectangle2D getBBox(String code, int angle, boolean fixed) {
		return getDrawer(code).getBBox(code, angle, fixed);
	}

	public double getGroupUnitLength() {
		return svgFontHieroglyphicDrawer.getGroupUnitLength();
	}

	public double getHeightOfA1() {
		return svgFontHieroglyphicDrawer.getHeightOfA1();
	}

	public LigatureZone getLigatureZone(int i, String code) {
		return getDrawer(code).getLigatureZone(i, code);
	}

	public Shape getShape(String code) {
		return getDrawer(code).getShape(code);
	}

	public Area getSignArea(String code, double x, double y, double xscale,
			double yscale, int angle, boolean reversed) {
		return getDrawer(code).getSignArea(code, x, y, xscale, yscale, angle,
				reversed);
	}

	

	public boolean isKnown(String code) {
		return getDrawer(code).isKnown(code);
	}

	public boolean isSmallBodyUsed() {
		return svgFontHieroglyphicDrawer.isSmallBodyUsed();
	}

	public void setSmallBodyUsed(boolean smallBody) {
		svgFontHieroglyphicDrawer.setSmallBodyUsed(smallBody);
	}

}
