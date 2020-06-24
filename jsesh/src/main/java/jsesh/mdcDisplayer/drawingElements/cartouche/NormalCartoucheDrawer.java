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
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.Cartouche;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.CartoucheSizeHelper;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * A drawer for "regular" cartouches.
 *
 * @author rosmord
 */
public class NormalCartoucheDrawer extends AbstractCartoucheDrawer {

    public NormalCartoucheDrawer(DrawingSpecification drawingSpecifications, TextDirection currentTextDirection, TextOrientation currentTextOrientation, MDCView currentView, Graphics2D g) {
        super(drawingSpecifications, currentTextDirection, currentTextOrientation, currentView, g);
    }

    @Override
    public void drawHorizontal(Cartouche cartouche) {
        Stroke stroke = drawingSpecifications.buildCartoucheStroke(cartouche.getType());
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

        // Compute horizontal space before and after the cartouche'stroke body :
        w1 = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                cartouche.getType(), leftElement);
        w2 = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                cartouche.getType(), rightElement);

        // Half line width : allows to have a close bounding box.
        float dy = drawingSpecifications.getCartoucheLineWidth() / 2f;
        float dx = drawingSpecifications.getCartoucheLineWidth() / 2f;

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

        g.setStroke(stroke);
        // Start
        if (leftElement != 0) {

            float p0x = dx;
            if (leftElement == 2) {
                p0x += drawingSpecifications.getCartoucheknotLength();
                g.draw(new Line2D.Double(dx * 1.5, p1.getY(), dx * 1.5, p2
                        .getY()));
            }
            g.draw(new CubicCurve2D.Double(p1.getX(), p1.getY(), p0x
                    - loopSkip, p1.getY(), p0x - loopSkip, p2.getY(), p2
                    .getX(), p2.getY()));

        }
        // Middle part.

        g.draw(new Line2D.Float(p1, p3));
        g.draw(new Line2D.Float(p2, p4));

        // End
        if (rightElement != 0) {

            float p0x = -dx;
            if (rightElement == 2) {
                p0x += -drawingSpecifications.getCartoucheknotLength();
                g.draw(new Line2D.Double(currentView.getWidth() - dx * 1.5,
                        p3.getY(), currentView.getWidth() - dx * 1.5, p4
                        .getY()));
            }
            g.draw(new CubicCurve2D.Double(p3.getX(), p3.getY(),
                    currentView.getWidth() + loopSkip + p0x, p3.getY(),
                    currentView.getWidth() + loopSkip + p0x, p4.getY(), p4
                    .getX(), p4.getY()));
        }

    }
    // end

    @Override
    public void drawVertical(Cartouche cartouche) {

        Stroke s = drawingSpecifications.buildCartoucheStroke(cartouche.getType());
        /*
		 * float w1 = drawingSpecifications.getCartoucheStartWidth(cartouche.getType(),
		 * cartouche .getStartPart()); float w2 =
		 * drawingSpecifications.getCartoucheEndWidth(cartouche.getType(), cartouche
		 * .getEndPart());
         */
        float w1, w2;

        // Compute vertical space before and after the cartouche's body :
        w1 = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                cartouche.getType(), cartouche.getStartPart());
        w2 = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                cartouche.getType(), cartouche.getEndPart());

        // Half line width : allows to have a close bounding box.
        float dy = drawingSpecifications.getCartoucheLineWidth() / 2f;
        float dx = drawingSpecifications.getCartoucheLineWidth() / 2f;

        // The respective limits of this cartouche's parts.
        //
        // w1 |
        // |
        // p2 p1
        // ...
        // p4 p3
        // |
        // w2|
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

            float p0x = dx;
            if (cartouche.getStartPart() == 2) {
                p0x += drawingSpecifications.getCartoucheknotLength();
                g.draw(new Line2D.Double(p2.getX(), dy, p1.getX(), dy));
            }

            g.draw(new CubicCurve2D.Double(p1.getX(), p1.getY(), p1.getX(),
                    p0x - loopSkip, p2.getX(), p0x - loopSkip, p2.getX(),
                    p2.getY()));
        }

        // Middle part.
        g.draw(new Line2D.Float(p1, p3));
        g.draw(new Line2D.Float(p2, p4));

        // End
        if (cartouche.getEndPart() != 0) {

            float p0x = -dx;
            if (cartouche.getEndPart() == 2) {
                p0x += -drawingSpecifications.getCartoucheknotLength();
                g.draw(new Line2D.Double(p4.getX(), currentView.getHeight()
                        - dy * 1.5, p3.getX(), currentView.getHeight() - dy
                        * 1.5));
            }
            g.draw(new CubicCurve2D.Double(p3.getX(), p3.getY(), p3.getX(),
                    currentView.getHeight() + loopSkip + p0x, p4.getX(),
                    currentView.getHeight() + loopSkip + p0x, p4.getX(), p4
                    .getY()));

        }
    }

}
