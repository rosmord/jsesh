/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout.insert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.elements.HieroglyphDrawer;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Kerning checked with the actual JSesh signs.
///
/// The `jsesh` module doesn't depend on the standard JSesh font (`jseshGlyphs`): its own
/// layout tests run on the Gnutrace font only, and so only check properties which don't depend on
/// the shape of the signs (see `KerningLayoutTest`). The tests here need the real font, which is
/// why they live in `jseshTests`. The JS port has the same signs, so its numbers should match these ones.
public class StandardFontKerningTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    private final JSeshRenderContext context = new JSeshRenderContext(
            JSeshStyle.DEFAULT.copy().options(o -> o.textOrientation(TextOrientation.VERTICAL)
                    .textDirection(TextDirection.LEFT_TO_RIGHT)).build(),
            PredefinedFonts.buildAllEmbeddedFonts());

    private List<Shape> outlines(String mdc) throws MDCSyntaxError {
        MDCView root = new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
        HieroglyphDrawer drawer = new HieroglyphDrawer(context.hieroglyphShapeRepository());
        return new ViewOutlineExtractor(drawer, drawer.scaleFromFontToStyle(context.jseshStyle()),
                TextDirection.LEFT_TO_RIGHT).outlines(root);
    }

    /// The standard font is really there (otherwise the test would silently run on Gnutrace).
    @Test
    public void standardFontIsUsed() {
        assertTrue(PredefinedFonts.buildStandardJSeshFont().getCodes().contains("A1"),
                "the standard JSesh font should be on the classpath");
    }

    /// `kA` has raised arms: `Hm` can go between them. Their bounding boxes then overlap, but the
    /// outlines must not touch.
    @Test
    public void vkernBringsSignsCloserWithoutTouching() throws MDCSyntaxError {
        List<Shape> plain = outlines("Hm-kA");
        List<Shape> kerned = outlines("Hm-%vkern-kA");
        assertEquals(2, kerned.size());

        Rectangle2D plainBottom = plain.get(1).getBounds2D();
        Rectangle2D kernedBottom = kerned.get(1).getBounds2D();
        assertTrue(kernedBottom.getMinY() < plainBottom.getMinY() - 1,
                plainBottom.getMinY() + " -> " + kernedBottom.getMinY());
        // Hm is not moved.
        assertEquals(plain.get(0).getBounds2D().getMinY(), kerned.get(0).getBounds2D().getMinY(), 1e-3);

        Area intersection = new Area(kerned.get(0));
        intersection.intersect(new Area(kerned.get(1)));
        assertTrue(intersection.isEmpty(), "the outlines of Hm and kA must not touch");
    }
}
