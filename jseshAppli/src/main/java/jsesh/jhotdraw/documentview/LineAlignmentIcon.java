/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.documentview;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import jsesh.sharedvocabulary.LineAlignment;

/// The usual picture of a line alignment: four lines of text, placed as the
/// alignment requires. Drawn, so that it follows the colour of the component.
///
/// In right-to-left text, the start is on the right: the picture is mirrored.
/// In text written in columns, it shows four columns (the start is the top).
final class LineAlignmentIcon implements Icon {

    private static final int SIZE = 16;

    /// The lengths of the four lines, in pixels.
    private static final int[] LENGTHS = { 14, 9, 14, 7 };

    private final LineAlignment alignment;
    private final boolean rightToLeft;
    private final boolean vertical;

    /// @param alignment the alignment to show, or null for a mixed one (the
    ///        icon is then empty).
    /// @param rightToLeft true if the text is written from right to left.
    /// @param vertical true if the text is written in columns.
    LineAlignmentIcon(LineAlignment alignment, boolean rightToLeft, boolean vertical) {
        this.alignment = alignment;
        this.rightToLeft = rightToLeft;
        this.vertical = vertical;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (alignment == null) {
            return;
        }
        g.setColor(c.getForeground());
        int width = SIZE - 2;
        for (int i = 0; i < LENGTHS.length; i++) {
            int length = alignment == LineAlignment.JUSTIFY ? width : LENGTHS[i];
            int start = switch (alignment) {
                case START, JUSTIFY -> 0;
                case END -> width - length;
                case CENTER -> (width - length) / 2;
            };
            // The position of the line (or column) across the text.
            int across = 2 + 3 * i + i / 2;
            if (vertical) {
                // Columns follow one another from the right in right-to-left text.
                if (rightToLeft) {
                    across = SIZE - 2 - across;
                }
                g.fillRect(x + across, y + 1 + start, 2, length);
            } else {
                if (rightToLeft) {
                    start = width - length - start;
                }
                g.fillRect(x + 1 + start, y + across, length, 2);
            }
        }
    }

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }
}
