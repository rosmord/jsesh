package jsesh.mdcDisplayer.draw;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import jsesh.mdc.constants.LexicalSymbolsUtils;
import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.lex.MDCShading;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.Cartouche;
import jsesh.mdc.model.ComplexLigature;
import jsesh.mdc.model.HRule;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.Modifier;
import jsesh.mdc.model.OptionsMap;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.Philology;
import jsesh.mdc.model.ShadingCode;
import jsesh.mdc.model.Superscript;
import jsesh.mdc.model.TopItemState;
import jsesh.mdc.model.ZoneStart;
import jsesh.mdc.utils.TranslitterationUtilities;
import jsesh.mdcDisplayer.drawingElements.CartoucheDrawerHelper;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.ShadingStyle;

/**
 * This file is free Software under the GNU LESSER GENERAL PUBLIC LICENCE.
 *
 *
 * (c) Serge Rosmorduc
 *
 * @author Serge Rosmorduc
 *
 */
public class SimpleElementDrawer extends ElementDrawer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareDrawing(DrawingSpecification drawingSpecifications) {
        super.prepareDrawing(drawingSpecifications);
        // shading = false;
        setDrawingState(new TopItemState());
    }

    /*
     * jsesh.mdc.model.ModelElementVisitor#visitAlphabeticText(jsesh.mdc.model.AlphabeticText)
     */
    @Override
    public void visitAlphabeticText(AlphabeticText t) {
        if (!postfix) {
            return;
        }
        if (t.getScriptCode() != ScriptCodes.COMMENT) {
            String text = t.getText();
            // TODO : support uppercase signs.
            if (t.getScriptCode() == 't') {
                text = TranslitterationUtilities.getActualTransliterationString(text, drawingSpecifications.getTransliterationEncoding());
            }

            if ("".equals(text)) {
                return;
            }
            g.setFont(drawingSpecifications.getFont(t.getScriptCode()));

            // g.drawString(text, 0, g.getFontMetrics().getAscent());
            FontRenderContext fontRenderContext = new FontRenderContext(
                    new AffineTransform(), true, true);
            // fontRenderContext= g.getFontRenderContext();
            TextLayout layout = new TextLayout(text, g.getFont(),
                    fontRenderContext);

            // The reference system is the view origin, but
            // layout.draw draws relatively to the text baseline, hence
            // the g.getFontMetrics().getAscent() here.
            // layout.draw(g, 0, g.getFontMetrics().getAscent());
            // IN THEORY, THIS IS THE CORRECT LINE.
            layout.draw(g, 0, layout.getAscent());
            //g.drawString(text, 0, layout.getAscent());

            // One day we might propose a caret drawing system ?
        }
    }

    /*
    * jsesh.mdc.model.ModelElementVisitor#visitCartouche(jsesh.mdc.model.Cartouche)
     *
     * NOTE: Cartouches are drawn larger than the typical cadrat Height, so that
     * the signs in the cartouche be as tall as the ones outside. The cartouche
     * positioning is done with setStartPoint(). If a Layout modifies the
     * cartouche's startPoint, it won't work. So we might be interested in
     * having a slightly richer model for views, with kinds of "alignment
     * points", usable for horizontal and vertical positionning.
     *
     *
     * TODO : use a simpler system for dimensions. Currently, the
     * "specification" class knows too many things, and it makes a good
     * realisation complicated.
     *
     */
    @Override
    public void visitCartouche(Cartouche c) {
        if (!postfix) {
            return;
        }
        CartoucheDrawerHelper helper = new CartoucheDrawerHelper(drawingSpecifications, currentTextDirection, currentTextOrientation, currentView, g);

        if (currentTextOrientation.isHorizontal()) {
            helper.visitHorizontalCartouche(c);
        } else {
            helper.visitVerticalCartouche(c);
        }
    }

    /**
     * @param h
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
     */
    @Override
    public void visitHieroglyph(Hieroglyph h) {
        if (!postfix) {
            if (h.getModifiers().hasInteger("shading")) {
                MDCShading shading = new MDCShading(""
                        + h.getModifiers().getInteger("shading"));
                doShade(shading.getShading());
            }
        }

        if (!postfix) {
            return;
        }
        for (Modifier m : h.getModifiers().asIterable()) {
            drawingSpecifications.getTagColor(m.getName()).ifPresent(
                    (c) -> {
                        g.setColor(c);
                    }
            );
        }

        if (h.getModifiers().getBoolean("red")) {
            g.setColor(drawingSpecifications.getRedColor());
        }
        if (h.getModifiers().getBoolean("i")) {
            g.setColor(drawingSpecifications.getGrayColor());
        }
        for (int i = 0; i < h.getModifiers().getNumberOfChildren(); i++) {
            Modifier mod = h.getModifiers().getModifierAt(i);
            Color c = drawingSpecifications.getColorForProperty(mod.getName());
            if (c != null) {
                g.setColor(c);
            }

        }
        switch (h.getType()) {
            case SymbolCodes.SMALLTEXT: {
                g.setFont(drawingSpecifications.getSuperScriptFont());

                String smallText = h.getSmallText();
                Dimension2D r = drawingSpecifications
                        .getSuperScriptDimensions(smallText);
                g.drawString(smallText, 0, (float) r.getHeight());
            }
            break;
            // Note : the only "special" codes are red points and probably
            // shades.
            // The other codes are dealt with by the HieroglyphsDrawer.
            case SymbolCodes.REDPOINT: {
                Color col = g.getColor();
                g.setColor(drawingSpecifications.getRedColor());
                g.scale(drawingSpecifications.getSignScale(), drawingSpecifications
                        .getSignScale());
                drawingSpecifications.getHieroglyphsDrawer().draw(g, h.getCode(),
                        0, currentView);
                g.setColor(col);
            }
            break;
            case SymbolCodes.HALFSPACE:
            case SymbolCodes.FULLSPACE:
                break;
            case SymbolCodes.FULLSHADE:
            case SymbolCodes.VERTICALSHADE:
            case SymbolCodes.HORIZONTALSHADE:
            case SymbolCodes.QUATERSHADE: {
                Rectangle2D.Float r = new Rectangle2D.Float(0, 0, currentView
                        .getWidth(), currentView.getHeight());
                Area area = new Area(r);
                shadeArea(area);
            }
            break;
            // NOTE : Obviously, it would be better to create some kind of "symbol"
            // class for signs which are not hieroglyphs.

            case SymbolCodes.BEGINSCRIBEADDITION:
            case SymbolCodes.ENDSCRIBEADDITION:
            case SymbolCodes.BEGINEDITORADDITION:
            case SymbolCodes.ENDEDITORADDITION:
            case SymbolCodes.BEGINEDITORSUPERFLUOUS:
            case SymbolCodes.ENDEDITORSUPERFLUOUS:
            case SymbolCodes.BEGINPREVIOUSLYREADABLE:
            case SymbolCodes.ENDPREVIOUSLYREADABLE:
            case SymbolCodes.BEGINERASE:
            case SymbolCodes.ENDERASE:
            case SymbolCodes.BEGINMINORADDITION:
            case SymbolCodes.ENDMINORADDITION:
            case SymbolCodes.BEGINDUBIOUS:
            case SymbolCodes.ENDDUBIOUS:
                // See below for a comment
                drawSign(h, 1);
                break;
            default:
                // Ok, now the baseSignScale stuff should not be there...
                float baseSignScale = drawingSpecifications.getSignScale();
                drawSign(h, baseSignScale);
                break;

        }
    }

    /**
     * Draw a hieroglyph using a hieroglyphicDrawer.
     *
     * This is overly complex. We should really have a new system where a) we
     * build view elements with all drawing information b) we draw them
     * mecanically.
     *
     * @param h : the sign to draw
     * @param baseSignScale a basic scale to apply to the original sign. This
     * could be considered as a font size, in a way.
     */
    private void drawSign(Hieroglyph h, float baseSignScale) {
        Graphics2D tmpG = (Graphics2D) g.create();
        boolean reversed = (h.isReversed() ^ currentTextDirection
                .equals(TextDirection.RIGHT_TO_LEFT));
        if (reversed) {
            // Note that the right side is at InternalWidth, not Width.
            // The following line did not work well when sign scaling wasn't
            // done through views.
            // Now, it seems to work.
            tmpG.transform(new AffineTransform(-1, 0, 0, 1, currentView
                    .getWidth(), 0));
        }

        // Actual drawing of the sign.
        if (drawingSpecifications.getHieroglyphsDrawer().isKnown(h.getCode())) {
            // scale the sign if needed.
            // Note that not all signs are scaled
            tmpG.scale(baseSignScale,
                    baseSignScale);

            // if the final scale is smaller than the selected
            // "smallBodyLimit", use the "small body font".
            double resultingA1Height = drawingSpecifications
                    .getHieroglyphsDrawer().getHeightOfA1()
                    * tmpG.getTransform().getScaleY()
                    / drawingSpecifications.getGraphicDeviceScale();
            if (resultingA1Height < drawingSpecifications
                    .getSmallBodyScaleLimit()) {
                drawingSpecifications.getHieroglyphsDrawer()
                        .setSmallBodyUsed(true);
            } else {
                drawingSpecifications.getHieroglyphsDrawer()
                        .setSmallBodyUsed(false);
            }

            // Actual drawing.
            drawingSpecifications.getHieroglyphsDrawer().draw(tmpG, h.getCode(),
                    h.getAngle(), currentView);
            // Restore environment.
            drawingSpecifications.getHieroglyphsDrawer().setSmallBodyUsed(
                    false);
            tmpG.scale(1 / baseSignScale,
                    1 / baseSignScale);
        } else {
            // Sign not found : draw its code.
            tmpG.setFont(drawingSpecifications.getSuperScriptFont());
            Color color = tmpG.getColor();
            tmpG.setColor(drawingSpecifications.getRedColor());
            Dimension2D r = drawingSpecifications
                    .getSuperScriptDimensions(h.getCode());
            tmpG.drawString(h.getCode(), 0, (float) r.getHeight());
            tmpG.setColor(color);
        }
        tmpG.dispose();
    }

    /*
     * 
     * jsesh.mdc.model.ModelElementVisitor#visitHRule(jsesh.mdc.model.HRule)
     */
    @Override
    public void visitHRule(HRule h) {
        if (!postfix) {
            return;
        }
        if (h.getType() == 'L') {
            g.setStroke(drawingSpecifications.getWideStroke());
        } else {
            g.setStroke(drawingSpecifications.getFineStroke());
        }
        g.draw(new Line2D.Float(h.getStartPos()
                * drawingSpecifications.getTabUnitWidth(), 0, h.getEndPos()
                * drawingSpecifications.getTabUnitWidth(), 0));
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.draw.ElementDrawer#visitPageBreak(jsesh.mdc.model.
	 * PageBreak)
     */
    @Override
    public void visitPageBreak(PageBreak b) {
        if (!postfix) {
            return;
        }
        if (!isPaged()) {
            g.setStroke(drawingSpecifications.getFineStroke());
            g.draw(new Line2D.Float(-10000f, 0, 10000f, 0));
        }
    }

    /*
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitPhilology(jsesh.mdc.model.Philology)
     */
    @Override
    public void visitPhilology(Philology p) {
        if (!postfix) {
            return;
        }
        try {

            drawBracket(p.getType() * 2 + 1, currentView.getWidth()
                    - drawingSpecifications.getPhilologyWidth(p.getType()), 0);
            drawBracket(p.getType() * 2, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawBracket(int code, float x, float y) {
        Font f = drawingSpecifications.getFont('l');

        // Save current transformation.
        // AffineTransform old= g.getTransform();
        Graphics2D tmpG = (Graphics2D) g.create();
        tmpG.setFont(f);
        tmpG.translate(x, y);
        String s = LexicalSymbolsUtils.getStringForPhilology(code);
        Rectangle2D d = drawingSpecifications.getTextDimensions('l', s);

        double scalex = drawingSpecifications.getPhilologyWidth(code)
                / d.getWidth();

        double scaley = currentView.getHeight() / d.getHeight();
        // scaley= 4;
        tmpG.scale(scalex, scaley);

        tmpG.drawString(s, (float) (-d.getMinX() * scalex), (float) ((d
                .getHeight() - d.getMaxY()) * scaley));

        // tmpG.drawString(s, 0, g.getFontMetrics().getAscent());
        // Restore :
        // g.setTransform(old);
        tmpG.dispose();
    }

    /*
     *
     * jsesh.mdc.model.ModelElementVisitor#visitSuperScript(jsesh.mdc.model.Superscript)
     */
    @Override
    public void visitSuperScript(Superscript s) {
        if (!postfix) {
            return;
        }
        String text = s.getText();
        Dimension2D dims = drawingSpecifications.getSuperScriptDimensions(text);
        g.setFont(drawingSpecifications.getSuperScriptFont());
        g.drawString(text, 0, g.getFontMetrics().getAscent());
        g.setStroke(drawingSpecifications.getFineStroke());
        g
                .draw(new Line2D.Float((float) dims.getWidth() / 2.0f,
                        (float) dims.getHeight()
                        + drawingSpecifications.getSmallSkip(),
                        (float) dims.getWidth() / 2.0f, currentView
                        .getInternalHeight()));

    }

    /*
	 * Note that the coordinate system in any of these methods is scaled !
     */
    @Override
    public void visitCadrat(Cadrat c) {
        if (postfix && isShadeAfter()) {
            doShade(c.getShading());
        }
        if (!postfix && !isShadeAfter()) {
            doShade(c.getShading());
        }
    }

    /**
     * Shade the current view.
     *
     * @param localShading the shading code.
     */
    private void doShade(int localShading) {
        // int localshading = c.getShading();
        // NOTE : we use a very simple system for global shading.
        if (isShaded()) {
            localShading = ShadingCode.FULL;
        }

        // Don't try to shade what is not shaded !
        if (localShading == ShadingCode.NONE) {
            return;
        }

        boolean fillTopLeft;
        boolean fillTopRight ;
        boolean fillBottomLeft;
        boolean fillBottomRight;

        Rectangle2D.Double r = new Rectangle2D.Double(0, 0, currentView
                .getWidth() / 2, currentView.getHeight() / 2);
        // IMPORTANT : we use the alpha chanel for transparency.
        // If this doesn't work for some implementations of Graphics2D,
        // We should have a "B plan".
        // Color gray = drawingSpecifications.getGrayColor();
        // Paint currentPaint = g.getPaint();
        // g.setPaint(gray);

        if (currentView.getParent() != null
                && currentView.getParent().getDirection().isLeftToRight()) {
            fillTopLeft = (localShading & ShadingCode.TOP_START) != 0;
            fillTopRight = (localShading & ShadingCode.TOP_END) != 0;
            fillBottomLeft = (localShading & ShadingCode.BOTTOM_START) != 0;
            fillBottomRight = (localShading & ShadingCode.BOTTOM_END) != 0;
        } else {
            fillTopLeft = (localShading & ShadingCode.TOP_END) != 0;
            fillTopRight = (localShading & ShadingCode.TOP_START) != 0;
            fillBottomLeft = (localShading & ShadingCode.BOTTOM_END) != 0;
            fillBottomRight = (localShading & ShadingCode.BOTTOM_START) != 0;
        }

        Area shadedArea = new Area();

        if (fillTopLeft) {
            shadedArea.add(new Area(r));
        }
        if (fillTopRight) {
            r.x = currentView.getWidth() / 2;
            shadedArea.add(new Area(r));
        }
        if (fillBottomLeft) {
            r.x = 0;
            r.y = currentView.getHeight() / 2;
            shadedArea.add(new Area(r));
        }
        if (fillBottomRight) {
            r.x = currentView.getWidth() / 2;
            r.y = currentView.getHeight() / 2;
            shadedArea.add(new Area(r));
        }

        shadeArea(shadedArea);

        // Fill the space between this view and the next one.
        // Note that it would be probably better to use a dedicated method
        // for this (r.width computation could
        // be much simpler, and things would be clearer).
        if (currentView.getNext() != null) {
            MDCView nextView = currentView.getNext();
            // if vertical borders are at the same level
            // TODO : adapt for columns
            if (isShaded() && currentTextOrientation.isHorizontal()
                    && currentView.nextIsHorizontallyAdjacent()) {
                // Shade the inter-cadrat space
                r.x = currentView.getWidth();
                r.y = 0f;
                r.height = currentView.getHeight();

                // The width : we need to express it in the current system.
                double outerWidth = nextView.getPosition().x
                        - currentView.getPosition().getX()
                        - currentView.getWidth();

                r.width = outerWidth / currentView.getXScale();

                // g.fill(r);
                shadeArea(new Area(r));
            }
        }
        // g.setPaint(currentPaint);

    }

    /**
     * Shade an area. Utility method (will move out of there).
     *
     * @param area
     */
    private void shadeArea(Area area) {
        // Easy way: paint the area in grey...
        // Complex way : intersect the area with line hatching...

        // Prevent us from messing with the area (not that important currently,
        // but who knows ?)
        if (drawingSpecifications.getShadingStyle().equals(
                ShadingStyle.GRAY_SHADING)) {
            Color col = g.getColor();
            g.setColor(drawingSpecifications.getGrayColor());
            g.fill(area);
            g.setColor(col);
        } else {
            area = (Area) area.clone();
            // Let's work in a protected graphic environment.
            Graphics2D tempG = (Graphics2D) g.create();

            // Move the area back into the page coordinate space...
            getPageCoordinateSystem().moveAreaToPageReferenceSystem(g, area);

            // sets the coordinate space to page coordinates.
            getPageCoordinateSystem().moveBackToPageReferenceSystem(tempG);

            new Shader().shadeArea(tempG, area);

            tempG.dispose();
        }
    }

    public void visitComplexLigature(ComplexLigature ligature) {

    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdc.model.ModelElementVisitor#visitZoneStart(jsesh.mdc.model.ZoneStart
	 * )
     */
    @Override
    public void visitZoneStart(ZoneStart start) {

    }

    /*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdc.model.ModelElementVisitor#visitOptionList(jsesh.mdc.model.
	 * OptionsMap)
     */
    public void visitOptionList(OptionsMap list) {

    }

    private boolean isShaded() {
        return getDrawingState().isShaded();
    }

}
