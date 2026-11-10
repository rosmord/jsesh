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
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Retrograde writing: the content of the quadrats is mirrored, their order along the line is not.
public class RetrogradeLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final JSeshStyle PLAIN = JSeshStyle.DEFAULT;
    private static final JSeshStyle RETROGRADE = PLAIN.copy().options(o -> o.retrograde(true)).build();

    private MDCView layout(String mdc, JSeshStyle style) throws MDCSyntaxError {
        JSeshRenderContext context = new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
    }

    @Test
    public void quadratDirectionIsReversed() {
        assertEquals(TextDirection.LEFT_TO_RIGHT, PLAIN.options().quadratDirection());
        assertEquals(TextDirection.RIGHT_TO_LEFT, RETROGRADE.options().quadratDirection());
        JSeshStyle rl = RETROGRADE.copy().options(o -> o.textDirection(TextDirection.RIGHT_TO_LEFT)).build();
        assertEquals(TextDirection.LEFT_TO_RIGHT, rl.options().quadratDirection());
    }

    @Test
    public void quadratsAreMirroredButTheirOrderIsNot() throws MDCSyntaxError {
        MDCView plain = layout("A1*B1-G17", PLAIN);
        MDCView retro = layout("A1*B1-G17", RETROGRADE);
        MDCView quadrat = retro.getSubView(0);
        assertEquals(TextDirection.RIGHT_TO_LEFT, quadrat.getDirection());
        assertEquals(TextDirection.RIGHT_TO_LEFT, quadrat.getFirstSubView().getDirection());
        assertEquals(TextDirection.LEFT_TO_RIGHT, retro.getDirection());
        // same layout coordinates: only the drawing differs.
        assertEquals(plain.getSubView(1).getPosition().x, retro.getSubView(1).getPosition().x, 1e-6);
        assertEquals(plain.getWidth(), retro.getWidth(), 1e-6);
    }

    @Test
    public void zoneOverridesTheDocument() throws MDCSyntaxError {
        MDCView inheriting = layout("%zone(A1*B1)", RETROGRADE).getSubView(0);
        assertEquals(TextDirection.RIGHT_TO_LEFT, inheriting.getFirstSubView().getFirstSubView().getDirection());
        MDCView forcedOff = layout("%zone[retrograde=no](A1*B1)", RETROGRADE).getSubView(0);
        assertEquals(TextDirection.LEFT_TO_RIGHT, forcedOff.getFirstSubView().getFirstSubView().getDirection());
        MDCView forcedOn = layout("%zone[retrograde=yes](A1*B1)", PLAIN).getSubView(0);
        assertEquals(TextDirection.LEFT_TO_RIGHT, forcedOn.getFirstSubView().getDirection());
        assertEquals(TextDirection.RIGHT_TO_LEFT, forcedOn.getFirstSubView().getFirstSubView().getDirection());
    }

    /// The elements which structure the line keep the direction of the text: only the
    /// quadrats are mirrored (see [jsesh.model.ModelElement#followsQuadratDirection()]).
    @Test
    public void zoneKeepsTheTextDirection() throws MDCSyntaxError {
        MDCView zone = layout("%zone(A1-B1-G17)", RETROGRADE).getSubView(0);
        assertEquals(TextDirection.LEFT_TO_RIGHT, zone.getDirection());
        MDCView content = zone.getFirstSubView();
        assertEquals(TextDirection.LEFT_TO_RIGHT, content.getDirection());
        for (int i = 0; i < content.getNumberOfSubviews(); i++) {
            assertEquals(TextDirection.RIGHT_TO_LEFT, content.getSubView(i).getDirection());
        }
    }
}
