package jsesh.mdcDisplayer.drawingElements;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.graphics.HieroglyphicFontManager;
import jsesh.hieroglyphs.graphics.LigatureZone;
import jsesh.hieroglyphs.graphics.LigatureZoneBuilder;
import jsesh.hieroglyphs.graphics.ShapeChar;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.swing.utils.ShapeHelper;

/**
 * This file is free Software under the GNU LESSER GENERAL PUBLIC LICENCE.
 *
 * (c) Serge Rosmorduc
 *
 * @author Serge Rosmorduc
 *
 */
/**
 * A Hieroglyphic drawer which takes its input from (SVG) fonts. Our default
 * implementation of hieroglyphs drawing and measuring.
 */
public class SVGFontHieroglyphicDrawer implements HieroglyphsDrawer {

    /*
	 * IMPORTANT IMPLEMENTATION NOTE: this implementation of the hieroglyphic
	 * drawers relies on a relatively simple encoding scheme for glyphs. Hence,
	 * we will rewrite codes to deal with special codes like '[['.
     */
    /**
     * Code used as "default" hieroglyph to compute sizes.
     */
    private static final String DEFAULT_CODE = "A1";

    private static HashMap<String, String> normalizedCodesMap = new HashMap<String, String>();

    static {
        normalizedCodesMap.put("[[", "BEGINERASE");
        normalizedCodesMap.put("]]", "ENDERASE");
        normalizedCodesMap.put("[{", "BEGINEDITORSUPERFLUOUS");
        normalizedCodesMap.put("}]", "ENDEDITORSUPERFLUOUS");
        normalizedCodesMap.put("[&", "BEGINEDITORADDITION");
        normalizedCodesMap.put("&]", "ENDEDITORADDITION");
        normalizedCodesMap.put("[\"", "BEGINPREVIOUSLYREADABLE");
        normalizedCodesMap.put("\"]", "ENDPREVIOUSLYREADABLE");
        normalizedCodesMap.put("[(", "BEGINMINORADDITION");
        normalizedCodesMap.put(")]", "ENDMINORADDITION");
        normalizedCodesMap.put("[?", "BEGINDUBIOUS");
        normalizedCodesMap.put("?]", "ENDDUBIOUS");
    }
    /**
     * The manager which associates codes with actual glyphs.
     */
    private HieroglyphicFontManager fontManager;

    /**
     * A map code for signs not managed by FontManager.
     */
    private Map<String, Float> nonHieroglyphic;

    private float heightOfA1 = 0;

    private boolean smallBodyUsed = false;

    public SVGFontHieroglyphicDrawer() {
        // IMPORTANT : I don't know it this absolutely needs to be a singleton.
        // If it's not, we might think of using prototypes.
        this.fontManager = DefaultHieroglyphicFontManager.getInstance();

        nonHieroglyphic = new HashMap<String, Float>();

        // Use The A1 sign as base
        // TODO : perhaps give the HieroglyphicDrawer some informations about
        // were it draws,
        // so that, for instance, shading can be based on expected cadrat
        // dimensions.
        Rectangle2D specA1 = fontManager.get(DEFAULT_CODE).getBbox();
        float w = (float) specA1.getWidth();
        float h = (float) specA1.getHeight();
        heightOfA1 = h;

        nonHieroglyphic.put("/", new Rectangle2D.Float(0, 0, w / 2f, h / 2f));
        nonHieroglyphic.put("//", new Rectangle2D.Float(0, 0, w, h));
        nonHieroglyphic.put("h/", new Rectangle2D.Float(0, 0, w, h / 2f));
        nonHieroglyphic.put("v/", new Rectangle2D.Float(0, 0, w / 2f, h));
    }

    /**
     * @param g
     * @param code
     * @param angle
     * @param view
     * @see
     * jsesh.mdcDisplayer.drawingElements.HieroglyphsDrawer#draw(Graphics2D,
     * String, int, MDCView)
     */
    public void draw(Graphics2D g, String code, int angle, ViewBox view) {
        Graphics2D tmpG = (Graphics2D) g.create();

        code = normalizeCode(code);
        ShapeChar glyph = null;
        // If we want to use a small body font, try to find the glyph.
        if (isSmallBodyUsed()) {
            glyph = fontManager.getSmallBody(code);
        }
        // If we don't want a small body font, or the glyph was not available
        // there,
        // try the normal shape.
        if (glyph == null) {
            glyph = fontManager.get(code);
        }
        tmpG.scale(view.getXScale(), view.getYScale());
        if (glyph != null) {
            glyph.draw(tmpG, 0, 0, 1.0, 1.0, (float) (angle * Math.PI / 180));
        } else {
            // If the glyph wasn't found, write its code.
            Rectangle2D r = tmpG.getFont().getStringBounds(code,
                    new FontRenderContext(new AffineTransform(), true, true));
            // Get the actual width of the view:
            // double w = view.getWidth() / view.getXScale();
            // (this was probably an error... the scale is used to scale
            // subview...)
            double w = view.getWidth();
            // Scale the text to fit :
            tmpG.scale(w / r.getWidth(), w / r.getWidth());
            if (code.length() > 0) {
                tmpG.drawString(code, (float) -r.getMinX(), (float) -r
                        .getMinY());
            }
        }
        tmpG.dispose();
    }

