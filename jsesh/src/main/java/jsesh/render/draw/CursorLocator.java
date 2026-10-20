package jsesh.render.draw;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jsesh.model.AlphabeticCharacter;
import jsesh.model.MDCPosition;
import jsesh.model.ModelElement;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.render.style.GeometrySpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;

/// Computes where the cursor should be drawn, from the view tree only.
/// Used through [TextGeometry#cursorAt(MDCPosition)].
///
/// A cursor stands in a gap of a container: between two subviews of a view (or
/// at one of its ends). To find it, we descend the view tree along the parent
/// path of the position, then look at the subviews on both sides of the gap.
/// Nothing here depends on the drawing recursion of [ViewDrawer], or on a
/// `Graphics2D`.
///
/// Inside a quadrat, the direction of the cursor is read from the geometry
/// of the subviews around the gap, as the layout has placed them. Only a few
/// questions are asked to the model: whether the edge gaps of the container
/// are stops ([ModelElement#edgeGapsAreStops()],
/// [ModelElement#edgeGapsAreInnerStops()]), whether its children are stacked
/// ([ModelElement#stacksChildren()], for the edges) and whether they are
/// placed freely ([ModelElement#placesChildrenFreely()]).
final class CursorLocator {

    private final PlacedView root;

    private final JSeshStyle style;

    /// @param root the view of the whole text.
    /// @param style the style used to build the views.
    CursorLocator(PlacedView root, JSeshStyle style) {
        this.root = root;
        this.style = style;
    }

    /// Finds the cursor shape for a position.
    ///
    /// @param position the position of the cursor, possibly inside a quadrat.
    /// @return the shape of the cursor, or empty if the position doesn't match the
    /// view (for instance if the view is not up to date).
    Optional<CursorShape> locate(MDCPosition position) {
        return root.follow(position.getParentPath()).flatMap(container -> {
            int gap = position.getLastIndex();
            if (gap < 0 || gap > container.childCount()) {
                return Optional.empty();
            }
            Optional<CursorShape> local = position.isTop()
                    ? locateInText(container, gap)
                    : locateInContainer(container, gap);
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
    /// subviews around the gap. Its direction is read from the geometry of
    /// these subviews, as the layout has placed them, not from the kind of
    /// container: elements side by side are separated by a vertical line,
    /// elements one above the other by a horizontal one.
    ///
    /// The first and last gaps of a container have a cursor of their own in a
    /// list of quadrats, and in a quadrat or a row (where it is inside the
    /// container, at its edge: [ModelElement#edgeGapsAreInnerStops()]). In the
    /// other containers, they are at the same place as the cursor outside the
    /// container, and have none.
    private Optional<CursorShape> locateInContainer(PlacedView container, int gap) {
        int n = container.childCount();
        ModelElement model = container.model();
        Line2D line;
        if (model.edgeGapsAreStops() && (gap == 0 || gap == n)) {
            line = edgeLine(container, gap, style.options().textOrientation() == TextOrientation.HORIZONTAL);
        } else if (model.edgeGapsAreInnerStops() && (gap == 0 || gap == n)) {
            line = edgeLine(container, gap, !model.stacksChildren());
        } else if (gap <= 0 || gap >= n) {
            return Optional.empty();
        } else if (model.placesChildrenFreely()) {
            line = endEdge(container, container.childBounds(gap - 1));
        } else {
            line = lineBetween(container, container.childBounds(gap - 1), container.childBounds(gap));
        }
        return Optional.of(new CursorShape(List.of(line), false));
    }

    /// The cursor at the first or last gap of a container: a list of quadrats
    /// (inside a cartouche...), a quadrat or a row.
    ///
    /// @param gap 0, or the number of children of `container`.
    /// @param horizontal true if the children follow each other along the x axis
    /// (the line is then vertical); false if along the y axis.
    private static Line2D edgeLine(PlacedView container, int gap, boolean horizontal) {
        int n = container.childCount();
        boolean leftToRight = isLeftToRight(container);
        MDCView v = container.view();
        if (n == 0) {
            return crossingLine(container, horizontal,
                    horizontal ? v.getInternalWidth() / 2.0 : v.getInternalHeight() / 2.0);
        } else if (gap == 0) {
            Rectangle2D first = container.childBounds(0);
            return crossingLine(container, horizontal,
                    horizontal ? (leftToRight ? first.getMinX() : first.getMaxX()) : first.getMinY());
        } else {
            Rectangle2D last = container.childBounds(n - 1);
            return crossingLine(container, horizontal,
                    horizontal ? (leftToRight ? last.getMaxX() : last.getMinX()) : last.getMaxY());
        }
    }

    /// The children are placed freely, and may overlap: there is no gap
    /// between them to cut through. The gap is their order in the container,
    /// so we mark the end edge of the child which precedes it.
    private static Line2D endEdge(PlacedView container, Rectangle2D before) {
        double x = isLeftToRight(container) ? before.getMaxX() : before.getMinX();
        return new Line2D.Double(x, before.getMinY(), x, before.getMaxY());
    }

    /// A line which crosses the container between two areas.
    ///
    /// If the areas are apart along one axis only, the line cuts across this
    /// axis. Otherwise (they overlap, or are placed diagonally, as in a complex
    /// ligature), it cuts across the direction in which their centres are the
    /// farthest apart.
    private static Line2D lineBetween(PlacedView container, Rectangle2D before, Rectangle2D after) {
        boolean apartX = before.getMaxX() <= after.getMinX() || after.getMaxX() <= before.getMinX();
        boolean apartY = before.getMaxY() <= after.getMinY() || after.getMaxY() <= before.getMinY();
        boolean vertical = apartX != apartY
                ? apartX
                : Math.abs(before.getCenterX() - after.getCenterX()) > Math.abs(before.getCenterY() - after.getCenterY());
        if (vertical) {
            double x = (before.getMaxX() <= after.getMinX())
                    ? (before.getMaxX() + after.getMinX()) / 2
                    : (after.getMaxX() + before.getMinX()) / 2;
            return crossingLine(container, true, x);
        }
        double y = (before.getMaxY() <= after.getMinY())
                ? (before.getMaxY() + after.getMinY()) / 2
                : (after.getMaxY() + before.getMinY()) / 2;
        return crossingLine(container, false, y);
    }

    /// A line which crosses the whole container.
    ///
    /// @param vertical true for a vertical line, which will be at abscissa `coordinate`;
    /// false for a horizontal one, at ordinate `coordinate`.
    private static Line2D crossingLine(PlacedView container, boolean vertical, double coordinate) {
        MDCView v = container.view();
        if (vertical) {
            return new Line2D.Double(coordinate, 0, coordinate, v.getInternalHeight());
        }
        return new Line2D.Double(0, coordinate, v.getInternalWidth(), coordinate);
    }

    private static boolean isLeftToRight(PlacedView container) {
        return container.view().getDirection().isLeftToRight();
    }
}
