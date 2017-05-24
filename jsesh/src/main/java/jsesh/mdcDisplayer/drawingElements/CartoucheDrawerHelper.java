/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2016) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est un programme informatique servant à mettre en place 
 * une base de données linguistique pour l'égyptien ancien.

 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence [CeCILL|CeCILL-B|CeCILL-C] telle que diffusée par le CEA, le CNRS et l'INRIA 
 * sur le site "http://www.cecill.info".

 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.

 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant 
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à 
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement, 
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité. 

 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez 
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package jsesh.mdcDisplayer.drawingElements;

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.Cartouche;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.CartoucheSizeHelper;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * Utility class for drawing cartouches and the like.
 * This is not a fine example of Object Oriented Design.
 * @author rosmord
 */
public class CartoucheDrawerHelper {
    // preferences
    private final DrawingSpecification drawingSpecifications;
    // arguments
    private final TextDirection currentTextDirection;
    private final TextOrientation currentTextOrientation;
    // technical
    private final MDCView currentView;
    private final Graphics2D g;

    public CartoucheDrawerHelper(DrawingSpecification drawingSpecifications, TextDirection currentTextDirection, TextOrientation currentTextOrientation, MDCView currentView, Graphics2D g) {
        this.drawingSpecifications = drawingSpecifications;
        this.currentTextDirection = currentTextDirection;
        this.currentTextOrientation = currentTextOrientation;
        this.currentView = currentView;
        this.g = g;
    }
    
    
    public void visitHorizontalCartouche(Cartouche c) {
        Stroke s = drawingSpecifications.buildCartoucheStroke(c.getType());
        /*
		 * float w1 = drawingSpecifications.getCartoucheStartWidth(c.getType(),
		 * c .getStartPart()); float w2 =
		 * drawingSpecifications.getCartoucheEndWidth(c.getType(), c
		 * .getEndPart());
         */
        float w1, w2;
        // The kind of elements found left and right of the cartouche.

        int leftElement, rightElement;

        if (currentTextDirection.isLeftToRight()) {
            leftElement = c.getStartPart();
            rightElement = c.getEndPart();
        } else {
            leftElement = c.getEndPart();
            rightElement = c.getStartPart();
        }

        // Compute horizontal space before and after the cartouche's body :
        w1 = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                c.getType(), leftElement);
        w2 = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                c.getType(), rightElement);

        // Half line width : allows to have a close bounding box.
        float dy = drawingSpecifications.getCartoucheLineWidth() / 2f;
        float dx = drawingSpecifications.getCartoucheLineWidth() / 2f;

        if (c.getType() == 'f') {
            dy += drawingSpecifications.getEnclosureBastionDepth();
        }
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
            switch (c.getType()) {
                case 'c': {
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
                break;
                case 'h': {
                    Point2D pa = new Point2D.Float(dx, dy);
                    Point2D pb = new Point2D.Float(dx, currentView.getHeight() - dy);
                    drawHutEnd(leftElement, pa, pb, p1, p2);
                }
                break;
                case 's': // Serekh
                {
                    Point2D pa = new Point2D.Float(dx, dy);
                    Point2D pb = new Point2D.Float(dx, currentView.getHeight() - dy);
                    if (leftElement == 1) {
                        drawHutEnd(leftElement, pa, pb, p1, p2);
                    } else {
                        drawSerekhEnd(p1, p2, pa, pb);
                    }
                }
                break;
                case 'f':
                    break;
            }
        }
        // Middle part.

        switch (c.getType()) {
            case 'c':
            case 'h':
            case 's':

                g.draw(new Line2D.Float(p1, p3));
                g.draw(new Line2D.Float(p2, p4));
                break;
            case 'f': {
                // We should improve this one. Basically, bastion size and above
                // all, skip, should be computed
                // (with some data as basis).
                float ex = p1.x + drawingSpecifications.getEnclosureBastionDepth()
                        + drawingSpecifications.getEnclosureBastionSkip();
                g.setStroke(s);
                g.draw(new Line2D.Float(p1, p3));
                g.draw(new Line2D.Float(p2, p4));
                while (ex + drawingSpecifications.getEnclosureBastionLength() < p3.x
                        - drawingSpecifications.getEnclosureBastionSkip()
                        - drawingSpecifications.getEnclosureBastionDepth()) {
                    g.fill(new Rectangle2D.Double(ex, p1.getY()
                            - drawingSpecifications.getEnclosureBastionDepth(),
                            drawingSpecifications.getEnclosureBastionLength(),
                            drawingSpecifications.getEnclosureBastionDepth()));
                    g.fill(new Rectangle2D.Double(ex, p2.getY(),
                            drawingSpecifications.getEnclosureBastionLength(),
                            drawingSpecifications.getEnclosureBastionDepth()));
                    ex += drawingSpecifications.getEnclosureBastionLength()
                            + drawingSpecifications.getEnclosureBastionSkip();

                }
            }
            break;
        }
        // End
        if (rightElement != 0) {
            switch (c.getType()) {
                case 'c': {
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
                break;
                case 'h': {
                    Point2D pa = new Point2D.Float(currentView.getWidth() - dx, dy);
                    Point2D pb = new Point2D.Float(currentView.getWidth() - dx,
                            currentView.getHeight() - dy);
                    drawHutEnd(rightElement, pa, pb, p3, p4);
                }
                break;
                case 's': // serekh
                {
                    Point2D pa = new Point2D.Float(currentView.getWidth() - dx, dy);
                    Point2D pb = new Point2D.Float(currentView.getWidth() - dx,
                            currentView.getHeight() - dy);

                    if (rightElement == 1) {
                        drawHutEnd(rightElement, pa, pb, p3, p4);
                    } else {
                        drawSerekhEnd(p3, p4, pa, pb);
                    }
                }
                break;
                case 'f':
                    break;
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

    private void drawHutEnd(int part, Point2D pa, Point2D pb, Point2D p1,
            Point2D p2) {
        if (part != 0) {
            /**
             * Orientation to use for the inner square.
             */
            int xSign = 1;
            int ySign = 1;
            g.draw(new Line2D.Float(pa, pb));
            g.draw(new Line2D.Float(pa, p1));
            g.draw(new Line2D.Float(pb, p2));

            // squareSize is either negative or positive :
            // it's positive for start of hut-sign, and negative for end.
            double squareSize = drawingSpecifications.getHwtSquareSize()
                    - drawingSpecifications.getCartoucheLineWidth() / 2f;

            if (currentTextOrientation.isHorizontal()) {
                if (p1.getX() < pa.getX()) {
                    xSign = -1;
                }
            } else {
                if (pa.getX() < pb.getX()) {
                    xSign = -1;
                }
                if (p1.getY() > pa.getY()) {
                    ySign = -1;
                }
                // For vertical text, the sides p1-pa and p2-pb have no
                // relationship with the
                // actual text orientation. We fix this here.
                if (!currentTextDirection.isLeftToRight()) {
                    if (part == 2) {
                        part = 3;
                    } else if (part == 3) {
                        part = 2;
                    }
                }

            }

            switch (part) {
                // square on the pb-p2 side.
                case 2: {
                    Point2D pca, pc, pcb;
                    if (currentTextOrientation.isHorizontal()) {
                        // point on the vertical line
                        pca = new Point2D.Double(pb.getX(), pb.getY() - squareSize);
                        // point on the horizontal line
                        pcb = new Point2D.Double(pb.getX() + xSign * squareSize, pb
                                .getY());
                        // square corner
                        pc = new Point2D.Double(pb.getX() + xSign * squareSize, pb
                                .getY()
                                - squareSize);
                    } else { // In Left-to-right columns : square to the left (to
                        // the right in R2L).
                        pca = new Point2D.Double(pb.getX(), pb.getY() - squareSize
                                * ySign);
                        pcb = new Point2D.Double(pb.getX() + xSign * squareSize, pb
                                .getY());
                        pc = new Point2D.Double(pb.getX() + xSign * squareSize, pb
                                .getY()
                                - squareSize * ySign);
                    }
                    g.draw(new Line2D.Float(pca, pc));
                    g.draw(new Line2D.Float(pc, pcb));
                }
                break;
                case 3: { // square on the pa-p1 side.
                    Point2D pca, pc, pcb;
                    if (currentTextOrientation.isHorizontal()) {
                        pca = new Point2D.Double(pa.getX(), pa.getY() + squareSize);
                        pcb = new Point2D.Double(pa.getX() + xSign * squareSize, pa
                                .getY());
                        pc = new Point2D.Double(pa.getX() + xSign * squareSize, p1
                                .getY()
                                + squareSize);
                    } else {
                        pca = new Point2D.Double(pa.getX(), pa.getY() - squareSize
                                * ySign);
                        pcb = new Point2D.Double(pa.getX() - xSign * squareSize, pa
                                .getY());
                        pc = new Point2D.Double(pa.getX() - xSign * squareSize, pa
                                .getY()
                                - squareSize * ySign);
                    }
                    g.draw(new Line2D.Float(pca, pc));
                    g.draw(new Line2D.Float(pc, pcb));
                }
            }
        }
    }

    
    /**
     * Very close to the horizontal routine (except that left-to-right vs.
     * right-to-left are not very important there).
     *
     * @param c
     */
    public void visitVerticalCartouche(Cartouche c) {

        Stroke s = drawingSpecifications.buildCartoucheStroke(c.getType());
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
                c.getType(), c.getStartPart());
        w2 = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                c.getType(), c.getEndPart());

        // Half line width : allows to have a close bounding box.
        float dy = drawingSpecifications.getCartoucheLineWidth() / 2f;
        float dx = drawingSpecifications.getCartoucheLineWidth() / 2f;

        if (c.getType() == 'f') {
            dx += drawingSpecifications.getEnclosureBastionDepth();
        }
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
        if (c.getStartPart() != 0) {
            switch (c.getType()) {
                case 'c': {
                    float p0x = dx;
                    if (c.getStartPart() == 2) {
                        p0x += drawingSpecifications.getCartoucheknotLength();
                        g.draw(new Line2D.Double(p2.getX(), dy, p1.getX(), dy));
                    }

                    g.draw(new CubicCurve2D.Double(p1.getX(), p1.getY(), p1.getX(),
                            p0x - loopSkip, p2.getX(), p0x - loopSkip, p2.getX(),
                            p2.getY()));
                }
                break;
                case 'h': {
                    Point2D pa = new Point2D.Float(currentView.getWidth() - dx, dy);
                    Point2D pb = new Point2D.Float(dx, dy);
                    drawHutEnd(c.getStartPart(), pa, pb, p1, p2);
                }
                break;
                case 's': // Serekh
                {
                    Point2D pb = new Point2D.Float(dx, dy);
                    Point2D pa = new Point2D.Float(currentView.getWidth() - dx, dy);
                    if (c.getStartPart() == 1) {
                        drawHutEnd(1, pa, pb, p1, p2);
                    } else {
                        drawSerekhEnd(p1, p2, pa, pb);
                    }
                }
                break;
                case 'f':
                    break;
            }
        }
        // Middle part.

        switch (c.getType()) {
            case 'c':
            case 'h':
            case 's':

                g.draw(new Line2D.Float(p1, p3));
                g.draw(new Line2D.Float(p2, p4));
                break;
            case 'f': {
                // We should improve this one. Basically, bastion size and above
                // all, skip, should be computed
                // (with some data as basis).
                float ex = p1.x + drawingSpecifications.getEnclosureBastionDepth()
                        + drawingSpecifications.getEnclosureBastionSkip();
                g.setStroke(s);
                g.draw(new Line2D.Float(p1, p3));
                g.draw(new Line2D.Float(p2, p4));
                while (ex + drawingSpecifications.getEnclosureBastionLength() < p3.x
                        - drawingSpecifications.getEnclosureBastionSkip()
                        - drawingSpecifications.getEnclosureBastionDepth()) {
                    g.fill(new Rectangle2D.Double(ex, p1.getY()
                            - drawingSpecifications.getEnclosureBastionDepth(),
                            drawingSpecifications.getEnclosureBastionLength(),
                            drawingSpecifications.getEnclosureBastionDepth()));
                    g.fill(new Rectangle2D.Double(ex, p2.getY(),
                            drawingSpecifications.getEnclosureBastionLength(),
                            drawingSpecifications.getEnclosureBastionDepth()));
                    ex += drawingSpecifications.getEnclosureBastionLength()
                            + drawingSpecifications.getEnclosureBastionSkip();

                }
            }
            break;
        }
        // End
        if (c.getEndPart() != 0) {
            switch (c.getType()) {
                case 'c': {
                    float p0x = -dx;
                    if (c.getEndPart() == 2) {
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
                break;
                case 'h': {

                    Point2D pb = new Point2D.Float(dx, currentView.getHeight() - dy);
                    Point2D pa = new Point2D.Float(currentView.getWidth() - dx,
                            currentView.getHeight() - dy);
                    drawHutEnd(c.getEndPart(), pa, pb, p3, p4);
                }
                break;
                case 's': // serekh
                {
                    Point2D pb = new Point2D.Float(dx, currentView.getHeight() - dy);
                    Point2D pa = new Point2D.Float(currentView.getWidth() - dx,
                            currentView.getHeight() - dy);

                    if (c.getEndPart() == 1) {
                        drawHutEnd(1, pa, pb, p3, p4);
                    } else {
                        drawSerekhEnd(p3, p4, pa, pb);
                    }
                }
                break;
                case 'f':
                    break;
            }
        }
        // end
    }

}
