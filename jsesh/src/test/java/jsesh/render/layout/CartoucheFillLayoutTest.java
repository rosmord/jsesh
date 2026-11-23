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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.Zone;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Layout of the cartouches which fill (`<-ra->[fill]`, see
/// [jsesh.model.Cartouche#isFill()]): they push the tab stops with their natural
/// width, and are stretched to the width of their cell, their content being
/// justified.
public class CartoucheFillLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-2;
    private static final JSeshStyle STYLE = JSeshStyle.DEFAULT;
    private static final double QUADRAT = STYLE.geometry().maxCadratHeight();
    private static final double TEN = 10 * QUADRAT;

    private static JSeshRenderContext context() {
        return new JSeshRenderContext(STYLE, PredefinedFonts.buildAllEmbeddedFonts());
    }

    private static JSeshTechRenderContext tech() {
        return new JSeshTechRenderContext(FRC, 1.0);
    }

    private MDCView layout(String mdc) throws MDCSyntaxError {
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context(), tech());
    }

    /// The content of the first item of the text, which is a zone.
    private MDCView zone(String mdc) throws MDCSyntaxError {
        MDCView zone = layout(mdc).getSubView(0);
        assertInstanceOf(Zone.class, zone.getModel());
        return zone.getFirstSubView();
    }

    /// The views of the cartouches with `[fill]` (the quadrats made of them).
    private static List<MDCView> fillingCartouches(MDCView content) {
        List<MDCView> result = new ArrayList<>();
        for (MDCView v : content.getSubViews()) {
            if (Layout.fillingCartouche(v.getModel()) != null) {
                result.add(v);
            }
        }
        return result;
    }

    private static double maxX(MDCView v) {
        return v.getPosition().x + v.getWidth();
    }

    private static double maxY(MDCView v) {
        return v.getPosition().y + v.getHeight();
    }

    /// The view of the content of a cartouche, from the view of its quadrat.
    private static MDCView innerContent(MDCView quadrat) {
        return quadrat.getFirstSubView().getFirstSubView().getFirstSubView();
    }

    private static final String TWO_LINES = "%%zone(%%tab-%s-%%tab-B1-!%%tab-%s-%%tab-B1)";

    private MDCView twoLines(String first, String second) throws MDCSyntaxError {
        return zone(String.format(TWO_LINES, first, second));
    }

    @Test
    public void cartouchesOfAColumnGetTheWidthOfTheWidest() throws MDCSyntaxError {
        MDCView content = twoLines("<-A1->[fill]", "<-A1-A1-A1-A1->[fill]");
        List<MDCView> cartouches = fillingCartouches(content);
        assertEquals(2, cartouches.size());
        assertEquals(cartouches.get(0).getWidth(), cartouches.get(1).getWidth(), EPSILON);
    }

    @Test
    public void theWidestKeepsItsNaturalWidth() throws MDCSyntaxError {
        MDCView plain = twoLines("<-A1->", "<-A1-A1-A1-A1->");
        MDCView filled = twoLines("<-A1->[fill]", "<-A1-A1-A1-A1->[fill]");
        double natural = plain.getSubView(plain.getNumberOfSubviews() - 3).getWidth();
        assertTrue(natural > 3 * QUADRAT, "natural: " + natural);
        assertEquals(natural, fillingCartouches(filled).get(1).getWidth(), EPSILON);
        // The short one is stretched.
        assertTrue(fillingCartouches(filled).get(0).getWidth() > natural - EPSILON);
    }

    @Test
    public void withoutFillTheCartouchesKeepTheirSize() throws MDCSyntaxError {
        MDCView plain = twoLines("<-A1->", "<-A1-A1-A1-A1->");
        MDCView shortOne = plain.getSubView(1);
        MDCView filled = twoLines("<-A1->[fill]", "<-A1->");
        assertEquals(shortOne.getWidth(), fillingCartouches(filled).get(0).getWidth(), EPSILON);
    }

    @Test
    public void theNextColumnStartsAfterTheWidestCartouche() throws MDCSyntaxError {
        MDCView plain = twoLines("<-A1->", "<-A1-A1-A1-A1->");
        MDCView filled = twoLines("<-A1->[fill]", "<-A1-A1-A1-A1->[fill]");
        // B1 is the 4th view of the first line, as the tabs are views.
        assertEquals(plain.getSubView(3).getPosition().x, filled.getSubView(3).getPosition().x, EPSILON);
    }

    @Test
    public void theStretchedCartoucheEndsWhereTheWidestEnds() throws MDCSyntaxError {
        MDCView content = twoLines("<-A1->[fill]", "<-A1-A1-A1-A1->[fill]");
        List<MDCView> cartouches = fillingCartouches(content);
        assertEquals(maxX(cartouches.get(1)) - cartouches.get(1).getPosition().x,
                maxX(cartouches.get(0)) - cartouches.get(0).getPosition().x, EPSILON);
        assertEquals(cartouches.get(1).getPosition().x, cartouches.get(0).getPosition().x, EPSILON);
    }

    @Test
    public void theContentIsJustified() throws MDCSyntaxError {
        MDCView content = twoLines("<-A1-A1->[fill]", "<-A1-A1-A1-A1->[fill]");
        MDCView cartouche = fillingCartouches(content).get(0);
        MDCView inner = innerContent(cartouche);
        MDCView first = inner.getSubView(0);
        MDCView last = inner.getSubView(inner.getNumberOfSubviews() - 1);
        MDCView reference = innerContent(fillingCartouches(content).get(1));
        assertEquals(reference.getWidth(), inner.getWidth(), EPSILON);
        // The first sign stays at the start, the last one goes to the end.
        assertEquals(0, first.getPosition().x, EPSILON);
        assertEquals(inner.getWidth(), maxX(last), EPSILON);
    }

    @Test
    public void aLoneSignIsCentered() throws MDCSyntaxError {
        MDCView content = twoLines("<-A1->[fill]", "<-A1-A1-A1-A1->[fill]");
        MDCView inner = innerContent(fillingCartouches(content).get(0));
        MDCView sign = inner.getSubView(0);
        assertEquals((inner.getWidth() - sign.getWidth()) / 2, sign.getPosition().x, EPSILON);
    }

    @Test
    public void inLinesTheHeightIsUnchanged() throws MDCSyntaxError {
        MDCView plain = twoLines("<-A1->", "<-A1-A1-A1-A1->");
        MDCView filled = twoLines("<-A1->[fill]", "<-A1-A1-A1-A1->[fill]");
        MDCView stretched = fillingCartouches(filled).get(0);
        assertEquals(plain.getSubView(1).getHeight(), stretched.getHeight(), EPSILON);
        assertEquals(plain.getSubView(1).getPosition().y, stretched.getPosition().y, EPSILON);
    }

    @Test
    public void theSignsKeepTheirSize() throws MDCSyntaxError {
        MDCView plain = twoLines("<-A1-A1->", "<-A1-A1-A1-A1->");
        MDCView filled = twoLines("<-A1-A1->[fill]", "<-A1-A1-A1-A1->[fill]");
        MDCView before = innerContent(plain.getSubView(1));
        MDCView after = innerContent(fillingCartouches(filled).get(0));
        for (int i = 0; i < before.getNumberOfSubviews(); i++) {
            assertEquals(before.getSubView(i).getWidth(), after.getSubView(i).getWidth(), EPSILON);
            assertEquals(before.getSubView(i).getHeight(), after.getSubView(i).getHeight(), EPSILON);
        }
        // Nor does the scale of the quadrat change.
        assertEquals(plain.getSubView(1).getXScale(), fillingCartouches(filled).get(0).getXScale(), EPSILON);
    }

    @Test
    public void inColumnsTheWidthIsUnchanged() throws MDCSyntaxError {
        String mdc = "%%zone[layout=columns](%%tab-%s-%%tab-B1-!%%tab-%s-%%tab-B1)";
        MDCView plain = zone(String.format(mdc, "<-A1->", "<-A1-A1-A1-A1->"));
        MDCView filled = zone(String.format(mdc, "<-A1->[fill]", "<-A1-A1-A1-A1->[fill]"));
        MDCView stretched = fillingCartouches(filled).get(0);
        assertEquals(plain.getSubView(1).getWidth(), stretched.getWidth(), EPSILON);
        assertTrue(stretched.getHeight() > plain.getSubView(1).getHeight() + QUADRAT);
    }

    /// The example of the user: three lines, two tab columns, with line justification.
    @Test
    public void aKingListHasColumnsOfEqualCartouches() throws MDCSyntaxError {
        String fill = "%zone(<-ra-nfr-r-ir-kA->[fill]-%tab-<-kA-kA-i->[fill]-![align=justify] "
                + "<-ra-Sps-s-s-kA->[fill]-%tab-<-v/-nTr-w-wsr*s:r->-![align=justify] "
                + "<-ra-nfr:f->[fill]-%tab-<-i-z-i->[fill])";
        MDCView content = zone(fill);
        MDCView plain = zone(fill.replace("[fill]", ""));
        // Views of each line: cartouche, %tab, cartouche, !
        int[] firstColumn = { 0, 4, 8 };
        double width = content.getSubView(0).getWidth();
        for (int i : firstColumn) {
            MDCView v = content.getSubView(i);
            assertEquals(width, v.getWidth(), EPSILON, "width of " + i);
            assertEquals(plain.getSubView(i).getHeight(), v.getHeight(), EPSILON, "height of " + i);
            assertEquals(plain.getSubView(i).getXScale(), v.getXScale(), EPSILON, "scale of " + i);
            assertEquals(content.getSubView(0).getPosition().x, v.getPosition().x, EPSILON);
        }
        // The second column starts at the same place in each line.
        assertEquals(content.getSubView(2).getPosition().x, content.getSubView(6).getPosition().x, EPSILON);
        assertEquals(content.getSubView(2).getPosition().x, content.getSubView(10).getPosition().x, EPSILON);
        for (int i : new int[] { 2, 6, 10 }) {
            assertEquals(plain.getSubView(i).getHeight(), content.getSubView(i).getHeight(), EPSILON);
        }
    }

    @Test
    public void outsideATabCellItTakesTheFreeSpace() throws MDCSyntaxError {
        MDCView content = zone("%zone[width=1000](A1-<-B1->[fill])");
        MDCView cartouche = fillingCartouches(content).get(0);
        assertEquals(TEN, maxX(cartouche), EPSILON);
    }

    @Test
    public void inTheLastCellItGoesToTheEndOfTheZone() throws MDCSyntaxError {
        MDCView content = zone("%zone[width=1000](A1-%tab-<-B1->[fill])");
        assertEquals(TEN, maxX(fillingCartouches(content).get(0)), EPSILON);
    }

    @Test
    public void withoutAnyFreeSpaceItKeepsItsSize() throws MDCSyntaxError {
        MDCView plain = layout("<-B1->");
        MDCView filled = layout("<-B1->[fill]");
        assertEquals(plain.getSubView(0).getWidth(), filled.getSubView(0).getWidth(), EPSILON);
    }

    @Test
    public void inColumnsTheyGetTheHeightOfTheTallest() throws MDCSyntaxError {
        MDCView content = zone(
                "%zone[layout=columns,height=1000](%tab-<-A1->[fill]-%tab-B1-!%tab-<-A1-A1-A1-A1->[fill]-%tab-B1)");
        List<MDCView> cartouches = fillingCartouches(content);
        assertEquals(2, cartouches.size());
        assertEquals(cartouches.get(0).getHeight(), cartouches.get(1).getHeight(), EPSILON);
        assertEquals(maxY(cartouches.get(0)), maxY(cartouches.get(1)), EPSILON);
    }

    @Test
    public void rightToLeftTextIsFilledTheSameWay() throws MDCSyntaxError {
        MDCView content = zone("%zone[dir=rl](%tab-<-A1->[fill]-%tab-B1-!%tab-<-A1-A1-A1-A1->[fill]-%tab-B1)");
        List<MDCView> cartouches = fillingCartouches(content);
        assertEquals(cartouches.get(0).getWidth(), cartouches.get(1).getWidth(), EPSILON);
    }

    @Test
    public void layingOutAgainGivesTheSameResult() throws MDCSyntaxError {
        MDCView text = layout(String.format("%%zone(" + TWO_LINES.substring(7), "<-A1->[fill]", "<-A1-A1-A1-A1->[fill]"));
        MDCView content = text.getSubView(0).getFirstSubView();
        MDCView cartouche = fillingCartouches(content).get(0);
        double width = cartouche.getWidth();
        double innerWidth = innerContent(cartouche).getWidth();
        new ViewBuilder().reLayout(text, context(), tech());
        MDCView again = fillingCartouches(text.getSubView(0).getFirstSubView()).get(0);
        assertNotNull(again);
        assertEquals(width, again.getWidth(), EPSILON);
        assertEquals(innerWidth, innerContent(again).getWidth(), EPSILON);
    }

    @Test
    public void afterAnEditItShrinksBack() throws MDCSyntaxError {
        // Views: 0 A1, 1 cartouche, 2 A1, 3 !, then the second line.
        MDCView text = layout("A1-<-B1->[fill]-A1!A1-A1-A1-A1-A1-A1-A1");
        double natural = layout("A1-<-B1->[fill]-A1").getSubView(1).getWidth();
        double stretched = text.getSubView(1).getWidth();
        assertTrue(stretched > natural + QUADRAT, "stretched: " + stretched);
        // Remove the second line.
        text.remove(4, text.getNumberOfSubviews());
        new ViewBuilder().reLayout(text, context(), tech());
        assertEquals(natural, text.getSubView(1).getWidth(), EPSILON);
    }
}
