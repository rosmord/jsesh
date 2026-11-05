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
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.constants.TextDirection;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.style.PageSpecification;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// The alignment of lines (`-![align=...]`, see
/// [jsesh.sharedvocabulary.LineAlignment]) in [LineLayout].
public class LineAlignmentLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-3;
    private static final JSeshStyle STYLE = JSeshStyle.DEFAULT;
    private static final float QUADRAT = STYLE.geometry().maxCadratHeight();

    /// A first line of five A1, against which the second line is aligned.
    private static final String FIRST_LINE = "A1-A1-A1-A1-A1-!";
    /// The index of the first view of the second line.
    private static final int SECOND = 6;

    private MDCView layout(String mdc, JSeshStyle style) throws MDCSyntaxError {
        JSeshRenderContext context = new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
    }

    private MDCView layout(String mdc) throws MDCSyntaxError {
        return layout(mdc, STYLE);
    }

    private static double left(MDCView text, int i) {
        return text.getSubView(i).getPosition().x;
    }

    private static double right(MDCView text, int i) {
        return left(text, i) + text.getSubView(i).getWidth();
    }

    @Test
    public void linesAreAtTheStartByDefault() throws MDCSyntaxError {
        MDCView text = layout(FIRST_LINE + "A1-!A1");
        assertEquals(left(text, 0), left(text, SECOND), EPSILON);
        assertEquals(left(text, 0), left(text, SECOND + 2), EPSILON);
    }

    @Test
    public void aCentredLineIsCentredOnTheLongestLine() throws MDCSyntaxError {
        MDCView text = layout(FIRST_LINE + "A1-![align=center]");
        assertEquals((left(text, 0) + right(text, 4)) / 2, (left(text, SECOND) + right(text, SECOND)) / 2, EPSILON);
    }

    @Test
    public void anEndLineEndsWithTheLongestLine() throws MDCSyntaxError {
        MDCView text = layout(FIRST_LINE + "A1-A1-![align=end]");
        assertEquals(right(text, 4), right(text, SECOND + 1), EPSILON);
    }

    @Test
    public void aJustifiedLineFillsTheWidthOfTheLongestLine() throws MDCSyntaxError {
        MDCView text = layout(FIRST_LINE + "A1-A1-A1-A1-![align=justify]");
        assertEquals(left(text, 0), left(text, SECOND), EPSILON);
        assertEquals(right(text, 4), right(text, SECOND + 3), EPSILON);
    }

    @Test
    public void aJustifiedLineWhichIsTooShortStaysAtTheStart() throws MDCSyntaxError {
        MDCView plain = layout(FIRST_LINE + "A1-A1-!");
        MDCView text = layout(FIRST_LINE + "A1-A1-![align=justify]");
        assertEquals(left(plain, SECOND), left(text, SECOND), EPSILON);
        assertEquals(left(plain, SECOND + 1), left(text, SECOND + 1), EPSILON);
    }

    @Test
    public void theLastLineWithoutBreakTakesTheAlignmentOfThePreviousOne() throws MDCSyntaxError {
        MDCView text = layout("A1-A1-A1-A1-A1-![align=end]A1");
        assertEquals(right(text, 4), right(text, SECOND), EPSILON);
    }

    @Test
    public void aPageBreakCarriesTheAlignmentOfItsLine() throws MDCSyntaxError {
        MDCView text = layout(FIRST_LINE + "A1-!![align=end]A1-A1-A1-A1-A1");
        assertEquals(right(text, 4), right(text, SECOND), EPSILON);
    }

    @Test
    public void inAZoneWithAWidthLinesAreAlignedOnThatWidth() throws MDCSyntaxError {
        MDCView zone = layout("%zone[width=1200](A1-![align=end]A1-A1-![align=center])").getSubView(0);
        MDCView content = zone.getFirstSubView();
        assertEquals(12 * QUADRAT, right(content, 0), EPSILON);
        assertEquals(6 * QUADRAT, (left(content, 2) + right(content, 3)) / 2, EPSILON);
    }

    @Test
    public void onAPageLinesAreAlignedOnItsUsableWidth() throws MDCSyntaxError {
        JSeshStyle paged = STYLE.copy().geometry(g -> g
                .page(new PageSpecification(200f, 0f, 10f, 30f, 8f, 8f))
                .leftMargin(10f).topMargin(8f)).build();
        MDCView text = layout("A1-![align=end]A1-![align=center]", paged);
        assertEquals(170, right(text, 0), EPSILON);
        assertEquals(90, (left(text, 2) + right(text, 2)) / 2, EPSILON);
    }

    @Test
    public void softWrappedLinesAreEachCentred() throws MDCSyntaxError {
        String mdc = "A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-![align=center]";
        float wrapWidth = 5 * QUADRAT;
        JSeshStyle wrapped = STYLE.copy().geometry(g -> g.wrapWidth(wrapWidth)).build();
        MDCView text = layout(mdc, wrapped);
        double margin = STYLE.geometry().leftMargin();
        double middle = margin + (wrapWidth - 2 * margin) / 2;
        List<Double> rows = IntStream.range(0, 20).mapToObj(i -> text.getSubView(i).getPosition().y)
                .distinct().toList();
        assertTrue(rows.size() > 2, "rows: " + rows);
        for (double y : rows) {
            List<Integer> row = IntStream.range(0, 20).filter(i -> text.getSubView(i).getPosition().y == y).boxed()
                    .toList();
            double centre = (left(text, row.get(0)) + right(text, row.get(row.size() - 1))) / 2;
            assertEquals(middle, centre, EPSILON, "row at " + y);
        }
    }

    @Test
    public void rightToLeftLinesAreLaidOutAsLeftToRightOnes() throws MDCSyntaxError {
        String mdc = FIRST_LINE + "A1-![align=end]A1-A1-![align=center]A1-A1-A1-A1-![align=justify]";
        MDCView ltr = layout(mdc);
        MDCView rtl = layout(mdc, STYLE.copy().options(o -> o.textDirection(TextDirection.RIGHT_TO_LEFT)).build());
        assertEquals(ltr.getWidth(), rtl.getWidth(), EPSILON);
        for (int i = 0; i < ltr.getNumberOfSubviews(); i++) {
            assertEquals(left(ltr, i), left(rtl, i), EPSILON, "view " + i);
        }
    }

    @Test
    public void alignmentCanBeIgnored() throws MDCSyntaxError {
        JSeshStyle ignoring = STYLE.copy().options(o -> o.ignoreLineAlignment(true)).build();
        MDCView text = layout(FIRST_LINE + "A1-![align=end]", ignoring);
        assertEquals(left(text, 0), left(text, SECOND), EPSILON);
    }

    @Test
    public void zonesKeepTheirAlignmentWhenItIsIgnoredOutside() throws MDCSyntaxError {
        JSeshStyle ignoring = STYLE.copy().options(o -> o.ignoreLineAlignment(true)).build();
        MDCView zone = layout("%zone[width=1200](A1-![align=end])", ignoring).getSubView(0);
        assertEquals(12 * QUADRAT, right(zone.getFirstSubView(), 0), EPSILON);
    }
}
