/**
 * This file is free Software under the GNU LESSER GENERAL PUBLIC LICENCE.
 */
package jsesh.render.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.text.Bidi;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import jsesh.render.style.JSeshStyle;
import jsesh.render.style.ShadingMode;
import jsesh.sharedvocabulary.KerningAxis;
import jsesh.sharedvocabulary.RunningAnnotationKind;
import jsesh.model.constants.LexicalSymbolsUtils;
import jsesh.model.constants.ScriptCode;
import jsesh.model.constants.SymbolCodes;
import jsesh.model.constants.TextDirection;
import jsesh.model.AlphabeticCharacter;
import jsesh.model.Cadrat;
import jsesh.model.Cartouche;
import jsesh.model.ComplexLigature;
import jsesh.model.HRule;
import jsesh.model.Hieroglyph;
import jsesh.model.Kerning;
import jsesh.model.LineBreak;
import jsesh.model.Modifier;
import jsesh.model.OptionsMap;
import jsesh.model.PageBreak;
import jsesh.model.Philology;
import jsesh.model.ShadingCode;
import jsesh.model.Superscript;
import jsesh.model.RunningAnnotation;
import jsesh.model.Tab;
import jsesh.model.TabLine;
import jsesh.model.TopItemState;
import jsesh.model.Zone;
import jsesh.model.ZoneParameters;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.elements.HieroglyphBodySize;
import jsesh.render.elements.HieroglyphDrawer;
import jsesh.render.elements.PhilologyHelper;
import jsesh.render.elements.SignPlacement;
import jsesh.render.elements.cartouche.CartoucheDrawerHelper;
import jsesh.render.view.MDCView;

/// The actual element drawer.
/// 
/// Defines how actual elements are draw.
/// 
/// TODO: this is a bit clumsy. We use inheritance, were delegation would be better.
/// 
/// Plus, this is the only actual implementation of ElementDrawer, so we could merge the two classes. 
/// The only problem is how responsabilities should be split.
/// 
///  (c) Serge Rosmorduc
///  @author Serge Rosmorduc
///  
public class DefaultElementDrawer extends ElementDrawer {
    HieroglyphDrawer hieroglyphsDrawer;

    @Override
    public void prepareDrawing(JSeshRenderContext renderContext, JSeshTechRenderContext techRenderContext) {
        super.prepareDrawing(renderContext, techRenderContext);
        // Sets the drawing state to its default: no shading, no red color.
        setDrawingState(new TopItemState());
        this.hieroglyphsDrawer = new HieroglyphDrawer(renderContext.hieroglyphShapeRepository());
    }

    @Override
    public void cleanup() {        
        super.cleanup();
        // Ensure we need to call prepareDrawing before drawing again.
        this.hieroglyphsDrawer = null;
    }

    /// Draws one character. Its position in its run of text (with kerning)
    /// was computed by the layout; the character is drawn at the origin of its
    /// view, with its baseline at the font's ascent.
    @Override
    public void visitAlphabeticCharacter(AlphabeticCharacter c) {
        if (!postfix) {
            return;
        }
        JSeshStyle jseshStyle = getJSeshStyle();
        String text = c.getDisplayString(jseshStyle.fonts().transliterationEncoding());
        Font font = jseshStyle.fonts().getFont(c.getScript());
        g.setFont(font);
        // Use the same font render context as the layout, so that the
        // characters' advances match their views.
        FontRenderContext frc = getTechRenderContext().fontRenderContext();
        if (!drawShapedCharacter(c, font, text, frc)) {
            TextLayout layout = new TextLayout(text, font, frc);
            layout.draw(g, 0, layout.getAscent());
        }
    }

