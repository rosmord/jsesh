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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.Insert;
import jsesh.model.constants.TextDirection;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.elements.HieroglyphDrawer;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Layout of `@insert` with real signs.
public class InsertLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    private JSeshRenderContext renderContext(TextDirection direction) {
        JSeshStyle style = JSeshStyle.DEFAULT.copy().options(o -> o.textDirection(direction)).build();
        return new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
    }

    private MDCView layout(String mdc, JSeshRenderContext renderContext) throws MDCSyntaxError {
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), renderContext,
                new JSeshTechRenderContext(FRC, 1.0));
    }

    private static MDCView findInsert(MDCView view) {
        if (view.getModel() instanceof Insert) {
            return view;
        }
        for (MDCView sub : view.getSubViews()) {
            MDCView found = findInsert(sub);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /// The outlines of a sub view of the insert, in the insert's drawing coordinates.
    private static List<Shape> placedSilhouette(ViewSilhouette silhouette, MDCView insert, int index) {
        Rectangle2D bounds = insert.getSubViewBounds(index);
        AffineTransform t = AffineTransform.getTranslateInstance(bounds.getMinX(), bounds.getMinY());
        List<Shape> result = new ArrayList<>();
        for (Shape s : silhouette.silhouette(insert.getSubView(index))) {
            result.add(t.createTransformedShape(s));
        }
        return result;
    }

    private void checkNoOverlap(String mdc, TextDirection direction) throws MDCSyntaxError {
        JSeshRenderContext context = renderContext(direction);
        MDCView insert = findInsert(layout(mdc, context));
        assertNotNull(insert, mdc);
        assertEquals(2, insert.getNumberOfSubviews());

        HieroglyphDrawer drawer = new HieroglyphDrawer(context.hieroglyphShapeRepository());
        ViewSilhouette silhouette = new ViewSilhouette(drawer, drawer.scaleFromFontToStyle(context.jseshStyle()),
                direction);
        Rectangle2D whole = new Rectangle2D.Double(0, 0, insert.getWidth(), insert.getHeight());
        ShapeMask main = new ShapeMask(whole, 64);
        for (Shape s : placedSilhouette(silhouette, insert, 0)) {
            main.fill(s, null);
        }
        ShapeMask inserted = new ShapeMask(main);
        for (Shape s : placedSilhouette(silhouette, insert, 1)) {
            inserted.fill(s, null);
        }
        assertFalse(main.isEmpty(), "main sign has an outline: " + mdc);
        assertFalse(inserted.isEmpty(), "inserted element has an outline: " + mdc);
        assertFalse(main.intersects(inserted), "no overlap: " + mdc + " " + direction);

        // Both views lie in the group's box.
        for (int i = 0; i < 2; i++) {
            Rectangle2D b = insert.getSubViewBounds(i);
            assertTrue(b.getMinX() >= -1e-3 && b.getMinY() >= -1e-3, mdc);
            assertTrue(b.getMaxX() <= insert.getWidth() + 1e-3 && b.getMaxY() <= insert.getHeight() + 1e-3, mdc);
        }
    }

    @ParameterizedTest
    @CsvSource({ "s", "e", "t", "b", "ts", "bs", "te", "be", "c" })
    public void insertedElementDoesNotOverlapTheMainSign(String dir) throws MDCSyntaxError {
        checkNoOverlap("@insert[dir=" + dir + "](O1,n:t)", TextDirection.LEFT_TO_RIGHT);
        checkNoOverlap("@insert[dir=" + dir + "](O1,n:t)", TextDirection.RIGHT_TO_LEFT);
    }

    @ParameterizedTest
    @CsvSource({ "te", "bs", "ts" })
    public void nestedInsertionsDoNotOverlap(String dir) throws MDCSyntaxError {
        // The main element of the outer insertion is itself an insertion.
        String mdc = "@insert[dir=" + dir + "](@insert[dir=c](O1,n:t),x)";
        checkNoOverlap(mdc, TextDirection.LEFT_TO_RIGHT);
        checkNoOverlap(mdc, TextDirection.RIGHT_TO_LEFT);
    }

    @Test
    public void mainElementCanBeAGroup() throws MDCSyntaxError {
        checkNoOverlap("@insert[dir=e](O1-O1,n)", TextDirection.LEFT_TO_RIGHT);
    }

    @Test
    public void insertedElementIsScaledDownToFit() throws MDCSyntaxError {
        JSeshRenderContext context = renderContext(TextDirection.LEFT_TO_RIGHT);
        MDCView insert = findInsert(layout("@insert[dir=c](O1,n:t:n:t)", context));
        assertNotNull(insert);
        assertTrue(insert.getSubView(1).getXScale() < 1.0f);
        assertEquals(insert.getSubView(1).getXScale(), insert.getSubView(1).getYScale(), 1e-6);
    }

    @Test
    public void enlargingTheFrameWidensTheGroup() throws MDCSyntaxError {
        JSeshRenderContext context = renderContext(TextDirection.LEFT_TO_RIGHT);
        MDCView plain = findInsert(layout("@insert[dir=e](O1,n)", context));
        MDCView wide = findInsert(layout("@insert[dir=e,dw=100](O1,n)", context));
        assertTrue(wide.getWidth() > plain.getWidth());
        assertEquals(plain.getHeight(), wide.getHeight(), 1e-3);
    }
}
