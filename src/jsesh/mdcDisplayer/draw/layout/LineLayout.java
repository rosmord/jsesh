/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 23 déc. 2004
 *
 */
package jsesh.mdcDisplayer.draw.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementAdapter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TabStop;
import jsesh.mdcDisplayer.draw.DrawingSpecifications;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.RelativePosition;

/**
 * Expert for laying out text organised in lines.
 * <p>
 * Currently works only for left-to-right text.
 * <p>
 * We suppose that:
 * <ul>
 * <li>Apart from the first element, the others can be placed absolutely.
 * <li>
 * </ul>
 * General organization of the code :
 * 
 * 
 * @author Serge Rosmorduc
 */

public class LineLayout extends TopItemLayout {

    MDCView documentView;

    Rectangle2D documentArea;

    Point2D insertionPoint;

    Zone zone;

    DrawingSpecifications drawingSpecifications;

    RelativePosition nextViewPosition;
    
    // End of zone to move

    private MDCView subView;

    private LineLayoutAux aux = new LineLayoutAux();

    /**
     * @param documentView
     */
    public LineLayout(MDCView documentView,
            DrawingSpecifications drawingSpecifications) {
        super();
        this.documentView = documentView;
        documentArea = new Rectangle2D.Double();
        insertionPoint = new Point2D.Double();
        this.drawingSpecifications = drawingSpecifications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.TopItemLayout#layoutElement(jsesh.mdcDisplayer.mdcView.MDCView)
     */
    public void layoutElement(MDCView subView) {
        this.subView = subView;
        //Point2D refPoint = state.getReferencePoint();
        // Compute the new reference point ?
        // Don't think so...
        //state.getNextItemPosition().translate(subView.getParent(), subView,
        //refPoint);
        subView.getModel().accept(aux);
        //state.setNextItemPosition(subView.getNextViewPosition());
        
        this.subView = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.TopItemLayout#endLayout()
     */
    public void endLayout() {
        if (!zone.isEmpty()) {
            flushZone();
        }
    }

    /**
     * Returns the total area for the document, after computation.
     * 
     * @return the area for the complete document.
     */

    public Rectangle2D getDocumentArea() {
        return this.documentArea;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.TopItemLayout#initState()
     */
    public void startLayout() {
        // Pseudo relative position for first element.
        nextViewPosition= new RelativePosition(0, RelativePosition.WEST, 0, RelativePosition.NORTH);
        zone = new Zone(documentView.getFirstSubViewPosition());
    }

    /**
     *  
     */
    private void flushZone() {
        // TODO : get sure that this methods works for left-to-right layout.
        // Compute the start point relative position for this line
        // RIght-to-left is :
        //zone.getZoneStart().setValues((float) 0, RelativePosition.WEST,
        //        -(float) zone.getMinY(), RelativePosition.PREVIOUS);
        //zone.getZoneStart().setValues((float) 0, RelativePosition.WEST,
        //        (float) zone.getHeight(), RelativePosition.PREVIOUS);

        // Normally, the zone upper left point is (0,0). It can change if the
        // zone
        // is highter than expected. Correct this position :

        zone.getZoneStart().setDy(
                zone.getZoneStart().getDy() + (float) -zone.getMinY());

        // Should it change for right-to-left orientation ?
        zone.getZoneStart().setDx(
                zone.getZoneStart().getDx() + (float) -zone.getMinX());

        // Compute the actual start point for the line
        zone.getZoneStart().translate(documentView, subView, insertionPoint);
        // update the zone

        // flush the zone
        documentArea.add(new Rectangle2D.Double(0, insertionPoint.getY()
                + zone.getMinY(), zone.getWidth(), zone.getHeight()));
    }

    private class LineLayoutAux extends ModelElementAdapter {
        /**
         * Called when a new line is met.
         * <ul>
         * <li>add the current zone to the page
         * <li>prepare a new zone.
         * </ul>
         * 
         * @see jsesh.mdc.model.ModelElementAdapter#visitLineBreak(jsesh.mdc.model.LineBreak)
         */

        public void visitLineBreak(LineBreak b) {
            // Add a minimal line break height to the current zone
            // (else empty lines would be 0 height)
            // Write
            flushZone();
            subView.getNextViewPosition().setValues(0, RelativePosition.WEST,
                    (float) (zone.getHeight() + zone.getMinY()),
                    RelativePosition.PREVIOUS);
            // create a new zone ?
            zone = new Zone(subView.getNextViewPosition());
            // Next view position is computed relatively to the zone.
            nextViewPosition= new RelativePosition(0,RelativePosition.WEST, 0, RelativePosition.NORTH);

        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.mdc.model.ModelElementAdapter#visitPageBreak(jsesh.mdc.model.PageBreak)
         */
        public void visitPageBreak(PageBreak b) {
            // TEMPORARY.
            visitLineBreak(new LineBreak());
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.mdc.model.ModelElementAdapter#visitTabStop(jsesh.mdc.model.TabStop)
         */
        public void visitTabStop(TabStop t) {
            visitDefault(t);
        }

        /**
         * Layout alphabetic text.
         * <p>
         * Note that alphabetic text is special, because a sequence of element
         * will always be laid out in a given orientation which depends</em>
         * only</em> on the text writing system. (currently, always
         * left-to-right, but if arabic and hebrew are added, this will change).
         */
        public void visitAlphabeticText(AlphabeticText t) {
            visitDefault(t);
        }

        /**
         * Lay out generic top items.
         * 
         * <ul>
         * <li>compute next element position
         * <li>update the current zone shape
         * </ul>
         * 
         * @see jsesh.mdc.model.ModelElementAdapter#visitDefault(jsesh.mdc.model.ModelElement)
         */

        public void visitDefault(ModelElement t) {
            // Get the position of the point in the zone ???
            Point2D refPoint = zone.getCurrentPointInZone();
            // Compute the starting point position of the element.
            nextViewPosition.translate(subView.getParent(),subView, refPoint);
            // IMPORTANT : right-to-left code
            //subView.getStartPoint().setValues(-subView.getWidth(),
            //        RelativePosition.PREVIOUS, 0, RelativePosition.PREVIOUS);
            // We might change startPoint.
            subView.getStartPoint().translate(subView.getParent(), subView,
                    refPoint);

            // Update the zone size
            zone.add(new Rectangle2D.Double(refPoint.getX() + subView.getDx(),
                    refPoint.getY() + subView.getDy(), subView.getWidth(),
                    subView.getHeight()));

            // prepare a small skip after v, if necessary
            // the skip is "integrated" in v,
            if (subView.getWidth() != 0) {
                // IMPORTANT : here difference with right to left code
                subView.getNextViewPosition().setDx(
                        subView.getWidth()
                                + drawingSpecifications.getSmallSkip());
            } else
                subView.getNextViewPosition().setDx(0);
            nextViewPosition= subView.getNextViewPosition();

        }
    }

}