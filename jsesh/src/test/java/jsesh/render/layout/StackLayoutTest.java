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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.font.FontRenderContext;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.Hieroglyph;
import jsesh.model.Stack;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Layout of `%stack`.
public class StackLayoutTest {

    private final JSeshRenderContext renderContext = new JSeshRenderContext(JSeshStyle.DEFAULT,
            PredefinedFonts.buildAllEmbeddedFonts());

    private MDCView layout(String mdc) throws MDCSyntaxError {
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), renderContext,
                new JSeshTechRenderContext(new FontRenderContext(null, true, true), 1.0));
    }

    private static MDCView find(MDCView view, java.util.function.Predicate<MDCView> p) {
        if (p.test(view)) {
            return view;
        }
        for (MDCView sub : view.getSubViews()) {
            MDCView found = find(sub, p);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static MDCView hieroglyph(MDCView view, String code) {
        return find(view, v -> v.getModel() instanceof Hieroglyph h && code.equals(h.getCode()));
    }

    @Test
    public void aTallGroupDoesNotShrinkTheOtherOne() throws MDCSyntaxError {
        MDCView alone = hieroglyph(layout("%stack(D,HD)"), "HD");
        MDCView withTallGroup = hieroglyph(layout("%stack(.\\70:D,HD)"), "HD");
        assertNotNull(alone);
        assertNotNull(withTallGroup);
        assertEquals(alone.getHeight(), withTallGroup.getHeight(), 1e-6);
    }

    @Test
    public void theTallGroupIsCalibratedToTheMaximalHeight() throws MDCSyntaxError {
        MDCView stack = find(layout("%stack(.\\70:D,HD)"), v -> v.getModel() instanceof Stack);
        assertNotNull(stack);
        assertTrue(stack.getHeight() <= renderContext.jseshStyle().geometry().maxCadratHeight() + 1e-6,
                "height " + stack.getHeight());
    }
}
