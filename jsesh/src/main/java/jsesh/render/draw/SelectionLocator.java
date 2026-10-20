/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.draw;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import jsesh.model.TextScope;

/// Computes the area to highlight for a selection inside a nested scope (the
/// content of a cartouche...), from the view tree only, like [CursorLocator].
/// Used through [TextGeometry#selection(TextScope, int, int)].
///
/// The items between the two limits are covered by rectangles. Consecutive
/// items on the same row (or column) of text are covered by a single
/// rectangle, which also covers the space between them.
///
/// Selections at the top level of the text are drawn by [ViewDrawer] itself,
/// one top-level view at a time.
final class SelectionLocator {

    private final PlacedView root;

    /// @param root the view of the whole text.
    SelectionLocator(PlacedView root) {
        this.root = root;
    }

    /// Finds the area of a selection in a scope.
    ///
    /// @param scope the scope of the selection.
    /// @param start the first gap of the selection, in the scope.
    /// @param end the last gap of the selection (`start <= end`).
    /// @return the area to highlight; empty if there is nothing to
    /// highlight, or if the view doesn't match the scope (for instance if it is
    /// not up to date).
    SelectionShape locate(TextScope scope, int start, int end) {
        return new SelectionShape(root.follow(scope.getPath())
                .map(container -> locateIn(container, start, end))
                .orElse(List.of()));
    }

    private List<Rectangle2D> locateIn(PlacedView container, int start, int end) {
        List<Rectangle2D> result = new ArrayList<>();
        int last = Math.min(end, container.childCount());
        Rectangle2D current = null;
        for (int i = Math.max(0, start); i < last; i++) {
            Rectangle2D r = container.toRoot(container.childBounds(i));
            if (current != null && sameRow(current, r)) {
                current.add(r);
            } else {
                if (current != null) {
                    result.add(current);
                }
                current = r;
            }
        }
        if (current != null) {
            result.add(current);
        }
        return result;
    }

    /// Are two areas on the same line or column of text? They are if they
    /// overlap across one of the axes.
    private static boolean sameRow(Rectangle2D a, Rectangle2D b) {
        boolean overlapY = Math.max(a.getMinY(), b.getMinY()) < Math.min(a.getMaxY(), b.getMaxY());
        boolean overlapX = Math.max(a.getMinX(), b.getMinX()) < Math.min(a.getMaxX(), b.getMaxX());
        return overlapX || overlapY;
    }
}
