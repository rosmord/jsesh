package jsesh.render.draw;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jsesh.document.caret.MDCCaret;
import jsesh.model.AbsoluteGroup;
import jsesh.model.AlphabeticCharacter;
import jsesh.model.BasicItemList;
import jsesh.model.ComplexLigature;
import jsesh.model.HBox;
import jsesh.model.MDCPosition;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementAdapter;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.render.style.GeometrySpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;

/// Computes where the cursor should be drawn, from the view tree only.
///
/// A cursor stands in a gap of a container: between two subviews of a view (or
/// at one of its ends). To find it, we descend the view tree along the parent
/// path of the position, then look at the subviews on both sides of the gap.
/// Nothing here depends on the drawing recursion of [ViewDrawer], or on a
/// `Graphics2D`.
///
/// Inside a quadrat, the kind of container decides the shape of the cursor;
/// each kind has its own method in the visitor `ContainerCursor`.
///
/// The result is a [CursorShape], which can draw itself:
///
/// ```java
/// new CursorLocator(style).locate(view, caret).ifPresent(c -> c.paint(g, view, style));
/// ```
public final class CursorLocator {

    private final JSeshStyle style;

    /// @param style the style used to build the views.
    public CursorLocator(JSeshStyle style) {
        this.style = style;
    }

    /// Finds the cursor shape for the insertion point of a caret.
    ///
    /// @param rootView the view of the whole text.
    /// @param caret the caret (may be null).
    /// @return the shape of the cursor, or empty if there is no caret, or if
    /// it doesn't match the view.
    public Optional<CursorShape> locate(MDCView rootView, MDCCaret caret) {
        if (caret == null || caret.getInsert() == null) {
            return Optional.empty();
        }
        return locate(rootView, caret.getInsertExactPosition());
    }

    /// Finds the cursor shape for a position.
    ///
    /// @param rootView the view of the whole text.
    /// @param position the position of the cursor, possibly inside a quadrat.
    /// @return the shape of the cursor, or empty if the position doesn't match the
    /// view (for instance if the view is not up to date).
    public Optional<CursorShape> locate(MDCView rootView, MDCPosition position) {
        return PlacedView.root(rootView).follow(position.getParentPath()).flatMap(container -> {
            int gap = position.getLastIndex();
            if (gap < 0 || gap > container.childCount()) {
                return Optional.empty();
            }
            Optional<CursorShape> local = position.isTop()
                    ? locateInText(container, gap)
                    : new ContainerCursor(container, gap).locate();
            return local.map(shape -> shape.mapLines(container::toRoot));
        });
    }

    // ------------------------------------------------------------------
    // Cursor between the top-level items of a text.
    // ------------------------------------------------------------------

    private Optional<CursorShape> locateInText(PlacedView text, int gap) {
        GeometrySpecification geometry = style.geometry();
        int n = text.childCount();
        if (n == 0) {
            // Empty text: the cursor stands at the origin.
            Line2D line = style.options().textOrientation().isHorizontal()
                    ? new Line2D.Double(0, 0, 0, geometry.maxCadratHeight())
                    : new Line2D.Double(0, 0, geometry.maxCadratWidth(), 0);
            return Optional.of(new CursorShape(List.of(line), true));
        }
        int before = gap - 1;
        int after = gap;
        boolean hasBefore = gap > 0;
        boolean hasAfter = gap < n;

        // A cursor stands between two views: draw it at the end of the previous
        // one, or at the start of the next one.
        // We draw cursors next to regular elements, not breaks. Between two
        // breaks, we favour the "start of the next view" tactic.
        boolean drawBeforeAfterView = hasAfter;
        if (hasAfter && isBreak(text.subView(after)) && hasBefore && !isBreak(text.subView(before))) {
            drawBeforeAfterView = false;
        }
        boolean drawAfterBeforeView = hasBefore && !isBreak(text.subView(before));

        // A single line in the gap shows where an insertion will take place.
        if (drawAfterBeforeView && drawBeforeAfterView) {
            Optional<Line2D> merged = mergedLine(lineNextToView(text, before, true),
                    lineNextToView(text, after, false));
            if (merged.isPresent()) {
                return Optional.of(new CursorShape(List.of(merged.get()), true));
            }
        }

        List<Line2D> lines = new ArrayList<>(2);
        if (drawAfterBeforeView) {
            lines.add(lineNextToView(text, before, true));
        }
        if (drawBeforeAfterView) {
            lines.add(lineNextToView(text, after, false));
        }
        if (lines.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new CursorShape(lines, true));
    }

