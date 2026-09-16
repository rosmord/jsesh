package jsesh.render.elements.cartouche;

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import jsesh.model.Cartouche;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.render.style.CartoucheSizeHelper;
import jsesh.render.style.GeometrySpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;

/**
 * Drawer for circular fences (town walls).
 */
class CircularFenceDrawer extends AbstractCartoucheDrawer {

    public CircularFenceDrawer(JSeshStyle jseshStyle, TextDirection currentTextDirection, TextOrientation currentTextOrientation, MDCView currentView, Graphics2D g) {
        super(jseshStyle, currentTextDirection, currentTextOrientation, currentView, g);
    }

    @Override
    public void drawHorizontal(Cartouche cartouche) {
        GeometrySpecification geometry = jseshStyle.geometry();
        Stroke stroke = geometry.cartoucheStroke();
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
        w1 = CartoucheSizeHelper.computeCartouchePartLength(jseshStyle,
                cartouche.getType(), leftElement);
        w2 = CartoucheSizeHelper.computeCartouchePartLength(jseshStyle,
                cartouche.getType(), rightElement);

        // Half line width : allows to have a close bounding box.
        float dy = geometry.cartoucheLineWidth() / 2f;
        float dx = geometry.cartoucheLineWidth() / 2f;

        Point2D.Float p1, p2, p3, p4;

        p1 = new Point2D.Float(w1, dy);
        p2 = new Point2D.Float(w1, currentView.getHeight() - dy);
        p3 = new Point2D.Float(currentView.getWidth() - w2, +dy);
        p4 = new Point2D.Float(currentView.getWidth() - w2, currentView
                .getHeight()
                - dy);

        // The necessary skip to get a nice bezier curve for our cartouche
        // loops.
        float loopSkip = geometry.cartoucheLoopLength() / 3;

        g.setStroke(stroke);
        // Start
        if (leftElement != 0) {

            float p0x = dx;            
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
            g.draw(new CubicCurve2D.Double(p3.getX(), p3.getY(),
                    currentView.getWidth() + loopSkip + p0x, p3.getY(),
                    currentView.getWidth() + loopSkip + p0x, p4.getY(), p4
                    .getX(), p4.getY()));
        }

    }
    // end

    @Override
    public void drawVertical(Cartouche cartouche) {

        GeometrySpecification geometry = jseshStyle.geometry();
        Stroke s = geometry.cartoucheStroke();
        /*
		 * float w1 = drawingSpecifications.getCartoucheStartWidth(cartouche.getType(),
		 * cartouche .getStartPart()); float w2 =
		 * drawingSpecifications.getCartoucheEndWidth(cartouche.getType(), cartouche
		 * .getEndPart());
         */
        float w1, w2;

        // Compute vertical space before and after the cartouche's body :
        w1 = CartoucheSizeHelper.computeCartouchePartLength(jseshStyle,
                cartouche.getType(), cartouche.getStartPart());
        w2 = CartoucheSizeHelper.computeCartouchePartLength(jseshStyle,
                cartouche.getType(), cartouche.getEndPart());

        // Half line width : allows to have a close bounding box.
        float dy = geometry.cartoucheLineWidth() / 2f;
        float dx = geometry.cartoucheLineWidth() / 2f;

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
        float loopSkip = geometry.cartoucheLineWidth() / 3;

        g.setStroke(s);
        // Start
        if (cartouche.getStartPart() != 0) {

            float p0x = dx;
           

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
            
            g.draw(new CubicCurve2D.Double(p3.getX(), p3.getY(), p3.getX(),
                    currentView.getHeight() + loopSkip + p0x, p4.getX(),
                    currentView.getHeight() + loopSkip + p0x, p4.getX(), p4
                    .getY()));

        }
    }

}
