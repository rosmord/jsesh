/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.font.FontRenderContext;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.Tab;
import jsesh.model.Zone;
import jsesh.model.ZoneContent;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Layout of dynamic tabs (`%tab`, see [jsesh.model.Tab]) in zones.
///
/// Positions are layout coordinates, always left to right (right-to-left
/// text is mirrored when drawn).
public class TabLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-3;
    private static final JSeshStyle STYLE = JSeshStyle.DEFAULT;
    private static final float QUADRAT = STYLE.geometry().maxCadratHeight();
    /// The default tab gap, in lines: the space between quadrats.
    private static final float GAP = STYLE.geometry().smallSkip();
    /// The default tab gap, in columns.
    private static final float COLUMN_GAP = STYLE.geometry().columnQuadratSkip();

    private MDCView layout(String mdc) throws MDCSyntaxError {
        JSeshRenderContext context = new JSeshRenderContext(STYLE, PredefinedFonts.buildAllEmbeddedFonts());
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
    }

    /// The view of the content of the zone which is the whole text.
    private MDCView zone(String mdc) throws MDCSyntaxError {
        MDCView zone = layout(mdc).getSubView(0);
        assertInstanceOf(Zone.class, zone.getModel());
        MDCView content = zone.getFirstSubView();
        assertInstanceOf(ZoneContent.class, content.getModel());
        return content;
    }

    private static double left(MDCView v, int i) {
        return v.getSubView(i).getPosition().x;
    }

    private static double right(MDCView v, int i) {
        return left(v, i) + v.getSubView(i).getWidth();
    }

    private static double top(MDCView v, int i) {
        return v.getSubView(i).getPosition().y;
    }

    private static double bottom(MDCView v, int i) {
        return top(v, i) + v.getSubView(i).getHeight();
    }

    // Subviews: 0 A1, 1 %tab, 2 B1, 3 !, 4-6 G17, 7 %tab, 8 C1.
    private static final String TWO_LINES = "A1-%tab-B1-!G17-G17-G17-%tab-C1";

    @Test
    public void cellsAfterATabStartAtTheSameStop() throws MDCSyntaxError {
        MDCView z = zone("%zone(" + TWO_LINES + ")");
        assertEquals(left(z, 2), left(z, 8), EPSILON);
        // The stop is pushed by the longest first cell, plus the gap.
        assertEquals(right(z, 6) + GAP, left(z, 8), EPSILON);
        assertEquals(0, left(z, 0), EPSILON);
        assertEquals(0, left(z, 4), EPSILON);
    }

    @Test
    public void theViewOfATabIsAtItsStop() throws MDCSyntaxError {
        MDCView z = zone("%zone(" + TWO_LINES + ")");
        assertInstanceOf(Tab.class, z.getSubView(1).getModel());
        assertEquals(left(z, 8), left(z, 1), EPSILON);
        assertEquals(left(z, 8), left(z, 7), EPSILON);
    }

    @Test
    public void theTabGapCanBeSet() throws MDCSyntaxError {
        MDCView z = zone("%zone[tabgap=100](" + TWO_LINES + ")");
        assertEquals(right(z, 6) + QUADRAT, left(z, 8), EPSILON);
    }

    @Test
    public void aTabAlignsTheCellBeforeIt() throws MDCSyntaxError {
        MDCView end = zone("%zone(A1-%tab[align=end]-B1-!G17-G17-G17-%tab-C1)");
        assertEquals(left(end, 8) - GAP, right(end, 0), EPSILON);
        MDCView center = zone("%zone(A1-%tab[align=center]-B1-!G17-G17-G17-%tab-C1)");
        assertEquals((left(center, 8) - GAP) / 2, (left(center, 0) + right(center, 0)) / 2, EPSILON);
    }

    @Test
    public void aJustifiedCellFillsItsColumn() throws MDCSyntaxError {
        // 0-1 A1, 2 %tab, 3 B1, 4 !, 5-9 G17, 10 %tab, 11 C1.
        MDCView z = zone("%zone(A1-A1-%tab[align=justify]-B1-!G17-G17-G17-G17-G17-%tab-C1)");
        assertEquals(0, left(z, 0), EPSILON);
        assertEquals(left(z, 11) - GAP, right(z, 1), EPSILON);
    }

    @Test
    public void aCellCanSpanSkippedStops() throws MDCSyntaxError {
        // 0-3 A1, 4 %tab[n=2], 5 B1, 6 !, 7 G17, 8 %tab, 9 C1, 10 %tab, 11 D1.
        MDCView z = zone("%zone(A1-A1-A1-A1-%tab[n=2]-B1-!G17-%tab-C1-%tab-D1)");
        // Stop 1 is pushed by G17 only.
        assertEquals(right(z, 7) + GAP, left(z, 9), EPSILON);
        // Stop 2 by the four A1 (longer than G17 + C1).
        assertEquals(right(z, 3) + GAP, left(z, 5), EPSILON);
        assertEquals(left(z, 5), left(z, 11), EPSILON);
    }

    @Test
    public void aWrongIndexIsTheNextOne() throws MDCSyntaxError {
        // 0 A1, 1 %tab, 2 B1, 3 %tab[n=1] (wrong: becomes 2), 4 C1, 5 !, 6 G17, 7 %tab[n=2], 8 D1.
        MDCView z = zone("%zone(A1-%tab-B1-%tab[n=1]-C1-!G17-%tab[n=2]-D1)");
        assertEquals(left(z, 4), left(z, 8), EPSILON);
    }

    @Test
    public void theLastCellTakesTheAlignmentOfTheLine() throws MDCSyntaxError {
        // 0 A1, 1 %tab, 2 B1, 3 !, 4-8 G17.
        MDCView z = zone("%zone(A1-%tab-B1-![align=end]G17-G17-G17-G17-G17)");
        assertEquals(right(z, 8), right(z, 2), EPSILON);
        assertEquals(0, left(z, 0), EPSILON);
    }

    @Test
    public void theLastCellIsAlignedInTheWidthOfTheZone() throws MDCSyntaxError {
        MDCView z = zone("%zone[width=1000](A1-%tab-B1-![align=end]G17)");
        assertEquals(10 * QUADRAT, right(z, 2), EPSILON);
    }

    @Test
    public void onlyTheTabsBeforeTheFirstSoftWrapCount() throws MDCSyntaxError {
        // 0 A1, 1 %tab, 2-7 B1, 8 %tab (after the wrap), 9 C1, 10 !, 11-12 G17, 13 %tab, 14 D1.
        MDCView z = zone("%zone[width=300](A1-%tab-B1-B1-B1-B1-B1-B1-%tab-C1-!G17-G17-%tab-D1)");
        assertEquals(left(z, 2), left(z, 14), EPSILON);
        assertEquals(right(z, 12) + GAP, left(z, 14), EPSILON);
        assertTrue(top(z, 9) > top(z, 2), "the line is wrapped");
    }

    @Test
    public void tabsWorkInColumns() throws MDCSyntaxError {
        MDCView z = zone("%zone[layout=columns](" + TWO_LINES + ")");
        assertEquals(top(z, 2), top(z, 8), EPSILON);
        assertEquals(bottom(z, 6) + COLUMN_GAP, top(z, 8), EPSILON);
        assertTrue(left(z, 8) > left(z, 2), "two columns");
    }

    @Test
    public void rightToLeftUsesTheSameLayoutCoordinates() throws MDCSyntaxError {
        MDCView ltr = zone("%zone(" + TWO_LINES + ")");
        MDCView rtl = zone("%zone[dir=rl](" + TWO_LINES + ")");
        for (int i : new int[] { 0, 2, 4, 8 }) {
            assertEquals(left(ltr, i), left(rtl, i), EPSILON);
        }
    }

    @Test
    public void linesWithoutTabsAreNotAffected() throws MDCSyntaxError {
        // 0 A1, 1 %tab, 2 B1, 3 !, 4-6 G17, 7 !, 8 A1, 9 B1.
        MDCView z = zone("%zone(A1-%tab-B1-!G17-G17-G17-!A1-B1)");
        assertEquals(0, left(z, 8), EPSILON);
        assertEquals(right(z, 8) + GAP, left(z, 9), EPSILON);
        assertEquals(GAP, left(z, 2) - right(z, 0), EPSILON);
    }

    @Test
    public void anAbsoluteTabAfterATabIsIgnored() throws MDCSyntaxError {
        // 0 A1, 1 %tab, 2 B1, 3 ?400, 4 C1.
        MDCView z = zone("%zone(A1-%tab-B1-?400-C1)");
        assertEquals(right(z, 2) + GAP, left(z, 4), EPSILON);
    }

    @Test
    public void tabsAreIgnoredOutsideZones() throws MDCSyntaxError {
        MDCView text = layout(TWO_LINES);
        MDCView plain = layout("A1-B1-!G17-G17-G17-C1");
        assertEquals(left(plain, 1), left(text, 2), EPSILON);
        assertEquals(left(plain, 6), left(text, 8), EPSILON);
    }

    @Test
    public void lonelyTabsAreHarmless() throws MDCSyntaxError {
        MDCView z = zone("%zone(%tab[n=3])");
        assertEquals(0, left(z, 0), EPSILON);
        zone("%zone(%tab-%tab-!%tab)");
    }

    @Test
    public void emptyCellsDoNotPushStops() throws MDCSyntaxError {
        // 0 %tab, 1 B1, 2 !, 3 A1, 4 %tab, 5 C1.
        MDCView z = zone("%zone(%tab-B1-!A1-%tab-C1)");
        assertEquals(right(z, 3) + GAP, left(z, 1), EPSILON);
        assertEquals(left(z, 1), left(z, 5), EPSILON);
    }
}
