package jsesh.render.draw;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jsesh.model.AbsoluteGroup;
import jsesh.model.AlphabeticCharacter;
import jsesh.model.BasicItemList;
import jsesh.model.ComplexLigature;
import jsesh.model.HBox;
import jsesh.model.MDCPosition;
import jsesh.model.ModelElement;
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
/// The resulting [Location] is expressed in the coordinate system used to draw the
/// subviews of the root view, that is, after applying
/// [MDCView#getAffineTransform()] of the root view.
public final class CursorLocator {

    /// Where to draw the cursor.
    ///
    /// @param lines the lines which make the cursor. In general, there is only one;
    /// between two top-level elements, there is one at the end of the previous
    /// element and one at the start of the next one.
    /// @param wide true for the (thick) cursor which stands between top-level items,
    /// false for the (thin) one inside a quadrat.
    public record Location(List<Line2D> lines, boolean wide) {

        public Location {
            lines = List.copyOf(lines);
        }

        /// @return the area covered by the cursor, in the same coordinates as the lines.
        public Rectangle2D bounds() {
            Rectangle2D result = null;
            for (Line2D line : lines) {
                if (result == null) {
                    result = line.getBounds2D();
                } else {
                    result.add(line.getBounds2D());
                }
            }
            return result;
        }
    }

    private CursorLocator() {
    }

    /// Finds the cursor shape for a position.
    ///
    /// @param rootView the view of the whole text.
    /// @param position the position of the cursor, possibly inside a quadrat.
    /// @param style the style used to build the views.
    /// @return the shape of the cursor, or empty if the position doesn't match the
    /// view (for instance if the view is not up to date).
    public static Optional<Location> locate(MDCView rootView, MDCPosition position, JSeshStyle style) {
        AffineTransform transform = new AffineTransform();
        MDCView container = rootView;
        ModelElement model = rootView.getModel();
        for (int i : position.getParentPath()) {
            if (model == null || i < 0 || i >= model.getNumberOfChildren() || i >= container.getNumberOfSubviews()) {
                return Optional.empty();
            }
            MDCView sub = container.getSubView(i);
            if (sub.getModel() != model.getChildAt(i)) {
                return Optional.empty();
            }
            // Same transformations as ViewDrawer.drawSubViews and drawView.
            transform.translate(originX(container, sub), sub.getPosition().y);
            transform.concatenate(sub.getAffineTransform());
            model = sub.getModel();
            container = sub;
        }
        int gap = position.getLastIndex();
        if (gap < 0 || gap > container.getNumberOfSubviews()) {
            return Optional.empty();
        }
        Optional<Location> shape;
        if (position.isTop()) {
            shape = locateInText(container, gap, style);
        } else {
            shape = locateInContainer(container, gap, style);
        }
        return shape.map(s -> new Location(
                s.lines().stream().map(l -> transformed(transform, l)).toList(), s.wide()));
    }

    private static Line2D transformed(AffineTransform transform, Line2D line) {
        if (transform.isIdentity()) {
            return line;
        }
        Point2D p1 = transform.transform(line.getP1(), null);
        Point2D p2 = transform.transform(line.getP2(), null);
        return new Line2D.Double(p1, p2);
    }

    /// Abscissa of the top-left corner of a subview, in the system used to draw the
    /// subviews of `container` (right-to-left containers are mirrored).
    private static double originX(MDCView container, MDCView sub) {
        if (container.getDirection().isLeftToRight()) {
            return sub.getPosition().x;
        }
        return container.getInternalWidth() - sub.getPosition().x - sub.getWidth();
    }

    // ------------------------------------------------------------------
    // Cursor between the top-level items of a text.
    // ------------------------------------------------------------------

