/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.font.FontRenderContext;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.document.DocumentPreferences;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.Hieroglyph;
import jsesh.model.LayoutSetting;
import jsesh.model.constants.SymbolCodes;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.DocumentPreferencesStyleConverter;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Small texts (`"..."`) follow the scale modifier: `"test"\300` is three times as big as `"test"`.
public class SmallTextScaleLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-3;

    private MDCView layout(String mdc) throws MDCSyntaxError {
        JSeshRenderContext context = new JSeshRenderContext(JSeshStyle.DEFAULT, PredefinedFonts.buildAllEmbeddedFonts());
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
    }

    private static MDCView smallTextIn(MDCView view) {
        if (view.getModel() instanceof Hieroglyph h && h.getType() == SymbolCodes.SMALLTEXT) {
            return view;
        }
        for (MDCView sub : view.getSubViews()) {
            MDCView found = smallTextIn(sub);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @Test
    public void scaleModifierScalesSmallText() throws MDCSyntaxError {
        MDCView plain = smallTextIn(layout("\"test\""));
        MDCView big = smallTextIn(layout("\"test\"\\300"));
        assertNotNull(plain);
        assertNotNull(big);
        assertEquals(3 * plain.getWidth(), big.getWidth(), EPSILON);
        assertEquals(3 * plain.getHeight(), big.getHeight(), EPSILON);
        assertEquals(3 * plain.getXScale(), big.getXScale(), EPSILON);
    }

    @Test
    public void smallTextSizeIsADocumentSetting() throws MDCSyntaxError {
        // The default: 40 % of the unit.
        assertEquals(0.4 * 18, JSeshStyle.DEFAULT.geometry().smallTextSize(), EPSILON);

        DocumentPreferences prefs = new DocumentPreferences().with(LayoutSetting.SMALL_TEXT_SIZE, 20.0);
        JSeshStyle small = DocumentPreferencesStyleConverter.applyDocumentPreferences(prefs, JSeshStyle.DEFAULT);
        assertEquals(0.2 * 18, small.geometry().smallTextSize(), EPSILON);
        assertEquals(20.0, DocumentPreferencesStyleConverter.toDocumentPreferences(small)
                .percent(LayoutSetting.SMALL_TEXT_SIZE), EPSILON);

        // It scales with the unit, and changes the size of the text.
        JSeshStyle doubled = DocumentPreferencesStyleConverter.applyDocumentPreferences(
                prefs.withUnit(36), JSeshStyle.DEFAULT);
        assertEquals(2 * small.geometry().smallTextSize(), doubled.geometry().smallTextSize(), EPSILON);

        JSeshRenderContext context = new JSeshRenderContext(small, PredefinedFonts.buildAllEmbeddedFonts());
        MDCView view = smallTextIn(new ViewBuilder().buildView(new MDCParserModelGenerator().parse("\"test\""),
                context, new JSeshTechRenderContext(FRC, 1.0)));
        assertEquals(small.smallTextDimensions(new JSeshTechRenderContext(FRC, 1.0), "test").getWidth(),
                view.getWidth(), EPSILON);
    }
}