    /// Draws a character which belongs to a run containing right-to-left text.
    ///
    /// Such characters can't be drawn alone: arabic letters take different
    /// forms depending on their neighbours, and mixed directions are reordered.
    /// So the whole run is laid out (as it was measured by [jsesh.render.layout.TextRun]), and
    /// only the part belonging to this character is drawn, using a clip.
    ///
    /// @return false if the character is not in such a run (nothing was
    /// drawn).
    private boolean drawShapedCharacter(AlphabeticCharacter c, Font font, String text, FontRenderContext frc) {
        // Gather the run around the current view.
        MDCView first = currentView;
        while (first.getPrevious() != null && first.getPrevious().getModel() instanceof AlphabeticCharacter p
                && p.isSameScriptAs(c)) {
            first = first.getPrevious();
        }
        var encoding = getJSeshStyle().fonts().transliterationEncoding();
        StringBuilder runText = new StringBuilder();
        int pieceStart = -1;
        int pieceEnd = -1;
        for (MDCView v = first; v != null && v.getModel() instanceof AlphabeticCharacter p
                && p.isSameScriptAs(c); v = v.getNext()) {
            if (v == currentView) {
                pieceStart = runText.length();
            }
            runText.append(p.getDisplayString(encoding));
            if (v == currentView) {
                pieceEnd = runText.length();
            }
        }
        boolean hasRtl = runText.codePoints().anyMatch(cp -> {
            byte d = Character.getDirectionality(cp);
            return d == Character.DIRECTIONALITY_RIGHT_TO_LEFT || d == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
        });
        if (!hasRtl || pieceStart < 0 || pieceEnd <= pieceStart) {
            return false;
        }
        String all = runText.toString();
        boolean baseRtl = !new Bidi(all, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).baseIsLeftToRight();
        AttributedString as = new AttributedString(all);
        as.addAttribute(TextAttribute.FONT, font);
        as.addAttribute(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        as.addAttribute(TextAttribute.RUN_DIRECTION,
                baseRtl ? TextAttribute.RUN_DIRECTION_RTL : TextAttribute.RUN_DIRECTION_LTR);
        TextLayout layout = new TextLayout(as.getIterator(), frc);
        Rectangle2D pieceBounds = layout.getLogicalHighlightShape(pieceStart, pieceEnd).getBounds2D();
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.clip(new Rectangle2D.Double(0, -layout.getAscent() * 2, currentView.getWidth(),
                    layout.getAscent() * 4 + layout.getDescent()));
            layout.draw(g2, (float) -pieceBounds.getX(), layout.getAscent());
        } finally {
            g2.dispose();
        }
        return true;
    }

