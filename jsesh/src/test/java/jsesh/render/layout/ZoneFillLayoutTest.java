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
import jsesh.model.HRule;
import jsesh.model.Zone;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Zones which fill their line: `%zone[fill=yes]`.
public class ZoneFillLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-2;
    private static final JSeshStyle STYLE = JSeshStyle.DEFAULT;
    private static final float QUADRAT = STYLE.geometry().maxCadratHeight();
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
    private MDCView outer(String mdc) throws MDCSyntaxError {
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

    @Test
    public void aZoneAloneOnItsLineTakesTheWidthOfTheParent() throws MDCSyntaxError {
        MDCView content = outer("%zone[width=1000](%zone[fill=yes](A1)!A1)");
        MDCView fill = content.getSubView(0);
        assertEquals(TEN, fill.getWidth(), EPSILON);
        assertEquals(QUADRAT, fill.getHeight(), 1);
        assertTrue(fill.isFilling());
    }

    @Test
    public void withoutWidthItTakesTheWidthOfTheLongestLine() throws MDCSyntaxError {
        MDCView text = layout("%zone[fill=yes,layout=columns](A1-A1)!A1-A1-A1-A1-A1");
        MDCView fill = text.getSubView(0);
        MDCView last = text.getSubView(text.getNumberOfSubviews() - 1);
        assertEquals(fill.getPosition().x, text.getSubView(2).getPosition().x, EPSILON);
        assertEquals(maxX(last), maxX(fill), EPSILON);
    }

    @Test
    public void itTakesTheRestOfItsLine() throws MDCSyntaxError {
        MDCView content = outer("%zone[width=1000](A1-%zone[fill=yes](A1)-A1)");
        MDCView first = content.getSubView(0);
        MDCView fill = content.getSubView(1);
        MDCView last = content.getSubView(2);
        assertEquals(0, first.getPosition().x, EPSILON);
        assertTrue(fill.getWidth() > 5 * QUADRAT, "width: " + fill.getWidth());
        assertEquals(TEN, maxX(last), EPSILON);
        assertTrue(last.getPosition().x >= maxX(fill) - EPSILON);
    }

    @Test
    public void severalFillZonesShareTheFreeSpace() throws MDCSyntaxError {
        MDCView content = outer("%zone[width=1000](%zone[fill=yes](A1)-%zone[fill=yes](A1))");
        MDCView a = content.getSubView(0);
        MDCView b = content.getSubView(1);
        assertEquals(a.getWidth(), b.getWidth(), EPSILON);
        assertEquals(TEN, maxX(b), EPSILON);
    }

    @Test
    public void fillWinsOverJustificationAndCentring() throws MDCSyntaxError {
        for (String align : new String[] { "justify", "center", "end" }) {
            MDCView natural = outer("%zone[width=1000](A1-%zone(A1)-A1-![align=start]-A1)");
            MDCView content = outer("%zone[width=1000](A1-%zone[fill=yes](A1)-A1-![align=" + align + "]-A1)");
            // The natural gaps are kept.
            assertEquals(0, content.getSubView(0).getPosition().x, EPSILON, align);
            assertEquals(natural.getSubView(1).getPosition().x, content.getSubView(1).getPosition().x, EPSILON,
                    align);
            assertEquals(TEN, maxX(content.getSubView(2)), EPSILON, align);
        }
    }

    @Test
    public void anExplicitWidthWinsOverFill() throws MDCSyntaxError {
        MDCView content = outer("%zone[width=1000](%zone[fill=yes,width=200](A1)!A1)");
        assertEquals(2 * QUADRAT, content.getSubView(0).getWidth(), EPSILON);
        assertTrue(!content.getSubView(0).isFilling());
    }

    @Test
    public void inALineWithTabsFillDoesNothing() throws MDCSyntaxError {
        MDCView natural = outer("%zone[width=1000](A1-%tab-%zone(A1))");
        MDCView content = outer("%zone[width=1000](A1-%tab-%zone[fill=yes](A1))");
        assertEquals(natural.getSubView(2).getWidth(), content.getSubView(2).getWidth(), EPSILON);
    }

    @Test
    public void inColumnsTheZoneFillsTheHeightOfTheColumn() throws MDCSyntaxError {
        MDCView content = outer("%zone[layout=columns,height=1000](%zone[fill=yes](A1)-A1)");
        MDCView fill = content.getSubView(0);
        MDCView last = content.getSubView(1);
        assertEquals(TEN, maxY(last), EPSILON);
        assertTrue(fill.getHeight() > 5 * QUADRAT, "height: " + fill.getHeight());
        assertTrue(fill.getWidth() < 2 * QUADRAT, "width: " + fill.getWidth());
    }

    @Test
    public void inColumnsAHeightWinsOverFillButNotAWidth() throws MDCSyntaxError {
        MDCView withHeight = outer("%zone[layout=columns,height=1000](%zone[fill=yes,height=200](A1)-A1)");
        assertEquals(2 * QUADRAT, withHeight.getSubView(0).getHeight(), EPSILON);
        MDCView withWidth = outer("%zone[layout=columns,height=1000](%zone[fill=yes,width=200](A1)-A1)");
        assertEquals(TEN, maxY(withWidth.getSubView(1)), EPSILON);
    }

    @Test
    public void rightToLeftTextIsFilledTheSameWay() throws MDCSyntaxError {
        MDCView content = outer("%zone[dir=rl,width=1000](A1-%zone[fill=yes](A1)-A1)");
        assertEquals(TEN, maxX(content.getSubView(2)), EPSILON);
    }

    @Test
    public void theFrameAndTheScaleAreTakenIntoAccount() throws MDCSyntaxError {
        MDCView content = outer("%zone[width=1000](%zone[fill=yes,frame,scale=50](A1)!A1)");
        MDCView fill = content.getSubView(0);
        assertEquals(TEN, fill.getWidth(), EPSILON);
        float padding = Layout.zoneFramePadding(STYLE);
        // The inner area, scaled, fills the zone inside the frame.
        assertEquals(TEN - 2 * padding, fill.getFirstSubView().getWidth(), EPSILON);
    }

    @Test
    public void theContentOfAFilledZoneIsAlignedInItsNewWidth() throws MDCSyntaxError {
        MDCView content = outer("%zone[width=1000](%zone[fill=yes](A1-![align=end]-A1-A1)!A1)");
        MDCView inner = content.getSubView(0).getFirstSubView();
        assertEquals(TEN, maxX(inner.getSubView(0)), EPSILON);
    }

    @Test
    public void aRuleInAFilledZoneSpansIt() throws MDCSyntaxError {
        MDCView content = outer("%zone[width=1000](%zone[fill=yes](A1!{l0,100})!A1)");
        MDCView inner = content.getSubView(0).getFirstSubView();
        MDCView rule = null;
        for (int i = 0; i < inner.getNumberOfSubviews(); i++) {
            if (inner.getSubView(i).getModel() instanceof HRule) {
                rule = inner.getSubView(i);
            }
        }
        assertTrue(rule != null);
        assertEquals(TEN, rule.getWidth(), EPSILON);
    }

    @Test
    public void nestedFillZonesFillTheirFilledParent() throws MDCSyntaxError {
        MDCView content = outer("%zone[width=1000](%zone[fill=yes](%zone[fill=yes,frame](A1)!A1)!A1)");
        MDCView middle = content.getSubView(0);
        MDCView innermost = middle.getFirstSubView().getSubView(0);
        assertEquals(TEN, middle.getWidth(), EPSILON);
        assertEquals(TEN, innermost.getWidth(), EPSILON);
    }

    @Test
    public void layingOutAgainGivesTheSameResult() throws MDCSyntaxError {
        String mdc = "%zone[fill=yes,frame](A1)!A1-A1-A1-A1-A1-%zone[fill=yes](A1)";
        MDCView text = layout(mdc);
        MDCView fill = text.getSubView(0);
        float width = fill.getWidth();
        double lastX = text.getSubView(text.getNumberOfSubviews() - 1).getPosition().x;
        new ViewBuilder().reLayout(text, context(STYLE), tech());
        assertEquals(width, fill.getWidth(), EPSILON);
        assertEquals(lastX, text.getSubView(text.getNumberOfSubviews() - 1).getPosition().x, EPSILON);
    }

    @Test
    public void afterAnEditTheZoneShrinksBack() throws MDCSyntaxError {
        MDCView text = layout("%zone[fill=yes](A1)!A1-A1-A1-A1-A1");
        MDCView fill = text.getSubView(0);
        float natural = fill.getNaturalLength();
        float width = fill.getWidth();
        // Remove all the signs of the second line.
        text.remove(2, text.getNumberOfSubviews());
        new ViewBuilder().reLayout(text, context(STYLE), tech());
        assertTrue(width > natural + 1);
        assertEquals(natural, fill.getWidth(), EPSILON);
        assertEquals(natural, fill.getFirstSubView().getWidth(), EPSILON);
    }

    @Test
    public void whenLineAlignmentIsIgnoredLinesAreNotFilled() throws MDCSyntaxError {
        JSeshStyle wrapped = STYLE.copy().geometry(g -> g.wrapWidth(20 * QUADRAT))
                .options(o -> o.ignoreLineAlignment(true)).build();
        MDCView text = layout("%zone[fill=yes](A1)!A1", wrapped);
        assertEquals(text.getSubView(0).getNaturalLength(), text.getSubView(0).getWidth(), EPSILON);
        // But the content of a zone keeps its alignment, and its fill zones.
        MDCView content = layout("%zone[width=1000](%zone[fill=yes](A1)!A1)", wrapped).getSubView(0)
                .getFirstSubView();
        assertEquals(TEN, content.getSubView(0).getWidth(), EPSILON);
    }

    @Test
    public void inAPageTheZoneTakesTheUsableWidth() throws MDCSyntaxError {
        JSeshStyle wrapped = STYLE.copy().geometry(g -> g.wrapWidth(20 * QUADRAT)).build();
        MDCView text = layout("%zone[fill=yes](A1)!A1", wrapped);
        assertEquals(wrapped.geometry().maxLineWidth(), text.getSubView(0).getWidth(), EPSILON);
    }
}
