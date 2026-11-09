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
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.Zone;
import jsesh.model.ZoneContent;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Layout of `%zone(...)`.
public class ZoneLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-3;
    private static final JSeshStyle STYLE = JSeshStyle.DEFAULT;
    private static final double QUADRAT = STYLE.geometry().maxCadratHeight();

    private MDCView layout(String mdc) throws MDCSyntaxError {
        return layout(mdc, STYLE);
    }

    private MDCView layout(String mdc, JSeshStyle style) throws MDCSyntaxError {
        JSeshRenderContext context = new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
    }

    /// The view of a zone, and the view of its content.
    private static MDCView content(MDCView zone) {
        assertInstanceOf(Zone.class, zone.getModel());
        MDCView content = zone.getFirstSubView();
        assertInstanceOf(ZoneContent.class, content.getModel());
        return content;
    }

    private static List<Double> rows(MDCView view) {
        return IntStream.range(0, view.getNumberOfSubviews())
                .mapToObj(i -> view.getSubView(i).getPosition().y)
                .distinct().sorted().toList();
    }

    private static List<Double> columns(MDCView view) {
        return IntStream.range(0, view.getNumberOfSubviews())
                .mapToObj(i -> view.getSubView(i).getPosition().x)
                .distinct().sorted().toList();
    }

    @Test
    public void aZoneHasTheNaturalSizeOfItsContent() throws MDCSyntaxError {
        MDCView plain = layout("A1-A1-A1");
        MDCView zone = layout("%zone(A1-A1-A1)").getSubView(0);
        double margins = 2 * STYLE.geometry().leftMargin();
        assertEquals(plain.getWidth() - margins, zone.getWidth(), EPSILON);
        assertEquals(3, content(zone).getNumberOfSubviews());
        assertEquals(1, rows(content(zone)).size());
    }

    @Test
    public void aZoneIsNotWrappedByDefault() throws MDCSyntaxError {
        MDCView zone = layout("%zone(A1-A1-A1-A1-A1-A1-A1-A1-A1-A1)").getSubView(0);
        assertEquals(1, rows(content(zone)).size());
    }

    @Test
    public void theWidthIsFixedAndLinesWrapAtIt() throws MDCSyntaxError {
        MDCView zone = layout("%zone[width=300](A1-A1-A1-A1-A1-A1-A1-A1-A1-A1)").getSubView(0);
        assertEquals(3 * QUADRAT, zone.getWidth(), EPSILON);
        assertTrue(rows(content(zone)).size() >= 3, "rows: " + rows(content(zone)));
    }

    @Test
    public void theHeightIsFixed() throws MDCSyntaxError {
        MDCView zone = layout("%zone[height=500](A1)").getSubView(0);
        assertEquals(5 * QUADRAT, zone.getHeight(), EPSILON);
    }

    @Test
    public void aFrameAddsItsPadding() throws MDCSyntaxError {
        MDCView plain = layout("%zone(A1-A1)").getSubView(0);
        MDCView framed = layout("%zone[frame](A1-A1)").getSubView(0);
        double padding = Layout.zoneFramePadding(STYLE);
        assertEquals(plain.getWidth() + 2 * padding, framed.getWidth(), EPSILON);
        assertEquals(plain.getHeight() + 2 * padding, framed.getHeight(), EPSILON);
        assertEquals(padding, content(framed).getPosition().x, EPSILON);
    }

    @Test
    public void anEmptyZoneIsAtLeastAQuadrat() throws MDCSyntaxError {
        MDCView zone = layout("%zone()").getSubView(0);
        assertTrue(zone.getWidth() >= QUADRAT - EPSILON);
        assertTrue(zone.getHeight() >= QUADRAT - EPSILON);
    }

    @Test
    public void aZoneInColumnsInsideLines() throws MDCSyntaxError {
        MDCView text = layout("A1-%zone[layout=columns](A1-A1-A1)-A1");
        MDCView zone = text.getSubView(1);
        MDCView content = content(zone);
        assertEquals(TextOrientation.VERTICAL, content.getOrientation());
        assertEquals(1, columns(content).size());
        assertEquals(3, rows(content).size());
        assertTrue(zone.getHeight() > zone.getWidth());
        // The surrounding text is still in one line.
        assertEquals(TextOrientation.HORIZONTAL, text.getSubView(0).getOrientation());
        assertTrue(text.getSubView(2).getPosition().x > zone.getPosition().x + zone.getWidth() - EPSILON);
    }

    @Test
    public void lineBreaksAndPageBreaksInAZoneStartNewLines() throws MDCSyntaxError {
        assertEquals(2, rows(content(layout("%zone(A1-A1!A1)").getSubView(0))).size());
        MDCView withPageBreak = content(layout("%zone(A1-!!-A1)").getSubView(0));
        double first = withPageBreak.getSubView(0).getPosition().y;
        double last = withPageBreak.getSubView(2).getPosition().y;
        assertTrue(last > first + QUADRAT - EPSILON, first + " / " + last);
    }

    @Test
    public void aRightToLeftZoneIsMirroredInsideALeftToRightText() throws MDCSyntaxError {
        MDCView text = layout("A1-%zone[dir=rl](A1-A2)");
        MDCView content = content(text.getSubView(1));
        assertEquals(TextDirection.LEFT_TO_RIGHT, text.getDirection());
        assertEquals(TextDirection.RIGHT_TO_LEFT, content.getDirection());
        // A1, the first sign, is drawn on the right.
        Rectangle2D first = content.getSubViewBounds(0);
        Rectangle2D second = content.getSubViewBounds(1);
        assertTrue(first.getX() > second.getX());
        assertEquals(TextDirection.RIGHT_TO_LEFT, content.getSubView(0).getDirection());
    }

    @Test
    public void settingsAreInheritedFromTheEnclosingZone() throws MDCSyntaxError {
        MDCView outer = content(layout("%zone[layout=columns,dir=rl](A1-%zone(A1-A1))").getSubView(0));
        MDCView inner = content(outer.getSubView(1));
        assertEquals(TextOrientation.VERTICAL, inner.getOrientation());
        assertEquals(TextDirection.RIGHT_TO_LEFT, inner.getDirection());
    }

    @Test
    public void zonesInALineAreSideBySideAndALineBreakStacksThem() throws MDCSyntaxError {
        MDCView sideBySide = layout("%zone(A1-A1)-%zone(A1)");
        assertEquals(sideBySide.getSubView(0).getPosition().y, sideBySide.getSubView(1).getPosition().y, EPSILON);
        assertTrue(sideBySide.getSubView(1).getPosition().x >= sideBySide.getSubView(0).getWidth());

        MDCView stacked = layout("%zone(A1-A1)!%zone(A1)");
        MDCView first = stacked.getSubView(0);
        MDCView second = stacked.getSubView(2);
        assertEquals(first.getPosition().x, second.getPosition().x, EPSILON);
        assertTrue(second.getPosition().y >= first.getPosition().y + first.getHeight() - EPSILON);
    }

    @Test
    public void aZoneTooWideForTheRestOfAWrappedLineGoesToTheNextLine() throws MDCSyntaxError {
        JSeshStyle style = STYLE.copy().geometry(g -> g.wrapWidth(8 * QUADRAT)).build();
        JSeshRenderContext context = new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
        MDCView text = new ViewBuilder().buildView(
                new MDCParserModelGenerator().parse("A1-A1-A1-A1-%zone[width=500](A1)"), context,
                new JSeshTechRenderContext(FRC, 1.0));
        assertTrue(text.getSubView(4).getPosition().y > text.getSubView(0).getPosition().y);
    }

    @Test
    public void separatorsAreInTheMiddleOfTheGapBetweenLines() throws MDCSyntaxError {
        MDCView content = content(layout("%zone[separator=5](A1!A1!A1)").getSubView(0));
        double[] separators = content.getSeparators();
        assertEquals(2, separators.length);
        List<Double> rows = rows(content);
        assertEquals(3, rows.size());
        for (int i = 0; i < 2; i++) {
            assertTrue(rows.get(i) + QUADRAT < separators[i] && separators[i] < rows.get(i + 1));
        }
    }

    @Test
    public void separatorsAreInTheMiddleOfTheGapBetweenColumns() throws MDCSyntaxError {
        MDCView content = content(layout("%zone[layout=columns,separator=5](A1!A1)").getSubView(0));
        double[] separators = content.getSeparators();
        assertEquals(1, separators.length);
        List<Double> columns = columns(content);
        assertEquals(2, columns.size());
        assertTrue(columns.get(0) < separators[0] && separators[0] < columns.get(1));
    }

    @Test
    public void wrappedLinesAreSeparatedToo() throws MDCSyntaxError {
        MDCView content = content(layout("%zone[width=300,separator=5](A1-A1-A1-A1-A1-A1-A1-A1-A1-A1)").getSubView(0));
        assertEquals(rows(content).size() - 1, content.getSeparators().length);
        assertTrue(content.getSeparators().length > 0);
    }

    @Test
    public void aSingleLineHasNoSeparator() throws MDCSyntaxError {
        assertEquals(0, content(layout("%zone[separator=5](A1-A1)").getSubView(0)).getSeparators().length);
    }

    @Test
    public void quadratHeightSetsTheHeightOfTheLines() throws MDCSyntaxError {
        List<Double> normal = rows(content(layout("%zone(A1!A1)").getSubView(0)));
        List<Double> tall = rows(content(layout("%zone[quadratheight=200](A1!A1)").getSubView(0)));
        assertTrue(tall.get(1) - tall.get(0) > 1.5 * (normal.get(1) - normal.get(0)));
    }

    @Test
    public void quadratWidthSetsTheWidthOfTheColumns() throws MDCSyntaxError {
        List<Double> normal = columns(content(layout("%zone[layout=columns](A1!A1)").getSubView(0)));
        List<Double> wide = columns(content(layout("%zone[layout=columns,quadratwidth=300](A1!A1)").getSubView(0)));
        assertTrue(Math.abs(wide.get(1) - wide.get(0)) > 1.5 * Math.abs(normal.get(1) - normal.get(0)));
    }

    /// The width of the first sign of the first quadrat of a zone.
    private static double firstSignWidth(MDCView zone) {
        MDCView quadrat = content(zone).getSubView(0);
        return quadrat.getSubView(0).getSubView(0).getWidth();
    }

    @Test
    public void aLoneLowBroadSignFillsTheColumnByDefault() throws MDCSyntaxError {
        double fitted = firstSignWidth(layout("%zone[layout=columns](n)").getSubView(0));
        assertEquals(STYLE.geometry().maxCadratWidth(), fitted, EPSILON);
    }

    @Test
    public void fitSignsNoKeepsTheNaturalWidthOfTheSign() throws MDCSyntaxError {
        double fitted = firstSignWidth(layout("%zone[layout=columns](n)").getSubView(0));
        double natural = firstSignWidth(layout("%zone[layout=columns,fitsigns=no](n)").getSubView(0));
        assertTrue(natural < fitted - 1, natural + " vs " + fitted);
    }

    @Test
    public void fitSignsIsInheritedFromTheDocumentAndFromTheEnclosingZone() throws MDCSyntaxError {
        JSeshStyle noFit = STYLE.copy().options(o -> o.fitLoneSigns(false)).build();
        double fitted = firstSignWidth(layout("%zone[layout=columns](n)").getSubView(0));
        double natural = firstSignWidth(layout("%zone[layout=columns](n)", noFit).getSubView(0));
        assertTrue(natural < fitted - 1, natural + " vs " + fitted);
        // A zone overrides the document, and its own setting is inherited by inner zones.
        assertEquals(fitted, firstSignWidth(layout("%zone[layout=columns,fitsigns=yes](n)", noFit).getSubView(0)), EPSILON);
        MDCView outer = layout("%zone[layout=columns,fitsigns=no](%zone(n))").getSubView(0);
        MDCView inner = content(outer).getSubView(0);
        assertEquals(natural, firstSignWidth(inner), EPSILON);
    }

    @Test
    public void aScaledZoneIsSmallerAndItsLengthsAreInItsOwnQuadrats() throws MDCSyntaxError {
        MDCView plain = layout("%zone(A1-A1-A1)").getSubView(0);
        MDCView half = layout("%zone[scale=50](A1-A1-A1)").getSubView(0);
        assertEquals(plain.getWidth() / 2, half.getWidth(), EPSILON);
        assertEquals(plain.getHeight() / 2, half.getHeight(), EPSILON);
        // The content is laid out at full size, and its view is scaled.
        assertEquals(0.5, content(half).getXScale(), EPSILON);
        assertEquals(content(plain).getSubView(2).getPosition().x, content(half).getSubView(2).getPosition().x, EPSILON);
        // width=300 is three quadrats of the zone.
        MDCView fixed = layout("%zone[scale=50,width=300](A1)").getSubView(0);
        assertEquals(1.5 * QUADRAT, fixed.getWidth(), EPSILON);
    }

    @Test
    public void theGapsAreNamedByRole() throws MDCSyntaxError {
        double unit = STYLE.geometry().unit() / 100.0;
        // In columns: columngap between columns, columnquadratgap between quadrats.
        MDCView columns = content(layout("%zone[layout=columns,columngap=100,columnquadratgap=50](A1-A1!A1)").getSubView(0));
        List<Double> xs = columns(columns);
        assertEquals(2, xs.size());
        MDCView first = columns.getSubView(0);
        assertEquals(xs.get(0) + first.getWidth() + 100 * unit, xs.get(1), EPSILON);
        List<Double> ys = rows(columns);
        assertEquals(ys.get(0) + first.getHeight() + 50 * unit, columns.getSubView(1).getPosition().y, EPSILON);
        // linegap doesn't change the columns.
        MDCView withLineGap = content(layout("%zone[layout=columns,linegap=300](A1-A1!A1)").getSubView(0));
        MDCView normal = content(layout("%zone[layout=columns](A1-A1!A1)").getSubView(0));
        assertEquals(columns(normal), columns(withLineGap));
        assertEquals(rows(normal), rows(withLineGap));
    }

    @Test
    public void signHeightScalesTheSigns() throws MDCSyntaxError {
        double normal = firstSignWidth(layout("%zone(A1)").getSubView(0));
        double small = firstSignWidth(layout("%zone[signheight=50](A1)").getSubView(0));
        assertEquals(normal / 2, small, 0.01);
    }
}
