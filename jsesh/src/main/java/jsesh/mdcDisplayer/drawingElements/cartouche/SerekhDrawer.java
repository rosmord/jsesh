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
 * A drawer for Serekh.
 *
 * @author rosmord
 */
public class SerekhDrawer extends AbstractCartoucheDrawer {

    public SerekhDrawer(DrawingSpecification drawingSpecifications, TextDirection currentTextDirection, TextOrientation currentTextOrientation, MDCView currentView, Graphics2D g) {
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
            if (leftElement == 1) {
                drawHutEnd(leftElement, pa, pb, p1, p2);
            } else {
                drawSerekhEnd(p1, p2, pa, pb);
            }

        }
        // Middle part.

        g.draw(new Line2D.Float(p1, p3));
        g.draw(new Line2D.Float(p2, p4));

        // End
        if (rightElement != 0) {

            Point2D pa = new Point2D.Float(currentView.getWidth() - dx, dy);
            Point2D pb = new Point2D.Float(currentView.getWidth() - dx,
                    currentView.getHeight() - dy);

            if (rightElement == 1) {
                drawHutEnd(rightElement, pa, pb, p3, p4);
            } else {
                drawSerekhEnd(p3, p4, pa, pb);
            }

        }
        // end    
    }

    @Override
    public void drawVertical(Cartouche cartouche) {

        Stroke s = drawingSpecifications.buildCartoucheStroke(cartouche.getType());
        /*
		 * float w1 = drawingSpecifications.getCartoucheStartWidth(c.getType(),
		 * c .getStartPart()); float w2 =
		 * drawingSpecifications.getCartoucheEndWidth(c.getType(), c
		 * .getEndPart());
         */
        float w1, w2;

        if (currentTextDirection.isLeftToRight()) {
        } else {
        }

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

            Point2D pb = new Point2D.Float(dx, dy);
            Point2D pa = new Point2D.Float(currentView.getWidth() - dx, dy);
            if (cartouche.getStartPart() == 1) {
                drawHutEnd(1, pa, pb, p1, p2);
            } else {
                drawSerekhEnd(p1, p2, pa, pb);
            }

        }
        // Middle part.

        g.draw(new Line2D.Float(p1, p3));
        g.draw(new Line2D.Float(p2, p4));

        // End
        if (cartouche.getEndPart() != 0) {

            Point2D pb = new Point2D.Float(dx, currentView.getHeight() - dy);
            Point2D pa = new Point2D.Float(currentView.getWidth() - dx,
                    currentView.getHeight() - dy);

            if (cartouche.getEndPart() == 1) {
                drawHutEnd(1, pa, pb, p3, p4);
            } else {
                drawSerekhEnd(p3, p4, pa, pb);
            }

        }
        // end
    }

    /**
     * Draws the "palace facade" part of an serekh. For horizontal serekh :
     *
     * @param p1 top corner for the text part of the serekh
     * @param p2 bottom corner of the text
     * @param pa top corner of the end of the palace facade
     * @param pb bottom corner of the end of the palace facade
     */
    private void drawSerekhEnd(Point2D p1, Point2D p2, Point2D pa, Point2D pb) {
        if (currentTextOrientation.isHorizontal()) {
            // The width of this part :
            double dx = pa.getX() - p1.getX();
            double dy = p2.getY() - p1.getY();
            // vertical lines positions (corniches)
            double line1x = p1.getX() + dx * 0.1;
            double line2x = p1.getX() + dx * 0.3;
            double line3x = p1.getX() + dx * 0.4;

            g.draw(new Line2D.Double(p1, pa));
            g.draw(new Line2D.Double(p2, pb));
            g.draw(new Line2D.Double(line1x, p1.getY(), line1x, p2.getY()));

            g.setStroke(drawingSpecifications.getFineStroke());
            g.draw(new Line2D.Double(line2x, p1.getY(), line2x, p2.getY()));
            g.draw(new Line2D.Double(line3x, p1.getY(), line3x, p2.getY()));

            // Vertical positions for inner lines :
            g.setStroke(drawingSpecifications.getFineStroke());

            // recesses
            // inner recesses
            double recessX = p1.getX() + dx * 0.5;

            for (int i = 1; i < 10; i += 3) {
                double posy1 = p1.getY() + 0.1 * dy * i;
                double posy2 = p1.getY() + 0.1 * dy * (i + 1);
                double posy3 = p1.getY() + 0.1 * dy * (i + 2);
                double endX = pa.getX();

                g.draw(new Line2D.Double(recessX, posy1, endX, posy1));
                g.draw(new Line2D.Double(recessX, posy3, endX, posy3));
                g.draw(new Line2D.Double(recessX, posy1, recessX, posy3));

                double innerX = recessX + 0.2 * (endX - recessX);
                g.draw(new Line2D.Double(innerX, posy2, endX, posy2));
            }
        } // TODO : find a more general mecanism to accomodate line and column
        // serekh without code near-duplication.
        else {
            // Usual disposition in columns :
            // p1...p2
            // pa...pb
            double dx = p2.getX() - p1.getX();
            double dy = pa.getY() - p1.getY();

            // horizontal lines positions (corniches)
            double line1y = p1.getY() + dy * 0.1;
            double line2y = p1.getY() + dy * 0.3;
            double line3y = p1.getY() + dy * 0.4;

            g.draw(new Line2D.Double(p1, pa));
            g.draw(new Line2D.Double(p2, pb));

            g.draw(new Line2D.Double(p1.getX(), line1y, p2.getX(), line1y));

            g.setStroke(drawingSpecifications.getFineStroke());
            g.draw(new Line2D.Double(p1.getX(), line2y, p2.getX(), line2y));
            g.draw(new Line2D.Double(p1.getX(), line3y, p2.getX(), line3y));

            g.setStroke(drawingSpecifications.getFineStroke());

            // recesses
            // inner recesses base line
            double recessY = p1.getY() + dy * 0.5;
            double endY = pa.getY();

            for (int i = 1; i < 10; i += 3) {
                double posx1 = p1.getX() + 0.1 * dx * i;
                double posx2 = p1.getX() + 0.1 * dx * (i + 1);
                double posx3 = p1.getX() + 0.1 * dx * (i + 2);

                g.draw(new Line2D.Double(posx1, recessY, posx1, endY));
                g.draw(new Line2D.Double(posx3, recessY, posx3, endY));
                g.draw(new Line2D.Double(posx1, recessY, posx3, recessY));

                double innerY = recessY + 0.2 * (endY - recessY);
                g.draw(new Line2D.Double(posx2, innerY, posx2, endY));
            }

        }
        g.setStroke(drawingSpecifications.buildCartoucheStroke('s'));

    }
}
