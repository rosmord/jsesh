/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 23 dec. 2004
 *
 */
package jsesh.mdcDisplayer.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementAdapter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TabStop;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.PageLayout;

/**
 * Expert for laying out text organised in lines.
 * <p>
 * We suppose that:
 * <ul>
 * <li>Apart from the first element, the others can be placed absolutely.
 * <li>
 * </ul>
 *
 * TODO: Understand a bug with PDF export when there is a page break in the
 * document. See if this bug is really a pdf export bug, or if it can be avoided
 * at the LineLayout level. (it's a NullPointerException, due to zone being
 * null). Ok. Understood: when we have a pagebreak, the visitPageBreak creates a
 * new zone (overriding the old one which was null). But when we output PDF,
 * instead of outputing graphics for the new zone, we do nothing. Thus the zone
 * stays null.
 *
 * @author Serge Rosmorduc
 */
public class LineLayout extends TopItemLayout {

    private final MDCView documentView;

    Rectangle2D documentArea;

    /**
     * Insertion point for the next zone.
     * <p>
     * Used to compute the positions of the zone, and then the whole document
     * size.
     */
    private final Point2D insertionPoint;

    /**
     * The zone being built.
     */
    private Zone zone;

    /**
     * A list of all zones, if we decide to justify the text.
     */
    private final List<Zone> allZones = new ArrayList<Zone>();
    /**
     * the position that will be used to place the current zone
     */

    private Point2D.Double zoneOriginPosition;

    private final DrawingSpecification drawingSpecifications;

    /**
     * The relative position given by the previous subview.
     */
    // private RelativePosition nextViewPosition;
    private final TextDirection currentTextDirection;

    /**
     * The current subview (for a topitem).
     */
    private MDCView subView;

    /**
     * Toggle used to indicates that at the current position, margins should be
     * taken into account. Used when we process the first line of a page.
     */
    private boolean addTopMargin;

    private final LineLayoutAux aux = new LineLayoutAux();

