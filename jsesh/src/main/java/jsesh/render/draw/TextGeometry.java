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

import jsesh.document.caret.MDCCaret;
import jsesh.model.MDCPosition;
import jsesh.model.TextScope;
import jsesh.render.style.GeometrySpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;

/// Graphical positions of views in a text.
/// Maps between positions in the
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
/// A `TextGeometry` is cheap to build; build a new one when the view changes, don't 
/// keep them.
public final class TextGeometry {

    private final MDCView rootView;

    private final JSeshStyle style;

    private final CursorLocator cursorLocator;

    private final SelectionLocator selectionLocator;

    private final PositionLocator positionLocator;

    /// @param rootView the view of the whole text.
    /// @param style the style used to build the view.
    public TextGeometry(MDCView rootView, JSeshStyle style) {
        this.rootView = rootView;
        this.style = style;
        PlacedView root = PlacedView.root(rootView);
        this.cursorLocator = new CursorLocator(root, style);
        this.selectionLocator = new SelectionLocator(root);
        this.positionLocator = new PositionLocator(root, style);
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

    /// Finds the position under a point, mostly to manage mouse clicks.
    ///
    /// The position is a gap of a sequence of items: of the text itself, or of
    /// a text in miniature which the cursor reaches in
    /// [jsesh.model.CaretGranularity#QUADRAT] mode (the content of a cartouche,
    /// later zones). The position is never inside a quadrat or a sub-quadrat,
    /// where it would be more disturbing than useful.
    ///
    /// When the point falls into a text in miniature, the position is the gap
    /// of this text nearest to the point. When it falls into any other
    /// quadrat, it is the gap on the side of the quadrat nearest to the point.
    /// When it falls into no quadrat, it is the end of the line (or column) it
    /// stands in, or the end of the text.
    ///
    /// @param point a point, in drawing coordinates. Any display scaling must
    /// be undone before calling this method.
    /// @return the position corresponding to the point (never null).
    public MDCPosition positionAt(Point2D point) {
        return positionLocator.locate(point);
    }

    /// Returns a rectangle around a top-level position: it spans the gap
    /// between the views on both sides of the position, and is as high as
    /// them.
    ///
    /// When one of these views is a break (line or page), only the other one
    /// is used. The same goes for the first view of a soft-wrapped row.
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

            // A soft-wrapped line ends here: the next view is on a lower row.
            boolean wrapped = !previousIsBreak && !nextIsBreak
                    && style.options().textOrientation().isHorizontal()
                    && nextRectangle.getMinY() >= previousRectangle.getMaxY();

            if (previousIsBreak || wrapped) {
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
