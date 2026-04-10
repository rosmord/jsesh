package jsesh.mdcDisplayer.drawingElements;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;

import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.hieroglyphs.signshape.LigatureZone;
import jsesh.mdcDisplayer.drawingElements.symbolDrawers.SpecialSymbolDrawer;
import jsesh.mdcDisplayer.layout.ExplicitPosition;
import jsesh.mdcDisplayer.mdcView.ViewBox;

/**
 * This hieroglyphic drawer is able to dispatch the drawings of signs according
 * to their codes and other features.
 * 
 * We hide this class behind the factory method of HieroglyphDrawer.
 * @author rosmord
 * 
 */
class HieroglyphDrawerDispatcher implements HieroglyphDrawer {

	private final SVGFontHieroglyphDrawer svgFontHieroglyphicDrawer;

	public HieroglyphDrawerDispatcher(SVGFontHieroglyphDrawer svgFontHieroglyphicDrawer) {
		this.svgFontHieroglyphicDrawer = svgFontHieroglyphicDrawer;
	}

	public HieroglyphDrawerDispatcher(HieroglyphShapeRepository fontManager) {
		this(new SVGFontHieroglyphDrawer(fontManager));
	}

	/**
	 * This method uses the right drawer for a specific code.
	 * @param code
	 * @return
	 */
	private HieroglyphDrawer getDrawerForCode(String code) {
		if (SpecialSymbolDrawer.getInstance().isSpecial(code)) {
			return SpecialSymbolDrawer.getInstance();
		} else {
			return svgFontHieroglyphicDrawer;
		}
	}

	@Override
	public void draw(Graphics2D g, String code, int angle, ViewBox view, HieroglyphBodySize bodySize) {
		getDrawerForCode(code).draw(g, code, angle, view, bodySize);
	}

	@Override
	public Rectangle2D getBBox(String code, int angle, boolean fixed) {
		return getDrawerForCode(code).getBBox(code, angle, fixed);
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
		return getDrawerForCode(code).getLigatureZone(i, code);
	}

	@Override
	public Shape getShape(String code) {
		return getDrawerForCode(code).getShape(code);
	}

	@Override
	public Area getSignArea(String code, double x, double y, double xscale,
			double yscale, int angle, boolean reversed) {
		return getDrawerForCode(code).getSignArea(code, x, y, xscale, yscale, angle,
				reversed);
	}

	@Override
	public boolean isKnown(String code) {
		return getDrawerForCode(code).isKnown(code);
	}

	@Override
	public List<ExplicitPosition> getPositions(List<String> codes) {
		return svgFontHieroglyphicDrawer.getPositions(codes);
	}
}
