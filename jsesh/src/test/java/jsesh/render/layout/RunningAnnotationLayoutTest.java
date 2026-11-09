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
import jsesh.model.RunningAnnotation;
import jsesh.model.Zone;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Layout of the running annotations (`%haplography`, `%lacuna`, see
/// [RunningAnnotation]): fixed size, and `size=fill`, in lines and columns,
/// with and without tabs.
public class RunningAnnotationLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-2;
    private static final JSeshStyle STYLE = JSeshStyle.DEFAULT;
    private static final float QUADRAT = STYLE.geometry().maxCadratHeight();
    private static final float GAP = STYLE.geometry().smallSkip();
    private static final float COLUMN_GAP = STYLE.geometry().columnQuadratSkip();
    /// The width of a zone of `width=1000`.
    private static final float TEN = 10 * QUADRAT;

    private static JSeshRenderContext context(JSeshStyle style) {
        return new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
    }

    private static JSeshTechRenderContext tech() {
        return new JSeshTechRenderContext(FRC, 1.0);
    }

    private MDCView layout(String mdc) throws MDCSyntaxError {
        return layout(mdc, STYLE);
    }

    private MDCView layout(String mdc, JSeshStyle style) throws MDCSyntaxError {
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context(style), tech());
    }

    /// The content of the first item of the text, which is a zone.
    private MDCView zone(String mdc) throws MDCSyntaxError {
        MDCView zone = layout(mdc).getSubView(0);
        assertInstanceOf(Zone.class, zone.getModel());
        return zone.getFirstSubView();
    }

    private static double maxX(MDCView v) {
        return v.getPosition().x + v.getWidth();
    }

    private static double maxY(MDCView v) {
        return v.getPosition().y + v.getHeight();
    }

    // ---- Fixed size ----

    @Test
    public void aFixedSizeIsAPercentOfAQuadrat() throws MDCSyntaxError {
        MDCView content = zone("%zone[width=1000](A1-%haplography[size=250]-A1)");
        MDCView a = content.getSubView(1);
        assertInstanceOf(RunningAnnotation.class, a.getModel());
        assertEquals(2.5 * QUADRAT, a.getWidth(), EPSILON);
        assertEquals(QUADRAT, a.getHeight(), EPSILON);
        assertTrue(!a.isFilling());
        // It takes room in the line.
        assertEquals(maxX(a) + GAP, content.getSubView(2).getPosition().x, EPSILON);
    }

    @Test
    public void inColumnsTheLengthIsTheHeight() throws MDCSyntaxError {
        MDCView content = zone("%zone[layout=columns](A1-%lacuna[size=200]-A1)");
        MDCView a = content.getSubView(1);
        assertEquals(2 * QUADRAT, a.getHeight(), EPSILON);
        assertEquals(STYLE.geometry().maxCadratWidth(), a.getWidth(), EPSILON);
        assertEquals(maxY(a) + COLUMN_GAP, content.getSubView(2).getPosition().y, EPSILON);
    }

    @Test
    public void theLegacyFormHasTheSameLayout() throws MDCSyntaxError {
        MDCView legacy = layout("A1-^-..-..-^-A1");
        MDCView modern = layout("A1-%haplography[size=200]-A1");
        assertEquals(modern.getSubView(1).getWidth(), legacy.getSubView(1).getWidth(), EPSILON);
        assertEquals(modern.getSubView(2).getPosition().x, legacy.getSubView(2).getPosition().x, EPSILON);
    }

    // ---- Fill, without tabs ----

    @Test
    public void itTakesTheRestOfItsLine() throws MDCSyntaxError {
        MDCView content = zone("%zone[width=1000](A1-%haplography[size=fill]-A1)");
        MDCView a = content.getSubView(1);
        MDCView last = content.getSubView(2);
        assertTrue(a.isFilling());
        assertTrue(a.getWidth() > 5 * QUADRAT, "width: " + a.getWidth());
        assertEquals(TEN, maxX(last), EPSILON);
        // Only the usual gaps are left around it.
        assertEquals(maxX(content.getSubView(0)) + GAP, a.getPosition().x, EPSILON);
        assertEquals(maxX(a) + GAP, last.getPosition().x, EPSILON);
    }

    @Test
    public void lastInItsLineItEndsAtTheEndOfTheLine() throws MDCSyntaxError {
        MDCView content = zone("%zone[width=1000](A1-%lacuna[size=fill])");
        assertEquals(TEN, maxX(content.getSubView(1)), EPSILON);
    }

    @Test
    public void severalOfThemShareTheFreeSpace() throws MDCSyntaxError {
        MDCView content = zone("%zone[width=1000](%haplography[size=fill]-A1-%lacuna[size=fill])");
        assertEquals(content.getSubView(0).getWidth(), content.getSubView(2).getWidth(), EPSILON);
        assertEquals(TEN, maxX(content.getSubView(2)), EPSILON);
    }

    @Test
    public void withoutWidthItTakesTheWidthOfTheLongestLine() throws MDCSyntaxError {
        MDCView text = layout("A1-%haplography[size=fill]-A1!A1-A1-A1-A1-A1");
        MDCView lastOfFirstLine = text.getSubView(2);
        MDCView lastOfSecondLine = text.getSubView(text.getNumberOfSubviews() - 1);
        assertEquals(maxX(lastOfSecondLine), maxX(lastOfFirstLine), EPSILON);
    }

    @Test
    public void inColumnsItTakesTheRestOfTheColumn() throws MDCSyntaxError {
        MDCView content = zone("%zone[layout=columns,height=1000](A1-%haplography[size=fill]-A1)");
        MDCView a = content.getSubView(1);
        assertTrue(a.getHeight() > 5 * QUADRAT, "height: " + a.getHeight());
        assertEquals(TEN, maxY(content.getSubView(2)), EPSILON);
        assertEquals(maxY(a) + COLUMN_GAP, content.getSubView(2).getPosition().y, EPSILON);
    }

    @Test
    public void rightToLeftTextIsFilledTheSameWay() throws MDCSyntaxError {
        MDCView content = zone("%zone[dir=rl,width=1000](A1-%haplography[size=fill]-A1)");
        assertEquals(TEN, maxX(content.getSubView(2)), EPSILON);
    }

    @Test
    public void whenLineAlignmentIsIgnoredItKeepsItsNaturalLength() throws MDCSyntaxError {
        JSeshStyle wrapped = STYLE.copy().geometry(g -> g.wrapWidth(20 * QUADRAT))
                .options(o -> o.ignoreLineAlignment(true)).build();
        MDCView text = layout("A1-%haplography[size=fill]-A1", wrapped);
        assertEquals(0, text.getSubView(1).getWidth(), EPSILON);
    }

    @Test
    public void layingOutAgainGivesTheSameResult() throws MDCSyntaxError {
        MDCView text = layout("A1-%haplography[size=fill]-A1!A1-A1-A1-A1-A1");
        MDCView a = text.getSubView(1);
        float width = a.getWidth();
        assertTrue(width > 2 * QUADRAT);
        double lastX = text.getSubView(2).getPosition().x;
        new ViewBuilder().reLayout(text, context(STYLE), tech());
        assertEquals(width, a.getWidth(), EPSILON);
        assertEquals(lastX, text.getSubView(2).getPosition().x, EPSILON);
    }

    @Test
    public void afterAnEditItShrinksBack() throws MDCSyntaxError {
        MDCView text = layout("A1-%haplography[size=fill]-A1!A1-A1-A1-A1-A1");
        MDCView a = text.getSubView(1);
        float width = a.getWidth();
        // Remove all the signs of the second line.
        text.remove(4, text.getNumberOfSubviews());
        new ViewBuilder().reLayout(text, context(STYLE), tech());
        assertTrue(width > 1);
        assertEquals(0, a.getWidth(), EPSILON);
    }

    // ---- Fill, with tabs ----

    @Test
    public void inATabCellItFillsUpToTheNextStop() throws MDCSyntaxError {
        // The second line pushes the first stop. Views: 0 A1, 1 annotation, 2 %tab, 3 B1, 4 !, 5-7 G17, 8 %tab, 9 C1.
        MDCView content = zone("%zone[width=1000](A1-%haplography[size=fill]-%tab-B1-!G17-G17-G17-%tab-C1)");
        MDCView a = content.getSubView(1);
        MDCView b = content.getSubView(3);
        double stop = b.getPosition().x;
        assertTrue(stop > 3 * QUADRAT, "stop: " + stop);
        assertEquals(stop - GAP, maxX(a), EPSILON);
        assertEquals(maxX(content.getSubView(0)) + GAP, a.getPosition().x, EPSILON);
    }

    @Test
    public void itDoesNotPushTheStop() throws MDCSyntaxError {
        MDCView with = zone("%zone[width=1000](A1-%haplography[size=fill]-%tab-B1)");
        MDCView without = zone("%zone[width=1000](A1-%tab-B1)");
        assertEquals(without.getSubView(2).getPosition().x, with.getSubView(3).getPosition().x, EPSILON);
    }

    @Test
    public void aCellWithOnlyAnAnnotationIsFilled() throws MDCSyntaxError {
        // Views: 0 A1, 1 %tab, 2 annotation, 3 %tab, 4 B1.
        MDCView content = zone("%zone[width=1000](A1-%tab-%lacuna[size=fill]-%tab-B1-!G17-%tab-G17-G17-G17-%tab-G17)");
        MDCView a = content.getSubView(2);
        double stop1 = content.getSubView(1).getPosition().x;
        double stop2 = content.getSubView(4).getPosition().x;
        assertTrue(a.getWidth() > QUADRAT, "width: " + a.getWidth());
        // The cell goes from stop 1 to stop 2 minus the gap.
        assertEquals(stop1, a.getPosition().x, EPSILON);
        assertEquals(stop2 - GAP, maxX(a), EPSILON);
    }

    @Test
    public void inTheLastCellItGoesToTheEndOfTheTable() throws MDCSyntaxError {
        MDCView content = zone("%zone[width=1000](A1-%tab-B1-%haplography[size=fill])");
        assertEquals(TEN, maxX(content.getSubView(3)), EPSILON);
    }

    @Test
    public void inColumnsWithTabsItFillsTheCell() throws MDCSyntaxError {
        // Views: 0 A1, 1 annotation, 2 %tab, 3 B1, then a second column.
        MDCView content = zone(
                "%zone[layout=columns,height=1000](A1-%haplography[size=fill]-%tab-B1-!G17-G17-G17-%tab-C1)");
        MDCView a = content.getSubView(1);
        double stop = content.getSubView(3).getPosition().y;
        assertTrue(stop > 3 * QUADRAT, "stop: " + stop);
        assertEquals(stop - COLUMN_GAP, maxY(a), EPSILON);
    }

    @Test
    public void withTabsLayingOutAgainGivesTheSameResult() throws MDCSyntaxError {
        MDCView text = layout("%zone[width=1000](A1-%haplography[size=fill]-%tab-B1-!G17-G17-G17-%tab-C1)");
        MDCView a = text.getSubView(0).getFirstSubView().getSubView(1);
        float width = a.getWidth();
        assertTrue(width > QUADRAT);
        new ViewBuilder().reLayout(text, context(STYLE), tech());
        assertEquals(width, a.getWidth(), EPSILON);
    }

    @Test
    public void filledZonesAreStillIgnoredInTabbedLines() throws MDCSyntaxError {
        MDCView natural = zone("%zone[width=1000](A1-%tab-%zone(A1))");
        MDCView content = zone("%zone[width=1000](A1-%tab-%zone[fill=yes](A1))");
        assertEquals(natural.getSubView(2).getWidth(), content.getSubView(2).getWidth(), EPSILON);
    }
}