    /**
     * @param documentView
     * @param drawingSpecifications
     */
    public LineLayout(MDCView documentView,
            DrawingSpecification drawingSpecifications) {
        super();
        this.documentView = documentView;
        documentArea = new Rectangle2D.Double();
        insertionPoint = new Point2D.Double();
        this.drawingSpecifications = drawingSpecifications;
        currentTextDirection = drawingSpecifications.getTextDirection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jsesh.mdcDisplayer.draw.TopItemLayout#layoutElement(jsesh.mdcDisplayer
     * .mdcView.MDCView)
     */
    @Override
    public void layoutElement(MDCView subView) {
        this.subView = subView;
        subView.getPosition().setLocation(0, 0);
        subView.getModel().accept(aux);
        this.subView = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.TopItemLayout#endLayout()
     */
    @Override
    public void endLayout() {
        if (!zone.isEmpty()) {
            flushZone();
        }
        if (drawingSpecifications.isJustified()) {
            // Compute the maximal width of zones.
            double width = 0;
            for (Zone z : allZones) {
                if (z.getWidth() > width) {
                    width = z.getWidth();
                }
            }
            width = width
                    - drawingSpecifications.getPageLayout().getLeftMargin();
            // Justify all zones to this width.
            for (int i = 0; i < allZones.size(); i++) {
                Zone z = allZones.get(i);
                // If it's not the last zone, or it looks large enough
                // This is a temporary fix. 
                // We should get somehow the information that the zone "would be centered".
                if (i != allZones.size() - 1 || z.getWidth() >= 0.75 * width) {
                    z.justifyWidthTo(drawingSpecifications.getPageLayout()
                            .getLeftMargin(), width);
                }
            }
        }
        // Ensure the margin is there ?
        documentArea
                .add(new Point2D.Double(
                                documentArea.getMaxX()
                                + drawingSpecifications.getPageLayout()
                                .getLeftMargin(), documentArea
                                .getMinY()
                                + drawingSpecifications.getPageLayout()
                                .getTopMargin()));
    }

    /**
     * Returns the total area for the document, after computation.
     *
     * @return the area for the complete document.
     */
    @Override
    public Rectangle2D getDocumentArea() {
        return this.documentArea;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.mdcDisplayer.draw.TopItemLayout#initState()
     */
    @Override
    public void startLayout() {
        allZones.clear();
        // Pseudo relative position for first element.
        addTopMargin = true;
        // nextViewPosition = documentView.getFirstSubViewPosition();
        resetView();
        documentView.setDirection(currentTextDirection);
        insertionPoint.setLocation(0f, 0f);
        initZoneForPage();
    }

    private void initZoneForPage() {
        zone = new Zone(0, drawingSpecifications.getMaxCadratHeight());
        zoneOriginPosition = new Point2D.Double();
    }

    /**
     *
     */
    public void resetView() {
        documentView.reset();
    }

    /**
     * Add the current zone to the current page.
     * <p>
     * <em>updates insertionPoint and documentArea.</em>
     */
    private void flushZone() {

        // We are supposed to add a new line to the text.
        // In the line coord. system, 0,0 is supposed to be the upper left
        // point.
        // However, some large signs (cartouches, for instance) can be highter
        // than expected,
        // which causes the minimal y values to be negative.
        // Now, we don't want this to cause problems, such as very small line
        // skips, or, worse
        // parts of cartouches being cut.
        PageLayout pageLayout = drawingSpecifications.getPageLayout();

        // Compute the margins we want.
        float marginx = pageLayout.getLeftMargin();
        float marginy = 0f;

        if (addTopMargin) {
            marginy = pageLayout.getTopMargin();
        }

        // change the zoneStart for this zone, so that the real top
        // of the zone is separated from the previous line by at least margin.
        double minx = zone.getMinX();
        double miny = zone.getMinY();

        zoneOriginPosition.y = zoneOriginPosition.y + marginy - miny;
        zoneOriginPosition.x = marginx - minx;

        // fix the coordinates of the views in the zone to their final value.
        zone.translateBy(zoneOriginPosition);

        // flush the zone. Add it to the current document area.
        documentArea
                .add(new Rectangle2D.Double(zoneOriginPosition.x + minx,
                                zoneOriginPosition.y + miny, zone.getWidth(), zone
                                .getHeight()));
        if (drawingSpecifications.isJustified()) {
            allZones.add(zone);
        }
        zone = null;
    }

    /**
     * Visitor used to layout individual elements on the whole document.
     * <p>
     * The internal layout of elements is done in SimpleLayout ; here we only
     * deal the position of the views on the global view.
     * <p>
     * More precisely, all visit.... methods should
     * <ul>
     * <li>add the element to the current zone, and prepare a new zone if
     * necessary (line breaks, page breaks...)
     * <li>
     * <li>change the nextviewposition of the current subview, and/or its
     * startPoint.
     * </ul>
     *
     * @author rosmord
     */
    private class LineLayoutAux extends ModelElementAdapter {

        /**
         * Called when a new line is met.
         * <ul>
         * <li>add the current zone to the page
         * <li>prepare a new zone.
         * </ul>
         *
         * @see
         * jsesh.mdc.model.ModelElementAdapter#visitLineBreak(jsesh.mdc.model.LineBreak)
         */
        @Override
        public void visitLineBreak(LineBreak b) {

            // compute the coordinate of the top-left point for the following
            // line (mesured relatively to the top-left point of the current
            // zone).
            // that is : this line's height, plus the necessary skip, plus extra
            // spacing for -!=...% notation.
            double verticalSkip = (zone.getHeight() + zone.getMinY() + drawingSpecifications
                    .getLineSkip());
            if (b.getSpacing() != 100) {
                verticalSkip = verticalSkip * b.getSpacing() / 100f;
            }

            // add the line break to the current line
            zone.add(subView);
            // flush the line.
            flushZone();

            // create a new zone.
            zone = new Zone(0, drawingSpecifications.getMaxCadratHeight());
            zoneOriginPosition.y += verticalSkip;
            addTopMargin = false;

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * jsesh.mdc.model.ModelElementAdapter#visitPageBreak(jsesh.mdc.model
         * .PageBreak)
         */
        @Override
        public void visitPageBreak(PageBreak b) {
            double dh = zone.getHeight();
            // flush the current line.
            flushZone();
            // If the page break starts a new page, we won't draw anything.
            if (!drawingSpecifications.isPaged()) {

                // build a zone containing only the page break.
                zone = new Zone(0f, 0f);
                zoneOriginPosition.y += drawingSpecifications.getLineSkip() + dh;

                subView.setWidth(32f);
                subView.setHeight(drawingSpecifications.getLineSkip());
                zone.add(subView);
                // We cheat : the subview's dimension is very large, but its width
                // for computation purposes
                // was 0.
                flushZone();
                // Zone for first line of the next page
                zone = new Zone(0, drawingSpecifications.getMaxCadratHeight());
            } else {
                initZoneForPage();
            }
            addTopMargin = true;
        }

        /*
         * (non-Javadoc)
         * 
         * For simplicity sake, we consider that tabs are sets relatively to the
         * current zone. <p> If this wasn't the case, we should create a new
         * zone after a tab. <p> For right-to-left text, the tabs are defined in
         * terms of the EAST side of the zone.
         * 
         * @see
         * jsesh.mdc.model.ModelElementAdapter#visitTabStop(jsesh.mdc.model.
         * TabStop)
         */
        @Override
        public void visitTabStop(TabStop tab) {
            float pos = tab.getStopPos()
                    * drawingSpecifications.getTabUnitWidth();
            zone.getCurrentPoint().setLocation(pos, 0);
            zone.add(subView);
        }

        /**
         * Layout alphabetic text.
         * <p>
         * Note that alphabetic text is special, because a sequence of element
         * will always be laid out in a given orientation which depends</em>
         * only</em> on the text writing system. (currently, always
         * left-to-right, but if arabic and hebrew are added, this will change).
         *
         * @param t
         */
        @Override
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
         * @see
         * jsesh.mdc.model.ModelElementAdapter#visitDefault(jsesh.mdc.model.ModelElement)
         */
        @Override
        public void visitDefault(ModelElement t) {
            double dx = 0, dy = 0;
            // prepare a small skip after v, if necessary
            // the skip is "integrated" in v,

            if (subView.getWidth() != 0) {
                dx = subView.getWidth() + drawingSpecifications.getSmallSkip();
            }
            // nextViewPosition = subView.getNextViewPosition();
            zone.add(subView);
            zone.moveCurrentPoint(dx, dy);
        }
    }

}
