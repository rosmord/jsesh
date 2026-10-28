/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.draw;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;
import java.util.OptionalInt;

import jsesh.model.AlphabeticCharacter;
import jsesh.model.CaretGranularity;
import jsesh.model.ItemSequence;
import jsesh.model.MDCPosition;
import jsesh.model.ModelElement;
import jsesh.model.TextScope;
import jsesh.model.ZoneContent;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.render.style.GeometrySpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;

/// Finds the position under a point, from the view tree only, like
/// [CursorLocator]. Used through [TextGeometry#positionAt(Point2D)].
///
/// A click puts the cursor in the gap of a *sequence of items*: the text
/// itself, or a text in miniature like the content of a cartouche (what the
/// cursor reaches in [CaretGranularity#QUADRAT] mode, see
/// [ModelElement#isCursorStop(int, CaretGranularity)]). It never goes inside a
/// quadrat, nor inside a sub-quadrat: there, a cursor would be more
/// disturbing than useful, and the click is resolved to the gap before or
/// after the whole quadrat.
final class PositionLocator {

    private final PlacedView root;

    private final JSeshStyle style;

    /// @param root the view of the whole text.
    /// @param style the style used to build the views.
    PositionLocator(PlacedView root, JSeshStyle style) {
        this.root = root;
        this.style = style;
    }

    /// @param point a point, in the root system.
    /// @return the position corresponding to the point (never null).
    MDCPosition locate(Point2D point) {
        return locateIn(root, point);
    }

    /// The position under a point, in the gaps of a sequence of items.
    ///
    /// @param sequence the view of the sequence (the text, the content of a
    ///        cartouche or of a zone...).
    /// @param rootPoint a point, in the root system.
    private MDCPosition locateIn(PlacedView sequence, Point2D rootPoint) {
        TextScope scope = TextScope.of(sequence.path());
        // The text and the zones have lines (or columns); the other sequences
        // are a single row.
        boolean nested = !scope.isRoot() && !(sequence.model() instanceof ZoneContent);
        int n = sequence.model().getNumberOfChildren();
        Optional<Point2D> found = sequence.toLocal(rootPoint);
        if (found.isEmpty()) {
            return scope.positionAt(n);
        }
        Point2D local = found.get();

        // The main idea is that we visit all positions until
        // we see we are too far.
        // For line orientation, this means either :
        // a) the vertical position of the current view fits the point,
        // but the horizontal position is either too large or too small
        // (depending on the text orientation).
        // or
        // b) the vertical position is too large.
        // In this cases, the position will be the previous position.
        //
        // For text in column, the system is the same, only reversed.
        GeometrySpecification geometry = style.geometry();
        TextOrientation orientation = orientation(sequence.view());
        TextDirection direction = sequence.view().getDirection();
        int count = sequence.childCount();
        // In columns, a point in the gap between two quadrats is resolved by
        // looking at the column as a whole (see columnGap).
        OptionalInt inColumn = nested || orientation.isHorizontal()
                ? OptionalInt.empty()
                : columnGap(sequence, local);
        for (int i = 0; i < count; i++) {
            MDCView subv = sequence.subView(i);
            Rectangle2D bounds = sequence.childBounds(i);
            double x = bounds.getMinX();
            double y = bounds.getMinY();

            // First test, the same in all cases : does the point fall
            // inside a view ?
            if (local.getX() >= x && local.getY() >= y
                    && local.getX() < x + subv.getWidth() + geometry.smallSkip()
                    && local.getY() < y + subv.getHeight() + geometry.smallSkip()) {
                Optional<PlacedView> inner = sequence.child(i).flatMap(item -> scopeAt(item, rootPoint));
                if (inner.isPresent()) {
                    return locateIn(inner.get(), rootPoint);
                }
                return scope.positionAt(gapOnItem(sequence, i, subv, x, y, local));
            } else if (!nested && inColumn.isEmpty()) {
                if (orientation.isHorizontal()) {
                    // Text for end of line. If the line ends with a break,
                    // the position is before it. Otherwise, the line was
                    // soft-wrapped, and the position is just after its last view.
                    if (y > local.getY()) {
                        if (i > 0 && sequence.model().getChildAt(i - 1).isBreak()) {
                            return scope.positionAt(i - 1);
                        }
                        return scope.positionAt(i);
                    }
                } else if (direction.isLeftToRight()) {
                    if (x > local.getX()) {
                        return scope.positionAt(i);
                    }
                } else if (x + geometry.maxCadratWidth() < local.getX()) {
                    return scope.positionAt(i);
                }
            }
        }
        if (inColumn.isPresent()) {
            return scope.positionAt(inColumn.getAsInt());
        }
        // If nothing was found, chose the last cadrat.
        return scope.positionAt(nested ? gapNearest(sequence, local) : n);
    }

