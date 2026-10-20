/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.draw;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.function.UnaryOperator;

import jsesh.render.style.JSeshStyle;

/// The shape of the cursor, as computed by [CursorLocator].
///
/// It is expressed in the coordinate system of the `Graphics2D` given to
/// [ViewDrawer] to draw the root view.
///
/// @param lines the lines which make the cursor. In general, there is only one;
/// between two top-level elements, there is one at the end of the previous
/// element and one at the start of the next one.
/// @param wide true for the (thick) cursor which stands between top-level items,
/// false for the (thin) one inside a quadrat.
public record CursorShape(List<Line2D> lines, boolean wide) {

    public CursorShape {
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

    /// Draws the cursor, on top of an already drawn view. It is independent
    /// from [ViewDrawer].
    ///
    /// @param g the graphics, in the same state as the one given to [ViewDrawer] to draw the root view.
    /// @param style the style, which gives the cursor's color and strokes.
    public void paint(Graphics2D g, JSeshStyle style) {
        Graphics2D tmpG = (Graphics2D) g.create();
        try {
            tmpG.setColor(style.painting().cursorColor());
            tmpG.setStroke(wide ? style.geometry().wideStroke() : style.geometry().fineStroke());
            for (Line2D line : lines) {
                tmpG.draw(line);
            }
        } finally {
            tmpG.dispose();
        }
    }

    /// The same cursor, with transformed lines.
    CursorShape mapLines(UnaryOperator<Line2D> transformation) {
        return new CursorShape(lines.stream().map(transformation).toList(), wide);
    }
}
