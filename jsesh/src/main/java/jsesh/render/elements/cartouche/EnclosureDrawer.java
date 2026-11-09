/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.render.elements.cartouche;

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jsesh.render.style.JSeshStyle;
import jsesh.sharedvocabulary.CartouchePart;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.model.Cartouche;
import jsesh.render.view.MDCView;

/**
 * Drawer for enclosures.
 *
 * We have four corner bastions, plus intermediary ones, separated by spaces
 * which are more or less as large as a bastion.
 * <p>
 * Current principle :
 * <ol>
 * <li> place corner bastions. They are corner-shaped, and have a length which
 * is
 * {@link DrawingPreferences#getEnclosureBastionLength()} + {@link DrawingPreferences#getEnclosureBastionDepth()}</li>
 * <li> then, compute how many intermediary bastions fit between two corners. to
 * do this, we compute the remaining real estate. Let <em>l</em> be the
 * available length, and <em>b</em> a bastion length. The number of bastions
 * will be :
 * <em>n = floor((l - 2b -b)/(2b))</em>. The idea is that we remove the length
 * of the corner bastions, and that we have an empty space both before the first
 * internal bastion and after the last one.
 * <li> then, the space between two bastions will be <em>(l - n*b) /
 * (n+1)</em></li>
 * </ol>
 *
 * @see DrawingPreferences#getEnclosureBastionDepth()
 * @see DrawingPreferences#getEnclosureBastionDepth()
 * @author rosmord
 */
class EnclosureDrawer extends AbstractCartoucheDrawer {

    public EnclosureDrawer(JSeshStyle jseshStyle, TextDirection currentTextDirection, TextOrientation currentTextOrientation, MDCView currentView, Graphics2D g) {
        super(jseshStyle, currentTextDirection, currentTextOrientation, currentView, g);
    }

    @Override
    public void drawHorizontal(Cartouche cartouche) {
        Point2D.Double topLeftCorner = new Point2D.Double(0, 0);
        boolean left, right;
        if (currentTextDirection.isLeftToRight()) {
            left = cartouche.getStartPart() != CartouchePart.NONE;
            right = cartouche.getEndPart() != CartouchePart.NONE;
        } else {
            left = cartouche.getEndPart() != CartouchePart.NONE;
            right = cartouche.getStartPart() != CartouchePart.NONE;
        }
        double width = currentView.getWidth();
        double height = currentView.getHeight();
        drawEnclosure(topLeftCorner, width, height, true, true, left, right);
    }

    @Override
    public void drawVertical(Cartouche cartouche) {
        Point2D.Double topLeftCorner = new Point2D.Double(0, 0);
        boolean drawTop = cartouche.getStartPart() != CartouchePart.NONE;
        boolean drawBottom = cartouche.getEndPart() != CartouchePart.NONE;
        double width = currentView.getWidth();
        double height = currentView.getHeight();
        drawEnclosure(topLeftCorner, width, height, drawTop, drawBottom, true, true);
    }

