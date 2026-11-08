/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 23 dec. 2004
 *
 */
package jsesh.render.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

import jsesh.sharedvocabulary.KerningAxis;
import jsesh.render.layout.insert.Kerner;

import jsesh.render.style.GeometrySpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.render.style.PageSpecification;
import jsesh.model.constants.TextDirection;
import jsesh.model.AlphabeticCharacter;
import jsesh.model.HRule;
import jsesh.model.LineBreak;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementAdapter;
import jsesh.model.PageBreak;
import jsesh.model.AbsoluteTab;
import jsesh.model.Tab;
import jsesh.render.view.MDCView;
import jsesh.sharedvocabulary.LineAlignment;

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
    private LineAccumulator zone;

    /// A line placed in the document: its views, and its vertical extent in
    /// document coordinates.
    private record PlacedLine(LineAccumulator line, double top, double height) {
    }

    /// The lines placed so far, with the alignment they will get in
    /// [#endLayout()].
    private final List<PlacedLine> alignedLines = new ArrayList<>();

    /// Their alignments (same indexes as [#alignedLines]).
    private final List<LineAlignment> alignments = new ArrayList<>();

    /// The lines of the current *logical line* (from the previous line break):
    /// several when it is soft-wrapped. Their alignment is only known at the
    /// line break which ends it.
    private final List<PlacedLine> currentLogicalLine = new ArrayList<>();

    /// The alignment of the last line ended by a line break: the last line of
    /// the text, if it has no line break, takes it.
    private LineAlignment previousAlignment = LineAlignment.START;

    /// Has the current logical line been soft-wrapped? Only the tabs before
    /// its first soft wrap are active.
    private boolean wrappedLogicalLine;

    /// The lines whose cells were placed by [TabLayout]: [#alignLines()]
    /// leaves them alone.
    private final Set<LineAccumulator> tabbedLines = Collections.newSetFromMap(new IdentityHashMap<>());

    /// The vertical extent (top, bottom) of each line of text, for the separators.
    private final List<double[]> lineExtents = new ArrayList<>();

    /**
     * the position that will be used to place the current zone
     */

    private Point2D.Double zoneOriginPosition;

    private final JSeshStyle jseshStyle;

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

    /// The number of the page of the last line placed, when the text is
    /// paginated (see [PageSpecification#isPaginated()]).
    private int pageIndex;

    private final LineLayoutAux aux = new LineLayoutAux();

    private final Kerner kerner;

    /// The elements of the current kerned group (since the last element which was not kerned).
    private final List<MDCView> kernedGroup = new ArrayList<>();

    /// Is there an `%hkern` operator after the current kerned group?
    private boolean pendingKerning;

    /// The kerning operators met since the last element; they are moved to the middle of the gap.
    private final List<MDCView> kerningViews = new ArrayList<>();

    /**
     * @param documentView
     * @param jseshStyle
     */
    public LineLayout(MDCView documentView,
            JSeshStyle jseshStyle, Kerner kerner) {
        super();
        this.kerner = kerner;
        this.documentView = documentView;
        documentArea = new Rectangle2D.Double();
        insertionPoint = new Point2D.Double();
        this.jseshStyle = jseshStyle;
        currentTextDirection = jseshStyle.options().textDirection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jsesh.render.draw.TopItemLayout#layoutElement(jsesh.mdcDisplayer
     * .mdcView.MDCView)
     */
    @Override
    public void layoutElement(MDCView subView) {
        this.subView = subView;
        if (fillLayout() != null) {
            fillLayout().prepare(subView);
        }
        subView.getPosition().setLocation(0, 0);
        subView.getModel().accept(aux);
        this.subView = null;
    }

    /// Places the run as a block, in its own reading direction: when the
    /// hieroglyphic text is right to left, the characters are mirrored here, so
    /// that drawing mirrors them back.
    @Override
    public void layoutTextRun(TextRun run, List<MDCView> views) {
        resetKerning();
        boolean mirrored = !currentTextDirection.isLeftToRight();
        if (!jseshStyle.geometry().isWrapping()) {
            placeRunPart(run, views, 0, views.size(), mirrored);
            return;
        }
        // Wrapping: the run is cut at spaces and punctuation, or anywhere if a
        // word is too long for a line.
        int n = views.size();
        boolean[] breakAfter = new boolean[n];
        boolean[] space = new boolean[n];
        for (int k = 0; k < n; k++) {
            AlphabeticCharacter c = (AlphabeticCharacter) views.get(k).getModel();
            breakAfter[k] = TextRun.canBreakAfter(c);
            space[k] = c.isSpace();
        }
        int from = 0;
        while (from < n) {
            int to = runPartEnd(run, from, breakAfter, space);
            placeRunPart(run, views, from, to, mirrored);
            from = to;
            if (from < n) {
                wrapLine();
            }
        }
    }

    /// Adds the characters `from` to `to - 1` of a run to the current line.
    private void placeRunPart(TextRun run, List<MDCView> views, int from, int to, boolean mirrored) {
        zone.addRun(views.subList(from, to), run.layoutOffsets(from, to, mirrored));
        double width = run.width(from, to);
        if (width != 0) {
            zone.moveCurrentPoint(width + jseshStyle.geometry().smallSkip(), 0);
        }
    }

    /// Finds how much of a run, starting at the character `from`, goes on the
    /// current line. If nothing fits, the line is wrapped (unless it is empty).
    ///
    /// The part ends after a space or punctuation. If not even a word fits on an
    /// empty line, it is cut between any two characters. Trailing spaces may
    /// go beyond the end of the line.
    ///
    /// @return the index after the last character to place on the line (greater than `from`).
    private int runPartEnd(TextRun run, int from, boolean[] breakAfter, boolean[] space) {
        int end = fittingEnd(run, from, breakAfter, space, true);
        if (end > from) {
            return end;
        }
        if (!zone.isEmpty()) {
            wrapLine();
            end = fittingEnd(run, from, breakAfter, space, true);
            if (end > from) {
                return end;
            }
        }
        // The word is larger than a line.
        return Math.max(from + 1, fittingEnd(run, from, breakAfter, space, false));
    }

    /// The end of the largest part of a run, starting at `from`, which fits the
    /// room left on the current line.
    ///
    /// @param atBreaksOnly true to end the part only after a space or
    /// punctuation (or at the end of the run).
    /// @return the index after the last character of the part, or `from` if nothing fits.
    private int fittingEnd(TextRun run, int from, boolean[] breakAfter, boolean[] space, boolean atBreaksOnly) {
        double available = maxLineWidth() - zone.getCurrentPoint().getX();
        int n = breakAfter.length;
        int best = from;
        for (int end = from + 1; end <= n; end++) {
            // Trailing spaces are not counted.
            int visibleEnd = end;
            while (visibleEnd > from + 1 && space[visibleEnd - 1]) {
                visibleEnd--;
            }
            if (run.width(from, visibleEnd) > available) {
                break;
            }
            if (!atBreaksOnly || end == n || breakAfter[end - 1]) {
                best = end;
            }
        }
        return best;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.render.draw.TopItemLayout#endLayout()
     */
    @Override
    public void endLayout() {
        if (!zone.isEmpty()) {
            flushZone();
        }
        endLogicalLine(previousAlignment);
        fitRules();
        layoutTabs();
        fillZones();
        if (!jseshStyle.options().ignoreLineAlignment()) {
            alignLines();
        }
        if (paginated()) {
            // The view covers all the pages.
            PageSpecification page = jseshStyle.geometry().page();
            documentArea.add(new Rectangle2D.Double(0, 0, page.pageWidth(),
                    pageIndex * (double) page.pageStride() + page.pageHeight()));
        }
        // Ensure the margin is there ?
        documentArea
                .add(new Point2D.Double(
                                documentArea.getMaxX()
                                + jseshStyle.geometry()
                                .leftMargin(), documentArea
                                .getMinY()
                                + jseshStyle.geometry()
                                .topMargin()));
    }

    /// Is the text cut into pages?
    private boolean paginated() {
        return jseshStyle.geometry().page().isPaginated();
    }

    /// Moves the current line to the top of the next page if it doesn't fit in
    /// the current one. A line which is alone on its page stays, however tall.
    ///
    /// @param marginy the top margin which will be added to the line's origin.
    private void avoidPageBottom(double marginy) {
        PageSpecification page = jseshStyle.geometry().page();
        double pageTop = pageIndex * (double) page.pageStride();
        double top = zoneOriginPosition.y + marginy;
        double bottom = top + zone.getHeight();
        double limit = pageTop + page.pageHeight() - page.marginBottom();
        if (bottom > limit && top > pageTop + page.marginTop() + 1e-3) {
            pageIndex++;
            double newTop = pageIndex * (double) page.pageStride() + page.marginTop();
            zoneOriginPosition.y = newTop - marginy;
        }
    }

    /// Ends the current logical line: gives its alignment to its lines.
    ///
    /// When a justified logical line is soft-wrapped, its last line is placed
    /// at the start, as the last line of a paragraph. Otherwise, justification
    /// always applies.
    ///
    /// @param alignment the alignment written on the line break which ends it.
    private void endLogicalLine(LineAlignment alignment) {
        for (int i = 0; i < currentLogicalLine.size(); i++) {
            PlacedLine line = currentLogicalLine.get(i);
            LineAlignment a = alignment;
            boolean lastOfWrapped = i > 0 && i == currentLogicalLine.size() - 1;
            if (a == LineAlignment.JUSTIFY && lastOfWrapped) {
                a = LineAlignment.START;
            }
            alignedLines.add(line);
            alignments.add(a);
        }
        currentLogicalLine.clear();
    }

    /// Widens the horizontal rules ([HRule]) to the width of the text, when
    /// there is no page nor zone width to fill (see `Layout.visitHRule`, which
    /// sets their width otherwise).
    ///
    /// The width of the text is the one of its longest line, the rules counting
    /// for their minimal width. A rule alone on its line (the normal case) gets
    /// that width; a rule which follows other elements gets what is left.
    private void fitRules() {
        if (jseshStyle.geometry().isWrapping()) {
            return;
        }
        double margin = jseshStyle.geometry().leftMargin();
        double width = 0;
        boolean hasRule = false;
        for (PlacedLine l : alignedLines) {
            hasRule |= l.line().getViews().stream().anyMatch(v -> v.getModel() instanceof HRule);
            Rectangle2D bounds = l.line().visibleBounds(0, 0);
            if (bounds != null) {
                width = Math.max(width, bounds.getMaxX() - margin);
            }
        }
        if (!hasRule) {
            return;
        }
        for (PlacedLine l : alignedLines) {
            boolean changed = false;
            for (MDCView v : l.line().getViews()) {
                if (v.getModel() instanceof HRule) {
                    double fitted = width - (v.getPosition().x - margin);
                    if (fitted > v.getWidth()) {
                        v.setWidth((float) fitted);
                        changed = true;
                    }
                }
            }
            if (changed) {
                Rectangle2D bounds = l.line().visibleBounds(l.top(), l.height());
                if (bounds != null) {
                    documentArea.add(bounds);
                }
            }
        }
    }

    /// Places the cells of the lines which have tabs on their tab stops
    /// (see [TabLayout]). The cell after the last tab takes the alignment of
    /// the line, in the width of the parent if it has one, else in the width
    /// of the longest line.
    private void layoutTabs() {
        if (!tabsEnabled()) {
            return;
        }
        double margin = jseshStyle.geometry().leftMargin();
        boolean ignoreAlignment = jseshStyle.options().ignoreLineAlignment();
        List<TabLayout.TabbedLine> lines = new ArrayList<>();
        double untabbedWidth = 0;
        for (int i = 0; i < alignedLines.size(); i++) {
            LineAccumulator line = alignedLines.get(i).line();
            if (line.hasTabs()) {
                LineAlignment a = ignoreAlignment ? LineAlignment.START : alignments.get(i);
                lines.add(new TabLayout.TabbedLine(line, margin, a));
                tabbedLines.add(line);
            } else {
                Rectangle2D bounds = line.visibleBounds(0, 0);
                if (bounds != null) {
                    untabbedWidth = Math.max(untabbedWidth, bounds.getMaxX() - margin);
                }
            }
        }
        if (lines.isEmpty()) {
            return;
        }
        double available = jseshStyle.geometry().isWrapping() ? maxLineWidth() : 0;
        new TabLayout(false, tabGap()).apply(lines, available, untabbedWidth);
        for (PlacedLine l : alignedLines) {
            if (tabbedLines.contains(l.line())) {
                Rectangle2D bounds = l.line().visibleBounds(l.top(), l.height());
                if (bounds != null) {
                    documentArea.add(bounds);
                }
            }
        }
    }

    /// Stretches the zones which fill their line (see [FillLayout]): each line
    /// is filled up to the width of the parent if it has one (a page, or a
    /// zone with a width), else up to the width of the longest line. The
    /// lines with tabs are left alone.
    private void fillZones() {
        FillLayout fill = fillLayout();
        if (fill == null) {
            return;
        }
        if (fill.stretches()) {
            double margin = jseshStyle.geometry().leftMargin();
            double width = 0;
            if (jseshStyle.geometry().isWrapping()) {
                width = maxLineWidth();
            } else {
                for (PlacedLine l : alignedLines) {
                    Rectangle2D bounds = l.line().visibleBounds(0, 0);
                    if (bounds != null) {
                        width = Math.max(width, bounds.getMaxX() - margin);
                    }
                }
            }
            for (PlacedLine l : alignedLines) {
                if (tabbedLines.contains(l.line())) {
                    continue;
                }
                Rectangle2D bounds = l.line().visibleBounds(0, 0);
                double used = bounds == null ? 0 : bounds.getMaxX() - margin;
                if (fill.fillLine(l.line(), width - used)) {
                    Rectangle2D filled = l.line().visibleBounds(l.top(), l.height());
                    if (filled != null) {
                        documentArea.add(filled);
                    }
                }
            }
        }
        fill.finish();
    }

    /// Places the lines according to their alignment, in the width of the
    /// parent if it has one (a page, or a zone with a width), else in the
    /// width of the longest line.
    private void alignLines() {
        if (!alignments.stream().anyMatch(a -> a != LineAlignment.START)) {
            return;
        }
        double width;
        if (jseshStyle.geometry().isWrapping()) {
            width = maxLineWidth();
        } else {
            // The right side of the visible content of the longest line
            // (lines are placed at the margin).
            width = 0;
            for (PlacedLine l : alignedLines) {
                Rectangle2D bounds = l.line().visibleBounds(0, 0);
                if (bounds != null) {
                    width = Math.max(width, bounds.getMaxX() - jseshStyle.geometry().leftMargin());
                }
            }
        }
        double margin = jseshStyle.geometry().leftMargin();
        for (int i = 0; i < alignedLines.size(); i++) {
            PlacedLine l = alignedLines.get(i);
            if (alignments.get(i) != LineAlignment.START && !tabbedLines.contains(l.line())) {
                l.line().alignTo(alignments.get(i), margin, width);
                Rectangle2D bounds = l.line().visibleBounds(l.top(), l.height());
                if (bounds != null) {
                    documentArea.add(bounds);
                }
            }
        }
    }

    /// The width available for the content of a line when wrapping: the page's
    /// usable width if there is a page, or else the wrapping width, minus the
    /// margins on both sides.
    private double maxLineWidth() {
        return jseshStyle.geometry().maxLineWidth();
    }

    /// Should the current line be wrapped before adding something which would
    /// extend it up to `x`?
    ///
    /// A line is never wrapped when empty: an element too large for the
    /// wrapping width is placed alone on its line.
    ///
    /// @param x the position of the right side of the new content, in the
    /// zone's coordinates.
    private boolean wouldOverflow(double x) {
        return jseshStyle.geometry().isWrapping() && !zone.isEmpty() && x > maxLineWidth();
    }

    /// Ends the current line because of soft wrapping, and starts the next one.
    private void wrapLine() {
        resetKerning();
        wrappedLogicalLine = true;
        double verticalSkip = zone.getHeight() + zone.getMinY() + jseshStyle.geometry().lineSkip();
        flushZone();
        startNextZone(verticalSkip);
    }

    /// Creates the zone for the next line.
    ///
    /// @param verticalSkip the distance between the top of the line which has just
    /// been flushed and the top of the new one.
    private void startNextZone(double verticalSkip) {
        zone = new LineAccumulator(0, jseshStyle.geometry().maxCadratHeight());
        zoneOriginPosition.y += verticalSkip;
        addTopMargin = false;
    }

    @Override
    public double[] getSeparatorPositions() {
        return separatorsBetween(lineExtents);
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
     * @see jsesh.render.draw.TopItemLayout#initState()
     */
    @Override
    public void startLayout() {
        alignedLines.clear();
        alignments.clear();
        currentLogicalLine.clear();
        tabbedLines.clear();
        wrappedLogicalLine = false;
        previousAlignment = LineAlignment.START;
        pageIndex = 0;
        // Pseudo relative position for first element.
        addTopMargin = true;
        // nextViewPosition = documentView.getFirstSubViewPosition();
        resetView();
        documentView.setDirection(currentTextDirection);
        insertionPoint.setLocation(0f, 0f);
        initZoneForPage();
    }

    private void resetKerning() {
        kernedGroup.clear();
        pendingKerning = false;
        kerningViews.clear();
    }

    private void initZoneForPage() {
        zone = new LineAccumulator(0, jseshStyle.geometry().maxCadratHeight());
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
        flushZone(true);
    }

    /// @param textLine is the zone a line of text (and not the mark of a page
    /// break)? Only lines are separated by rules.
    private void flushZone(boolean textLine) {

        // We are supposed to add a new line to the text.
        // In the line coord. system, 0,0 is supposed to be the upper left
        // point.
        // However, some large signs (cartouches, for instance) can be highter
        // than expected,
        // which causes the minimal y values to be negative.
        // Now, we don't want this to cause problems, such as very small line
        // skips, or, worse
        // parts of cartouches being cut.
        GeometrySpecification pageLayout = jseshStyle.geometry();

        // Compute the margins we want.
        float marginx = pageLayout.leftMargin();
        float marginy = 0f;

        if (addTopMargin) {
            marginy = pageLayout.topMargin();
        }

        // change the zoneStart for this zone, so that the real top
        // of the zone is separated from the previous line by at least margin.
        double minx = zone.getMinX();
        double miny = zone.getMinY();

        if (textLine && paginated()) {
            avoidPageBottom(marginy);
        }

        zoneOriginPosition.y = zoneOriginPosition.y + marginy - miny;
        zoneOriginPosition.x = marginx - minx;

        // fix the coordinates of the views in the zone to their final value.
        zone.translateBy(zoneOriginPosition);

        // flush the zone. Add it to the current document area.
        documentArea
                .add(new Rectangle2D.Double(zoneOriginPosition.x + minx,
                                zoneOriginPosition.y + miny, zone.getWidth(), zone
                                .getHeight()));
        if (textLine) {
            lineExtents.add(new double[] { zoneOriginPosition.y + miny,
                    zoneOriginPosition.y + miny + zone.getHeight() });
            currentLogicalLine.add(new PlacedLine(zone, zoneOriginPosition.y + miny, zone.getHeight()));
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
         * jsesh.model.ModelElementAdapter#visitLineBreak(jsesh.model.LineBreak)
         */
        @Override
        public void visitLineBreak(LineBreak b) {
            resetKerning();

            // compute the coordinate of the top-left point for the following
            // line (mesured relatively to the top-left point of the current
            // zone).
            // that is : this line's height, plus the necessary skip, plus extra
            // spacing for -!=...% notation.
            double verticalSkip = (zone.getHeight() + zone.getMinY() + jseshStyle.geometry()
                    .lineSkip());
            if (b.getSpacing() != 100) {
                verticalSkip = verticalSkip * b.getSpacing() / 100f;
            }

            // add the line break to the current line
            zone.add(subView);
            // flush the line.
            flushZone();
            endLogicalLine(b.getAlignment());
            previousAlignment = b.getAlignment();
            wrappedLogicalLine = false;

            // create a new zone.
            startNextZone(verticalSkip);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * jsesh.model.ModelElementAdapter#visitPageBreak(jsesh.model
         * .PageBreak)
         */
        @Override
        public void visitPageBreak(PageBreak b) {
            resetKerning();
            double dh = zone.getHeight();
            // flush the current line.
            flushZone();
            endLogicalLine(b.getAlignment());
            previousAlignment = b.getAlignment();
            wrappedLogicalLine = false;
            if (paginated()) {
                // Pages are stacked: the next line goes to the top of the next page.
                pageIndex++;
                zone = new LineAccumulator(0, jseshStyle.geometry().maxCadratHeight());
                zoneOriginPosition.y = pageIndex * (double) jseshStyle.geometry().page().pageStride();
            } else if (!jseshStyle.options().paged()) {
                // If the page break starts a new page, we won't draw anything.

                // build a zone containing only the page break.
                zone = new LineAccumulator(0f, 0f);
                zoneOriginPosition.y += jseshStyle.geometry().lineSkip() + dh;

                subView.setWidth(32f);
                subView.setHeight(jseshStyle.geometry().lineSkip());
                zone.add(subView);
                // We cheat : the subview's dimension is very large, but its width
                // for computation purposes
                // was 0.
                flushZone(false);
                // LineAccumulator for first line of the next page
                zone = new LineAccumulator(0, jseshStyle.geometry().maxCadratHeight());
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
         * jsesh.model.ModelElementAdapter#visitAbsoluteTab(jsesh.model.
         * AbsoluteTab)
         */
        @Override
        public void visitAbsoluteTab(AbsoluteTab tab) {
            resetKerning();
            if (tabsEnabled() && zone.hasTabs()) {
                // Absolute positions mean nothing after a dynamic tab.
                zone.add(subView);
                return;
            }
            float pos = tab.getStopPos()
                    * jseshStyle.geometry().tabUnitWidth();
            zone.getCurrentPoint().setLocation(pos, 0);
            zone.add(subView);
        }

        /// A dynamic tab ends the current tab cell, if tabs are laid out, and if
        /// the logical line has not been soft-wrapped yet. Otherwise, it is an
        /// empty element.
        @Override
        public void visitTab(Tab tab) {
            resetKerning();
            if (tabsEnabled() && !wrappedLogicalLine) {
                zone.addTab(subView, tab.getIndex(), tab.getAlignment());
            } else {
                zone.add(subView);
            }
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
         * jsesh.model.ModelElementAdapter#visitDefault(jsesh.model.ModelElement)
         */
        @Override
        public void visitDefault(ModelElement t) {
            double dx = 0, dy = 0;
            // prepare a small skip after v, if necessary
            // the skip is "integrated" in v,

            if (subView.getWidth() != 0) {
                dx = subView.getWidth() + jseshStyle.geometry().smallSkip();
            }
            Optional<KerningAxis> kerning = KerningPacker.kerningOf(subView);
            // A kerned element stays with the previous one: no wrapping before it.
            if (kerning.isEmpty() && !pendingKerning && subView.getWidth() != 0
                    && wouldOverflow(zone.computeNewSizeWith(subView).getMaxX())) {
                wrapLine();
            }
            if (kerning.isPresent()) {
                // Takes no room. In lines, only %hkern is meaningful.
                pendingKerning |= !kernedGroup.isEmpty() && kerning.get() == KerningAxis.HORIZONTAL;
                // In the unit of the previous element, so that justification keeps kerned elements together.
                zone.addKerned(subView);
                kerningViews.add(subView);
                return;
            }
            if (pendingKerning) {
                // Place the view where it would go, and move it back.
                subView.getPosition().setLocation(zone.getCurrentPoint().getX() + subView.getDeltaBaseX(),
                        zone.getCurrentPoint().getY() + subView.getDeltaBaseY());
                double shift = kerner.shift(kernedGroup, subView, KerningAxis.HORIZONTAL,
                        jseshStyle.geometry().smallSkip());
                zone.moveCurrentPoint(-shift, 0);
                zone.addKerned(subView);
            } else {
                zone.add(subView);
            }
            if (!kernedGroup.isEmpty()) {
                MDCView last = kernedGroup.get(kernedGroup.size() - 1);
                double middle = (last.getPosition().x + last.getWidth() + subView.getPosition().x) / 2;
                for (MDCView k : kerningViews) {
                    k.getPosition().x = middle - k.getWidth() / 2;
                }
            }
            kerningViews.clear();
            if (!pendingKerning) {
                kernedGroup.clear();
            }
            pendingKerning = false;
            kernedGroup.add(subView);
            zone.moveCurrentPoint(dx, dy);
        }
    }

}
