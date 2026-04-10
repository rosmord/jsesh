package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import jsesh.hieroglyphs.signshape.LigatureZone;
import jsesh.mdcDisplayer.drawingElements.BasicSignDrawer;
import jsesh.mdcDisplayer.drawingElements.HieroglyphBodySize;
import jsesh.mdcDisplayer.drawingElements.HieroglyphDrawer;
import jsesh.mdcDisplayer.layout.ExplicitPosition;
import jsesh.mdcDisplayer.mdcView.ViewBox;
import jsesh.swing.utils.ShapeHelper;

/**
 * This class is responsible for drawing special symbols, which are not handled
 * by the normal hieroglyphic fonts.
 * 
 * It is stateless, and costly to create, so we use a singleton.
 */
public class SpecialSymbolDrawer implements BasicSignDrawer {

	private static final SpecialSymbolDrawer instance = new SpecialSymbolDrawer();

	public static SpecialSymbolDrawer getInstance() {
		return instance;
	}

	public static final float PARENTHESIS_STROKE_WIDTH = 0.75f;
	public static final float EDITOR_MARKUP_SMALL_HEIGHT = 2f;
	public static final float EDITOR_MARKUP_HEIGHT = 18f;

	
	private final HashMap<String, SymbolDrawerDelegate> specificDrawers = new HashMap<String, SymbolDrawerDelegate>();

	public boolean isSpecial(String code) {
		return specificDrawers.containsKey(code);
	}

	private SpecialSymbolDrawer() {
		specificDrawers.put("[[", new OpenErasedSymbolDelegate());
		specificDrawers.put("]]", new CloseErasedSymbolDelegate());
		specificDrawers.put("[?", new OpenDubiousSymbolDelegate());
		specificDrawers.put("?]", new CloseDubiousSymbolDelegate());
		specificDrawers.put("[&", new OpenEditorAdditionSymbolDelegate());
		specificDrawers.put("&]", new CloseEditorAdditionSymbolDelegate());
		specificDrawers.put("[{", new OpenSuperfluousSymbolDelegate());
		specificDrawers.put("}]", new CloseSuperfluousSymbolDelegate());
		specificDrawers.put("[\"", new OpenPreviouslyReadableSymbolDelegate());
		specificDrawers.put("\"]", new ClosePreviouslyReadableSymbolDelegate());
		specificDrawers.put("['", new OpenScribeAdditionSymbolDelegate());
		specificDrawers.put("']", new CloseScribeAdditionSymbolDelegate());
		specificDrawers.put("[(", new OpenMinorAdditionSymbolDelegate());
		specificDrawers.put(")]", new CloseMinorAdditionSymbolDelegate());

	}

	@Override
	public void draw(Graphics2D g2d, String code, int angle, ViewBox currentView, HieroglyphBodySize bodySize) {
		Stroke oldStroke = g2d.getStroke();
		float strokeWidth = PARENTHESIS_STROKE_WIDTH; // * currentView.getXScale();
		g2d.setStroke(new BasicStroke(strokeWidth));
		specificDrawers.get(code).draw(g2d, angle, currentView, strokeWidth);
		g2d.setStroke(oldStroke);
	}

	@Override
	public Rectangle2D getBBox(String code, int angle, boolean fixed) {
		Rectangle2D result = new Rectangle2D.Double(0, 0, getBaseWidth(code), EDITOR_MARKUP_SMALL_HEIGHT);
		if (angle != 0 || fixed) {
			result = new Rectangle2D.Double(0, 0, getBaseWidth(code), EDITOR_MARKUP_HEIGHT);
			if (angle != 0) {
				GeneralPath path = new GeneralPath(result);
				path.transform(AffineTransform.getRotateInstance(angle
						* Math.PI / 180f));
				result = path.getBounds2D();
			}
		}
		return result;
	}

	private float getBaseWidth(String code) {
		return specificDrawers.get(code).getBaseWidth();
	}

	@Override
	public Optional<LigatureZone> getLigatureZone(int i, String code) {
		return Optional.empty();
	}

	@Override
	public Shape getShape(String code) {
		return new Rectangle2D.Double(0, 0, getBaseWidth(code), EDITOR_MARKUP_HEIGHT);
	}

	@Override
	public Area getSignArea(String code, double x, double y, double xscale,
			double yscale, int angle, boolean reversed) {
		Shape s = ShapeHelper.transformShape(x, y, xscale, yscale, angle
				* Math.PI / 180.0, getShape(code));
		return new Area(s);
	}

		@Override
	public boolean isKnown(String code) {
		return isSpecial(code);
	}
	

	@Override
	public List<ExplicitPosition> getPositions(List<String> codes) {
		return List.of();
	}
}