    /// A line halfway between the end of a view and the start of the next one.
    ///
    /// @return the line, or empty if the two lines are not parallel, or are not on the same
    /// row (or column) of text, as when a line wraps.
    private static Optional<Line2D> mergedLine(Line2D end, Line2D start) {
        if (isVerticalLine(end) && isVerticalLine(start)) {
            if (!overlap(end.getY1(), end.getY2(), start.getY1(), start.getY2())) {
                return Optional.empty();
            }
            double x = (end.getX1() + start.getX1()) / 2;
            return Optional.of(new Line2D.Double(x, Math.min(end.getY1(), start.getY1()),
                    x, Math.max(end.getY2(), start.getY2())));
        }
        if (isHorizontalLine(end) && isHorizontalLine(start)) {
            if (!overlap(end.getX1(), end.getX2(), start.getX1(), start.getX2())) {
                return Optional.empty();
            }
            double y = (end.getY1() + start.getY1()) / 2;
            return Optional.of(new Line2D.Double(Math.min(end.getX1(), start.getX1()), y,
                    Math.max(end.getX2(), start.getX2()), y));
        }
        return Optional.empty();
    }

    private static boolean isVerticalLine(Line2D l) {
        return l.getX1() == l.getX2();
    }

    private static boolean isHorizontalLine(Line2D l) {
        return l.getY1() == l.getY2();
    }

    private static boolean overlap(double a1, double a2, double b1, double b2) {
        return Math.max(Math.min(a1, a2), Math.min(b1, b2)) < Math.min(Math.max(a1, a2), Math.max(b1, b2));
    }

    private static boolean isBreak(MDCView v) {
        return v.getModel() != null && v.getModel().isBreak();
    }

    /// A line standing just after or just before a top-level view.
    ///
    /// @param text the view for the whole text.
    /// @param i the index of a subview of `text`.
    /// @param after true to place the line after the subview (in reading order), false for before.
    /// @return the line, in the coordinate system used to draw the subviews of `text`.
    private Line2D lineNextToView(PlacedView text, int i, boolean after) {
        GeometrySpecification geometry = style.geometry();
        TextDirection textDirection = style.options().textDirection();
        TextOrientation textOrientation = style.options().textOrientation();
        MDCView v = text.subView(i);
        // Next to alphabetic characters, the cursor follows the
        // text's own direction, and the height of the letters.
        boolean horizontalCursor = textOrientation.isHorizontal();
        boolean leftToRight = textDirection.isLeftToRight();
        double cursorHeight = geometry.maxCadratHeight();
        if (v.getModel() instanceof AlphabeticCharacter c) {
            horizontalCursor = true;
            leftToRight = !c.getScript().isRightToLeft();
            cursorHeight = v.getHeight();
        }
        // Local coordinates, origin at the top left corner of v.
        Line2D line;
        if (horizontalCursor) {
            // The line is at the right of v if we are after v in a left-to-right text,
            // or before v in a right-to-left one.
            double x = (after == leftToRight) ? v.getWidth() : 0;
            line = new Line2D.Double(x, 0, x, cursorHeight);
        } else { // columns
            double y = after ? v.getHeight() : 0;
            line = new Line2D.Double(0, y, geometry.maxCadratWidth(), y);
        }
        Rectangle2D bounds = text.childBounds(i);
        double dx = bounds.getMinX();
        double dy = bounds.getMinY();
        return new Line2D.Double(line.getX1() + dx, line.getY1() + dy, line.getX2() + dx, line.getY2() + dy);
    }

    // ------------------------------------------------------------------
    // Cursor inside a quadrat, a cartouche...
    // ------------------------------------------------------------------

    /// The cursor in a gap of a container below the top level.
    ///
    /// The cursor is a line which crosses the container, between the two
    /// subviews around the gap. Its direction depends on the kind of
    /// container, hence one `visit` method for each kind.
    ///
    /// Except in a list of quadrats, the first and last gaps of a container
    /// have no cursor of their own: they are at the same place as the cursor
    /// outside the container.
    private final class ContainerCursor extends ModelElementAdapter {

        private final PlacedView container;

        private final int gap;

