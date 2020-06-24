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
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.Cartouche;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * Base class for implementing drawers.
 *
 * @author rosmord
 */
public abstract class AbstractCartoucheDrawer {

    // preferences
    protected final DrawingSpecification drawingSpecifications;
    // arguments
    protected final TextDirection currentTextDirection;
    protected final TextOrientation currentTextOrientation;
    // technical
    protected final MDCView currentView;
    protected final Graphics2D g;

    public AbstractCartoucheDrawer(DrawingSpecification drawingSpecifications, TextDirection currentTextDirection, TextOrientation currentTextOrientation, MDCView currentView, Graphics2D g) {
        this.drawingSpecifications = drawingSpecifications;
        this.currentTextDirection = currentTextDirection;
        this.currentTextOrientation = currentTextOrientation;
        this.currentView = currentView;
        this.g = g;
    }

    public abstract void drawHorizontal(Cartouche cartouche);

    public abstract void drawVertical(Cartouche cartouche);

    /**
     * Draws a Hwt ending, or, in any case, a square ending (which can be used
     * in a serekh too).
     *
     * @param part the part code (as of MdC).
     * @param pa
     * @param pb
     * @param p1
     * @param p2
     */
    protected void drawHutEnd(int part, Point2D pa, Point2D pb, Point2D p1,
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

}
