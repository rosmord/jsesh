/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.mdcDisplayer.drawingElements.cartouche;

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.Cartouche;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.CartoucheSizeHelper;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * A drawer for Hwt-signs.
 *
 * @author rosmord
 */
public class HwtDrawer extends AbstractCartoucheDrawer {

    public HwtDrawer(DrawingSpecification drawingSpecifications, TextDirection currentTextDirection, TextOrientation currentTextOrientation, MDCView currentView, Graphics2D g) {
        super(drawingSpecifications, currentTextDirection, currentTextOrientation, currentView, g);
    }

    @Override
    public void drawHorizontal(Cartouche cartouche) {
        Stroke s = drawingSpecifications.buildCartoucheStroke(cartouche.getType());
        float w1, w2;
        // The kind of elements found left and right of the cartouche.

        int leftElement, rightElement;

        if (currentTextDirection.isLeftToRight()) {
            leftElement = cartouche.getStartPart();
            rightElement = cartouche.getEndPart();
        } else {
            leftElement = cartouche.getEndPart();
            rightElement = cartouche.getStartPart();
        }

        // Compute horizontal space before and after the cartouche's body :
        w1 = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                cartouche.getType(), leftElement);
        w2 = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                cartouche.getType(), rightElement);

        // Half line width : allows to have a close bounding box.
        float dy = drawingSpecifications.getCartoucheLineWidth() / 2f;
        float dx = drawingSpecifications.getCartoucheLineWidth() / 2f;

        // The respective limits of this cartouche's parts.
        //
        // start p1------p3 end
        //   
        // p2------p4
        // <....>
        // w1 w2
        //
        Point2D.Float p1, p2, p3, p4;

        p1 = new Point2D.Float(w1, dy);
        p2 = new Point2D.Float(w1, currentView.getHeight() - dy);
        p3 = new Point2D.Float(currentView.getWidth() - w2, +dy);
        p4 = new Point2D.Float(currentView.getWidth() - w2, currentView
                .getHeight()
                - dy);

        // The necessary skip to get a nice bezier curve for our cartouche
        // loops.
        float loopSkip = drawingSpecifications.getCartoucheLoopLength() / 3;

        g.setStroke(s);
        // Start
        if (leftElement != 0) {

            Point2D pa = new Point2D.Float(dx, dy);
            Point2D pb = new Point2D.Float(dx, currentView.getHeight() - dy);
            drawHutEnd(leftElement, pa, pb, p1, p2);

        }
        // Middle part.

        g.draw(new Line2D.Float(p1, p3));
        g.draw(new Line2D.Float(p2, p4));

        // End
        if (rightElement != 0) {

            Point2D pa = new Point2D.Float(currentView.getWidth() - dx, dy);
            Point2D pb = new Point2D.Float(currentView.getWidth() - dx,
                    currentView.getHeight() - dy);
            drawHutEnd(rightElement, pa, pb, p3, p4);
        }
    }

    @Override
    public void drawVertical(Cartouche cartouche) {
        Stroke s = drawingSpecifications.buildCartoucheStroke(cartouche.getType());
        float w1, w2;
        // Compute vertical space before and after the cartouche's body :
        w1 = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                cartouche.getType(), cartouche.getStartPart());
        w2 = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                cartouche.getType(), cartouche.getEndPart());

        // Half line width : allows to have a close bounding box.
        float dy = drawingSpecifications.getCartoucheLineWidth() / 2f;
        float dx = drawingSpecifications.getCartoucheLineWidth() / 2f;

        Point2D.Float p1, p2, p3, p4;

        p1 = new Point2D.Float(currentView.getWidth() - dx, w1);
        p2 = new Point2D.Float(dx, w1);
        p3 = new Point2D.Float(currentView.getWidth() - dx, currentView
                .getHeight()
                - dy - w2);
        p4 = new Point2D.Float(dx, currentView.getHeight() - dy - w2);

        // The necessary skip to get a nice bezier curve for our cartouche
        // loops.
        float loopSkip = drawingSpecifications.getCartoucheLoopLength() / 3;

        g.setStroke(s);
        // Start
        if (cartouche.getStartPart() != 0) {

            Point2D pa = new Point2D.Float(currentView.getWidth() - dx, dy);
            Point2D pb = new Point2D.Float(dx, dy);
            drawHutEnd(cartouche.getStartPart(), pa, pb, p1, p2);

        }
        // Middle part.

        g.draw(new Line2D.Float(p1, p3));
        g.draw(new Line2D.Float(p2, p4));

        // End
        if (cartouche.getEndPart() != 0) {

            Point2D pb = new Point2D.Float(dx, currentView.getHeight() - dy);
            Point2D pa = new Point2D.Float(currentView.getWidth() - dx,
                    currentView.getHeight() - dy);
            drawHutEnd(cartouche.getEndPart(), pa, pb, p3, p4);
        }
        // end
    }

   
}
