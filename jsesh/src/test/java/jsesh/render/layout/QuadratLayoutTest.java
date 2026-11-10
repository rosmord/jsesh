/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.font.FontRenderContext;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.constants.TextOrientation;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// How quadrats which are too wide or too high are scaled: the width limit
/// (`honourMaxQuadratWidthInLines`) and the way of scaling (`uniformQuadratScaling`).
public class QuadratLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-3;

    private static final JSeshStyle DEFAULT = JSeshStyle.DEFAULT;
    private static final JSeshStyle UNIFORM = DEFAULT.copy().options(o -> o.uniformQuadratScaling(true)).build();
    private static final JSeshStyle NO_WIDTH_LIMIT = DEFAULT.copy()
            .options(o -> o.honourMaxQuadratWidthInLines(false)).build();
    private static final JSeshStyle COLUMNS = DEFAULT.copy()
            .options(o -> o.textOrientation(TextOrientation.VERTICAL)).build();
    private static final JSeshStyle COLUMNS_UNIFORM = DEFAULT.copy()
            .options(o -> o.textOrientation(TextOrientation.VERTICAL).uniformQuadratScaling(true)).build();

    /// A quadrat whose first line is too wide for the quadrat width (and too high for the quadrat, as a whole).
    private static final String WIDE_AND_HIGH = "A1*A1*A1*A1:t";
    /// A quadrat which is too wide, but low.
    private static final String WIDE_AND_LOW = "n*n*n*n*n*n:n";

    /// The view of the first quadrat of the text.
    private MDCView quadrat(String mdc, JSeshStyle style) throws MDCSyntaxError {
        JSeshRenderContext context = new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
        MDCView root = new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
        return root.getSubView(0);
    }

    private static double maxWidth(JSeshStyle style) {
        return style.geometry().maxCadratWidth();
    }

    private static double maxHeight(JSeshStyle style) {
        return style.geometry().maxCadratHeight();
    }

    @Test
    public void byDefaultEachLineIsScaledToTheMaximalWidth() throws MDCSyntaxError {
        MDCView q = quadrat(WIDE_AND_HIGH, DEFAULT);
        MDCView wideLine = q.getSubView(0);
        MDCView narrowLine = q.getSubView(1);
        // The first line is scaled to the maximal width; the other one, which fits, is not scaled.
        assertEquals(maxWidth(DEFAULT), wideLine.getWidth(), EPSILON);
        assertTrue(wideLine.getXScale() < 1);
        assertEquals(1.0, narrowLine.getXScale(), EPSILON);
        assertTrue(q.getHeight() <= maxHeight(DEFAULT) + EPSILON);
    }

    @Test
    public void uniformScalingScalesTheQuadratAsAWhole() throws MDCSyntaxError {
        MDCView q = quadrat(WIDE_AND_HIGH, UNIFORM);
        // The lines keep their natural scale: the scale is the quadrat's.
        assertEquals(1.0, q.getSubView(0).getXScale(), EPSILON);
        assertEquals(1.0, q.getSubView(1).getXScale(), EPSILON);
        assertTrue(q.getXScale() < 1);
        assertEquals(q.getXScale(), q.getYScale(), EPSILON);
        assertTrue(q.getWidth() <= maxWidth(UNIFORM) + EPSILON);
        assertTrue(q.getHeight() <= maxHeight(UNIFORM) + EPSILON);
    }

    @Test
    public void uniformScalingUsesTheSmallestOfTheWidthAndHeightScales() throws MDCSyntaxError {
        MDCView plain = quadrat(WIDE_AND_HIGH, NO_WIDTH_LIMIT);
        // (The plain quadrat has already been scaled to the maximal height.)
        double scaleForWidth = maxWidth(UNIFORM) / (plain.getWidth() / plain.getXScale());
        double scaleForHeight = maxHeight(UNIFORM) / (plain.getHeight() / plain.getYScale());
        MDCView q = quadrat(WIDE_AND_HIGH, UNIFORM);
        assertEquals(Math.min(scaleForWidth, scaleForHeight), q.getXScale(), EPSILON);
    }

    @Test
    public void withoutAWidthLimitTheQuadratIsOnlyScaledToTheHeight() throws MDCSyntaxError {
        MDCView q = quadrat(WIDE_AND_HIGH, NO_WIDTH_LIMIT);
        assertTrue(q.getWidth() > maxWidth(NO_WIDTH_LIMIT));
        assertEquals(1.0, q.getSubView(0).getXScale(), EPSILON);
        assertTrue(q.getHeight() <= maxHeight(NO_WIDTH_LIMIT) + EPSILON);
    }

    @Test
    public void uniformScalingDoesNotChangeAQuadratWithoutWidthLimit() throws MDCSyntaxError {
        JSeshStyle uniformNoLimit = NO_WIDTH_LIMIT.copy().options(o -> o.uniformQuadratScaling(true)).build();
        MDCView a = quadrat(WIDE_AND_HIGH, NO_WIDTH_LIMIT);
        MDCView b = quadrat(WIDE_AND_HIGH, uniformNoLimit);
        assertEquals(a.getWidth(), b.getWidth(), EPSILON);
        assertEquals(a.getHeight(), b.getHeight(), EPSILON);
        assertEquals(a.getXScale(), b.getXScale(), EPSILON);
    }

    @Test
    public void aLowQuadratKeepsTheHeightOfALineWhenScaledForItsWidth() throws MDCSyntaxError {
        for (JSeshStyle style : new JSeshStyle[] { DEFAULT, UNIFORM }) {
            MDCView q = quadrat(WIDE_AND_LOW, style);
            assertEquals(maxWidth(style), q.getWidth(), EPSILON);
            assertEquals(maxHeight(style), q.getHeight(), EPSILON);
        }
    }

    @Test
    public void aSingleLineQuadratIsNotScaledForItsWidth() throws MDCSyntaxError {
        for (JSeshStyle style : new JSeshStyle[] { DEFAULT, UNIFORM }) {
            MDCView q = quadrat("n*n*n*n*n*n", style);
            assertTrue(q.getWidth() > maxWidth(style));
        }
    }

    @Test
    public void inColumnsTheWidthIsAlwaysLimited() throws MDCSyntaxError {
        for (JSeshStyle style : new JSeshStyle[] { COLUMNS, COLUMNS_UNIFORM }) {
            MDCView q = quadrat(WIDE_AND_HIGH, style);
            assertEquals(maxWidth(style), q.getWidth(), EPSILON);
        }
    }

    @Test
    public void inColumnsUniformScalingScalesTheQuadratAsAWhole() throws MDCSyntaxError {
        MDCView q = quadrat(WIDE_AND_HIGH, COLUMNS_UNIFORM);
        assertEquals(1.0, q.getSubView(0).getXScale(), EPSILON);
        assertTrue(q.getXScale() < 1);
        assertEquals(q.getXScale(), q.getYScale(), EPSILON);
    }

    @Test
    public void aQuadratWhichFitsIsNotScaled() throws MDCSyntaxError {
        for (JSeshStyle style : new JSeshStyle[] { DEFAULT, UNIFORM }) {
            MDCView q = quadrat("n:n", style);
            assertEquals(1.0, q.getXScale(), EPSILON);
            assertEquals(1.0, q.getSubView(0).getXScale(), EPSILON);
        }
    }
}
