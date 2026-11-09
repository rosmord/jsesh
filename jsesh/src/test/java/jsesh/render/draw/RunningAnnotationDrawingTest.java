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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.RunningAnnotation;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// The drawing of the running annotations: a solid line with arrows for an
/// haplography, a dashed one without arrows for a lacuna; along the column in
/// vertical text.
class RunningAnnotationDrawingTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final JSeshStyle STYLE = JSeshStyle.DEFAULT;
    private static final int MARGIN = 20;
    /// The drawing is enlarged, so that the fine details are several pixels wide.
    private static final int SCALE = 4;

    private MDCView view;
    private BufferedImage image;

    /// Lays out and draws the text, at scale 1, on a white background; the
    /// picture is also saved in `build/reports/pictures`, to be looked at.
    private void draw(String name, String mdc) throws MDCSyntaxError, IOException {
        JSeshRenderContext context = new JSeshRenderContext(STYLE, PredefinedFonts.buildAllEmbeddedFonts());
        JSeshTechRenderContext tech = new JSeshTechRenderContext(FRC, 1.0);
        view = new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context, tech);
        int width = (int) Math.ceil(view.getWidth() * SCALE) + 2 * MARGIN;
        int height = (int) Math.ceil(view.getHeight() * SCALE) + 2 * MARGIN;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        // No anti-aliasing: the fine lines are 0.5 wide, and would be pale grey.
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        g.translate(MARGIN, MARGIN);
        g.scale(SCALE, SCALE);
        new ViewDrawer().draw(g, context, tech, view);
        g.dispose();
        File out = new File(System.getProperty("buildDir", "build") + "/reports/pictures/" + name + ".png");
        out.getParentFile().mkdirs();
        ImageIO.write(image, "png", out);
    }

    /// The view of the first running annotation of the text.
    private MDCView annotationView() {
        for (int i = 0; i < view.getNumberOfSubviews(); i++) {
            if (view.getSubView(i).getModel() instanceof RunningAnnotation) {
                return view.getSubView(i);
            }
        }
        throw new AssertionError("no annotation");
    }

    private boolean dark(int x, int y) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
            return false;
        }
        return (image.getRGB(x, y) & 0xFF) < 128;
    }

    /// The number of dark pixels in a row, from x0 (included) to x1 (excluded), in page coordinates.
    private int darkInRow(int y, int x0, int x1) {
        int n = 0;
        for (int x = x0; x < x1; x++) {
            n += dark(x + MARGIN, y + MARGIN) ? 1 : 0;
        }
        return n;
    }

    private int darkInColumn(int x, int y0, int y1) {
        int n = 0;
        for (int y = y0; y < y1; y++) {
            n += dark(x + MARGIN, y + MARGIN) ? 1 : 0;
        }
        return n;
    }

    /// A coordinate of the view, in the picture.
    private static int px(double coordinate) {
        return (int) Math.round(coordinate * SCALE);
    }

    /// The dark pixels of a rectangle (page coordinates).
    private int darkIn(int x0, int y0, int x1, int y1) {
        int n = 0;
        for (int y = y0; y < y1; y++) {
            n += darkInRow(y, x0, x1);
        }
        return n;
    }

    @Test
    void anHaplographyIsASolidLineWithArrows() throws Exception {
        draw("haplography-horizontal", "%haplography[size=400]");
        MDCView a = annotationView();
        int x0 = px(a.getPosition().x);
        int x1 = px(a.getPosition().x + a.getWidth());
        int middle = px(a.getPosition().y + a.getHeight() / 2);
        int inner = 10 * SCALE;
        // Solid in the middle (the line is a few pixels wide).
        int rowDark = 0;
        for (int dy = -2; dy <= 2; dy++) {
            rowDark = Math.max(rowDark, darkInRow(middle + dy, x0 + inner, x1 - inner));
        }
        assertEquals(x1 - x0 - 2 * inner, rowDark);
        // Nothing across the line, in the middle of it.
        assertEquals(0, darkInColumn((x0 + x1) / 2, middle - 12, middle - 4));
        assertEquals(0, darkInColumn((x0 + x1) / 2, middle + 4, middle + 12));
        // Arrow heads at both ends: dark pixels across the line.
        assertTrue(darkIn(x0, middle - 8, x0 + 12, middle - 3) > 0);
        assertTrue(darkIn(x0, middle + 4, x0 + 12, middle + 9) > 0);
        assertTrue(darkIn(x1 - 12, middle - 8, x1, middle - 3) > 0);
        assertTrue(darkIn(x1 - 12, middle + 4, x1, middle + 9) > 0);
        // Nothing before the line.
        assertEquals(0, darkInRow(middle, 0, x0 - 2));
    }

    @Test
    void aLacunaIsDashedWithoutArrows() throws Exception {
        draw("lacuna-horizontal", "%lacuna[size=400]");
        MDCView a = annotationView();
        int x0 = px(a.getPosition().x);
        int x1 = px(a.getPosition().x + a.getWidth());
        int middle = px(a.getPosition().y + a.getHeight() / 2);
        int length = x1 - x0;
        int rowDark = 0;
        for (int dy = -2; dy <= 2; dy++) {
            rowDark = Math.max(rowDark, darkInRow(middle + dy, x0, x1));
        }
        assertTrue(rowDark > length / 4, "dark: " + rowDark + " of " + length);
        assertTrue(rowDark < 3 * length / 4, "dark: " + rowDark + " of " + length);
        // No arrow heads: nothing above or below the line.
        assertEquals(0, darkIn(x0, middle - 12, x1, middle - 4));
        assertEquals(0, darkIn(x0, middle + 4, x1, middle + 12));
    }

    @Test
    void inColumnsTheLineIsVertical() throws Exception {
        draw("haplography-vertical", "%zone[layout=columns](A1-%haplography[size=300]-A1)");
        MDCView outer = view.getSubView(0);
        MDCView zone = outer.getFirstSubView();
        MDCView a = null;
        for (int i = 0; i < zone.getNumberOfSubviews(); i++) {
            if (zone.getSubView(i).getModel() instanceof RunningAnnotation) {
                a = zone.getSubView(i);
            }
        }
        double ox = outer.getPosition().x + zone.getPosition().x;
        double oy = outer.getPosition().y + zone.getPosition().y;
        int x = px(ox + a.getPosition().x + a.getWidth() / 2);
        int y0 = px(oy + a.getPosition().y);
        int y1 = px(oy + a.getPosition().y + a.getHeight());
        int inner = 10 * SCALE;
        int col = 0;
        for (int dx = -2; dx <= 2; dx++) {
            col = Math.max(col, darkInColumn(x + dx, y0 + inner, y1 - inner));
        }
        assertEquals(y1 - y0 - 2 * inner, col);
        // Arrow heads: dark pixels beside the line at both ends.
        assertTrue(darkIn(x - 9, y0, x - 3, y0 + 12) > 0);
        assertTrue(darkIn(x + 4, y1 - 12, x + 10, y1) > 0);
    }

    @Test
    void inRightToLeftTextTheLineIsStillThere() throws Exception {
        draw("haplography-rtl", "%zone[dir=rl,width=800](A1-%haplography[size=fill]-A1)");
        int withLine = darkIn(0, 0, image.getWidth(), image.getHeight());
        draw("haplography-rtl-without", "%zone[dir=rl,width=800](A1-A1)");
        assertTrue(withLine > darkIn(0, 0, image.getWidth(), image.getHeight()));
    }
}
