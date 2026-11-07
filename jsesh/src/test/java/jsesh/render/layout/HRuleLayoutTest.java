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

/// The width of horizontal rules (`{l0,10}`): one quadrat at least, else the width available
/// (zone), else the width of the text. The positions written in the rule are not used.
public class HRuleLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-3;
    private static final JSeshStyle STYLE = JSeshStyle.DEFAULT;

    private MDCView layout(String mdc) throws MDCSyntaxError {
        JSeshRenderContext context = new JSeshRenderContext(STYLE, PredefinedFonts.buildAllEmbeddedFonts());
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
    }

    private static MDCView ruleIn(MDCView parent) {
        for (int i = 0; i < parent.getNumberOfSubviews(); i++) {
            if (parent.getSubView(i).getModel() instanceof HRule) {
                return parent.getSubView(i);
            }
        }
        return null;
    }

    /// The right side of the content which is not a rule, from the left margin.
    private static double textWidth(MDCView view) {
        double max = 0;
        for (int i = 0; i < view.getNumberOfSubviews(); i++) {
            MDCView v = view.getSubView(i);
            if (!(v.getModel() instanceof HRule) && v.getWidth() > 0) {
                max = Math.max(max, v.getPosition().x + v.getWidth() - STYLE.geometry().leftMargin());
            }
        }
        return max;
    }

    @Test
    public void aLoneRuleIsOneQuadratWide() throws MDCSyntaxError {
        MDCView rule = ruleIn(layout("{l0,500}"));
        assertNotNull(rule);
        assertEquals(STYLE.geometry().maxCadratWidth(), rule.getWidth(), EPSILON);
    }

    @Test
    public void theRuleHasTheWidthOfTheText() throws MDCSyntaxError {
        MDCView view = layout("A1-A1-A1-A1-A1-A1-!{l0,1}");
        MDCView rule = ruleIn(view);
        assertNotNull(rule);
        assertEquals(textWidth(view), rule.getWidth(), EPSILON);
        // And more than the minimum.
        assertTrue(rule.getWidth() > STYLE.geometry().maxCadratWidth());
    }

    @Test
    public void theRuleFillsTheWidthOfItsZone() throws MDCSyntaxError {
        MDCView zone = layout("%zone[width=300]({l0,1})").getSubView(0);
        assertInstanceOf(Zone.class, zone.getModel());
        MDCView rule = ruleIn(zone.getFirstSubView());
        assertNotNull(rule);
        // The width of a zone is in percents of the unit (here, 300 % = 3 quadrats).
        assertEquals(3 * STYLE.geometry().maxCadratHeight(), rule.getWidth(), EPSILON);
    }
}
