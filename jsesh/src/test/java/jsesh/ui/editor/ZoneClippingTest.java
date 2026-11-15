/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jsesh.render.draw.DefaultElementDrawer;
import jsesh.render.draw.ViewDrawer;
import jsesh.render.view.MDCView;

/// The items of a zone which are outside the clip are not drawn, as those of
/// the text: in a long zone, only the visible part is drawn, and it is drawn
/// as without clipping.
class ZoneClippingTest {

    private static final String LINE = "A1-G17-N35-A2:D21-R8-G5-ra:Z1-i-mn:n-Htp:t*p-D46-x:t!";

    /// Counts the elements drawn.
    private static final class CountingDrawer extends DefaultElementDrawer {
        int count;

        @Override
        public void drawElement(MDCView e, Graphics2D g, boolean postfix) {
            if (!postfix) {
                count++;
            }
            super.drawElement(e, g, postfix);
        }
    }

    private record Drawing(BufferedImage image, int count) {
    }

    private static Drawing draw(JMDCEditor editor, Rectangle clip, boolean clipping) {
        MDCView view = editor.getView();
        BufferedImage image = new BufferedImage((int) view.getWidth() + 20, (int) view.getHeight() + 20,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setClip(clip);
        CountingDrawer elements = new CountingDrawer();
        ViewDrawer drawer = new ViewDrawer(elements);
        drawer.setClip(clipping);
        drawer.drawViewAndSelection(g, editor.getRenderContext(), editor.buildTechRenderContext(), view, null);
        g.dispose();
        return new Drawing(image, elements.count);
    }

    private static int[] pixels(BufferedImage image, Rectangle area) {
        return image.getRGB(area.x, area.y, area.width, area.height, null, 0, area.width);
    }

    @ParameterizedTest
    @ValueSource(strings = { "%zone", "%zone[dir=rl]", "%zone[layout=columns]", "%zone[scale=50]",
            "%zone(A1-%zone[dir=rl]" })
    void onlyTheVisiblePartOfAZoneIsDrawn(String zone) {
        String body = LINE.repeat(60);
        String mdc = zone.contains("(") ? "A1-A2!" + zone + "(" + body + ")-B1)" : "A1-A2!" + zone + "(" + body + ")";
        JMDCEditor editor = new JMDCEditor();
        editor.setMDCText(mdc);
        MDCView view = editor.getView();
        // A band in the middle of the text.
        Rectangle clip = view.getWidth() > view.getHeight()
                ? new Rectangle((int) (view.getWidth() / 2), 0, 150, (int) view.getHeight())
                : new Rectangle(0, (int) (view.getHeight() / 2), (int) view.getWidth(), 150);
        Drawing clipped = draw(editor, clip, true);
        Drawing full = draw(editor, clip, false);
        assertArrayEquals(pixels(full.image(), clip), pixels(clipped.image(), clip));
        assertTrue(clipped.count() * 4 < full.count(), clipped.count() + " / " + full.count());
    }
}
