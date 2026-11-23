/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import jsesh.ui.editor.JMDCEditor;

/// The scale modifier changes the size of what is *drawn*, not only the room taken:
/// `"test"\300` is three times as wide as `"test"`.
class SmallTextScaleDrawingTest {

    private final JMDCEditor editor = new JMDCEditor();

    /// Width in pixels of the non-white part of the drawing.
    private int inkWidth(String mdc) {
        editor.setMDCText(mdc);
        Dimension size = new Dimension(1200, 600);
        editor.setSize(size);
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size.width, size.height);
        editor.paint(g);
        g.dispose();
        int min = Integer.MAX_VALUE;
        int max = -1;
        for (int y = 0; y < size.height; y++) {
            for (int x = 0; x < size.width; x++) {
                if ((image.getRGB(x, y) & 0xFFFFFF) < 0x808080) {
                    min = Math.min(min, x);
                    max = Math.max(max, x);
                }
            }
        }
        assertTrue(max >= min, "nothing drawn for " + mdc);
        return max - min + 1;
    }

    /// Note that 300 % would not do here: a quadrat is limited to the maximal quadrat
    /// height, and the text, once enlarged, is higher than that.
    @Test
    void scaleModifierScalesTheDrawnText() {
        double plain = inkWidth("\"test\"");
        double big = inkWidth("\"test\"\\200");
        // The ink of such a small text is a few pixels wide: allow for rounding.
        // Without the fix, the ratio is 1.
        assertEquals(2.0, big / plain, 0.3);
    }
}
