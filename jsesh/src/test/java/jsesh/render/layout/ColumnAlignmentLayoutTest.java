/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.font.FontRenderContext;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.style.PageSpecification;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// The alignment of columns (`-![align=...]`, see
/// [jsesh.sharedvocabulary.LineAlignment]) in [ColumnLayout]: *start* is the
/// top, *end* the bottom.
public class ColumnAlignmentLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-3;
    private static final JSeshStyle STYLE = JSeshStyle.DEFAULT.copy()
            .options(o -> o.textOrientation(TextOrientation.VERTICAL)).build();
    private static final double QUADRAT = STYLE.geometry().maxCadratHeight();

    /// A first column of five A1, against which the second column is aligned.
    private static final String FIRST_COLUMN = "A1-A1-A1-A1-A1-!";
    /// The index of the first view of the second column.
    private static final int SECOND = 6;

    private MDCView layout(String mdc, JSeshStyle style) throws MDCSyntaxError {
        JSeshRenderContext context = new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
    }

    private MDCView layout(String mdc) throws MDCSyntaxError {
        return layout(mdc, STYLE);
    }

    private static double top(MDCView text, int i) {
        return text.getSubView(i).getPosition().y;
    }

    private static double bottom(MDCView text, int i) {
        return top(text, i) + text.getSubView(i).getHeight();
    }

    @Test
    public void columnsAreAtTheTopByDefault() throws MDCSyntaxError {
        MDCView text = layout(FIRST_COLUMN + "A1-!A1");
        assertEquals(top(text, 0), top(text, SECOND), EPSILON);
        assertEquals(top(text, 0), top(text, SECOND + 2), EPSILON);
    }

    @Test
    public void aCentredColumnIsCentredOnTheLongestColumn() throws MDCSyntaxError {
        MDCView text = layout(FIRST_COLUMN + "A1-![align=center]");
        assertEquals((top(text, 0) + bottom(text, 4)) / 2, (top(text, SECOND) + bottom(text, SECOND)) / 2, EPSILON);
    }

    @Test
    public void anEndColumnEndsWithTheLongestColumn() throws MDCSyntaxError {
        MDCView text = layout(FIRST_COLUMN + "A1-A1-![align=end]");
        assertEquals(bottom(text, 4), bottom(text, SECOND + 1), EPSILON);
    }

    @Test
    public void aJustifiedColumnFillsTheHeightOfTheLongestColumn() throws MDCSyntaxError {
        MDCView text = layout(FIRST_COLUMN + "A1-A1-A1-![align=justify]");
        assertEquals(top(text, 0), top(text, SECOND), EPSILON);
        assertEquals(bottom(text, 4), bottom(text, SECOND + 2), EPSILON);
    }

    @Test
    public void aSingleElementCannotBeJustified() throws MDCSyntaxError {
        MDCView text = layout(FIRST_COLUMN + "A1-![align=justify]");
        assertEquals(top(text, 0), top(text, SECOND), EPSILON);
    }

    @Test
    public void columnsAreNotMovedHorizontally() throws MDCSyntaxError {
        MDCView plain = layout(FIRST_COLUMN + "A1-A1-!");
        MDCView text = layout(FIRST_COLUMN + "A1-A1-![align=end]");
        assertEquals(plain.getWidth(), text.getWidth(), EPSILON);
        assertEquals(plain.getSubView(SECOND).getPosition().x, text.getSubView(SECOND).getPosition().x, EPSILON);
    }

    @Test
    public void theLastColumnWithoutBreakTakesTheAlignmentOfThePreviousOne() throws MDCSyntaxError {
        MDCView text = layout("A1-A1-A1-A1-A1-![align=end]A1");
        assertEquals(bottom(text, 4), bottom(text, SECOND), EPSILON);
    }

    @Test
    public void aPageBreakCarriesTheAlignmentOfItsColumn() throws MDCSyntaxError {
        MDCView text = layout(FIRST_COLUMN + "A1-!![align=end]A1-A1-A1-A1-A1");
        assertEquals(bottom(text, 4), bottom(text, SECOND), EPSILON);
    }

    @Test
    public void inAZoneWithAHeightColumnsAreAlignedOnThatHeight() throws MDCSyntaxError {
        MDCView zone = layout("%zone[layout=columns,height=1000](A1-![align=end]A1-A1-![align=center])",
                JSeshStyle.DEFAULT).getSubView(0);
        MDCView content = zone.getFirstSubView();
        assertEquals(10 * QUADRAT, bottom(content, 0), EPSILON);
        assertEquals(5 * QUADRAT, (top(content, 2) + bottom(content, 3)) / 2, EPSILON);
    }

    @Test
    public void inAZoneWithoutHeightColumnsAreAlignedOnTheLongestOne() throws MDCSyntaxError {
        MDCView zone = layout("%zone[layout=columns](A1-A1-A1-!A1-![align=end])", JSeshStyle.DEFAULT)
                .getSubView(0);
        MDCView content = zone.getFirstSubView();
        assertEquals(bottom(content, 2), bottom(content, 4), EPSILON);
    }

    @Test
    public void onAPageWithAHeightColumnsAreAlignedOnItsUsableHeight() throws MDCSyntaxError {
        JSeshStyle paged = STYLE.copy().geometry(g -> g
                .page(new PageSpecification(200.0, 120.0, 10.0, 30.0, 8.0, 8.0))
                .leftMargin(10.0).topMargin(8.0)).build();
        MDCView text = layout("A1-![align=end]A1-![align=center]", paged);
        assertEquals(8 + 104, bottom(text, 0), EPSILON);
        assertEquals(8 + 52, (top(text, 2) + bottom(text, 2)) / 2, EPSILON);
    }

    @Test
    public void onAPageWithoutHeightColumnsAreAlignedOnTheLongestOne() throws MDCSyntaxError {
        JSeshStyle paged = STYLE.copy().geometry(g -> g
                .page(new PageSpecification(200.0, 0.0, 10.0, 30.0, 8.0, 8.0))
                .leftMargin(10.0).topMargin(8.0)).build();
        MDCView text = layout(FIRST_COLUMN + "A1-![align=end]", paged);
        assertEquals(bottom(text, 4), bottom(text, SECOND), EPSILON);
    }

    @Test
    public void rightToLeftColumnsHaveTheSameVerticalPositions() throws MDCSyntaxError {
        String mdc = FIRST_COLUMN + "A1-![align=end]A1-A1-![align=center]A1-A1-A1-![align=justify]";
        MDCView ltr = layout(mdc);
        MDCView rtl = layout(mdc, STYLE.copy().options(o -> o.textDirection(TextDirection.RIGHT_TO_LEFT)).build());
        for (int i = 0; i < ltr.getNumberOfSubviews(); i++) {
            assertEquals(top(ltr, i), top(rtl, i), EPSILON, "view " + i);
        }
    }

    @Test
    public void columnsKeepTheirAlignmentWhenLineAlignmentIsIgnored() throws MDCSyntaxError {
        // Wrapping to the width of the editor doesn't change the height of the columns.
        JSeshStyle ignoring = STYLE.copy().options(o -> o.ignoreLineAlignment(true)).build();
        MDCView text = layout(FIRST_COLUMN + "A1-![align=end]", ignoring);
        assertEquals(bottom(text, 4), bottom(text, SECOND), EPSILON);
    }

    @Test
    public void justificationKeepsKernedElementsTogether() throws MDCSyntaxError {
        String second = "A1-%vkern-A1-A1-![align=justify]";
        MDCView plain = layout(FIRST_COLUMN + second.replace("[align=justify]", ""));
        MDCView text = layout(FIRST_COLUMN + second);
        // A1, %vkern, A1: the kerned pair is one unit.
        double plainGap = top(plain, SECOND + 2) - top(plain, SECOND);
        assertEquals(plainGap, top(text, SECOND + 2) - top(text, SECOND), EPSILON);
        assertEquals(bottom(text, 4), bottom(text, SECOND + 3), EPSILON);
    }
}
