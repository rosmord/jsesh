package jsesh.mdcDisplayer.drawingElements;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;

import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.hieroglyphs.signshape.LigatureZone;
import jsesh.mdcDisplayer.layout.ExplicitPosition;
import jsesh.mdcDisplayer.mdcView.ViewBox;

/**
 * A hieroglyph drawer is able to draw signs (hieroglyphs, ecdotic symbols, etc) on a graphic context, and to provide geometric information about them.
 * 
 * @author rosmord
 * 
 */
public class HieroglyphDrawer  {

	private final SVGFontHieroglyphDrawer svgFontHieroglyphicDrawer;

	public HieroglyphDrawer(HieroglyphShapeRepository fontManager) {
		this.svgFontHieroglyphicDrawer = new SVGFontHieroglyphDrawer(fontManager);		
	}

	/**
	 * This method uses the right drawer for a specific code.
	 * Either it will look for SVG versions of hieroglyphs, or specific drawers for ecdotic symbols.
	 * @param code
	 * @return
	 */
	private BasicSignDrawer getDrawerForCode(String code) {
		if (SpecialSymbolDrawer.getInstance().isSpecial(code)) {
			return SpecialSymbolDrawer.getInstance();
		} else {
			return svgFontHieroglyphicDrawer;
		}
	}

	public void draw(Graphics2D g, String code, int angle, ViewBox view, HieroglyphBodySize bodySize) {
		getDrawerForCode(code).draw(g, code, angle, view, bodySize);
	}

	
	public Rectangle2D getBBox(String code, int angle, boolean fixed) {
		return getDrawerForCode(code).getBBox(code, angle, fixed);
	}

	
	public double getGroupUnitLength() {
		return svgFontHieroglyphicDrawer.getGroupUnitLength();
	}

	
	public double getHeightOfA1() {
		return svgFontHieroglyphicDrawer.getHeightOfA1();
	}

	
	public Optional<LigatureZone> getLigatureZone(int i, String code) {
		return getDrawerForCode(code).getLigatureZone(i, code);
	}

	
	public Shape getShape(String code) {
		return getDrawerForCode(code).getShape(code);
	}

	public Area getSignArea(String code, double x, double y, double xscale,
			double yscale, int angle, boolean reversed) {
		return getDrawerForCode(code).getSignArea(code, x, y, xscale, yscale, angle,
				reversed);
	}

	public boolean isKnown(String code) {
		return getDrawerForCode(code).isKnown(code);
	}


	public List<ExplicitPosition> getPositions(List<String> codes) {
		return svgFontHieroglyphicDrawer.getPositions(codes);
	}
}
