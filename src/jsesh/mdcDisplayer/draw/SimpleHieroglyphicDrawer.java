package jsesh.mdcDisplayer.draw;

import java.util.HashMap;
import java.util.Map;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import jsesh.bzr.simple.ShapeChar;
import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.HieroglyphicFontManager;
import jsesh.mdcDisplayer.layout.ComplexLigatureLayout;
import jsesh.mdcDisplayer.mdcView.MDCView;

/**
 * This file is free Software under the GNU LESSER GENERAL PUBLIC LICENCE.
 * 
 * (c) Serge Rosmorduc
 * 
 * @author Serge Rosmorduc
 * 
 */

public class SimpleHieroglyphicDrawer implements HieroglyphsDrawer {

	/**
	 * Code used as "default" hieroglyph to compute sizes.
	 */
	private static final String DEFAULT_CODE = "A1";

	/**
	 * The manager which associates codes with actual glyphs.
	 */
	private HieroglyphicFontManager fontManager;

	/**
	 * A map code for signs not managed by FontManager.
	 */
	private Map nonHieroglyphs;

	public SimpleHieroglyphicDrawer() {
		// IMPORTANT : I don't know it this absolutely needs to be a singleton.
		// If it's not, we might think of using prototypes.
		this.fontManager = DefaultHieroglyphicFontManager.getInstance();
		nonHieroglyphs = new HashMap();

		// Use The A1 sign as base
		// TODO : perhaps give the HieroglyphicDrawer some informations about
		// were it draws,
		// so that, for instance, shading can be based on expected cadrat
		// dimensions.
		Rectangle2D specA1 = fontManager.get(DEFAULT_CODE).getBbox();
		float w = (float) specA1.getWidth();
		float h = (float) specA1.getHeight();

		nonHieroglyphs.put("/", new Rectangle2D.Float(0, 0, w / 2f, h / 2f));
		nonHieroglyphs.put("//", new Rectangle2D.Float(0, 0, w, h));
		nonHieroglyphs.put("h/", new Rectangle2D.Float(0, 0, w, h / 2f));
		nonHieroglyphs.put("v/", new Rectangle2D.Float(0, 0, w / 2f, h));
	}

	/**
	 * @param g
	 * @param code
	 * @param angle
	 * @param view
	 * @see jsesh.mdcDisplayer.draw.HieroglyphsDrawer#draw(Graphics2D, String,
	 *      int, MDCView)
	 */
	public void draw(Graphics2D g, String code, int angle, MDCView view) {
		ShapeChar glyph = fontManager.get(code);
		if (glyph == null) {
			Rectangle2D r = g.getFont().getStringBounds(code,
					new FontRenderContext(new AffineTransform(), true, true));
			// Get the actual width of the view:
			double w = view.getWidth() / view.getXScale();
			// Scale the text to fit :
			g.scale(w / r.getWidth(), w / r.getWidth());
			if (code.length() > 0)
				g.drawString(code, (float) -r.getMinX(), (float) -r.getMinY());
		} else {
			glyph.draw(g, 0, 0, 1.0, 1.0, (float) (angle * Math.PI / 180));
		}
	}

	/**
	 * @see jsesh.mdcDisplayer.draw.HieroglyphsDrawer#getBBox(String)
	 */
	public Rectangle2D getBBox(String code) {
		Rectangle2D result = null;
		ShapeChar glyph = fontManager.get(code);
		if (glyph != null) {
			result = glyph.getBbox();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.HieroglyphsDrawer#getShape(java.lang.String)
	 */
	public Shape getShape(String code) {
		Shape result = null;
		ShapeChar glyph = fontManager.get(code);
		if (glyph != null)
			result = glyph.getShape();
		else
			result = getBBox(code);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.HieroglyphsDrawer#getTransformedShape(java.lang.String,
	 *      double, double, double, double, float, boolean)
	 */

	public Shape getTransformedShape(String code, double x, double y,
			double xscale, double yscale, int angle, boolean reversed) {
		Shape result = null;
		ShapeChar glyph = fontManager.get(code);
		if (glyph != null)
			// FIXME : use reversed !
			result = glyph.getTransformedShape(x, y, xscale, yscale, angle
					* Math.PI / 180.0);
		else
			result = getBBox(code);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.HieroglyphsDrawer#getSignArea(java.lang.String,
	 *      double, double, double, double, float, boolean)
	 */
	public Area getSignArea(String code, double x, double y, double xscale,
			double yscale, int angle, boolean reversed) {
		Area result = null;
		ShapeChar glyph = fontManager.get(code);
		if (glyph != null)
			// FIXME : use reversed !
			result = glyph.getSignArea(x, y, xscale, yscale, angle * Math.PI
					/ 180.0);
		else
			result = new Area();
		return result;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.HieroglyphsDrawer#isKnown(java.lang.String)
	 */
	public boolean isKnown(String code) {
		return (fontManager.get(code) != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.HieroglyphsDrawer#getLigatureArea(int,
	 *      java.lang.String)
	 */
	public Rectangle2D getLigatureArea(int i, String code) {
		ShapeChar glyph = fontManager.get(code);
		Rectangle2D result = null;
		if (glyph != null) {
			result = glyph.getZone(i);
			if (result == null) {
				ComplexLigatureLayout l = new ComplexLigatureLayout(glyph);
				result = l.getLigatureArea(i);
			}
		}
		return result;
	}
}
