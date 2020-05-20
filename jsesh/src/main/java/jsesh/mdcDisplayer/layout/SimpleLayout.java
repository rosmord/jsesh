package jsesh.mdcDisplayer.layout;

/*
 * This file is free Software 
 * under the GNU LESSER GENERAL PUBLIC LICENCE.
 */
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jsesh.hieroglyphs.graphics.LigatureZone;
import jsesh.mdc.constants.*;
import jsesh.mdc.model.*;
import jsesh.mdc.utils.TranslitterationUtilities;
import jsesh.mdcDisplayer.drawingElements.HieroglyphsDrawer;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.ViewIterator;
import jsesh.mdcDisplayer.preferences.CartoucheSizeHelper;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 *
 * This class is responsible for laying out the view element correctly.
 *
 * (c) Serge Rosmorduc
 *
 * @author Serge Rosmorduc
 * @see jsesh.mdcDisplayer.layout.AbstractLayout
 */
public class SimpleLayout extends AbstractLayout {

    protected LigatureManager ligatureManager;
    private boolean inAbsoluteGroup = false;

    public SimpleLayout() {
        ligatureManager = LigatureManager.getInstance();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.draw.AbstractLayout#preLayoutHook(jsesh.mdcDisplayer
	 * .mdcView.MDCView)
     */
    public void preLayoutHook(MDCView view, int depth) {
        if (view.getModel() instanceof Cartouche) {
            centerSigns = true;
        } else if (view.getModel() instanceof AbsoluteGroup) {
            inAbsoluteGroup = true;
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.layout.Layout#postLayout(jsesh.mdcDisplayer.mdcView
	 * .MDCView)
     */
    @Override
    public void postLayoutHook(MDCView result, int depth) {

    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.layout.AbstractLayout#commonLayoutHook()
     */
    @Override
    protected void commonLayoutHook() {
        currentView.setDirection(currentTextDirection);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.AbstractLayout#reset()
     */
    @Override
    public void reset(DrawingSpecification drawingSpecifications) {
        this.drawingSpecifications = drawingSpecifications;
        currentTextOrientation = drawingSpecifications.getTextOrientation();
        currentTextDirection = drawingSpecifications.getTextDirection();
        centerSigns = drawingSpecifications.isSmallSignsCentered();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.layout.Layout#cleanup()
     */
    @Override
    public void cleanup() {
        drawingSpecifications = null;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdc.model.ModelElementAdapter#visitAbsoluteGroup(jsesh.mdc.model
	 * .AbsoluteGroup) Layout an absolute group.
     */
    @Override
    public void visitAbsoluteGroup(AbsoluteGroup g) {
        // Fix the position for all subviews.
        for (int i = 0; i < currentView.getNumberOfSubviews(); i++) {
            MDCView subv = currentView.getSubView(i);
            Hieroglyph h = g.getHieroglyphAt(i);
            subv.resetPos();
            double x, y;
            x = h.getX() * getGroupUnitScale();
            y = h.getY() * getGroupUnitScale();
            subv.getPosition().setLocation(x, y);
        }
        currentView.fitToSubViews(false);
        inAbsoluteGroup = false;
    }

    /*
	 * Layout a view built around an alphabetic text.
	 * 
	 * @see jsesh.mdc.model.ModelElementVisitor#visitAlphabeticText(AlphabeticText)
     */
    // Implementation note for those reading the code :
    //
    // This class, as many in JSesh, uses the design pattern Visitor,
    // which is a kind of elaborated OO switch...case.
    // When this method is called, the alphabetic text "t" passed as argument
    // is always the model for the currentView (which can be accessed as a
    // protected
    // field of AbstractLayout).
    //
    // This avoids writing code like that :
    // layout(MDCView v) {
    // ModelElement e= v.getModelElement();
    // if (e instanceof AlphabeticText) {
    // .... here the code for laying out alphabetic text
    // } else if (...) // many else if...
    //
    // The previous code is bad, because it gives a mammoth method, hard to
    // modify.
    //
    // On the other hand, argument passing is not really simple in the visitor
    // pattern, as we use class fields to do this. But, in return, one can
    // quite easily extend our SimpleLayout to adapt it.
    @Override
    public void visitAlphabeticText(AlphabeticText t) {

        // Comments are not displayed.
        if (t.getScriptCode() == ScriptCodes.COMMENT) {
            currentView.reset();
            currentView.resetPos();
            return;
        }

        String text = t.getText();
        if (t.getScriptCode() == 't') {
            text = TranslitterationUtilities.getActualTransliterationString(text,
                    drawingSpecifications.getTransliterationEncoding());
        }
        // Compute the text dimensions :
        Rectangle2D dims;

        // TODO : remove getTextDimensions and create a better system.
        // = drawingSpecifications.getTextDimensions(t.getScriptCode(), text);
        // Ok now what we really want to do is
        // a) get the "actual" text we want to display.
        // b) get the correct font for this.
        if ("".equals(text)) {
            // Do nothing => empty view.
        } else {
            Font f = drawingSpecifications.getFont(t.getScriptCode());

            FontRenderContext fontRenderContext = drawingSpecifications
                    .getFontRenderContext();

            TextLayout layout = new TextLayout(text, f, fontRenderContext);

            dims = layout.getBounds();

            currentView.setHeight((float) dims.getHeight());

            currentView.setWidth(layout.getAdvance());

            // centered hieroglyphs (later we will propose a system replacing
            // /2.0
            // with some stored data.
            // Align the text base with the hieroglyphs...
            currentView.setDeltaBaseY(+drawingSpecifications
                    .getMaxCadratHeight() / 2.0 - layout.getAscent());
        }

    }

    /*
	 * @see jsesh.mdc.model.ModelElementVisitor#visitBasicItemList(jsesh.mdc.model.BasicItemList)
     */
    public void visitBasicItemList(BasicItemList l) {
        if (drawingSpecifications.getTextOrientation().equals(
                TextOrientation.HORIZONTAL)) {
            visitDefault(l);
        } else {
            currentView.stackTop(drawingSpecifications.getSmallSkip());
            currentView.centerSubViewHorizontally();
        }
    }

    /**
     * lay out a quadrant. This method is way too long.
     *
     * This method is currently quite complex, as the ultimate quadrant shape
     * depends on its environment.
     * <p>
     * besides, many parts have been added when they were needed.
     *
     * TODO : re-interpret this method as a modular list of layout processes.
     * (or something like that).
     *
     * NOTE : It appears that "lone" elements on a line, on a cadrat, lone
     * lines, etc... tend to deserve specific processing. We might consider this
     * when we refactor the code.
     *
     * @param c
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitCadrat(jsesh.mdc.model.Cadrat)
     */
    @Override
    public void visitCadrat(Cadrat c) {
        new QuadrantLayout(drawingSpecifications, centerSigns,
                currentTextOrientation).layout(currentView, c);
    }

    /**
     * Lay out a cartouche. Lay out its internal parts, and also provide space
     * for the cartouche frame.
     * <p>
     * Remember that a cartouche contains only one subview.
     *
     * @param c
     */
    @Override
    public void visitCartouche(Cartouche c) {
        // layout the cartouche's internals :
        MDCView subView = currentView.getFirstSubView();

        // the dimension along the principal axis of the cartouche (width for
        // horizontal ones, height for vertical)
        float longDim = 0;
        // the other dimension.
        float smallDim = 2 * CartoucheSizeHelper.computeCartoucheSecondaryLength(
                drawingSpecifications, c.getType());
        ;

        /**
         * cartouche inner part translation (reference and signs will be
         * computed later)
         */
        float dLong, dSmall;
        dSmall = CartoucheSizeHelper.computeCartoucheSecondaryLength(
                drawingSpecifications, c.getType());

        longDim += CartoucheSizeHelper.computeCartouchePartLength(
                drawingSpecifications, c.getType(), c.getStartPart());

        dLong = longDim;

        longDim += CartoucheSizeHelper.computeCartouchePartLength(
                drawingSpecifications, c.getType(), c.getEndPart());

        if (currentTextOrientation.isHorizontal()) {
            currentView.setWidth(subView.getWidth() + longDim);
            currentView.setHeight(Math.max(subView.getHeight() + smallDim,
                    drawingSpecifications.getMaxCadratHeight() + smallDim));
            // Position the cartouche's inner part :
            subView.getPosition().setLocation(dLong, dSmall);

        } else {
            currentView.setHeight(subView.getHeight() + longDim);
            // Maybe temporary :
            if (subView.getWidth() < drawingSpecifications.getMaxCadratWidth()
                    + smallDim) {
                currentView.setWidth(drawingSpecifications.getMaxCadratWidth()
                        + smallDim);
            } else {
                currentView.setWidth(subView.getWidth() + smallDim);
            }

            float innerMargin = (currentView.getWidth() - subView.getWidth()) / 2f;

            // Position the cartouche's inner part :
            subView.getPosition().setLocation(innerMargin, dLong);

        }

        // Move the cartouche so that its inner part is
        // aligned
        // with
        // the main text.
        if (currentTextOrientation.isHorizontal()) {
            currentView.setDeltaBaseY(-dSmall);
            currentView.setDeltaBaseX(0);
        } else {
            currentView.setDeltaBaseY(0);
            currentView.setDeltaBaseX(-dSmall);
        }

        // Reset centering conditions (potentially changed in preLayoutHook)
        centerSigns = drawingSpecifications.isSmallSignsCentered();

    }

    @Override
    public void visitComplexLigature(ComplexLigature ligature) {
        List<Optional<LigatureZone>> zones = new ArrayList<>(3);
        HieroglyphsDrawer d = drawingSpecifications.getHieroglyphsDrawer();

        // TODO  attach ligature zones to final (rotated, scaled) shapes
        // TODO not to hieroglyphic codes !!!
        for (int i = 0; i < 3; i++) {
            float scale = drawingSpecifications.getSignScale();
            final float scale1; // we need a final var for map argument.
            if (ligature.getHieroglyph().getRelativeSize() != 100)
                scale1= scale* ligature.getHieroglyph().getFLoatScale();
            else
                scale1= scale;
            Optional<LigatureZone> ligatureZone
                    = d.getLigatureZone(i, ligature.getHieroglyph().getCode())
                            .map(z -> z.scale(scale1));
            zones.add(ligatureZone);
        }

        int index = 0;
        // The order of subviews will be view for : beforegroup,
        // hieroglyph, and aftergroup
        // beforegroup and aftergroup can influence the size and position of
        // the whole view (well, a simple fit will do)
        if (ligature.getBeforeGroup() != null) {
            MDCView beforeGroupView = currentView.getSubView(index);
            if (zones.get(0).isPresent()) {
                zones.get(0).ifPresent(z -> z.placeView(beforeGroupView));
            } else {
                // No zone : ^^^ operator has then the same value as "*".
            }
            // Update index for hieroglyph view
            index++;
        }
        // The hieroglyph is placed at 0,0, with scale 1.
        // (basically, we do nothing)
        index++;
        if (ligature.getAfterGroup() != null) {
            MDCView afterGroupView = currentView.getSubView(index);
            // If there is a "bottom" zone, we prefer it to the "after" zone.
            if (zones.get(2).isPresent()) {
                zones.get(2).ifPresent(z -> z.placeView(afterGroupView));
            } else if (zones.get(1).isPresent()) {
                zones.get(1).ifPresent(z -> z.placeView(afterGroupView));
            } else {
                // No zone : &&& operator has then the same value as "*".
            }
        }
        // Now, zone1 might for instance have negative coordinates. We correct
        // this :
        currentView.fitToSubViews(true);
    }

    /*
     * Default method: lay out internal elements, and set view size accordingly.
     * <p>
     * This method is called for all elements which have no specific method
     * declared, and can also be called by others to lay out the internal
     * elements.
     *
     * <p>
     * This method honours the text orientation and direction.
     *
     */
    @Override
    public void visitDefault(ModelElement t) {
        if (currentView.getNumberOfSubviews() != 0) {
            if (drawingSpecifications.getTextOrientation().equals(
                    TextOrientation.HORIZONTAL)) {
                /* stick elements one after another */
                float width = currentView.getSumOfSubViewsWidths();
                width = width + drawingSpecifications.getSmallSkip()
                        * (currentView.getNumberOfSubviews() - 1);
                currentView.setWidth(width);
                currentView.setHeight(currentView.getMaximalHeightOfSubView());
                currentView.distributeHorizontally();
            } else {
                /* vertical text orientation */
 /* stick elements one on top of the other */
                float height = currentView.getSumOfSubViewsHeights();
                height = height + drawingSpecifications.getSmallSkip()
                        * (currentView.getNumberOfSubviews() - 1);
                currentView.setWidth(currentView.getMaximalWidthOfSubView());
                currentView.setHeight(height);
                currentView.distributeFromTopToBottom();
            }
        }
    }

    /*
     * @see jsesh.mdc.model.ModelElementVisitor#visitHBox(jsesh.mdc.model.HBox)
     */
    @Override
    public void visitHBox(HBox b) {

        float h = currentView.getMaximalHeightOfSubView();

        // First, enlarge high signs which stand alone. The
        // idea, especially in vertical texts, is to deal with
        // cases like sw*(i*i:Z2)
        // in this example, we want the "sw" sign to be as high as
        // the whole cadrat.
        for (ViewIterator iter = currentView.iterator(); iter.hasNext();) {

            MDCView subView = iter.next();
            HorizontalListElement group = (HorizontalListElement) subView
                    .getModel();
            // If the group is a lone hight sign, scale it to match the
            // height.

            // Something might also be done for subcadrats in a later
            // version.
            boolean shouldEnlarge = false;
            if (group.containsOnlyOneSign()) {
                if (subView.getHeight() > drawingSpecifications
                        .getLargeSignSizeRatio()
                        * drawingSpecifications.getHieroglyphsDrawer()
                                .getHeightOfA1()) {
                    shouldEnlarge = true;
                }
            }

            if (shouldEnlarge) {
                // we limit scaling to the cases where the sign is not
                // explicitely scaled.
                Hieroglyph hiero = group.getLoneSign();
                if (hiero.getRelativeSize() == 100) {
                    float scale = h / subView.getHeight();
                    subView.reScale(scale);
                }
            }
            new ViewVerticalEnlarger().enlarge(subView, h);
        }

        // Compute the correct box dimensions
        float w = currentView.getSumOfSubViewsWidths();
        w += (currentView.getNumberOfSubviews() - 1)
                * drawingSpecifications.getSmallSkip();

        currentView.setHeight(h);
        currentView.setWidth(w);

        // The task of setting the positions of this view's subviews is dealt
        // with in the cadrat.
        for (ViewIterator iter = currentView.iterator(); iter.hasNext();) {
            MDCView subView = iter.next();
            new ViewVerticalEnlarger().enlarge(subView, h);
        }
    }

    /*
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
     */
    @Override
    public void visitHieroglyph(Hieroglyph h) {
        // IMPORTANT : signs type should be a secure enum class.
        switch (h.getType()) {
            case SymbolCodes.SMALLTEXT: {
                // Temporary hack as proof of concept.
                String smallText = h.getSmallText();
                Dimension2D r = drawingSpecifications
                        .getSuperScriptDimensions(smallText);
                currentView.setHeight((float) r.getHeight());
                currentView.setWidth((float) r.getWidth());
            }
            break;
            case SymbolCodes.FULLSHADE:
            case SymbolCodes.FULLSPACE:
                currentView.setWidth(drawingSpecifications.getMaxCadratWidth());
                currentView.setHeight(drawingSpecifications.getMaxCadratHeight());
                break;
            case SymbolCodes.QUATERSHADE:
            case SymbolCodes.HALFSPACE:
                currentView
                        .setWidth(drawingSpecifications.getMaxCadratWidth() / 2.0f);
                currentView
                        .setHeight(drawingSpecifications.getMaxCadratHeight() / 2.0f);
                break;
            case SymbolCodes.VERTICALSHADE:
                currentView
                        .setWidth(drawingSpecifications.getMaxCadratWidth() / 2.0f);
                currentView.setHeight(drawingSpecifications.getMaxCadratHeight());
                break;
            case SymbolCodes.HORIZONTALSHADE:
                currentView.setWidth(drawingSpecifications.getMaxCadratWidth());
                currentView
                        .setHeight(drawingSpecifications.getMaxCadratHeight() / 2f);
                break;
            case SymbolCodes.BEGINERASE:
            case SymbolCodes.ENDERASE:
            case SymbolCodes.REDPOINT:
            case SymbolCodes.BLACKPOINT:
            case SymbolCodes.BEGINEDITORADDITION:
            case SymbolCodes.ENDEDITORADDITION:
            case SymbolCodes.BEGINEDITORSUPERFLUOUS:
            case SymbolCodes.ENDEDITORSUPERFLUOUS:
            case SymbolCodes.BEGINPREVIOUSLYREADABLE:
            case SymbolCodes.ENDPREVIOUSLYREADABLE:
            case SymbolCodes.BEGINMINORADDITION:
            case SymbolCodes.ENDMINORADDITION:
            case SymbolCodes.BEGINSCRIBEADDITION:
            case SymbolCodes.ENDSCRIBEADDITION:
            case SymbolCodes.BEGINDUBIOUS:
            case SymbolCodes.ENDDUBIOUS: {
                // Two cases: either we have a base fixed shape (one quadrant
                // high-sign, typically)
                // or we will take our shape from the surrounding signs.
                // In the last case, we choose an arbitrary small size.

                boolean fixed = (h.getRelativeSize() != 100 || inAbsoluteGroup
                        || h.isAloneInQuadrant() || h.getAngle() != 0);

                if (!fixed) {
                    currentView.setYStretchable(true);
                }

                Rectangle2D rect = drawingSpecifications.getHieroglyphsDrawer()
                        .getBBox(h.getCode(), h.getAngle(), fixed);
                currentView.setWidth((float) rect.getWidth());
                currentView.setHeight((float) rect.getHeight());
            }
            break;

            case SymbolCodes.MDCCODE: {
                HieroglyphsDrawer d = drawingSpecifications.getHieroglyphsDrawer();

                String code = h.getCode();

                if (d.isKnown(code)) {
                    int angle = h.getAngle();
                    Rectangle2D s = d.getBBox(code, angle, true);

                    // Take the font size into account.
                    currentView
                            .setHeight((float) (s.getHeight() * drawingSpecifications
                                    .getSignScale()));
                    currentView
                            .setWidth((float) (s.getWidth() * drawingSpecifications
                                    .getSignScale()));
                } else {
                    Dimension2D r = drawingSpecifications
                            .getSuperScriptDimensions(code);
                    currentView.setHeight((float) r.getHeight());
                    currentView.setWidth((float) r.getWidth());
                }
            }
            break;
        }
        if (h.getRelativeSize() != 100) {
            /*
			 * currentView.setWidth(currentView.getWidth() * h.getRelativeSize()
			 * / 100f); currentView.setHeight(currentView.getHeight() *
			 * h.getRelativeSize() / 100f);
             */
            currentView.reScale(h.getRelativeSize() / 100f);
        }

    }

    /*
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitHRule(jsesh.mdc.model.HRule)
     */
    @Override
    public void visitHRule(HRule h) {
        // NOTE : some views have a size which is a function of their
        // parent's size. For instance, some signs can fill an empty space
        // sizing should be better described (for instance, we could
        // use some "fill power" dimension, as is done in (La)TeX).

        currentView.setWidth(drawingSpecifications.getPageLayout()
                .getTextWidth());
        currentView.setHeight(drawingSpecifications.getWideLineWidth());
    }

    /*
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitLigature(jsesh.mdc.model.Ligature)
     */
    @Override
    public void visitLigature(Ligature l) {
        // Find the ligature
        String codes[] = new String[l.getNumberOfChildren()];
        for (int i = 0; i < codes.length; i++) {
            codes[i] = l.getHieroglyphAt(i).getCode();
        }
        ExplicitPosition pos[] = LigatureManager.getInstance().getPositions(
                codes);

        if (pos != null) {
            // Now, place the signs and compute the width :

            for (int k = 0; k < pos.length; k++) {
                MDCView subv = currentView.getSubView(k);
                float x, y;
                // So, the ligature coordinates suppose unscaled signs.
                // We scale them...
                float signScale = drawingSpecifications.getSignScale();
                x = (float) (pos[k].getX() * signScale * getGroupUnitScale());
                y = (float) (pos[k].getY() * signScale * getGroupUnitScale());
                subv.resetPos();
                subv.reScale(pos[k].getScale() / 100f);

                subv.getPosition().setLocation(x, y);

            }
        } else {
            // Use complex ligature if l has two elements :
            if (l.getNumberOfChildren() == 2) {
                // a) compute the larger element...
                // b) select which area to use
                // We consider first that the first element is larger, and that
                // we put the second "below" the sign.
                int largerSignIndex = 0; // Index of the "larger" sign
                int smallerSignIndex = 1; // Index of the "smaller" sign
                int ligatureZonePosition = 2; // position of ligature zone (0, 1 or 2)
                // If the second element is larger, use it and link *before*
                if (currentView.getSubView(1).getHeight() * 0.8 > currentView
                        .getSubView(0).getHeight()) {
                    largerSignIndex = 1;
                    smallerSignIndex = 0;
                    ligatureZonePosition = 0;
                }
                // In case the second ligature zone is missing from the first
                // element, try zone 1.
                if (ligatureZonePosition == 2
                        && !  drawingSpecifications.getHieroglyphsDrawer()
                                .getLigatureZone(ligatureZonePosition,
                                        l.getHieroglyphAt(largerSignIndex).getCode()).isPresent()) {
                    ligatureZonePosition = 1;
                }
                Optional<LigatureZone> ligatureZone = drawingSpecifications
                        .getHieroglyphsDrawer().getLigatureZone(ligatureZonePosition,
                                l.getHieroglyphAt(largerSignIndex).getCode());

                // Build the ligature.
                // Make it better ???
                if (ligatureZone.isPresent()) {
                    LigatureZone z = ligatureZone.get();
                    currentView.getSubView(largerSignIndex).resetPos();
                    // Take into account the sign scale..
                    z = z.scale(drawingSpecifications
                            .getSignScale());
                    // NEW code for the new complex ligature system.
                    z.placeView(currentView.getSubView(smallerSignIndex));
                    // End of new code.
                }
            } else if (l.getNumberOfChildren() == 3) {
                // We try a ligature centered around the 2nd sign.
                String code = l.getHieroglyphAt(1).getCode();
                Optional<LigatureZone> r1, r2;
                r1 = drawingSpecifications.getHieroglyphsDrawer()
                        .getLigatureZone(0, code);
                r2 = drawingSpecifications.getHieroglyphsDrawer()
                        .getLigatureZone(1, code);
                if (r1.isPresent() && r2.isPresent()) {
                    LigatureZone z1 = r1.get().scale(drawingSpecifications.getSignScale());
                    LigatureZone z2 = r2.get().scale(drawingSpecifications.getSignScale());
                    currentView.getSubView(1).resetPos();
                    z1.placeView(currentView.getSubView(0));
                    z2.placeView(currentView.getSubView(2));
                }
            }
        }
        currentView.fitToSubViews(true);
    }

    /*
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitLineBreak(jsesh.mdc.model.LineBreak)
     */
    @Override
    public void visitLineBreak(LineBreak b) {
        // The actual layout is done in TopItemList, because we need a global
        // view to decide LineBreak skips.
        currentView.setWidth(0.0f);
        currentView.setHeight(drawingSpecifications.getMaxCadratHeight());
    }

    /*
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitModifier(jsesh.mdc.model.Modifier)
     */
    @Override
    public void visitModifier(Modifier mod) {
        super.visitModifier(mod);
    }

    /*
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitModifierList(jsesh.mdc.model.ModifiersList)
     */
    @Override
    public void visitModifierList(ModifiersList l) {
        super.visitModifierList(l);
    }

    /*
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitOverwrite(jsesh.mdc.model.Overwrite)
     */
    @Override
    public void visitOverwrite(Overwrite o) {
        double wmax, hmax;
        wmax = Math.max(currentView.getSubView(0).getWidth(), currentView
                .getSubView(1).getWidth());
        hmax = Math.max(currentView.getSubView(0).getHeight(), currentView
                .getSubView(1).getHeight());
        currentView.setWidth((float) wmax);
        currentView.setHeight((float) hmax);
        currentView.centerSubViewHorizontally();
        currentView.centerSubViewsVertically();
    }

    /*
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitPageBreak(jsesh.mdc.model.PageBreak)
     */
    @Override
    public void visitPageBreak(PageBreak b) {
        /*
		 * currentView.getStartPoint().setValues( 0f, RelativePosition.WEST,
		 * drawingSpecifications.getMaxCadratHeight(),
		 * RelativePosition.PREVIOUS);
		 * currentView.getNextViewPosition().setValues( 0,
		 * RelativePosition.WEST, drawingSpecifications.getMaxCadratHeight() * 2
		 * + drawingSpecifications.getLineSkip() * 2,
		 * RelativePosition.PREVIOUS);
         */
    }

    /*
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitPhilology(jsesh.mdc.model.Philology)
     */
    @Override
    public void visitPhilology(Philology p) {
        // visitDefault(p.getBasicItemList());
        MDCView subView = currentView.getFirstSubView();

        float margin = drawingSpecifications.getPhilologyWidth(p.getType())
                + drawingSpecifications.getSmallSkip();

        currentView.getFirstSubView().getPosition().setLocation(margin, 0);
        currentView.setWidth(margin * 2 + subView.getWidth());
        currentView.setHeight(subView.getHeight());
    }

    /*
     *
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitSubCadrat(jsesh.mdc.model.SubCadrat)
     */
    @Override
    public void visitSubCadrat(SubCadrat c) {
        currentView.setWidth(currentView.getSumOfSubViewsWidths());
        currentView.setHeight(currentView.getMaximalHeightOfSubView());
        currentView.distributeHorizontally();
    }

    /*
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitSuperScript(jsesh.mdc.model.Superscript)
     */
    @Override
    public void visitSuperScript(Superscript s) {
        Dimension2D dims = drawingSpecifications.getSuperScriptDimensions(s
                .getText());
        currentView.setWidth((float) dims.getWidth());
        currentView.setHeight((float) Math.max(
                drawingSpecifications.getMaxCadratHeight(), dims.getHeight()
                + drawingSpecifications.getSmallSkip() * 2));
    }

    /*
     * @see
     * jsesh.mdc.model.ModelElementVisitor#visitTabStop(jsesh.mdc.model.TabStop)
     */
    @Override
    public void visitTabStop(TabStop t) {
        // Empty method. Tab stops are handled when laying out the TopItemList.
    }

    /**
     * Layout the top item list.
     *
     * @param t the list of items to lay out.
     * @see
     * jsesh.mdc.model.ModelElementAdapter#visitTopItemList(jsesh.mdc.model.
     * TopItemList)
     */
    @Override
    public void visitTopItemList(TopItemList t) {
        // monoliticVisitTopItemList(t);
        compositeVisitTopItemList(t);
    }

    /**
     * Layout a top item list using a strategy class.
     *
     * @param t
     * @see
     * jsesh.mdc.model.ModelElementAdapter#visitTopItemList(jsesh.mdc.model.TopItemList)
     */
    public void compositeVisitTopItemList(TopItemList t) {

        // currentView contains the content for all pages.
        // we need
        // a) to extract the individual page content
        // b) to layout this content as individual lines
        // c) to dispatch these lines in individual pages, and layout them
        // again.
        // (althought it might be interesting to retrieve the work done in b,
        // but this requires a notion of "line".
        // LineLayout topItemLayout= null;
        TopItemLayout topItemLayout = null;

        // using a factory at this point would be overkill.
        if (currentTextOrientation.equals(TextOrientation.HORIZONTAL)) {
            topItemLayout = new LineLayout(currentView, drawingSpecifications);
        } else {
            topItemLayout = new ColumnLayout(currentView, drawingSpecifications);
        }

        ViewIterator i = currentView.iterator();
        topItemLayout.startLayout();
        // Ok. Currently, even for long texts, the
        // layout time is very short
        // between 71 and 21ms for Horus and Seth, which is a really long text
        // (by ancient egyptian standards)
        // on a eeepc, which is not specially the fastest computer in the world.
        // So we don't bother for efficiency too much at that time.
        while (i.hasNext()) {
            MDCView v = i.next();
            v.resetPos();
            topItemLayout.layoutElement(v);
            // TODO : add here something to change the current layout if needed.
        }
        topItemLayout.endLayout();
        currentView
                .setWidth((float) topItemLayout.getDocumentArea().getWidth());
        currentView.setHeight((float) topItemLayout.getDocumentArea()
                .getHeight());
    }

    /**
     * Auxiliary expert, able to enlarge view vertically. This is used in an
     * hbox, to set subcadrats to the hbox's height.
     *
     * @author rosmord
     *
     */
    class ViewVerticalEnlarger extends ModelElementAdapter {

        float height;

        MDCView view;

        /**
         * @param subView
         * @param h
         */
        public void enlarge(MDCView subView, float h) {
            height = h;
            view = subView;
            subView.getModel().accept(this);
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementAdapter#visitSubCadrat(jsesh.mdc.model
		 * .SubCadrat)
         */
        public void visitSubCadrat(SubCadrat c) {
            MDCView basicElementsListView = view.getFirstSubView();
            boolean changed = false;
            for (int i = 0; i < basicElementsListView.getNumberOfSubviews(); i++) {
                MDCView elementView = basicElementsListView.getSubView(i);
                if (elementView.isYStretchable()) {
                    elementView.setHeight(height);
                    elementView.distributeFromTopToBottom();
                    changed = true;
                }
            }
            if (changed) {
                basicElementsListView.fitToSubViews(false);
                view.fitToSubViews(false);
            }

        }

        public void visitHieroglyph(Hieroglyph h) {
            if (view.isYStretchable()) {
                view.setHeight(height);
            }
        }
    }

    private double getGroupUnitScale() {
        return drawingSpecifications.getHieroglyphsDrawer()
                .getGroupUnitLength() *(drawingSpecifications.getStandardSignHeight() / 18.0);
    }
}