    private static Optional<Location> locateInText(MDCView text, int gap, JSeshStyle style) {
        GeometrySpecification geometry = style.geometry();
        int n = text.getNumberOfSubviews();
        if (n == 0) {
            // Empty text: the cursor stands at the origin.
            Line2D line = style.options().textOrientation().isHorizontal()
                    ? new Line2D.Double(0, 0, 0, geometry.maxCadratHeight())
                    : new Line2D.Double(0, 0, geometry.maxCadratWidth(), 0);
            return Optional.of(new Location(List.of(line), true));
        }
        MDCView before = gap > 0 ? text.getSubView(gap - 1) : null;
        MDCView after = gap < n ? text.getSubView(gap) : null;

        // A cursor stands between two views: draw it at the end of the previous
        // one, or at the start of the next one.
        // We draw cursors next to regular elements, not breaks. Between two
        // breaks, we favour the "start of the next view" tactic.
        boolean drawBeforeAfterView = after != null;
        if (after != null && isBreak(after) && before != null && !isBreak(before)) {
            drawBeforeAfterView = false;
        }
        boolean drawAfterBeforeView = before != null && !isBreak(before);

        // A single line in the gap shows where an insertion will take place.
        if (drawAfterBeforeView && drawBeforeAfterView) {
            Optional<Line2D> merged = mergedLine(lineNextToView(text, before, true, style),
                    lineNextToView(text, after, false, style));
            if (merged.isPresent()) {
                return Optional.of(new Location(List.of(merged.get()), true));
            }
        }

        List<Line2D> lines = new ArrayList<>(2);
        if (drawAfterBeforeView) {
            lines.add(lineNextToView(text, before, true, style));
        }
        if (drawBeforeAfterView) {
            lines.add(lineNextToView(text, after, false, style));
        }
        if (lines.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Location(lines, true));
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
    /// @param v a subview of `text`.
    /// @param after true to place the line after `v` (in reading order), false for before.
    /// @return the line, in the coordinate system used to draw the subviews of `text`.
    private static Line2D lineNextToView(MDCView text, MDCView v, boolean after, JSeshStyle style) {
        GeometrySpecification geometry = style.geometry();
        TextDirection textDirection = style.options().textDirection();
        TextOrientation textOrientation = style.options().textOrientation();
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
        double dx = originX(text, v);
        double dy = v.getPosition().y;
        return new Line2D.Double(line.getX1() + dx, line.getY1() + dy, line.getX2() + dx, line.getY2() + dy);
    }

    // ------------------------------------------------------------------
    // Cursor inside a quadrat, a cartouche...
    // ------------------------------------------------------------------

    /// The cursor stands between two subviews of `v`, or at one of the edges of `v`
    /// if `v` is a list of quadrats (inside a cartouche...).
    ///
    /// In an [AbsoluteGroup], where signs are placed freely, it is a line along
    /// the end edge of the sign which precedes the gap.
    ///
    /// The cursor is a line which crosses the view, between the two subviews: a
    /// vertical line between the elements of a row (or the quadrats of a
    /// horizontal list), a horizontal one between rows or between the signs of
    /// a ligature.
    private static Optional<Location> locateInContainer(MDCView v, int gap, JSeshStyle style) {
        int n = v.getNumberOfSubviews();
        boolean leftToRight = v.getDirection().isLeftToRight();
        Line2D line;
        if (v.getModel() instanceof BasicItemList) {
            // Quadrats follow each other along the line (or the column) of text.
            boolean horizontal = style.options().textOrientation() == TextOrientation.HORIZONTAL;
            if (n == 0) {
                line = crossingLine(v, horizontal,
                        horizontal ? v.getInternalWidth() / 2.0 : v.getInternalHeight() / 2.0);
            } else if (gap == 0) {
                Rectangle2D first = subViewBounds(v, 0);
                line = crossingLine(v, horizontal,
                        horizontal ? (leftToRight ? first.getMinX() : first.getMaxX()) : first.getMinY());
            } else if (gap == n) {
                Rectangle2D last = subViewBounds(v, n - 1);
                line = crossingLine(v, horizontal,
                        horizontal ? (leftToRight ? last.getMaxX() : last.getMinX()) : last.getMaxY());
            } else {
                line = lineBetween(v, subViewBounds(v, gap - 1), subViewBounds(v, gap), horizontal);
            }
        } else {
            if (gap <= 0 || gap >= n) {
                // Same place as the cursor outside this element.
                return Optional.empty();
            }
            Rectangle2D before = subViewBounds(v, gap - 1);
            Rectangle2D after = subViewBounds(v, gap);
            if (v.getModel() instanceof AbsoluteGroup) {
                // The signs are placed freely, and may overlap: there is no
                // gap between them to cut through. The gap is their order in the
                // group, so we mark the end of the sign which precedes it.
                boolean ltr = leftToRight;
                double x = ltr ? before.getMaxX() : before.getMinX();
                line = new Line2D.Double(x, before.getMinY(), x, before.getMaxY());
            } else if (v.getModel() instanceof HBox) {
                // Elements side by side: vertical line.
                line = lineBetween(v, before, after, true);
            } else if (v.getModel() instanceof ComplexLigature) {
                // The parts are arranged freely around the main sign. Cut across the
                // direction which separates them.
                double dx = Math.abs(before.getCenterX() - after.getCenterX());
                double dy = Math.abs(before.getCenterY() - after.getCenterY());
                line = lineBetween(v, before, after, dx > dy);
            } else {
                // Elements one above the other: horizontal line.
                line = lineBetween(v, before, after, false);
            }
        }
        return Optional.of(new Location(List.of(line), false));
    }

    /// A line between two areas.
    ///
    /// @param v the view containing the areas.
    /// @param vertical true for a vertical line (areas side by side).
    private static Line2D lineBetween(MDCView v, Rectangle2D before, Rectangle2D after, boolean vertical) {
        if (vertical) {
            double x = (before.getMaxX() <= after.getMinX())
                    ? (before.getMaxX() + after.getMinX()) / 2
                    : (after.getMaxX() + before.getMinX()) / 2;
            return crossingLine(v, true, x);
        }
        double y = (before.getMaxY() <= after.getMinY())
                ? (before.getMaxY() + after.getMinY()) / 2
                : (after.getMaxY() + before.getMinY()) / 2;
        return crossingLine(v, false, y);
    }

    /// A line which crosses the whole view.
    ///
    /// @param vertical true for a vertical line, which will be at abscissa `coordinate`;
    /// false for a horizontal one, at ordinate `coordinate`.
    private static Line2D crossingLine(MDCView v, boolean vertical, double coordinate) {
        if (vertical) {
            return new Line2D.Double(coordinate, 0, coordinate, v.getInternalHeight());
        }
        return new Line2D.Double(0, coordinate, v.getInternalWidth(), coordinate);
    }

    /// The area occupied by a subview, in the coordinate system used to draw
    /// the subviews of `v`.
    private static Rectangle2D subViewBounds(MDCView v, int i) {
        MDCView subv = v.getSubView(i);
        return new Rectangle2D.Double(originX(v, subv), subv.getPosition().y, subv.getWidth(), subv.getHeight());
    }
}
