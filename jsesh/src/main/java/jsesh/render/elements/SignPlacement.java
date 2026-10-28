/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.elements;

import java.awt.geom.AffineTransform;

/// How the shape of a sign is placed in its view.
///
/// Both drawing and layout need this: drawing paints the sign this way, and
/// layout (for instance for `%insert`) must reason on the shape which will
/// actually be painted. Keeping the rule here ensures they agree.
public final class SignPlacement {

    private SignPlacement() {
    }

    /// The transformation which mirrors a sign inside its view.
    ///
    /// @param viewWidth the view's width.
    public static AffineTransform mirror(double viewWidth) {
        return new AffineTransform(-1, 0, 0, 1, viewWidth, 0);
    }

    /// The transformation from glyph coordinates (the shape as it is drawn by
    /// [HieroglyphDrawer], rotation included) to the coordinates of the sign's view.
    ///
    /// @param reversed true if the sign is mirrored.
    /// @param viewWidth the view's width.
    /// @param baseSignScale the scale from the font to the style (see [HieroglyphDrawer#scaleFromFontToStyle]).
    /// @param xScale the view's horizontal scale.
    /// @param yScale the view's vertical scale.
    public static AffineTransform glyphToView(boolean reversed, double viewWidth, double baseSignScale, double xScale,
            double yScale) {
        AffineTransform result = reversed ? mirror(viewWidth) : new AffineTransform();
        result.scale(baseSignScale, baseSignScale);
        result.scale(xScale, yScale);
        return result;
    }
}
