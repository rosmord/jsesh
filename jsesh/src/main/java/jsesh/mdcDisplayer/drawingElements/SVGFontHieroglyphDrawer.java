/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.mdcDisplayer.drawingElements;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;

import jsesh.hieroglyphs.data.coreMdC.ManuelDeCodage;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.hieroglyphs.signshape.LigatureZone;
import jsesh.hieroglyphs.signshape.LigatureZoneBuilder;
import jsesh.hieroglyphs.signshape.ShapeChar;
import jsesh.mdcDisplayer.layout.ExplicitPosition;
import jsesh.mdcDisplayer.mdcView.ViewBox;
import jsesh.swing.utils.ShapeHelper;

/**
 * A Hieroglyphic drawer which takes its input from (SVG) fonts. Our default
 * implementation of hieroglyphs drawing and measuring.
 */
class SVGFontHieroglyphDrawer implements BasicSignDrawer {

    /*
	 * IMPORTANT IMPLEMENTATION NOTE: this implementation of the hieroglyphic
	 * drawers relies on a relatively simple encoding scheme for glyphs. Hence,
	 * we will rewrite codes to deal with special codes like '[['.
     */
    /**
     * Code used as "default" hieroglyph to compute sizes.
     */
    private static final String DEFAULT_CODE = "A1";

    
    /**
     * The manager which associates codes with actual glyphs.
     */
    private final HieroglyphShapeRepository fontManager;

   

    /**
     * The height of the A1 sign, used as a reference for scaling.
     */
    private float heightOfA1 = 0;

    /**
     * The catalogue of predefined ligatures inherited from tksesh.
     */
    private final TkseshLigatureCatalogue tkseshLigatureCatalogue = TkseshLigatureCatalogue.getInstance();

    /**
     * Shading code sizes     
     */
    private final ShadingCodeCatalogue shadingCodeCatalogue;

    /**
     * Builds a new SVGFontHieroglyphDrawer with the given font manager.
     * 
     * The font manager should provide at least the A1 sign, which is used as a reference for scaling.
     * @param hieroglyphicFontManager
     */
    public SVGFontHieroglyphDrawer(HieroglyphShapeRepository hieroglyphicFontManager) {
    	this.fontManager = hieroglyphicFontManager;        

        // Use The A1 sign as base
        // TODO : perhaps give the HieroglyphicDrawer some informations about
        // were it draws,
        // so that, for instance, shading can be based on expected cadrat
        // dimensions.
        Rectangle2D specA1 = fontManager.get(DEFAULT_CODE).getBbox();
        float w = (float) specA1.getWidth();
        float h = (float) specA1.getHeight();
        heightOfA1 = h;
        shadingCodeCatalogue = new ShadingCodeCatalogue(w,h);

       
    }

    @Override
    public void draw(Graphics2D g, String code, int angle, ViewBox view, HieroglyphBodySize bodySize) {
        Graphics2D tmpG = (Graphics2D) g.create();

        ShapeChar glyph = null;
        // If we want to use a small body font, try to find the glyph.
        if (bodySize == HieroglyphBodySize.SMALL) {
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

    @Override
    public Rectangle2D getBBox(String code, int angle, boolean fixed) {
        Rectangle2D result = null;

        if (angle == 0) {
            ShapeChar glyph = fontManager.get(code);
            if (glyph != null) {
                result = glyph.getBbox();
            } else if (shadingCodeCatalogue.containsCode(code)) {
                result = getNonHieroglyphic(code).getBounds2D();
            }
        } else if (angle != 0) {
            Shape shape = getShape(code);
            AffineTransform rot = AffineTransform.getRotateInstance(angle
                    * Math.PI / 180f);
            shape = rot.createTransformedShape(shape);
            result = shape.getBounds2D();
        }

        return result;
    }
    
    private Shape getNonHieroglyphic(String code) {
        return shadingCodeCatalogue.getSizeForCode(code);
    }


    @Override
    public Shape getShape(String code) {
        Shape result = null;
        ShapeChar glyph = fontManager.get(code);
        if (glyph != null) {
            result = glyph.getShape();
        } else {
            result = getBBox(code, 0, true);
        }
        return result;
    }

  
    @Override
    public Area getSignArea(String code, double x, double y, double xscale,
            double yscale, int angle, boolean reversed) {        
        Area result = null;
        ShapeChar glyph = fontManager.get(code);
        if (glyph != null) // FIXME : use reversed !
        {
            result = glyph.getSignArea(x, y, xscale, yscale, angle * Math.PI
                    / 180.0);
        } else if (shadingCodeCatalogue.containsCode(code)) {
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
        return (fontManager.get(code) != null || shadingCodeCatalogue
                .containsCode(code));
    }

    @Override
    public Optional<LigatureZone> getLigatureZone(int i, String code) {
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

    public double getHeightOfA1() {
        return heightOfA1;
    }

    public double getGroupUnitLength() {
        return getHeightOfA1() / 1000f; // Why was it 10000 ????????????
    }

    @Override
    public List<ExplicitPosition> getPositions(List<String> codes) {
        ManuelDeCodage manuelDeCodage = ManuelDeCodage.getInstance();
        List<String> normalizedCodes = codes.stream().map(c -> manuelDeCodage.getCanonicalCode(c))
            .toList();
        return tkseshLigatureCatalogue.get(normalizedCodes);
    }
    

}
