package jsesh.mdcDisplayer.layout;

/*
 * This file is free Software 
 * under the GNU LESSER GENERAL PUBLIC LICENCE.
 */

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import jsesh.mdc.constants.LexicalSymbolsUtils;
import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.BasicItemList;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.Cartouche;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.HRule;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.Ligature;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.Modifier;
import jsesh.mdc.model.ModifiersList;
import jsesh.mdc.model.Overwrite;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.Philology;
import jsesh.mdc.model.SubCadrat;
import jsesh.mdc.model.Superscript;
import jsesh.mdc.model.TabStop;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.draw.HieroglyphsDrawer;
import jsesh.mdcDisplayer.draw.LigatureManager;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.RelativePosition;
import jsesh.mdcDisplayer.mdcView.ViewIterator;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;
import jsesh.utils.TranslitterationUtilities;

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

    /**
     * Do we set cadrats to their maximal width. This should be set in the
     * toplevel of columns.
     */

    protected boolean honourMaxWidth;

    /**
     * Do we set cadrats to their maximal height. This should be set in the
     * toplevel of lines. If the cadrat is bigger, it will grow smaller. If it's
     * smaller, it will be left as such (for smaller cadrats, the "centering"
     * attribute is the key).
     */
    protected boolean honourMaxHeight;

    protected LigatureManager ligatureManager;

    public SimpleLayout() {
        ligatureManager = LigatureManager.getInstance();
        // FIXME : choose a good place to initialize the "honour" variables.
        honourMaxHeight = false;
        honourMaxWidth = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.AbstractLayout#preLayoutHook(jsesh.mdcDisplayer.mdcView.MDCView)
     */
    public void preLayoutHook(MDCView view, boolean embedded) {
        if (currentTextOrientation.isHorizontal()) {
            honourMaxHeight = true;
            honourMaxWidth = false;
        } else {
            honourMaxHeight = false;
            honourMaxWidth = true;
        }
        if (view.getModel() instanceof Cartouche)
            centerSigns = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.AbstractLayout#reset()
     */
    public void reset(DrawingSpecifications drawingSpecifications) {
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
    public void cleanup() {
        drawingSpecifications = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdc.model.ModelElementAdapter#visitAbsoluteGroup(jsesh.mdc.model.AbsoluteGroup)
     *      Layout an absolute group.
     */
    public void visitAbsoluteGroup(AbsoluteGroup g) {
        currentView.getFirstSubViewPosition().clear();
        // Fix the position for all subviews.
        for (int i = 0; i < currentView.getNumberOfSubviews(); i++) {
            MDCView subv = currentView.getSubView(i);
            Hieroglyph h = g.getHieroglyphAt(i);
            subv.resetPos();
            float x, y;
            x = h.getX() * drawingSpecifications.getBaseLength() / 1000.0f;
            y = h.getY() * drawingSpecifications.getBaseLength() / 1000.0f;
            if (currentTextDirection.isLeftToRight()) {
                subv.getStartPoint().setValues(x, RelativePosition.WEST, y,
                        RelativePosition.NORTH);
            } else {
                subv.getStartPoint().setValues(-x - subv.getWidth(),
                        RelativePosition.EAST, y, RelativePosition.NORTH);
            }
            // We used to compute the view dimension here, but
            // the fitToSubView method is more convenient to use
        }
        currentView.fitToSubViews();
    }

    /**
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
    //    ModelElement e= v.getModelElement();
    //    if (e instanceof AlphabeticText) {
    //       .... here the code for laying out alphabetic text
    //    } else if (...) // many else if...
    //
    // The previous code is bad, because it gives a mammouth method, hard to
    // modify.
    // 
    // On the other hand, argument passing is not really simple in the visitor
    // pattern, as we use class fields to do this. But, in return, one can
    // quite easily extend our SimpleLayout to adapt it.
    public void visitAlphabeticText(AlphabeticText t) {

        // Comments are not displayed.
        if (t.getScriptCode() == ScriptCodes.COMMENT) {
        	currentView.reset();
        	currentView.resetPos();
            return;
        }

        String text= t.getText();
        if (t.getScriptCode()== 't')
        	text= TranslitterationUtilities.toLowerCase(text);
        // Compute the text dimensions :
        Rectangle2D dims = drawingSpecifications.getTextDimensions(t
                .getScriptCode(), text);
        currentView.setHeight((float) dims.getHeight());
        currentView.setWidth((float) dims.getWidth());
        currentView.setDx((float) dims.getMinX());
        // the base line in dims is at 0. We want to align it with the sign

        // TODO : include a mecanism in drawingSpecifications to choose the
        // position of text relatively to hieroglyphs
        // (for instance, centered).

        // same base for text and hieroglyphs :
        currentView.setDy((float) +dims.getMinY()
                + drawingSpecifications.getMaxCadratHeight());
        // centered hieroglyphs
        currentView.setDy((float) +dims.getMinY()
                + drawingSpecifications.getMaxCadratHeight() / 2f);

    }

    /**
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
     * lay out a cadrat.
     * <p>
     * In theory, it would be a good idea to lay out cadrat twice : once upward,
     * and one downward. This would allow internal cadrats to take the exact
     * space needed by their container. This is not currently the case, and we
     * deal with it in the following way : any embedded cadrat has maxHeight as
     * height.
     * 
     * @see jsesh.mdc.model.ModelElementVisitor#visitCadrat(jsesh.mdc.model.Cadrat)
     */
    public void visitCadrat(Cadrat c) {
        ViewIterator i;
        float maxWidth = drawingSpecifications.getMaxCadratWidth();

        // First, we make sure that no hbox is too wide.
        // Except when there is only one HBox ; in this case,
        // the cadrat is "flat" and it's better not to scale it.
        // We don't scale, either, if one of the hboxes contains
        // a "large" sign (a sign which can grow horizontally).

        boolean wide = c.isWide();

        if ((honourMaxWidth)
                || (currentView.getNumberOfSubviews() != 1 && !wide)) {
            i = currentView.iterator();
            while (i.hasNext()) {
                i.next().scaleForMaxWidth(maxWidth);
            }
        }

        if (wide) {
            float maxw;
            if (honourMaxWidth)
                maxw = maxWidth;
            else
                maxw = currentView.getMaximalWidthOfSubView();
            for (int k = 0; k < currentView.getNumberOfSubviews(); k++) {
                if (c.getHBox(k).isWide()) {
                    currentView.getSubView(k).scaleToWidth(maxw);
                }
            }
        }

        // CADRAT HEIGHT
        // A cadrat is a list of horizontal boxes.
        // Its height is basically the sum of the heights of these boxes.
        float h = currentView.getSumOfSubViewsHeights();
        // add the interline spaces
        h += (currentView.getNumberOfSubviews() - 1)
                * drawingSpecifications.getSmallSkip();

        // If the cadrat is not high enough, we add some space to set its height
        // (if it's too large, it will be scaled anyway).
        // Note :
        // This should be done in a number of cases :
        // a) generally in line composition

        if ((honourMaxHeight // && drawingSpecifications.getOrientation() ==
                || embedded)
                // TextOrientation.HORIZONTAL
                && h < drawingSpecifications.getMaxCadratHeight())
            h = drawingSpecifications.getMaxCadratHeight();

        currentView.setHeight(h);

        // CADRAT WIDTH
        // We now proceed to the cadrat's width :
        // Compute "natural" width
        float w = currentView.getMaximalWidthOfSubView();

        if (honourMaxWidth && (!embedded) && w < maxWidth /*
                                                           * &&
                                                           * currentView.getNumberOfSubviews() ==
                                                           * 1
                                                           */) {
            w = maxWidth;
        }

        currentView.setWidth(w);

        // We redistribute views.
        if (currentView.getNumberOfSubviews() > 1) {
            currentView.distributeFromTopToBottom();
        } else {
            if (centerSigns)
                currentView.centerSubViewsVertically();
            else
                currentView.stickSubViewsTo(RelativePosition.SOUTH);
        }

        // Ok. Now, we are going to modify the hboxes' views so
        // that the cadrats are squared.
        i = currentView.iterator();

        while (i.hasNext()) {
            MDCView sub = i.next();
            if (sub.getWidth() < w) {
                // enlarge the box.
                sub.setWidth(w);
            }
            if (sub.getNumberOfSubviews() == 1) {
                // Currently, we simply center the element.
                sub.centerSubViewHorizontally();
            } else {
                sub.distributeHorizontally(currentTextDirection);
                // We align the bottom lines of subviews.
                sub.stickSubViewsTo(RelativePosition.SOUTH);
            }
        }
        // Now, if needed, we scale the cadrat :
        if (honourMaxHeight && h > drawingSpecifications.getMaxCadratHeight()) {
            currentView.reScale(drawingSpecifications.getMaxCadratHeight() / h);
        }
    }

    /**
     * Lay out a cartouche. Lay out its internal parts, and also provide space
     * for the cartouche frame.
     * <p>
     * Remember that a cartouche contains only one subview.
     */
    public void visitCartouche(Cartouche c) {
        // layout the cartouche's internals :
        MDCView subView = currentView.getFirstSubView();

        // margin (same for all types of cartouches).
        /*
         * float margin = drawingSpecifications.getCartoucheMargin() +
         * drawingSpecifications.getCartoucheLineWidth();
         */

        // the dimension along the principal axis of the cartouche (width for
        // horizontal ones, height for vertical)
        float longDim = 0;
        // the other dimension.
        float smallDim = 2 * drawingSpecifications
                .computeCartoucheSecondaryLength(c.getType());
        ;

         
        /**
         * cartouche inner part translation (reference and signs will be
         * computed later)
         */
        float dLong, dSmall;
        dLong = 0;
        dSmall = drawingSpecifications.computeCartoucheSecondaryLength(c
                .getType());

        longDim += drawingSpecifications.computeCartouchePartLength(
                c.getType(), c.getStartPart());

        dLong = longDim;

        longDim += drawingSpecifications.computeCartouchePartLength(
                c.getType(), c.getEndPart());

        if (currentTextOrientation.isHorizontal()) {
            currentView.setWidth(subView.getWidth() + longDim);
            currentView.setHeight(subView.getHeight() + smallDim);
            if (currentTextDirection.isLeftToRight()) {
                //              Position the cartouche's inner part :
                currentView.setFirstSubViewPosition(new RelativePosition(dLong,
                        RelativePosition.WEST, dSmall, RelativePosition.NORTH));

            } else {
                //              Position the cartouche's inner part :
                currentView.setFirstSubViewPosition(new RelativePosition(-dLong
                        - currentView.getFirstSubView().getWidth(),
                        RelativePosition.EAST, dSmall, RelativePosition.NORTH));

            }
        } else {
            currentView.setHeight(subView.getHeight() + longDim);
            // Maybe temporary :
            if (subView.getWidth() < drawingSpecifications.getMaxCadratWidth())
            	currentView.setWidth(drawingSpecifications.getMaxCadratWidth()+smallDim);
            else
            	currentView.setWidth(subView.getWidth() + smallDim);	
            
            float innerMargin= (currentView.getWidth() - subView.getWidth()) / 2f;
            
            if (currentTextDirection.isLeftToRight()) {
                //              Position the cartouche's inner part :
                currentView.setFirstSubViewPosition(new RelativePosition(
                        innerMargin, RelativePosition.WEST, dLong,
                        RelativePosition.NORTH));

            } else {
                //              Position the cartouche's inner part :
                currentView.setFirstSubViewPosition(new RelativePosition(
                        -innerMargin - subView.getWidth(), RelativePosition.EAST,
                        dLong, RelativePosition.NORTH));

            }

        }

        // Move the cartouche so that its inner part is
        // aligned
        // with
        // the main text.

        if (currentTextOrientation.isHorizontal()) {
        	currentView.setDy(-dSmall);
        	currentView.setDx(0);
        } else {
        	currentView.setDy(0);
        	if (currentTextDirection.isLeftToRight()) {        		
        		currentView.setDx(-dSmall);
        	} else {
        		currentView.setDx(-dSmall);
        	}
        }

        // Reset centering conditions (potentially changed in preLayoutHook)
        centerSigns = drawingSpecifications.isSmallSignsCentered();

    }

    /**
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
                currentView.distributeHorizontally(currentTextDirection);
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

    /**
     * @see jsesh.mdc.model.ModelElementVisitor#visitHBox(jsesh.mdc.model.HBox)
     */
    public void visitHBox(HBox b) {
        // Compute the correct box dimensions
        float w = currentView.getSumOfSubViewsWidths();
        w += (currentView.getNumberOfSubviews() - 1)
                * drawingSpecifications.getSmallSkip();

        float h = currentView.getMaximalHeightOfSubView();
        currentView.setHeight(h);
        currentView.setWidth(w);
        // The task of setting the positions of this view's subviews is dealt
        // with in the cadrat.

        // TODO : add code here that will set subviews to the right size
        // (specifically :
        // subviews for subcadrats will be given "h" as height,
        // and in those, the elements which are really cadrats of more than one
        // layer will also be given this size.
        // then those cadrat's internal layers will be placed correctly.
    }

    /**
     * @see jsesh.mdc.model.ModelElementVisitor#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
     */
    public void visitHieroglyph(Hieroglyph h) {
        // IMPORTANT : signs type should be a secure enum class.
        switch (h.getType()) {
        case SymbolCodes.FULLSHADE:
        case SymbolCodes.FULLSPACE:
            currentView.setWidth(drawingSpecifications.getCadratWidth());
            currentView.setHeight(drawingSpecifications.getMaxCadratHeight());
            break;
        case SymbolCodes.QUATERSHADE:
        case SymbolCodes.HALFSPACE:
            currentView.setWidth(drawingSpecifications.getCadratWidth() / 2.0f);
            currentView
                    .setHeight(drawingSpecifications.getMaxCadratHeight() / 2.0f);
            break;
        case SymbolCodes.VERTICALSHADE:
            currentView.setWidth(drawingSpecifications.getCadratWidth() / 2.0f);
            currentView.setHeight(drawingSpecifications.getMaxCadratHeight());
            break;
        case SymbolCodes.HORIZONTALSHADE:
            currentView.setWidth(drawingSpecifications.getCadratWidth());
            currentView
                    .setHeight(drawingSpecifications.getMaxCadratHeight() / 2f);
            break;
        case SymbolCodes.REDPOINT:
        case SymbolCodes.BLACKPOINT:
        case SymbolCodes.MDCCODE: {
            HieroglyphsDrawer d = drawingSpecifications.getHieroglyphsDrawer();
            Rectangle2D s;
            if (d.isKnown(h.getCode())) {
                s = d.getBBox(h.getCode());
                currentView.setWidth((float) s.getWidth());
                currentView.setHeight((float) s.getHeight());
                // TODO : maybe use the bounding box x and y as dx,dy for the
                // sign ?
                if (h.getAngle() != 0) {
                    // TODO : centralize decisions about the rotations.
                    // (rotation is found both here and in the drawing class).
                    Shape shape = drawingSpecifications.getHieroglyphsDrawer()
                            .getShape(h.getCode());
                    AffineTransform rot = AffineTransform.getRotateInstance(h
                            .getAngle()
                            * Math.PI / 180f);
                    shape = rot.createTransformedShape(shape);
                    Rectangle2D r = shape.getBounds2D();
                    currentView.setHeight((float) r.getHeight());
                    currentView.setWidth((float) r.getWidth());

                }
            } else {
                Dimension2D r = drawingSpecifications
                        .getSuperScriptDimensions(h.getCode());
                currentView.setHeight((float) r.getHeight());
                currentView.setWidth((float) r.getWidth());
            }
        }
            break;
        // NOTE : Obviously, it would be better to create some kind of "symbol"
        // class for signs which are not hieroglyphs.
        case SymbolCodes.BEGINERASE:
        case SymbolCodes.ENDERASE:
        case SymbolCodes.BEGINEDITORADDITION:
        case SymbolCodes.ENDEDITORADDITION:
        case SymbolCodes.BEGINEDITORSUPERFLUOUS:
        case SymbolCodes.ENDEDITORSUPERFLUOUS:
        case SymbolCodes.BEGINPREVIOUSLYREADABLE:
        case SymbolCodes.ENDPREVIOUSLYREADABLE:
        case SymbolCodes.BEGINSCRIBEADDITION:
        case SymbolCodes.ENDSCRIBEADDITION: {
            String txt = LexicalSymbolsUtils.getStringForPhilology(h.getType());
            Rectangle2D r = drawingSpecifications.getTextDimensions('l', txt);
            currentView.setWidth((float) r.getWidth());
            currentView.setHeight((float) r.getHeight());
        }
            break;
        }
        if (h.getRelativeSize() != 100) {
            /*
             * currentView.setWidth(currentView.getWidth() * h.getRelativeSize() /
             * 100f); currentView.setHeight(currentView.getHeight() *
             * h.getRelativeSize() / 100f);
             */
            currentView.reScale(h.getRelativeSize() / 100f);
        }

    }

    /**
     * @see jsesh.mdc.model.ModelElementVisitor#visitHRule(jsesh.mdc.model.HRule)
     */
    public void visitHRule(HRule h) {
        // NOTE : some views have a size which is a function of their
        // parent's size. For instance, some signs can fill an empty space
        // sizing should be better described (for instance, we could
        // use some "fill power" dimension, as is done in (La)TeX).
        currentView.setWidth(drawingSpecifications.getPageWidth());
        currentView.setHeight(drawingSpecifications.getRuleWidth());
    }

    /**
     * @see jsesh.mdc.model.ModelElementVisitor#visitLigature(jsesh.mdc.model.Ligature)
     */
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

            currentView.getStartPoint().clear();
            for (int k = 0; k < pos.length; k++) {
                MDCView subv = currentView.getSubView(k);
                float x, y;
                x = pos[k].getX() * drawingSpecifications.getBaseLength()
                        / 1000f;
                y = pos[k].getY() * drawingSpecifications.getBaseLength()
                        / 1000f;
                subv.resetPos();
                subv.reScale(pos[k].getScale() / 100f);
                if (currentTextDirection.isLeftToRight())
                    subv.getStartPoint().setValues(x, RelativePosition.WEST, y,
                            RelativePosition.NORTH);
                else
                    subv.getStartPoint().setValues(-x - subv.getWidth(),
                            RelativePosition.EAST, y, RelativePosition.NORTH);
            }
        }
        currentView.fitToSubViews();
    }

    /**
     * @see jsesh.mdc.model.ModelElementVisitor#visitLineBreak(jsesh.mdc.model.LineBreak)
     */
    public void visitLineBreak(LineBreak b) {
        // The actual layout is done in TopItemList, because we need a global
        // view to decide LineBreak skips.
        currentView.setWidth(0.1f);
        currentView.setHeight(drawingSpecifications.getMaxCadratHeight());
    }

    /**
     * @see jsesh.mdc.model.ModelElementVisitor#visitModifier(jsesh.mdc.model.Modifier)
     */
    public void visitModifier(Modifier mod) {
        super.visitModifier(mod);
    }

    /**
     * @see jsesh.mdc.model.ModelElementVisitor#visitModifierList(jsesh.mdc.model.ModifiersList)
     */
    public void visitModifierList(ModifiersList l) {
        super.visitModifierList(l);
    }

    /**
     * @see jsesh.mdc.model.ModelElementVisitor#visitOverwrite(jsesh.mdc.model.Overwrite)
     */
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

    /**
     * @see jsesh.mdc.model.ModelElementVisitor#visitPageBreak(jsesh.mdc.model.PageBreak)
     */
    public void visitPageBreak(PageBreak b) {
        /*
         * currentView.getStartPoint().setValues( 0f, RelativePosition.WEST,
         * drawingSpecifications.getMaxCadratHeight(),
         * RelativePosition.PREVIOUS);
         * currentView.getNextViewPosition().setValues( 0,
         * RelativePosition.WEST, drawingSpecifications.getMaxCadratHeight() * 2 +
         * drawingSpecifications.getLineSkip() * 2, RelativePosition.PREVIOUS);
         */
    }

    /**
     * @see jsesh.mdc.model.ModelElementVisitor#visitPhilology(jsesh.mdc.model.Philology)
     */
    public void visitPhilology(Philology p) {
        //visitDefault(p.getBasicItemList());
        MDCView subView = currentView.getFirstSubView();

        float margin = drawingSpecifications.getPhilologyWidth(p.getType())
                + drawingSpecifications.getSmallSkip();

        currentView.getFirstSubViewPosition().setValues(margin,
                RelativePosition.WEST, 0, RelativePosition.NORTH);
        currentView.setWidth(margin * 2 + subView.getWidth());
        currentView.setHeight(subView.getHeight());
    }

    /**
     * 
     * @see jsesh.mdc.model.ModelElementVisitor#visitSubCadrat(jsesh.mdc.model.SubCadrat)
     */
    public void visitSubCadrat(SubCadrat c) {
        currentView.setWidth(currentView.getSumOfSubViewsWidths());
        currentView.setHeight(currentView.getMaximalHeightOfSubView());
        currentView.distributeHorizontally(TextDirection.LEFT_TO_RIGHT);
    }

    /**
     * @see jsesh.mdc.model.ModelElementVisitor#visitSuperScript(jsesh.mdc.model.Superscript)
     */
    public void visitSuperScript(Superscript s) {
        Dimension2D dims = drawingSpecifications.getSuperScriptDimensions(s
                .getText());
        currentView.setWidth((float) dims.getWidth());
        currentView.setHeight((float) Math.max(drawingSpecifications
                .getMaxCadratHeight(), dims.getHeight()
                + drawingSpecifications.getSmallSkip() * 2));
    }

    /**
     * @see jsesh.mdc.model.ModelElementVisitor#visitTabStop(jsesh.mdc.model.TabStop)
     */
    public void visitTabStop(TabStop t) {
        // Empty method. Tab stops are handled when laying out the TopItemList.
    }

    /*
     * Layout the top item
     * 
     * @see jsesh.mdc.model.ModelElementAdapter#visitTopItemList(jsesh.mdc.model.TopItemList)
     */
    public void visitTopItemList(TopItemList t) {
        //monoliticVisitTopItemList(t);
        compositeVisitTopItemList(t);
    }

    /**
     * Layout a top item using a strategy class.
     * <p>
     * Currently, only use a line layout.
     * 
     * @param t
     * @see jsesh.mdc.model.ModelElementAdapter#visitTopItemList(jsesh.mdc.model.TopItemList)
     */
    public void compositeVisitTopItemList(TopItemList t) {
        //LineLayout topItemLayout= null;
        TopItemLayout topItemLayout = null;
        // TODO : the state is probably something to deprecate
        TopItemLayoutPosition state = new TopItemLayoutPosition();

        // TODO : use a factory ?
        if (currentTextOrientation.equals(TextOrientation.HORIZONTAL))
            topItemLayout = new LineLayout(currentView, drawingSpecifications);
        else
            topItemLayout = new ColumnLayout(currentView, drawingSpecifications);

        topItemLayout.setState(state);

        ViewIterator i = currentView.iterator();
        topItemLayout.startLayout();
        while (i.hasNext()) {
            MDCView v = i.next();
            v.resetPos();
            /*v.getStartPoint().clear();
            v.getNextViewPosition().clear();*/
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
     * Arrange the elements in the top level view.
     * 
     * This method does make a lot of decision, and is becoming full of
     * "instanceof" operators. It's now deprecated and replaced by compositeVisitTopItemList.
     * 
     * @param t
     * @see jsesh.mdc.model.ModelElementVisitor#visitTopItemList(jsesh.mdc.model.TopItemList)
     * @deprecated
     */

    public void monoliticVisitTopItemList(TopItemList t) {
        // The job of a toplayout is to place the views for the various
        // TopItems and to ajust the size of the total view accordingly.
        // The current TopItemLayout may change, as text orientation might be
        // mixed.
        // TopLayout work by arranging "chunks" of text. An array is either a
        // part of line or of column.
        // They are automatically stopped when the text changes orientation or
        // when an end of line is met.
        // At that time, the exact position of the "chunk" can be computed, and
        // its first element can be placed
        // NO WAY : consider right-to-left column text.

        //TopItemLayout topLayout= new TopItemHorizontalLeftToRightLayout();

        float documentWidth = 0f, documentHeight = 0f;
        float currentLineWidth = 0f;
        float currentlineHeight = 0f;

        MDCView v = null;
        /*
         * x= drawingSpecifications.getLeftMargin(); y=
         * drawingSpecifications.getTopMargin();
         */

        currentView.getFirstSubViewPosition().setValues(
                drawingSpecifications.getLeftMargin(), RelativePosition.WEST,
                drawingSpecifications.getTopMargin(), RelativePosition.NORTH);

        ViewIterator i = currentView.iterator();

        //currentlineHeight = drawingSpecifications.getMaxCadratHeight();
        currentlineHeight = drawingSpecifications.getMaxCadratHeight();
        currentLineWidth = drawingSpecifications.getLeftMargin();

        while (i.hasNext()) {
            v = i.next();
            RelativePosition p = v.getNextViewPosition();

            //
            // We should use a visitor !
            if (v.getModel() instanceof LineBreak) {
                LineBreak nl = (LineBreak) v.getModel();
                // Update width and height.
                if (currentLineWidth > documentWidth)
                    documentWidth = currentLineWidth;
                documentHeight += (currentlineHeight + drawingSpecifications
                        .getLineSkip())
                        * (nl.getSpacing() / 100.0);
                // Next element : abcisse is relative to a side of the
                // global view. vertical position is relative to the current
                // line.
                p.setValues(drawingSpecifications.getLeftMargin(),
                        RelativePosition.WEST, (float) ((drawingSpecifications
                                .getLineSkip() + currentlineHeight) * (nl
                                .getSpacing() / 100.0)),
                        RelativePosition.PREVIOUS);

                // Initialize for next line :
                // TODO : the line placement should probably be done after the
                // whole line has been seen.
                // (we might for instance keep the RelativePosition that places
                // the line in a variable).

                currentlineHeight = drawingSpecifications.getMaxCadratHeight();
                currentLineWidth = drawingSpecifications.getLeftMargin();
            } else if (v.getModel() instanceof PageBreak) {
                //	Update width and height.
                if (currentLineWidth > documentWidth)
                    documentWidth = currentLineWidth;

                documentHeight += currentlineHeight
                        + drawingSpecifications.getLineSkip() * 2
                        + drawingSpecifications.getTopMargin();

                v.getStartPoint()
                        .setValues(
                                0,
                                RelativePosition.WEST,
                                drawingSpecifications.getLineSkip()
                                        + currentlineHeight,
                                RelativePosition.PREVIOUS);

                // Next element : abcisse is relative to a side of the
                // global view. vertical position is relative to the current
                // line.
                p.setValues(drawingSpecifications.getLeftMargin(),
                        RelativePosition.WEST, drawingSpecifications
                                .getLineSkip()
                                + drawingSpecifications.getTopMargin(),
                        RelativePosition.PREVIOUS);
                v.setWidth(1000f);
                v.setHeight(drawingSpecifications.getLineSkip());
                // Initialize for next line :
                // TODO : See above for line skips. factorize this code.

                currentlineHeight = drawingSpecifications.getMaxCadratHeight();
                currentLineWidth = drawingSpecifications.getLeftMargin();
            } else if (v.getModel() instanceof TabStop) {
                TabStop tab = (TabStop) v.getModel();
                float pos = tab.getStopPos() / 200.0f
                        * drawingSpecifications.getBaseLength();
                ;
                p.setDx(pos);
                p.setXAnchor(RelativePosition.WEST);
                if (pos > currentLineWidth)
                    currentLineWidth = pos;
            } else {
                //
                // "Normal" element (non line break).
                //

                // Update dimensions

                if (v.getHeight() > currentlineHeight)
                    currentlineHeight = v.getHeight();
                //
                // Compute the position of the next element.

                // prepare a small skip after v, if necessary
                // the skip is "integrated" in v,

                if (v.getWidth() != 0) {
                    p
                            .setDx(v.getWidth()
                                    + drawingSpecifications.getSmallSkip());
                    float newWidth = currentLineWidth + v.getWidth()
                            + drawingSpecifications.getSmallSkip();
                    if (newWidth > currentLineWidth)
                        currentLineWidth = newWidth;
                } else
                    p.setDx(0);
            }
        }
        // add a new line if the last sign was not a line end.
        if (v != null && !(v.getModel() instanceof LineBreak)) {
            if (currentLineWidth > documentWidth)
                documentWidth = currentLineWidth;
            documentHeight += currentlineHeight;
        }
        documentHeight += drawingSpecifications.getLineSkip();
        currentView.setWidth(documentWidth);
        currentView.setHeight(documentHeight);
    }
}