    /*
     * jsesh.model.ModelElementVisitor#visitCartouche(jsesh.model.Cartouche)
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
        JSeshStyle jseshStyle = getJSeshStyle();
        if (!postfix) {
            return;
        }
        CartoucheDrawerHelper helper = new CartoucheDrawerHelper(jseshStyle, currentTextDirection,
                currentTextOrientation, currentView, g);

        if (currentTextOrientation.isHorizontal()) {
            helper.visitHorizontalCartouche(c);
        } else {
            helper.visitVerticalCartouche(c);
        }
    }

    /**
     * @param h
     * @see
     *      jsesh.model.ModelElementVisitor#visitHieroglyph(jsesh.model.Hieroglyph)
     */
    @Override
    public void visitHieroglyph(Hieroglyph h) {
        JSeshStyle jseshStyle = getJSeshStyle();

        if (!postfix) {
            if (h.getModifiers().hasInteger("shading")) {
                doShade(ShadingCode.parse("" + h.getModifiers().getInteger("shading")));
            }
        }

        if (!postfix) {
            return;
        }
        for (Modifier m : h.getModifiers().asIterable()) {
            jseshStyle.painting().tagColor(m.getName()).ifPresent(
                    (c) -> {
                        g.setColor(c);
                    });
        }

        // With "underline for red", red signs inside a quadrat stay black.
        if (h.getModifiers().getBoolean("red") && !jseshStyle.painting().underlineRed()) {
            g.setColor(jseshStyle.painting().redColor());
        }
        if (h.getModifiers().getBoolean("i")) {
            g.setColor(jseshStyle.painting().grayColor());
        }

        switch (h.getType()) {
            case SymbolCodes.SMALLTEXT: {
                g.setFont(jseshStyle.fonts().superScriptFont());

                String smallText = h.getSmallText();
                Dimension2D r = jseshStyle.fonts()
                        .superScriptDimensions(getTechRenderContext(), smallText);
                g.drawString(smallText, 0, (float) r.getHeight());
            }
                break;
            // Note : the only "special" codes are red points and probably
            // shades.
            // The other codes are dealt with by the HieroglyphsDrawer.
            case SymbolCodes.REDPOINT: {
                Graphics2D tempG = (Graphics2D) g.create();
                double scale = hieroglyphsDrawer.scaleFromFontToStyle(jseshStyle);
                tempG.setColor(jseshStyle.painting().redColor());
                tempG.scale(scale, scale);
                hieroglyphsDrawer.draw(tempG, h.getCode(), 0, currentView, HieroglyphBodySize.STANDARD);
                tempG.dispose();
            }
                break;
            case SymbolCodes.HALFSPACE:
            case SymbolCodes.FULLSPACE:
                break;
            case SymbolCodes.FULLSHADE:
            case SymbolCodes.VERTICALSHADE:
            case SymbolCodes.HORIZONTALSHADE:
            case SymbolCodes.QUATERSHADE: {
                Rectangle2D.Double r = new Rectangle2D.Double(0, 0, currentView
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
                double baseSignScale = hieroglyphsDrawer.scaleFromFontToStyle(jseshStyle);
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
     * @param h             : the sign to draw
     * @param baseSignScale a basic scale to apply to the original sign. This
     *                      could be considered as a font size, in a way.
     */
    private void drawSign(Hieroglyph h, double baseSignScale) {
        JSeshStyle jseshStyle = getJSeshStyle();

        Graphics2D tmpG = (Graphics2D) g.create();
        boolean reversed = (h.isReversed() ^ currentTextDirection
                .equals(TextDirection.RIGHT_TO_LEFT));
        if (reversed) {
            // Note that the right side is at InternalWidth, not Width.
            // The following line did not work well when sign scaling wasn't
            // done through views.
            // Now, it seems to work.
            tmpG.transform(SignPlacement.mirror(currentView.getWidth()));
        }

        // Actual drawing of the sign.
        if (hieroglyphsDrawer.isKnown(h.getCode())) {
            // scale the sign if needed.
            // Note that not all signs are scaled
            tmpG.scale(baseSignScale,
                    baseSignScale);

            // if the final scale is smaller than the selected
            // "smallBodyLimit", use the "small body font".
            double resultingA1Height = hieroglyphsDrawer.getHeightOfA1()
                    * tmpG.getTransform().getScaleY()
                    / getTechRenderContext().graphicDeviceScale();

            HieroglyphBodySize bodySize;
            if (resultingA1Height < jseshStyle.geometry()
                    .smallBodyScaleLimit()) {
                bodySize = HieroglyphBodySize.SMALL;
            } else {
                bodySize = HieroglyphBodySize.STANDARD;
            }

            // Actual drawing.
            hieroglyphsDrawer.draw(tmpG, h.getCode(),
                    h.getAngle(), currentView, bodySize);
            tmpG.scale(1 / baseSignScale,
                    1 / baseSignScale);
        } else {
            // Sign not found : draw its code.
            tmpG.setFont(jseshStyle.fonts().superScriptFont());
            Color color = tmpG.getColor();
            tmpG.setColor(jseshStyle.painting().redColor());
            Dimension2D r = jseshStyle.fonts()
                    .superScriptDimensions(getTechRenderContext(),h.getCode());
            tmpG.drawString(h.getCode(), 0, (float) r.getHeight());
            tmpG.setColor(color);
        }
        tmpG.dispose();
    }

    /*
     * 
     * jsesh.model.ModelElementVisitor#visitHRule(jsesh.model.HRule)
     */
    @Override
    public void visitHRule(HRule h) {
        JSeshStyle jseshStyle = getJSeshStyle();
        if (!postfix) {
            return;
        }
        if (h.getType() == 'L') {
            g.setStroke(jseshStyle.geometry().wideStroke());
        } else {
            g.setStroke(jseshStyle.geometry().fineStroke());
        }
        // The view has the width of the text (see Layout#visitHRule); the
        // positions of the rule are percents of it, from the start of the
        // text: the right edge in right-to-left text.
        double width = currentView.getWidth();
        double start = h.startFraction();
        double end = h.endFraction();
        if (currentTextDirection == TextDirection.RIGHT_TO_LEFT) {
            double mirroredStart = 1 - end;
            end = 1 - start;
            start = mirroredStart;
        }
        g.draw(new Line2D.Double(start * width, 0, end * width, 0));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jsesh.render.draw.ElementDrawer#visitPageBreak(jsesh.model.
     * PageBreak)
     */
    /// Kerning operators are invisible, unless kerning marks are shown. The mark,
    /// `>|<` for `%hkern`, turned for `%vkern`, is centred on the view, which has no size.
    @Override
    public void visitKerning(Kerning kerning) {
        if (!postfix || !isShowKerningMarks()) {
            return;
        }
        JSeshStyle jseshStyle = getJSeshStyle();
        double half = jseshStyle.geometry().maxCadratHeight() / 6.0;
        double tip = half / 2.0;
        // Drawn for %hkern; coordinates are swapped for %vkern.
        double[][] lines = {
                { 0, -half, 0, half },
                { -2 * tip, -tip / 2, -tip / 2, 0 }, { -tip / 2, 0, -2 * tip, tip / 2 },
                { 2 * tip, -tip / 2, tip / 2, 0 }, { tip / 2, 0, 2 * tip, tip / 2 } };
        boolean horizontal = kerning.getAxis() == KerningAxis.HORIZONTAL;
        Path2D.Double mark = new Path2D.Double();
        for (double[] l : lines) {
            if (horizontal) {
                mark.moveTo(l[0], l[1]);
                mark.lineTo(l[2], l[3]);
            } else {
                mark.moveTo(l[1], l[0]);
                mark.lineTo(l[3], l[2]);
            }
        }
        g.setColor(jseshStyle.painting().cursorColor());
        g.setStroke(jseshStyle.geometry().fineStroke());
        g.draw(mark);
    }

    /// Explicit line breaks are invisible, unless line break marks are shown. The mark is
    /// a "return" arrow (down, then back towards the start of the line), drawn after the
    /// last sign of the line, and vertically centred on the view. It follows the text
    /// direction, and is turned for vertical text. Lines which are merely wrapped have
    /// no `LineBreak` and get no mark.
    @Override
    public void visitLineBreak(LineBreak b) {
        if (!postfix || !isShowLineBreakMarks()) {
            return;
        }
        JSeshStyle jseshStyle = getJSeshStyle();
        double unit = jseshStyle.geometry().maxCadratHeight() / 12.0;
        boolean horizontal = currentTextOrientation.isHorizontal();
        // In horizontal text, the view is as high as a quadrat, and has no width.
        // In vertical text, it is as wide as a quadrat, and (almost) has no height.
        double cross = (horizontal ? currentView.getHeight() : currentView.getWidth()) / 2.0;
        // Direction of the text. For vertical text, the mark is the horizontal one, transposed.
        double dir = (horizontal && currentTextDirection == TextDirection.RIGHT_TO_LEFT) ? -1.0 : 1.0;
        double x = 3 * unit * dir;
        // The return arrow: down the first stroke, back along the second, with the head at the end.
        double[][] lines = {
                { x, cross - 2 * unit, x, cross },
                { x, cross, 0, cross },
                { 0, cross, unit * dir, cross - unit },
                { 0, cross, unit * dir, cross + unit } };
        Path2D.Double mark = new Path2D.Double();
        for (double[] l : lines) {
            if (horizontal) {
                mark.moveTo(l[0], l[1]);
                mark.lineTo(l[2], l[3]);
            } else {
                mark.moveTo(l[1], l[0]);
                mark.lineTo(l[3], l[2]);
            }
        }
        g.setColor(jseshStyle.painting().cursorColor());
        g.setStroke(jseshStyle.geometry().fineStroke());
        g.draw(mark);
    }

    /// Dynamic tabs are invisible, unless tabs are shown. The mark is a blue
    /// line across the line (or column) at the tab stop, with a small flag
    /// towards the text which follows, and the stop index just above the line
    /// (or on the left of the column), where the signs don't hide it.
    @Override
    public void visitTab(Tab tab) {
        if (!postfix || !isShowTabs()) {
            return;
        }
        JSeshStyle jseshStyle = getJSeshStyle();
        boolean horizontal = currentTextOrientation.isHorizontal();
        double quadrat = jseshStyle.geometry().maxCadratHeight();
        double length = horizontal ? quadrat : jseshStyle.geometry().maxCadratWidth();
        g.setColor(TAB_COLOR);
        g.setStroke(jseshStyle.geometry().fineStroke());
        Path2D.Double mark = new Path2D.Double();
        double flag = quadrat / 8.0;
        double dir = (horizontal && currentTextDirection == TextDirection.RIGHT_TO_LEFT) ? -1.0 : 1.0;
        if (horizontal) {
            mark.moveTo(0, 0);
            mark.lineTo(0, length);
            mark.moveTo(0, 0);
            mark.lineTo(flag * dir, 0);
        } else {
            mark.moveTo(0, 0);
            mark.lineTo(length, 0);
            mark.moveTo(0, 0);
            mark.lineTo(0, flag);
        }
        g.draw(mark);
        String label = Integer.toString(TabLine.stopIndexOf(tab));
        Font previousFont = g.getFont();
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 1).deriveFont((float) (quadrat / 4.0)));
        double labelWidth = g.getFontMetrics().getStringBounds(label, g).getWidth();
        double ascent = g.getFontMetrics().getAscent();
        double descent = g.getFontMetrics().getDescent();
        // The index is outside the line (or column), so that the signs don't hide it.
        if (horizontal) {
            double x = dir > 0 ? 0 : -labelWidth;
            g.drawString(label, (float) x, (float) -descent);
        } else {
            g.drawString(label, (float) (-labelWidth - flag / 4.0), (float) ascent);
        }
        g.setFont(previousFont);
    }

    /// Draws a running annotation: a line along the text, across the middle of
    /// its view. An haplography has an arrow head at each end, and a lacuna is
    /// dashed. In columns, the line is vertical.
    @Override
    public void visitRunningAnnotation(RunningAnnotation annotation) {
        if (!postfix) {
            return;
        }
        JSeshStyle jseshStyle = getJSeshStyle();
        boolean horizontal = currentTextOrientation.isHorizontal();
        double length = horizontal ? currentView.getWidth() : currentView.getHeight();
        double across = (horizontal ? currentView.getHeight() : currentView.getWidth()) / 2.0;
        if (length <= 0) {
            return;
        }
        double lineWidth = jseshStyle.geometry().fineLineWidth();
        // Coordinates are (along, across); swapped for columns.
        Path2D.Double line = new Path2D.Double();
        if (annotation.getKind() == RunningAnnotationKind.HAPLOGRAPHY) {
            double head = Math.min(jseshStyle.geometry().maxCadratHeight() / 5.0, length / 2.0);
            g.setStroke(jseshStyle.geometry().fineStroke());
            line.moveTo(0, across);
            line.lineTo(length, across);
            line.moveTo(head, across - head / 2.0);
            line.lineTo(0, across);
            line.lineTo(head, across + head / 2.0);
            line.moveTo(length - head, across - head / 2.0);
            line.lineTo(length, across);
            line.lineTo(length - head, across + head / 2.0);
        } else {
            double dash = Math.max(lineWidth * 4.0, jseshStyle.geometry().maxCadratHeight() / 20.0);
            g.setStroke(new BasicStroke((float) lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
                    new float[] { (float) dash, (float) dash }, 0.0f));
            line.moveTo(0, across);
            line.lineTo(length, across);
        }
        if (!horizontal) {
            line.transform(new AffineTransform(0, 1, 1, 0, 0, 0));
        }
        g.draw(line);
    }

    /// The colour of the marks of dynamic tabs.
    private static final Color TAB_COLOR = new Color(0x1E, 0x64, 0xE6);

    /// Draws the frame of the zone, if it has one, and its limits, if asked
    /// to (for editors).
    @Override
    public void visitZone(Zone zone) {
        if (!postfix) {
            return;
        }
        JSeshStyle jseshStyle = getJSeshStyle();
        if (zone.getParameters().frame()) {
            double lineWidth = jseshStyle.geometry().cartoucheLineWidth();
            g.setStroke(new BasicStroke((float) lineWidth));
            g.draw(new Rectangle2D.Double(lineWidth / 2.0, lineWidth / 2.0,
                    currentView.getWidth() - lineWidth, currentView.getHeight() - lineWidth));
        }
        if (zone.getParameters().separator() != ZoneParameters.UNSET) {
            drawSeparators(zone, jseshStyle);
        }
        if (isShowZoneLimits()) {
            double dash = jseshStyle.geometry().maxCadratHeight() / 6.0;
            g.setColor(jseshStyle.painting().cursorColor());
            g.setStroke(new BasicStroke((float) jseshStyle.geometry().fineLineWidth(), BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, new float[] { (float) dash, (float) dash }, 0.0f));
            g.draw(new Rectangle2D.Double(0, 0, currentView.getWidth(), currentView.getHeight()));
        }
    }

    /// Draws the rules between the lines (or columns) of a zone, over its
    /// whole inner width (or height).
    private void drawSeparators(Zone zone, JSeshStyle jseshStyle) {
        MDCView content = currentView.getFirstSubView();
        double[] positions = content.getSeparators();
        if (positions.length == 0) {
            return;
        }
        // The positions are in the coordinates of the content, which a zone
        // with a scale scales.
        double scale = zone.getParameters().scaleFactor();
        double thickness = zone.getParameters().separator() * jseshStyle.geometry().percentUnit() * scale;
        boolean horizontal = content.getOrientation() == null || content.getOrientation().isHorizontal();
        double x0 = content.getPosition().x;
        double y0 = content.getPosition().y;
        g.setStroke(new BasicStroke((float) thickness));
        for (double pos : positions) {
            if (horizontal) {
                g.draw(new Line2D.Double(x0, y0 + pos * scale, x0 + content.getWidth(), y0 + pos * scale));
            } else {
                // Columns are placed left to right; right-to-left ones are drawn mirrored.
                double x = content.layoutToDrawing(new Rectangle2D.Double(pos, 0, 0, 0)).getX() * scale;
                g.draw(new Line2D.Double(x0 + x, y0, x0 + x, y0 + content.getHeight()));
            }
        }
    }

    @Override
    public void visitPageBreak(PageBreak b) {        
        if (!postfix) {
            return;
        }
        JSeshStyle jseshStyle = getJSeshStyle();
        if (!jseshStyle.options().paged() && !jseshStyle.geometry().page().isPaginated()) {
            g.setStroke(jseshStyle.geometry().fineStroke());
            g.draw(new Line2D.Double(-10000.0, 0, 10000.0, 0));
        }
    }

    /*
     * @see
     * jsesh.model.ModelElementVisitor#visitPhilology(jsesh.model.Philology)
     */
    @Override
    public void visitPhilology(Philology p) {
        if (!postfix) {
            return;
        }
        try {

            drawBracket(p.getType() * 2 + 1, currentView.getWidth()
                    -
                    PhilologyHelper.philologyWidth(p.getType()), 0);
            drawBracket(p.getType() * 2, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawBracket(int code, double x, double y) {
        JSeshStyle jseshStyle = getJSeshStyle();        
        Font f = jseshStyle.fonts().getFont(ScriptCode.LATIN);

        // Save current transformation.
        // AffineTransform old= g.getTransform();
        Graphics2D tmpG = (Graphics2D) g.create();
        tmpG.setFont(f);
        tmpG.translate(x, y);
        String s = LexicalSymbolsUtils.getStringForPhilology(code);
        Rectangle2D d = jseshStyle.fonts().textDimensions(getTechRenderContext(), ScriptCode.LATIN, s);

        double scalex = PhilologyHelper.philologyWidth(code)
                / d.getWidth();

        double scaley = currentView.getHeight() / d.getHeight();
        // scaley= 4;
        tmpG.scale(scalex, scaley);

        tmpG.drawString(s, (float) (-d.getMinX() * scalex), (float) ((d
                .getHeight() - d.getMaxY()) * scaley));
        tmpG.dispose();
    }

    /*
     *
     * jsesh.model.ModelElementVisitor#visitSuperScript(jsesh.model.
     * Superscript)
     */
    @Override
    public void visitSuperScript(Superscript s) {
        if (!postfix) {
            return;
        }
        JSeshStyle jseshStyle = getJSeshStyle();
        String text = s.getText();
        Dimension2D dims = jseshStyle.fonts().superScriptDimensions(getTechRenderContext(), text);
        g.setFont(jseshStyle.fonts().superScriptFont());
        g.drawString(text, 0, g.getFontMetrics().getAscent());
        g.setStroke(jseshStyle.geometry().fineStroke());
        g
                .draw(new Line2D.Double(dims.getWidth() / 2.0,
                        dims.getHeight()
                                + jseshStyle.geometry().smallSkip(),
                        dims.getWidth() / 2.0, currentView
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
        // Monochrome convention: red quadrats are underlined (they are drawn
        // in black). ViewDrawer decides which ones, and how far the line
        // extends to join the next red quadrat's.
        if (!postfix && isUnderlined()) {
            JSeshStyle style = getJSeshStyle();
            g.setStroke(style.geometry().fineStroke());
            double y = currentView.getHeight() + style.geometry().underlineGap();
            g.draw(new Line2D.Double(-getUnderlineExtendLeft(), y,
                    currentView.getWidth() + getUnderlineExtendRight(), y));
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
        boolean fillTopRight;
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
                r.y = 0.0;
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
        JSeshStyle jseshStyle = getJSeshStyle();
        // Easy way: paint the area in grey...
        // Complex way : intersect the area with line hatching...

        // Prevent us from messing with the area (not that important currently,
        // but who knows ?)
        if (jseshStyle.painting().shadingStyle().equals(
                ShadingMode.GRAY_SHADING)) {
            Color col = g.getColor();
            g.setColor(jseshStyle.painting().grayColor());
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
   
    public void visitOptionList(OptionsMap list) {

    }

    private boolean isShaded() {
        return getDrawingState().isShaded();
    }

}