    /**
     * Draws an enclosure at the given coordinates.
     * <p>
     * The coordinates correspond to the whole drawing space, including the
     * bastions.
     *
     * @param topLeftCorner top left corner of the rectangle to draw (without
     * the bastions).
     * @param outerWidth width of the enclosure
     * @param outerHeight height of the enclosure
     * @param drawTop should we draw the top side of the enclosure
     * @param drawBottom should we draw the bottom side of the enclosure
     * @param drawLeft should we draw the left side of the enclosure
     * @param drawRight should we draw the right side of the enclosure
     */
    public void drawEnclosure(Point2D.Double topLeftCorner, double outerWidth, double outerHeight,
            boolean drawTop, boolean drawBottom, boolean drawLeft, boolean drawRight) {
        //Stroke lineStroke = jseshStyle.buildCartoucheStroke('f');
        Stroke lineStroke = jseshStyle.geometry().cartoucheStroke();
        double bLength = jseshStyle.geometry().bastionLength();
        double bWidth = jseshStyle.geometry().bastionDepth();

        // Account for the  bastion depth.
        // dy = drawingSpecifications.getEnclosureBastionDepth();
        // The frame
        Point2D.Double topLeft, bottomLeft, topRight, bottomRight;

        double topX = topLeftCorner.x;
        double topY = topLeftCorner.y;
        double innerWidth = outerWidth;
        double innerHeight = outerHeight;

        if (drawLeft) {
            topX += bWidth;
            innerWidth -= bWidth;
        }
        if (drawTop) {
            topY += bWidth;
            innerHeight -= bWidth;
        }
        if (drawBottom) {
            innerHeight -= bWidth;
        }
        if (drawRight) {
            innerWidth -= bWidth;
        }

        topLeft = new Point2D.Double(topX, topY);
        bottomLeft = new Point2D.Double(topLeft.x, topLeft.y + innerHeight);
        topRight = new Point2D.Double(topLeft.x + innerWidth, topLeft.y);
        bottomRight = new Point2D.Double(topLeft.x + innerWidth, bottomLeft.y);

        BastionDrawingInfo horizontalBastionInfo = new BastionDrawingInfo(innerWidth);
        BastionDrawingInfo verticalBastionInfo = new BastionDrawingInfo(innerHeight);

        g.setStroke(lineStroke);

        // Sides
        if (drawTop) {
            g.draw(new Line2D.Double(topLeft, topRight));
            drawHorizontalBastions(horizontalBastionInfo, topLeft.x, topRight.y, BastionPosition.BEFORE);
        }

        if (drawBottom) {
            g.draw(new Line2D.Double(bottomLeft, bottomRight));
            drawHorizontalBastions(horizontalBastionInfo, bottomLeft.x, bottomLeft.y, BastionPosition.AFTER);
        }

        if (drawLeft) {
            g.draw(new Line2D.Double(topLeft, bottomLeft));
            drawVerticalBastions(verticalBastionInfo, topLeft.x, topLeft.y, BastionPosition.BEFORE);
        }

        if (drawRight) {
            g.draw(new Line2D.Double(topRight, bottomRight));
            drawVerticalBastions(verticalBastionInfo, topRight.x, topRight.y, BastionPosition.AFTER);

        }

        // Corner bastions
        if (drawTop && drawLeft) {
            double angleX = topLeft.x - bWidth;
            double angleYTop = topLeft.y - bWidth;
            fillRect(angleX, angleYTop, bWidth + bLength, bWidth);
            fillRect(angleX, angleYTop, bWidth, bWidth + bLength);
        }

        if (drawBottom && drawLeft) {
            double angleX = topLeft.x - bWidth;
            double angleYBottom = bottomLeft.y;
            fillRect(angleX, angleYBottom - bLength, bWidth, bLength);
            fillRect(angleX, angleYBottom, bWidth + bLength, bWidth);
        }

        if (drawTop && drawRight) {
            double angleX = topRight.x;
            double angleYTop = topRight.y - bWidth;
            fillRect(angleX - bLength, angleYTop, bWidth + bLength, bWidth);
            fillRect(angleX, angleYTop, bWidth, bWidth + bLength);

        }

        if (drawBottom && drawRight) {
            double angleX = topRight.x;
            double angleYBottom = bottomRight.y;

            fillRect(angleX, angleYBottom - bLength, bWidth, bLength);
            fillRect(angleX - bLength, angleYBottom, bWidth + bLength, bWidth);
        }
    }


    private void fillRect(double x0, double y0, double w, double h) {
        g.fill(new Rectangle2D.Double(x0, y0, w, h));
    }

    private void drawHorizontalBastions(BastionDrawingInfo info, double startX, double startY, BastionPosition bastionPosition) {
        double x = startX + info.getInterBastionSkip() + jseshStyle.geometry().bastionLength();
        double bastionY;
        if (bastionPosition == BastionPosition.BEFORE) {
            bastionY = startY - jseshStyle.geometry().bastionDepth();
        } else {
            bastionY = startY;
        }
        for (int i = 0; i < info.getNumberOfBastions(); i++) {
            fillRect(x, bastionY,
                    jseshStyle.geometry().bastionLength(),
                    jseshStyle.geometry().bastionDepth());
            x += jseshStyle.geometry().bastionLength()
                    + info.getInterBastionSkip();
        }
    }

    private void drawVerticalBastions(BastionDrawingInfo info, double x, double startY, BastionPosition bastionPosition) {
        double y = startY + info.getInterBastionSkip() + jseshStyle.geometry().bastionLength();
        double bastionX;
        if (bastionPosition == BastionPosition.BEFORE) {
            bastionX = x - jseshStyle.geometry().bastionDepth();
        } else {
            bastionX = x;
        }

        for (int i = 0; i < info.getNumberOfBastions(); i++) {
            fillRect(bastionX, y,
                    jseshStyle.geometry().bastionDepth(),
                    jseshStyle.geometry().bastionLength()
            );

            y += jseshStyle.geometry().bastionLength()
                    + info.getInterBastionSkip();
        }
    }

    /**
     * The necessary information for drawing bastions along a side.
     */
    private class BastionDrawingInfo {

        private int numberOfBastions;
        private double interBastionSkip;

        public BastionDrawingInfo(double availableRealEstate) {
            double bastionWidth = jseshStyle.geometry().bastionLength();
            this.numberOfBastions = (int) ((availableRealEstate - 3.0 * bastionWidth) / (2 * bastionWidth));
            this.interBastionSkip = (availableRealEstate - (this.numberOfBastions + 2) * bastionWidth)
                    / (this.numberOfBastions + 1);
            if (this.numberOfBastions == 1) {
                // One intermediary bastion is simply ugly. And seems to be avoided in
                // some actual examples.
                this.numberOfBastions = 0;
                this.interBastionSkip = 0;
            }
        }

        public double getInterBastionSkip() {
            return interBastionSkip;
        }

        public int getNumberOfBastions() {
            return numberOfBastions;
        }
    }

    private enum BastionPosition {
        BEFORE, AFTER
    }

}
