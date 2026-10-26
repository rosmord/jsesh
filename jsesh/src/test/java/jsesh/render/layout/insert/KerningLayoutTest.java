/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout.insert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.constants.KerningAxis;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.elements.HieroglyphDrawer;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Layout of `@hkern` and `@vkern` with real signs.
public class KerningLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-3;

    private JSeshRenderContext renderContext(TextOrientation orientation, TextDirection direction) {
        JSeshStyle style = JSeshStyle.DEFAULT.copy()
                .options(o -> o.textOrientation(orientation).textDirection(direction)).build();
        return new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
    }

    private MDCView layout(String mdc, JSeshRenderContext context) throws MDCSyntaxError {
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
    }

    /// The bounds of the signs of a text, as drawn, in document coordinates.
    private List<Rectangle2D> signBounds(String mdc, TextOrientation orientation, TextDirection direction)
            throws MDCSyntaxError {
        JSeshRenderContext context = renderContext(orientation, direction);
        MDCView root = layout(mdc, context);
        HieroglyphDrawer drawer = new HieroglyphDrawer(context.hieroglyphShapeRepository());
        ViewSilhouette silhouette = new ViewSilhouette(drawer, drawer.scaleFromFontToStyle(context.jseshStyle()),
                direction);
        List<Rectangle2D> result = new ArrayList<>();
        for (Shape s : silhouette.silhouette(root)) {
            result.add(s.getBounds2D());
        }
        return result;
    }

    private void assertSameDrawing(String expected, String actual, TextOrientation orientation)
            throws MDCSyntaxError {
        for (TextDirection direction : TextDirection.values()) {
            List<Rectangle2D> e = signBounds(expected, orientation, direction);
            List<Rectangle2D> a = signBounds(actual, orientation, direction);
            assertEquals(e.size(), a.size(), actual);
            for (int i = 0; i < e.size(); i++) {
                Rectangle2D r = e.get(i), s = a.get(i);
                String message = actual + " " + orientation + " " + direction + " sign " + i;
                assertEquals(r.getMinX(), s.getMinX(), EPSILON, message);
                assertEquals(r.getMinY(), s.getMinY(), EPSILON, message);
                assertEquals(r.getWidth(), s.getWidth(), EPSILON, message);
                assertEquals(r.getHeight(), s.getHeight(), EPSILON, message);
            }
        }
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "p:q; p:@hkern:q",
            "p*q; p*@vkern*q",
            "p:q-n; p:@hkern:q-n",
            "p:q*t; p:@hkern[a=1]:q*@vkern*t",
            "p:q:n; @hkern:p:q:n:@hkern",
            "(p-q)*t; (p-@vkern-q)*t",
            "<-p-q->; <-p-@vkern-q->",
    })
    public void kerningInTheWrongContextIsIgnored(String expected, String actual) throws MDCSyntaxError {
        assertSameDrawing(expected, actual, TextOrientation.HORIZONTAL);
        assertSameDrawing(expected, actual, TextOrientation.VERTICAL);
    }

    @Test
    public void vkernIsIgnoredInLines() throws MDCSyntaxError {
        assertSameDrawing("p-q-n", "p-@vkern-q-n", TextOrientation.HORIZONTAL);
    }

    @Test
    public void hkernIsIgnoredInColumns() throws MDCSyntaxError {
        assertSameDrawing("p-q-n", "p-@hkern-q-n", TextOrientation.VERTICAL);
    }

    /// In columns, quadrats are separated by `lineSkip`; `@vkern` brings them to about `smallSkip`.
    @Test
    public void vkernKernsInColumns() throws MDCSyntaxError {
        List<Rectangle2D> plain = signBounds("t-A1", TextOrientation.VERTICAL, TextDirection.LEFT_TO_RIGHT);
        List<Rectangle2D> kerned = signBounds("t-@vkern-A1", TextOrientation.VERTICAL,
                TextDirection.LEFT_TO_RIGHT);
        double plainGap = plain.get(1).getMinY() - plain.get(0).getMaxY();
        double kernedGap = kerned.get(1).getMinY() - kerned.get(0).getMaxY();
        assertTrue(kernedGap < plainGap - 1, plainGap + " -> " + kernedGap);
        assertTrue(kernedGap > 0, "the signs don't touch: " + kernedGap);
        // t is not moved.
        assertEquals(plain.get(0).getMinY(), kerned.get(0).getMinY(), EPSILON);
    }

    /// The kerned groups can't be brought any closer.
    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "HORIZONTAL; w-@hkern-D58",
            "HORIZONTAL; A1-@hkern-x",
            "VERTICAL; t-@vkern-A1",
    })
    public void kerningIsStable(TextOrientation orientation, String mdc) throws MDCSyntaxError {
        for (TextDirection direction : TextDirection.values()) {
            JSeshRenderContext context = renderContext(orientation, direction);
            MDCView root = layout(mdc, context);
            HieroglyphDrawer drawer = new HieroglyphDrawer(context.hieroglyphShapeRepository());
            Kerner kerner = new Kerner(drawer, drawer.scaleFromFontToStyle(context.jseshStyle()), direction);
            KerningAxis axis = orientation.isHorizontal() ? KerningAxis.HORIZONTAL : KerningAxis.VERTICAL;
            double skip = context.jseshStyle().geometry().smallSkip();
            double shift = kerner.shift(List.of(root.getSubView(0)), root.getSubView(2), axis, skip);
            assertEquals(0, shift, 0.5, mdc + " " + direction);
        }
    }

    @Test
    public void hkernInAGroupNarrowsIt() throws MDCSyntaxError {
        for (TextDirection direction : TextDirection.values()) {
            JSeshRenderContext context = renderContext(TextOrientation.HORIZONTAL, direction);
            float plain = layout("w*D58", context).getSubView(0).getSubView(0).getWidth();
            float kerned = layout("w*@hkern*D58", context).getSubView(0).getSubView(0).getWidth();
            assertTrue(kerned < plain - 1, plain + " -> " + kerned);
        }
    }

    @Test
    public void rightToLeftIsTheMirrorOfLeftToRight() throws MDCSyntaxError {
        for (String mdc : List.of("w-@hkern-D58", "w*@hkern*D58-n")) {
            JSeshRenderContext ltr = renderContext(TextOrientation.HORIZONTAL, TextDirection.LEFT_TO_RIGHT);
            JSeshRenderContext rtl = renderContext(TextOrientation.HORIZONTAL, TextDirection.RIGHT_TO_LEFT);
            MDCView a = layout(mdc, ltr);
            MDCView b = layout(mdc, rtl);
            assertEquals(a.getWidth(), b.getWidth(), EPSILON, mdc);
            for (int i = 0; i < a.getNumberOfSubviews(); i++) {
                assertEquals(a.getSubView(i).getPosition().x, b.getSubView(i).getPosition().x, EPSILON, mdc);
            }
        }
    }

    /// Justification moves kerned elements together: the kerned gap is kept, while the others grow.
    @Test
    public void justificationKeepsKerning() throws MDCSyntaxError {
        String mdc = "w-@hkern-D58-A1-A1-A1-A1-A1-A1-!A1-A1-A1-A1-A1-A1-A1-A1-A1";
        for (TextDirection direction : TextDirection.values()) {
            JSeshRenderContext plainContext = renderContext(TextOrientation.HORIZONTAL, direction);
            JSeshRenderContext justifiedContext = new JSeshRenderContext(
                    plainContext.jseshStyle().copy().options(o -> o.justified(true)).build(),
                    PredefinedFonts.buildAllEmbeddedFonts());
            MDCView plain = layout(mdc, plainContext);
            MDCView justified = layout(mdc, justifiedContext);
            // Sub views: w, @hkern, D58, A1...
            assertEquals(gap(plain, 0, 2), gap(justified, 0, 2), EPSILON, "kerned gap, " + direction);
            assertTrue(gap(justified, 2, 3) > gap(plain, 2, 3) + 0.5, "the line is justified, " + direction);
        }
    }

    /// The space between two sub views of the text, in layout coordinates.
    private static double gap(MDCView text, int first, int second) {
        MDCView a = text.getSubView(first);
        return text.getSubView(second).getPosition().x - (a.getPosition().x + a.getWidth());
    }
}
