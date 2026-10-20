/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.draw;

import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

import jsesh.document.caret.MDCCaret;
import jsesh.model.AlphabeticCharacter;
import jsesh.model.MDCPosition;
import jsesh.model.TextScope;
import jsesh.render.style.GeometrySpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.render.style.LayoutOptions;
import jsesh.render.view.MDCView;

/// Where things are in a laid out text: the mapping between positions in the
/// model and points in the drawing.
///
/// It finds the cursor, the selections in nested scopes, and the position
/// under a point (for mouse clicks). It works from the view tree only,
/// independently from the drawing recursion of [ViewDrawer]. Coordinates are
/// those of the `Graphics2D` given to [ViewDrawer] to draw the root view; the
/// results which are shapes paint themselves:
///
/// ```java
/// TextGeometry geometry = new TextGeometry(view, style);
/// geometry.cursorAt(caret).ifPresent(c -> c.paint(g, style));
/// ```
///
/// A `TextGeometry` is cheap to build; build a new one when the view changes.
public final class TextGeometry {

    private final MDCView rootView;

    private final JSeshStyle style;

    private final CursorLocator cursorLocator;

    private final SelectionLocator selectionLocator;

    /// @param rootView the view of the whole text.
    /// @param style the style used to build the view.
    public TextGeometry(MDCView rootView, JSeshStyle style) {
        this.rootView = rootView;
        this.style = style;
        PlacedView root = PlacedView.root(rootView);
        this.cursorLocator = new CursorLocator(root, style);
        this.selectionLocator = new SelectionLocator(root);
    }

    /// Finds the cursor shape for the insertion point of a caret.
    ///
    /// @param caret the caret (may be null).
    /// @return the shape of the cursor, or empty if there is no caret, or if
    /// it doesn't match the view.
    public Optional<CursorShape> cursorAt(MDCCaret caret) {
        if (caret == null || caret.getInsert() == null) {
            return Optional.empty();
        }
        return cursorAt(caret.getInsertExactPosition());
    }

    /// Finds the cursor shape for a position.
    ///
    /// @param position the position of the cursor, possibly inside a quadrat.
    /// @return the shape of the cursor, or empty if the position doesn't match the
    /// view (for instance if the view is not up to date).
    public Optional<CursorShape> cursorAt(MDCPosition position) {
        return cursorLocator.locate(position);
    }

    /// Finds the area of a selection in a nested scope (the content of a
    /// cartouche...). Selections at the top level are drawn by [ViewDrawer].
    ///
    /// @param scope the scope of the selection.
    /// @param start the first gap of the selection, in the scope.
    /// @param end the last gap of the selection (`start <= end`).
    /// @return the area to highlight; empty if there is nothing to highlight,
    /// or if the view doesn't match the scope (for instance if it is not up to
    /// date).
    public SelectionShape selection(TextScope scope, int start, int end) {
        return selectionLocator.locate(scope, start, end);
    }

