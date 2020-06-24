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
        Stroke lineStroke = drawingSpecifications.buildCartoucheStroke(cartouche.getType());

        // MdC code for cartouche extremities.
        int leftElementCode, rightElementCode;

        if (currentTextDirection.isLeftToRight()) {
            leftElementCode = cartouche.getStartPart();
            rightElementCode = cartouche.getEndPart();
        } else {
            leftElementCode = cartouche.getEndPart();
            rightElementCode = cartouche.getStartPart();
        }

        float spaceBefore = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                cartouche.getType(), leftElementCode);
        float spaceAfter = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                cartouche.getType(), rightElementCode);

        // Half line width : allows to have a close bounding box.
        float dy = drawingSpecifications.getCartoucheLineWidth() / 2f;
        float dx = drawingSpecifications.getCartoucheLineWidth() / 2f;

        // Account for the  bastion depth.
        dy += drawingSpecifications.getEnclosureBastionDepth();

        Point2D.Float topLeft, bottomLeft, topRight, bottomRight;

        topLeft = new Point2D.Float(spaceBefore, dy);
        bottomLeft = new Point2D.Float(spaceBefore, currentView.getHeight() - dy);
        topRight = new Point2D.Float(currentView.getWidth() - spaceAfter, dy);
        bottomRight = new Point2D.Float(currentView.getWidth() - spaceAfter, currentView
                .getHeight()
                - dy);

        float innerWidth = currentView.getWidth() - spaceAfter - spaceBefore;
        float innerHeight = currentView.getHeight() - 2 * dy;
        BastionDrawingInfo horizontalBastionInfo = new BastionDrawingInfo(innerWidth);
        BastionDrawingInfo verticalBastionInfo = new BastionDrawingInfo(innerHeight);

        g.setStroke(lineStroke);
        // Start
        if (leftElementCode != 0) {
            g.draw(new Line2D.Float(topLeft, bottomLeft));
            drawVerticalBastions(verticalBastionInfo, topLeft.x, topLeft.y, BastionPosition.BEFORE);
            // Draw angle bastions. 
            float bLength = drawingSpecifications.getEnclosureBastionLength();
            float bWidth = drawingSpecifications.getEnclosureBastionDepth();
            float angleX = topLeft.x - bWidth;
            float angleYTop = topLeft.y - bWidth;
            float angleYBottom = bottomLeft.y;
            fillRect(angleX, angleYTop, bWidth + bLength, bWidth);
            fillRect(angleX, angleYTop, bWidth, bWidth + bLength);
            fillRect(angleX, angleYBottom - bLength, bWidth, bLength);
            fillRect(angleX, angleYBottom, bWidth + bLength, bWidth);
            drawLine(topLeft.x, topLeft.y, topLeft.x, topLeft.y + drawingSpecifications.getEnclosureBastionLength());

            g.setStroke(lineStroke);
        }

        drawLine(topLeft, topRight);
        drawLine(bottomLeft, bottomRight);

        drawHorizontalBastions(horizontalBastionInfo, topLeft.x, topLeft.y, BastionPosition.BEFORE);
        drawHorizontalBastions(horizontalBastionInfo, topLeft.x, bottomLeft.y, BastionPosition.AFTER);
        if (rightElementCode != 0) {
            g.draw(new Line2D.Float(topRight, bottomRight));

            // Draw angle bastions. 
            float bLength = drawingSpecifications.getEnclosureBastionLength();
            float bWidth = drawingSpecifications.getEnclosureBastionDepth();
            float angleX = topRight.x;
            float angleYTop = topRight.y - bWidth;
            float angleYBottom = bottomRight.y;
            fillRect(angleX - bLength, angleYTop, bWidth + bLength, bWidth);
            fillRect(angleX, angleYTop, bWidth, bWidth + bLength);
            
            fillRect(angleX, angleYBottom - bLength, bWidth, bLength);
            fillRect(angleX - bLength, angleYBottom, bWidth + bLength, bWidth);

            drawVerticalBastions(verticalBastionInfo, topRight.x, topRight.y, BastionPosition.AFTER);
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
        float x = startX + info.getInterBastionSkip()+ drawingSpecifications.getEnclosureBastionLength();
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
                    drawingSpecifications.getEnclosureBastionLength(),
                    drawingSpecifications.getEnclosureBastionDepth());

            y += drawingSpecifications.getEnclosureBastionLength()
                    + info.getInterBastionSkip();
        }
    }

    @Override
    public void drawVertical(Cartouche cartouche) {

        Stroke stroke = drawingSpecifications.buildCartoucheStroke(cartouche.getType());      
      
        // Compute vertical space before and after the cartouche's body :
        float spaceBefore = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                cartouche.getType(), cartouche.getStartPart());
        float spaceAfter = CartoucheSizeHelper.computeCartouchePartLength(drawingSpecifications,
                cartouche.getType(), cartouche.getEndPart());

        // Half line width : allows to have a close bounding box.
        float dy = drawingSpecifications.getCartoucheLineWidth() / 2f;
        float dx = drawingSpecifications.getCartoucheLineWidth() / 2f;

        dx += drawingSpecifications.getEnclosureBastionDepth();

        Point2D.Float topLeft, topRight, bottomLeft, bottomRight;
        
        
       
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
            this.interBastionSkip = (availableRealEstate - (this.numberOfBastions+2) * bastionWidth)
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
