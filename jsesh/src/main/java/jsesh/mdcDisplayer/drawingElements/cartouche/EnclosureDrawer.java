/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.mdcDisplayer.drawingElements.cartouche;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.Cartouche;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.CartoucheSizeHelper;
import jsesh.mdcDisplayer.preferences.DrawingPreferences;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

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
public class EnclosureDrawer extends AbstractCartoucheDrawer {

    public EnclosureDrawer(DrawingSpecification drawingSpecifications, TextDirection currentTextDirection, TextOrientation currentTextOrientation, MDCView currentView, Graphics2D g) {
        super(drawingSpecifications, currentTextDirection, currentTextOrientation, currentView, g);
    }

    @Override
    public void drawHorizontal(Cartouche cartouche) {
        Point2D.Float topLeftCorner = new Point2D.Float(0, 0);
        boolean left, right;
        if (currentTextDirection.isLeftToRight()) {
            left = cartouche.getStartPart() != 0;
            right = cartouche.getEndPart() != 0;
        } else {
            left = cartouche.getEndPart() != 0;
            right = cartouche.getStartPart() != 0;
        }
        float width = currentView.getWidth();
        float height = currentView.getHeight();
        drawEnclosure(topLeftCorner, width, height, true, true, left, right);
    }

    @Override
    public void drawVertical(Cartouche cartouche) {
        Point2D.Float topLeftCorner = new Point2D.Float(0, 0);
        boolean drawTop = cartouche.getStartPart() != 0;
        boolean drawBottom = cartouche.getEndPart()!= 0;        
        float width = currentView.getWidth();
        float height = currentView.getHeight();
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
    public void drawEnclosure(Point2D.Float topLeftCorner, float outerWidth, float outerHeight,
            boolean drawTop, boolean drawBottom, boolean drawLeft, boolean drawRight) {
        Stroke lineStroke = drawingSpecifications.buildCartoucheStroke('f');
        float bLength = drawingSpecifications.getEnclosureBastionLength();
        float bWidth = drawingSpecifications.getEnclosureBastionDepth();

        // Half line width : allows to have a close bounding box.
        float dy = drawingSpecifications.getCartoucheLineWidth() / 2f;
        float dx = drawingSpecifications.getCartoucheLineWidth() / 2f;

        // Account for the  bastion depth.
        // dy = drawingSpecifications.getEnclosureBastionDepth();
        // The frame
        Point2D.Float topLeft, bottomLeft, topRight, bottomRight;

        float topX = topLeftCorner.x;
        float topY = topLeftCorner.y;
        float innerWidth = outerWidth;
        float innerHeight = outerHeight;

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

        topLeft = new Point2D.Float(topX, topY);
        bottomLeft = new Point2D.Float(topLeft.x, topLeft.y + innerHeight);
        topRight = new Point2D.Float(topLeft.x + innerWidth, topLeft.y);
        bottomRight = new Point2D.Float(topLeft.x + innerWidth, bottomLeft.y);

        BastionDrawingInfo horizontalBastionInfo = new BastionDrawingInfo(innerWidth);
        BastionDrawingInfo verticalBastionInfo = new BastionDrawingInfo(innerHeight);

        g.setStroke(lineStroke);

        // Sides
        if (drawTop) {
            g.draw(new Line2D.Float(topLeft, topRight));
            drawHorizontalBastions(horizontalBastionInfo, topLeft.x, topRight.y, BastionPosition.BEFORE);
        }

        if (drawBottom) {
            g.draw(new Line2D.Float(bottomLeft, bottomRight));
            drawHorizontalBastions(horizontalBastionInfo, bottomLeft.x, bottomLeft.y, BastionPosition.AFTER);
        }

        if (drawLeft) {
            g.draw(new Line2D.Float(topLeft, bottomLeft));
            drawVerticalBastions(verticalBastionInfo, topLeft.x, topLeft.y, BastionPosition.BEFORE);
        }

        if (drawRight) {
            g.draw(new Line2D.Float(topRight, bottomRight));
            drawVerticalBastions(verticalBastionInfo, topRight.x, topRight.y, BastionPosition.AFTER);

        }

        // Corner bastions
        if (drawTop && drawLeft) {
            float angleX = topLeft.x - bWidth;
            float angleYTop = topLeft.y - bWidth;
            fillRect(angleX, angleYTop, bWidth + bLength, bWidth);
            fillRect(angleX, angleYTop, bWidth, bWidth + bLength);
        }

        if (drawBottom && drawLeft) {
            float angleX = topLeft.x - bWidth;
            float angleYBottom = bottomLeft.y;
            fillRect(angleX, angleYBottom - bLength, bWidth, bLength);
            fillRect(angleX, angleYBottom, bWidth + bLength, bWidth);
        }

        if (drawTop && drawRight) {
            float angleX = topRight.x;
            float angleYTop = topRight.y - bWidth;
            fillRect(angleX - bLength, angleYTop, bWidth + bLength, bWidth);
            fillRect(angleX, angleYTop, bWidth, bWidth + bLength);

        }

        if (drawBottom && drawRight) {
            float angleX = topRight.x;
            float angleYBottom = bottomRight.y;

            fillRect(angleX, angleYBottom - bLength, bWidth, bLength);
            fillRect(angleX - bLength, angleYBottom, bWidth + bLength, bWidth);
        }

    }

    private void drawLine(float x0, float y0, float x1, float y1) {
        g.draw(new Line2D.Float(x0, y0, x1, y1));
    }

    private void drawLine(Point2D.Float p1, Point2D.Float p2) {
        g.draw(new Line2D.Float(p1, p2));
    }

    private void fillRect(float x0, float y0, float w, float h) {
        g.fill(new Rectangle2D.Double(x0, y0, w, h));
    }

    private void drawHorizontalBastions(BastionDrawingInfo info, float startX, float startY, BastionPosition bastionPosition) {
        float x = startX + info.getInterBastionSkip() + drawingSpecifications.getEnclosureBastionLength();
        float bastionY;
        if (bastionPosition == BastionPosition.BEFORE) {
            bastionY = startY - drawingSpecifications.getEnclosureBastionDepth();
        } else {
            bastionY = startY;
        }
        for (int i = 0; i < info.getNumberOfBastions(); i++) {
            fillRect(x, bastionY,
                    drawingSpecifications.getEnclosureBastionLength(),
                    drawingSpecifications.getEnclosureBastionDepth());
            x += drawingSpecifications.getEnclosureBastionLength()
                    + info.getInterBastionSkip();
        }
    }

    private void drawVerticalBastions(BastionDrawingInfo info, float x, float startY, BastionPosition bastionPosition) {
        float y = startY + info.getInterBastionSkip() + drawingSpecifications.getEnclosureBastionLength();
        float bastionX;
        if (bastionPosition == BastionPosition.BEFORE) {
            bastionX = x - drawingSpecifications.getEnclosureBastionDepth();
        } else {
            bastionX = x;
        }

        for (int i = 0; i < info.getNumberOfBastions(); i++) {
            fillRect(bastionX, y,
                    drawingSpecifications.getEnclosureBastionDepth(),
                    drawingSpecifications.getEnclosureBastionLength()
            );

            y += drawingSpecifications.getEnclosureBastionLength()
                    + info.getInterBastionSkip();
        }
    }

    /**
     * The necessary information for drawing bastions along a side.
     */
    private class BastionDrawingInfo {

        private int numberOfBastions;
        private float interBastionSkip;

        public BastionDrawingInfo(float availableRealEstate) {
            float bastionWidth = drawingSpecifications.getEnclosureBastionLength();
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

        public float getInterBastionSkip() {
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
