package jsesh.mdcDisplayer.drawingElements;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

import jsesh.hieroglyphs.graphics.LigatureZone;
import jsesh.mdcDisplayer.drawingElements.symbolDrawers.SpecialSymbolDrawer;

/**
 * This hieroglyphic drawer is able to dispatch the drawings of signs according
 * to their codes and other features.
 * 
 * @author rosmord
 * 
 */
public class HieroglyphicDrawerDispatcher implements HieroglyphsDrawer {

	private final SVGFontHieroglyphicDrawer svgFontHieroglyphicDrawer = new SVGFontHieroglyphicDrawer();

	private HieroglyphsDrawer getDrawer(String code) {
		if (SpecialSymbolDrawer.getInstance().isSpecial(code)) {
			return SpecialSymbolDrawer.getInstance();
		} else {
			return svgFontHieroglyphicDrawer;
		}
	}

        @Override
	public void draw(Graphics2D g, String code, int angle, ViewBox view) {
		getDrawer(code).draw(g, code, angle, view);
	}

        @Override
	public Rectangle2D getBBox(String code, int angle, boolean fixed) {
		return getDrawer(code).getBBox(code, angle, fixed);
	}

        @Override
	public double getGroupUnitLength() {
		return svgFontHieroglyphicDrawer.getGroupUnitLength();
	}

        @Override
	public double getHeightOfA1() {
		return svgFontHieroglyphicDrawer.getHeightOfA1();
	}

        @Override
	public Optional<LigatureZone> getLigatureZone(int i, String code) {
		return getDrawer(code).getLigatureZone(i, code);
	}

        @Override
	public Shape getShape(String code) {
		return getDrawer(code).getShape(code);
	}

        @Override
	public Area getSignArea(String code, double x, double y, double xscale,
			double yscale, int angle, boolean reversed) {
		return getDrawer(code).getSignArea(code, x, y, xscale, yscale, angle,
				reversed);
	}

	

        @Override
	public boolean isKnown(String code) {
		return getDrawer(code).isKnown(code);
	}

        @Override
	public boolean isSmallBodyUsed() {
		return svgFontHieroglyphicDrawer.isSmallBodyUsed();
	}

        @Override
	public void setSmallBodyUsed(boolean smallBody) {
		svgFontHieroglyphicDrawer.setSmallBodyUsed(smallBody);
	}

}
