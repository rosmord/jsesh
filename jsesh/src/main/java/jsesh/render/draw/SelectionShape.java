/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/// The area of a selection inside a nested scope (the content of a
/// cartouche...), as computed by [SelectionLocator].
///
/// It is expressed in the coordinate system of the `Graphics2D` given to
/// [ViewDrawer] to draw the root view.
///
/// @param areas the rectangles to highlight; empty if there is nothing to
/// highlight.
public record SelectionShape(List<Rectangle2D> areas) {

    public SelectionShape {
        areas = List.copyOf(areas);
    }

    /// @return true if there is nothing to highlight.
    public boolean isEmpty() {
        return areas.isEmpty();
    }

    /// Highlights the selection, on top of an already drawn view.
    ///
    /// @param g the graphics, in the same state as the one given to [ViewDrawer] to draw the root view.
    /// @param color the highlight color.
    public void paint(Graphics2D g, Color color) {
        if (areas.isEmpty()) {
            return;
        }
        Graphics2D tmpG = (Graphics2D) g.create();
        try {
            tmpG.setColor(color);
            for (Rectangle2D area : areas) {
                tmpG.fill(area);
            }
        } finally {
            tmpG.dispose();
        }
    }
}
