/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
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
import jsesh.render.layout.insert.ViewOutlineExtractor;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// The space between the signs of a quadrat (`innerquadratgap`,
/// [jsesh.render.style.GeometrySpecification#innerQuadratSkip()]) is
/// independent of the space between quadrats (`quadratgap`).
public class InnerQuadratGapLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-3;

    /// The bounds of the signs of a text, as drawn, in document coordinates.
    private List<Rectangle2D> signBounds(String mdc, TextOrientation orientation, double smallSkip,
            double innerQuadratSkip) throws MDCSyntaxError {
        JSeshStyle style = JSeshStyle.DEFAULT.copy()
                .options(o -> o.textOrientation(orientation))
                .geometry(g -> g.smallSkip(smallSkip).innerQuadratSkip(innerQuadratSkip))
                .build();
        JSeshRenderContext context = new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
        MDCView root = new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
        HieroglyphDrawer drawer = new HieroglyphDrawer(context.hieroglyphShapeRepository());
        ViewOutlineExtractor extractor = new ViewOutlineExtractor(drawer,
                drawer.scaleFromFontToStyle(context.jseshStyle()), TextDirection.LEFT_TO_RIGHT);
        List<Rectangle2D> result = new ArrayList<>();
        for (Shape s : extractor.outlines(root)) {
            result.add(s.getBounds2D());
        }
        return result;
    }

    private static double horizontalGap(List<Rectangle2D> signs, int i) {
        return signs.get(i + 1).getMinX() - signs.get(i).getMaxX();
    }

    private static double verticalGap(List<Rectangle2D> signs, int i) {
        return signs.get(i + 1).getMinY() - signs.get(i).getMaxY();
    }

    @Test
    public void theSignsOfALineOfAQuadratUseTheInnerQuadratGap() throws MDCSyntaxError {
        String mdc = "Z1*Z1-Z1";
        List<Rectangle2D> narrow = signBounds(mdc, TextOrientation.HORIZONTAL, 2, 2);
        List<Rectangle2D> wide = signBounds(mdc, TextOrientation.HORIZONTAL, 2, 6);
        // Between the signs of the quadrat.
        assertEquals(4, horizontalGap(wide, 0) - horizontalGap(narrow, 0), EPSILON);
        // Between the quadrats.
        assertEquals(horizontalGap(narrow, 1), horizontalGap(wide, 1), EPSILON);
    }

    @Test
    public void theQuadratGapDoesNotChangeTheSignsOfAQuadrat() throws MDCSyntaxError {
        String mdc = "Z1*Z1-Z1";
        List<Rectangle2D> narrow = signBounds(mdc, TextOrientation.HORIZONTAL, 2, 2);
        List<Rectangle2D> wide = signBounds(mdc, TextOrientation.HORIZONTAL, 6, 2);
        assertEquals(horizontalGap(narrow, 0), horizontalGap(wide, 0), EPSILON);
        assertEquals(4, horizontalGap(wide, 1) - horizontalGap(narrow, 1), EPSILON);
    }

    /// In columns, the quadrats keep their natural height, so the space between
    /// the lines of a quadrat is the inner quadrat gap.
    @Test
    public void theLinesOfAQuadratUseTheInnerQuadratGap() throws MDCSyntaxError {
        String mdc = "X1:X1";
        List<Rectangle2D> narrow = signBounds(mdc, TextOrientation.VERTICAL, 2, 2);
        List<Rectangle2D> wide = signBounds(mdc, TextOrientation.VERTICAL, 2, 6);
        assertEquals(4, verticalGap(wide, 0) - verticalGap(narrow, 0), EPSILON);
    }

    /// The setting of a zone is in percents of the unit (18 points by default).
    @Test
    public void aZoneSetsItsOwnInnerQuadratGap() throws MDCSyntaxError {
        List<Rectangle2D> plain = signBounds("%zone[fitsigns=yes](Z1*Z1-Z1)", TextOrientation.HORIZONTAL, 2, 2);
        List<Rectangle2D> zone = signBounds("%zone[innerquadratgap=50](Z1*Z1-Z1)", TextOrientation.HORIZONTAL, 2,
                2);
        assertEquals(7, horizontalGap(zone, 0) - horizontalGap(plain, 0), EPSILON);
        assertEquals(horizontalGap(plain, 1), horizontalGap(zone, 1), EPSILON);
    }
}