    /// For text in columns: the gap in the column a point falls into.
    ///
    /// The items of a column are not aligned on a common edge, so that the
    /// horizontal position of one item says little about the column of the
    /// point. We use the horizontal extent of the whole column instead.
    ///
    /// @return the gap before the first item of the column which lies below
    ///         the point (or the gap at the end of the column, before its
    ///         line break), or empty if the point is in no column.
    private OptionalInt columnGap(PlacedView sequence, Point2D local) {
        double skip = style.geometry().smallSkip();
        int count = sequence.childCount();
        int start = 0;
        while (start < count) {
            int end = start;
            double minX = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            // The column ends on a break, which is not part of its content.
            while (end < count && !sequence.model().getChildAt(end).isBreak()) {
                Rectangle2D b = sequence.childBounds(end);
                minX = Math.min(minX, b.getMinX());
                maxX = Math.max(maxX, b.getMinX() + sequence.subView(end).getWidth() + skip);
                end++;
            }
            if (end > start && local.getX() >= minX && local.getX() < maxX) {
                for (int j = start; j < end; j++) {
                    if (sequence.childBounds(j).getMinY() > local.getY()) {
                        return OptionalInt.of(j);
                    }
                }
                return OptionalInt.of(end);
            }
            start = end + 1;
        }
        return OptionalInt.empty();
    }

    /// The gap, between the views of a sequence, which is the nearest to a
    /// point which falls on none of them. Used for the sequences below the top
    /// level, which are a single row (or column) of items.
    private int gapNearest(PlacedView sequence, Point2D local) {
        boolean horizontal = orientation(sequence.view()).isHorizontal();
        boolean leftToRight = sequence.view().getDirection().isLeftToRight();
        int gap = 0;
        for (int i = 0; i < sequence.childCount(); i++) {
            Rectangle2D bounds = sequence.childBounds(i);
            boolean before;
            if (!horizontal) {
                before = bounds.getCenterY() < local.getY();
            } else if (leftToRight) {
                before = bounds.getCenterX() < local.getX();
            } else {
                before = bounds.getCenterX() > local.getX();
            }
            if (before) {
                gap = i + 1;
            }
        }
        return gap;
    }

    /// The gap, before or after the view `i`, nearest to a point which falls
    /// on this view.
    private int gapOnItem(PlacedView sequence, int i, MDCView subv, double x, double y, Point2D local) {
        // Alphabetic characters follow their own direction, and
        // are on a horizontal line even in columns.
        boolean horizontal = orientation(sequence.view()).isHorizontal();
        boolean leftToRight = sequence.view().getDirection().isLeftToRight();
        if (subv.getModel() instanceof AlphabeticCharacter c) {
            horizontal = true;
            leftToRight = !c.getScript().isRightToLeft();
        }
        if (horizontal) {
            boolean firstHalf = local.getX() < x + subv.getWidth() / 2.0f;
            return firstHalf == leftToRight ? i : i + 1;
        }
        // Columns
        return local.getY() < y + subv.getHeight() / 2.0f ? i : i + 1;
    }

    /// The orientation of the text a view belongs to: inside a zone, it may
    /// differ from the document's.
    private TextOrientation orientation(MDCView v) {
        return v.getOrientation() != null ? v.getOrientation() : style.options().textOrientation();
    }

    /// The sequence of items, reachable by a click, which stands under a
    /// point inside a top-level item (or its descendants).
    ///
    /// @param view a view the point falls into.
    /// @param rootPoint a point, in the root system.
    /// @return the view of the innermost such sequence found first, or empty
    ///         if the point lies in no such sequence.
    private Optional<PlacedView> scopeAt(PlacedView view, Point2D rootPoint) {
        for (int i = 0; i < view.childCount(); i++) {
            Optional<PlacedView> child = view.child(i)
                    .filter(c -> c.toLocal(rootPoint).filter(c::containsLocal).isPresent());
            if (child.isEmpty()) {
                continue;
            }
            if (isClickScope(child.get())) {
                return child;
            }
            Optional<PlacedView> deeper = scopeAt(child.get(), rootPoint);
            if (deeper.isPresent()) {
                return deeper;
            }
        }
        return Optional.empty();
    }

    /// Is a view the sequence of items of a text in miniature, where a click
    /// puts the cursor? These are the sequences the cursor reaches in
    /// [CaretGranularity#QUADRAT] mode, whatever the current granularity: a
    /// click is not an arrow key.
    private static boolean isClickScope(PlacedView view) {
        ModelElement model = view.model();
        return model instanceof ItemSequence && model.isCursorStop(0, CaretGranularity.QUADRAT);
    }
}