    /**
     * @see jsesh.mdcDisplayer.drawingElements.HieroglyphsDrawer#getBBox(String)
     */
    public Rectangle2D getBBox(String code, int angle, boolean fixed) {
        Rectangle2D result = null;

        code = normalizeCode(code);

        // TODO : maybe use the bounding box x and y as dx,dy for the
        // sign ?
        // TODO : clean this a lot...
        if (angle == 0) {
            ShapeChar glyph = fontManager.get(code);
            if (glyph != null) {
                result = glyph.getBbox();
            } else if (nonHieroglyphic.containsKey(code)) {
                result = getNonHieroglyphic(code).getBounds2D();
            }
        } else if (angle != 0) {
            // TODO : centralize decisions about the rotations.
            // (rotation is found both here and in the drawing class).
            // Other point : getShape may call us, but only with angle = 0, so
            // there is no infinite recursion...
            Shape shape = getShape(code);
            AffineTransform rot = AffineTransform.getRotateInstance(angle
                    * Math.PI / 180f);
            shape = rot.createTransformedShape(shape);
            result = shape.getBounds2D();
        }

        return result;
    }

    private Shape getNonHieroglyphic(String code) {
        return nonHieroglyphic.get(code);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.HieroglyphsDrawer#getShape(java.lang.String)
     */
    public Shape getShape(String code) {
        code = normalizeCode(code);
        Shape result = null;
        ShapeChar glyph = fontManager.get(code);
        if (glyph != null) {
            result = glyph.getShape();
        } else {
            result = getBBox(code, 0, true);
        }
        return result;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.draw.HieroglyphsDrawer#getSignArea(java.lang.String,
	 * double, double, double, double, float, boolean)
     */
    public Area getSignArea(String code, double x, double y, double xscale,
            double yscale, int angle, boolean reversed) {
        code = normalizeCode(code);
        Area result = null;
        ShapeChar glyph = fontManager.get(code);
        if (glyph != null) // FIXME : use reversed !
        {
            result = glyph.getSignArea(x, y, xscale, yscale, angle * Math.PI
                    / 180.0);
        } else if (nonHieroglyphic.containsKey(code)) {
            Shape s = ShapeHelper.transformShape(x, y, xscale, yscale, angle
                    * Math.PI / 180.0, getNonHieroglyphic(code));
            result = new Area(s);
        } else {
            result = new Area();
        }
        return result;

    }

  
    @Override
    public boolean isKnown(String code) {
        code = normalizeCode(code);
        return (fontManager.get(code) != null || nonHieroglyphic
                .containsKey(code));
    }

    @Override
    public Optional<LigatureZone> getLigatureZone(int i, String code) {
        code = normalizeCode(code);
        ShapeChar glyph = fontManager.get(code);
        Optional<LigatureZone> result = Optional.empty();
        if (glyph != null) {
            if (!glyph.hasZones()) {
                LigatureZoneBuilder l = new LigatureZoneBuilder(glyph);
                for (int k = 0; k < 3; k++) {
                    glyph.setZone(k, l.getLigatureArea(k));
                }

            }
            LigatureZone z = glyph.getZone(i);
            if (z != null) {
                result = Optional.of(z);
            }
        }
        return result;
    }

    @Override
    public double getHeightOfA1() {
        return heightOfA1;
    }

    @Override
    public double getGroupUnitLength() {
        return getHeightOfA1() / 1000f; // Why was it 10000 ????????????
    }

    @Override
    public void setSmallBodyUsed(boolean smallBody) {
        this.smallBodyUsed = smallBody;
    }

    @Override
    public boolean isSmallBodyUsed() {
        return smallBodyUsed;
    }

    /**
     * Returns a normalized version of code. In particular, deals with codes
     * like '[[' and ']]'.
     *
     * @param code
     * @return
     */
    private String normalizeCode(String code) {
        if (normalizedCodesMap.containsKey(code)) {
            return normalizedCodesMap.get(code);
        } else {
            return code;
        }
    }
}