    /// Finds the top-level position under a point, mostly to manage mouse
    /// clicks.
    ///
    /// When the point falls into a quadrat, the position is the gap on the
    /// side of the quadrat nearest to the point. When it falls into no
    /// quadrat, it is the end of the line (or column) it stands in, or the
    /// end of the text.
    ///
    /// @param point a point, in drawing coordinates. Any display scaling must
    /// be undone before calling this method.
    /// @return the position corresponding to the point (never null).
    public MDCPosition positionAt(Point2D point) {
        int pos = -1;
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
        MDCView v = rootView;
        if (v.getNumberOfSubviews() != 0) {
            Point2D.Float local = new Point2D.Float();
            // First, compute the point's position in terms of v reference
            // system :
            try {
                v.getAffineTransform().inverseTransform(point, local);
            } catch (NoninvertibleTransformException e) {
                // Should not happen !
                e.printStackTrace();
            }

            GeometrySpecification geometry = style.geometry();
            LayoutOptions options = style.options();

            int i = 0;
            while (pos == -1 && i < v.getNumberOfSubviews()) {
                MDCView subv = v.getSubView(i);
                Rectangle2D subvBounds = v.getSubViewBounds(i);
                double x = subvBounds.getMinX();
                double y = subvBounds.getMinY();

                // First test, the same in all cases : does the point fall
                // inside a view ?
                if (local.x >= x && local.y >= y
                        && local.x < x + subv.getWidth() + geometry.smallSkip()
                        && local.y < y + subv.getHeight() + geometry.smallSkip()) {
                    // Alphabetic characters follow their own direction, and
                    // are on a horizontal line even in columns.
                    boolean horizontal = options.textOrientation().isHorizontal();
                    boolean leftToRight = options.textDirection().isLeftToRight();
                    if (subv.getModel() instanceof AlphabeticCharacter c) {
                        horizontal = true;
                        leftToRight = !c.getScript().isRightToLeft();
                    }
                    if (horizontal) {
                        if (local.x < x + subv.getWidth() / 2.0f) {
                            if (leftToRight) {
                                pos = i;
                            } else {
                                pos = i + 1;
                            }
                        } else if (leftToRight) {
                            pos = i + 1;
                        } else {
                            pos = i;
                        }
                    } else { // Columns
                        if (local.y < y + subv.getHeight() / 2.0f) {
                            pos = i;
                        } else {
                            pos = i + 1;
                        }
                    }
                } else if (options.textOrientation().isHorizontal()) {
                    // Text for end of line.
                    if (y > local.y) {
                        pos = (i > 0 ? i - 1 : 0);
                    }
                } else {
                    if (options.textDirection().isLeftToRight()) {
                        if (x > local.x) {
                            pos = i;
                        }
                    } else {
                        if (x + geometry.maxCadratWidth() < local.x) {
                            pos = i;
                        }
                    }
                }
                i++;
            }
        }

        // If nothing was found, chose the last cadrat.
        if (pos == -1) {
            return new MDCPosition(v.getModel().getNumberOfChildren());
        } else {
            return new MDCPosition(pos);
        }
    }

    /// Returns a rectangle around a top-level position: it spans the gap
    /// between the views on both sides of the position, and is as high as
    /// them.
    ///
    /// When one of these views is a break (line or page), only the other one
    /// is used.
    ///
    /// @param position a top-level position.
    /// @return a rectangle around the position, in drawing coordinates.
    public Rectangle2D areaAround(MDCPosition position) {
        MDCView v = rootView;
        GeometrySpecification geometry = style.geometry();
        // position is the next index. The rectangle should include both
        // views i-1 and i (if they exist).

        int index = position.getIndex();
        Rectangle2D result = new Rectangle2D.Float(0, 0, geometry.smallSkip(),
                geometry.maxCadratHeight());

        // non empty view :
        if (v.getNumberOfSubviews() != 0) {
            boolean previousIsBreak = false;
            boolean nextIsBreak = false;

            Rectangle2D previousRectangle = new Rectangle2D.Float(0, 0, geometry
                    .smallSkip(), geometry.maxCadratHeight());
            Rectangle2D nextRectangle = new Rectangle2D.Float(0, v.getHeight()
                    - geometry.maxCadratHeight(), geometry.smallSkip(),
                    geometry
                            .maxCadratHeight());

            if (index > 0) {
                previousRectangle = new Rectangle2D.Double(v.getSubView(
                        index - 1).getPosition().x
                        + v.getSubView(index - 1).getWidth(),
                        v.getSubView(
                                index - 1).getPosition().y,
                        1, v.getSubView(index - 1)
                                .getHeight());
            }
            if (index < v.getNumberOfSubviews()) {
                nextRectangle = new Rectangle2D.Double(v.getSubView(index)
                        .getPosition().x, v.getSubView(index).getPosition().y,
                        1, v.getSubView(index).getHeight());
            }

            if (index <= 0 || v.getSubView(index - 1) == null
                    || v.getSubView(index - 1).getModel().isBreak()) {
                previousIsBreak = true;
            }

            if (index >= v.getNumberOfSubviews() || v.getSubView(index) == null
                    || v.getSubView(index).getModel().isBreak()) {
                nextIsBreak = true;
            }

            if (previousIsBreak) {
                result = nextRectangle;
            } else if (nextIsBreak) {
                result = previousRectangle;
            } else {
                result = previousRectangle;
                result.add(nextRectangle);
            }
        }
        // Now, the rectangle has been computed regardless of the view's
        // direction. Correct it :
        return v.layoutToDrawing(result);
    }
}
