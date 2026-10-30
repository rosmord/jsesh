/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jsesh.parser.ast.AstCadrat;
import jsesh.parser.ast.AstDocument;
import jsesh.parser.ast.AstHieroglyph;
import jsesh.parser.ast.AstKerning;
import jsesh.sharedvocabulary.KerningAxis;

/// Tests for the `%hkern` and `%vkern` operators in [MDCParser].
public class MDCParserKerningTest {

    private AstDocument parse(String mdc) throws MDCSyntaxError {
        return new MDCParser().parse(mdc);
    }

    private AstCadrat cadrat(AstDocument document, int index) {
        return (AstCadrat) document.topItems().items().get(index);
    }

    @Test
    public void kerningInAHorizontalGroup() throws MDCSyntaxError {
        AstDocument document = parse("p*%hkern*q");
        assertEquals(1, document.topItems().items().size());
        AstCadrat cadrat = cadrat(document, 0);
        assertEquals(1, cadrat.hBoxes().size());
        assertEquals(3, cadrat.hBoxes().get(0).elements().size());
        assertInstanceOf(AstHieroglyph.class, cadrat.hBoxes().get(0).elements().get(0));
        AstKerning kerning = (AstKerning) cadrat.hBoxes().get(0).elements().get(1);
        assertEquals(KerningAxis.HORIZONTAL, kerning.axis());
        assertTrue(kerning.options().options().isEmpty());
    }

    @Test
    public void kerningAsALineOfAQuadrat() throws MDCSyntaxError {
        AstCadrat cadrat = cadrat(parse("p:%vkern:q"), 0);
        assertEquals(3, cadrat.hBoxes().size());
        AstKerning kerning = (AstKerning) cadrat.hBoxes().get(1).elements().get(0);
        assertEquals(KerningAxis.VERTICAL, kerning.axis());
    }

    @Test
    public void kerningAsAQuadrat() throws MDCSyntaxError {
        AstDocument document = parse("p-%hkern-q");
        assertEquals(3, document.topItems().items().size());
        AstCadrat cadrat = cadrat(document, 1);
        assertInstanceOf(AstKerning.class, cadrat.hBoxes().get(0).elements().get(0));
    }

    @Test
    public void kerningWithOptions() throws MDCSyntaxError {
        AstCadrat cadrat = cadrat(parse("p-%vkern[a=2,b,c=x]-q"), 1);
        AstKerning kerning = (AstKerning) cadrat.hBoxes().get(0).elements().get(0);
        assertEquals(KerningAxis.VERTICAL, kerning.axis());
        assertEquals(3, kerning.options().options().size());
        assertEquals(2, kerning.options().getInt("a", 0));
    }

    @Test
    public void otherOperatorsAreUnknown() {
        assertThrows(MDCSyntaxError.class, () -> parse("p-%foo-q"));
        assertThrows(MDCSyntaxError.class, () -> parse("p-%clear-q"));
        assertThrows(MDCSyntaxError.class, () -> parse("p-%[id=1]-q"));
    }

    @Test
    public void keywordsAreNotDelimitedByTheLexer() throws MDCSyntaxError {
        // There is no generic "%word" token any more: "%hkernx" is "%hkern" followed by the sign "x".
        AstDocument document = parse("p-%hkernx-q");
        assertEquals(4, document.topItems().items().size());
        assertInstanceOf(AstKerning.class, cadrat(document, 1).hBoxes().get(0).elements().get(0));
    }
}
