/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/// Draws a [JMDCEditor] with the dynamic tabs shown or hidden, in headless
/// mode. The images are saved in `build/test-images` for a visual check.
class TabRenderingTest {

    private static BufferedImage render(JMDCEditor editor, String name) {
        Dimension d = editor.getPreferredSize();
        BufferedImage image = new BufferedImage(Math.max(d.width, 100), Math.max(d.height, 100),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        editor.setSize(image.getWidth(), image.getHeight());
        editor.paint(g);
        g.dispose();
        try {
            File dir = new File(System.getProperty("buildDir", "build"), "test-images");
            dir.mkdirs();
            ImageIO.write(image, "png", new File(dir, "tabs-" + name + ".png"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return image;
    }

    private static boolean sameImage(BufferedImage a, BufferedImage b) {
        return a.getWidth() == b.getWidth() && a.getHeight() == b.getHeight()
                && Arrays.equals(a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0, a.getWidth()),
                        b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0, b.getWidth()));
    }

    /// The number of pixels in the colour of the tab marks (the cursor is in
    /// another blue).
    private static int tabPixels(BufferedImage image) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color c = new Color(image.getRGB(x, y));
                if (c.getBlue() > 150 && c.getRed() < 160 && c.getGreen() > 60 && c.getBlue() > c.getGreen() + 30) {
                    count++;
                }
            }
        }
        return count;
    }

    @ParameterizedTest
    @ValueSource(strings = { "lines", "columns", "rl" })
    void tabsAreOnlyDrawnWhenShown(String variant) {
        String options = switch (variant) {
            case "columns" -> "[layout=columns]";
            case "rl" -> "[dir=rl]";
            default -> "";
        };
        JMDCEditor editor = new JMDCEditor();
        editor.setMDCText("%zone" + options + "(A1-%tab-B1-%tab[n=3,align=end]-C1-!G17-G17-G17-%tab-D1)");
        editor.getWorkflow().setCursor(new jsesh.model.MDCPosition(1));
        BufferedImage hidden = render(editor, variant + "-hidden");
        editor.setShowTabs(true);
        BufferedImage shown = render(editor, variant + "-shown");
        assertTrue(tabPixels(shown) > tabPixels(hidden) + 10, "tab marks when shown");
        assertFalse(sameImage(hidden, shown));
    }
}
