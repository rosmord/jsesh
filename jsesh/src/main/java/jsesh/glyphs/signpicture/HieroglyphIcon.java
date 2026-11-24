/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.glyphs.signpicture;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import jsesh.glyphs.shape.ShapeChar;
import jsesh.utils.swing.GraphicsUtils;

/// A vector icon for a single sign.
///
/// The sign is drawn when the icon is painted, not in advance in a bitmap. So it
/// stays sharp whatever the scale of the screen (Retina, Windows at 150 %...) or
/// of the interface: the `Graphics2D` it receives already carries that scale.
///
/// Built by [HieroglyphIconBuilder#createHieroglyphIcon]. The geometry
/// (size, scale, position of the sign) is computed once, at creation.
final class HieroglyphIcon implements Icon {

    private final ShapeChar glyph;
    private final SignPictureLayout layout;
    private final boolean transparent;

    HieroglyphIcon(ShapeChar glyph, SignPictureLayout layout, boolean transparent) {
        this.glyph = glyph;
        this.layout = layout;
        this.transparent = transparent;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            if (!transparent) {
                g2.setColor(Color.WHITE);
                g2.fillRect(x, y, layout.width(), layout.height());
            }
            GraphicsUtils.antialias(g2);
            g2.setColor(Color.BLACK);
            glyph.draw(g2, x + layout.xOrigin(), y + layout.yOrigin(), layout.scale(), layout.scale(), 0);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public int getIconWidth() {
        return layout.width();
    }

    @Override
    public int getIconHeight() {
        return layout.height();
    }
}