        /// The line, in the system used to draw the subviews of the container.
        private Line2D line;

        ContainerCursor(PlacedView container, int gap) {
            this.container = container;
            this.gap = gap;
        }

        Optional<CursorShape> locate() {
            container.model().accept(this);
            return Optional.ofNullable(line).map(l -> new CursorShape(List.of(l), false));
        }

        /// Quadrats follow each other along the line (or the column) of text.
        /// The edges of the list are stops too (inside a cartouche...).
        @Override
        public void visitBasicItemList(BasicItemList l) {
            int n = container.childCount();
            boolean leftToRight = isLeftToRight();
            boolean horizontal = style.options().textOrientation() == TextOrientation.HORIZONTAL;
            MDCView v = container.view();
            if (n == 0) {
                line = crossingLine(horizontal,
                        horizontal ? v.getInternalWidth() / 2.0 : v.getInternalHeight() / 2.0);
            } else if (gap == 0) {
                Rectangle2D first = container.childBounds(0);
                line = crossingLine(horizontal,
                        horizontal ? (leftToRight ? first.getMinX() : first.getMaxX()) : first.getMinY());
            } else if (gap == n) {
                Rectangle2D last = container.childBounds(n - 1);
                line = crossingLine(horizontal,
                        horizontal ? (leftToRight ? last.getMaxX() : last.getMinX()) : last.getMaxY());
            } else {
                line = lineBetween(before(), after(), horizontal);
            }
        }

        /// The signs are placed freely, and may overlap: there is no gap between
        /// them to cut through. The gap is their order in the group, so we mark
        /// the end edge of the sign which precedes it.
        @Override
        public void visitAbsoluteGroup(AbsoluteGroup g) {
            if (hasInnerGap()) {
                Rectangle2D before = before();
                double x = isLeftToRight() ? before.getMaxX() : before.getMinX();
                line = new Line2D.Double(x, before.getMinY(), x, before.getMaxY());
            }
        }

        /// Elements side by side: vertical line.
        @Override
        public void visitHBox(HBox b) {
            if (hasInnerGap()) {
                line = lineBetween(before(), after(), true);
            }
        }

        /// The parts are arranged freely around the main sign. Cut across the
        /// direction which separates them.
        @Override
        public void visitComplexLigature(ComplexLigature ligature) {
            if (hasInnerGap()) {
                Rectangle2D before = before();
                Rectangle2D after = after();
                double dx = Math.abs(before.getCenterX() - after.getCenterX());
                double dy = Math.abs(before.getCenterY() - after.getCenterY());
                line = lineBetween(before, after, dx > dy);
            }
        }

        /// Other containers (quadrats, ligatures...): elements one above the
        /// other, horizontal line.
        @Override
        public void visitDefault(ModelElement e) {
            if (hasInnerGap()) {
                line = lineBetween(before(), after(), false);
            }
        }

        private boolean hasInnerGap() {
            return gap > 0 && gap < container.childCount();
        }

        private boolean isLeftToRight() {
            return container.view().getDirection().isLeftToRight();
        }

        private Rectangle2D before() {
            return container.childBounds(gap - 1);
        }

        private Rectangle2D after() {
            return container.childBounds(gap);
        }

        /// A line between two areas.
        ///
        /// @param vertical true for a vertical line (areas side by side).
        private Line2D lineBetween(Rectangle2D before, Rectangle2D after, boolean vertical) {
            if (vertical) {
                double x = (before.getMaxX() <= after.getMinX())
                        ? (before.getMaxX() + after.getMinX()) / 2
                        : (after.getMaxX() + before.getMinX()) / 2;
                return crossingLine(true, x);
            }
            double y = (before.getMaxY() <= after.getMinY())
                    ? (before.getMaxY() + after.getMinY()) / 2
                    : (after.getMaxY() + before.getMinY()) / 2;
            return crossingLine(false, y);
        }

        /// A line which crosses the whole container.
        ///
        /// @param vertical true for a vertical line, which will be at abscissa `coordinate`;
        /// false for a horizontal one, at ordinate `coordinate`.
        private Line2D crossingLine(boolean vertical, double coordinate) {
            MDCView v = container.view();
            if (vertical) {
                return new Line2D.Double(coordinate, 0, coordinate, v.getInternalHeight());
            }
            return new Line2D.Double(0, coordinate, v.getInternalWidth(), coordinate);
        }
    }
}
