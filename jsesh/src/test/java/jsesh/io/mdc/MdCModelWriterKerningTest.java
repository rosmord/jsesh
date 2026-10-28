/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.io.mdc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.Cadrat;
import jsesh.model.HieroglyphicText;
import jsesh.model.Kerning;
import jsesh.model.ModelElementDeepAdapter;
import jsesh.model.constants.KerningAxis;
import jsesh.parser.MDCSyntaxError;

/// `%hkern` and `%vkern` in the model: building from MdC, writing back, copying.
public class MdCModelWriterKerningTest {

    private HieroglyphicText parse(String mdc) throws MDCSyntaxError {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private static List<Kerning> findKernings(HieroglyphicText text) {
        List<Kerning> found = new ArrayList<>();
        text.accept(new ModelElementDeepAdapter() {
            @Override
            public void visitKerning(Kerning kerning) {
                found.add(kerning);
            }
        });
        return found;
    }

    @Test
    public void modelIsBuiltFromMdC() throws MDCSyntaxError {
        List<Kerning> kernings = findKernings(parse("p*%hkern*q-n:%vkern[a=2,b]:t"));
        assertEquals(2, kernings.size());
        assertEquals(KerningAxis.HORIZONTAL, kernings.get(0).getAxis());
        assertEquals("", kernings.get(0).getOptions());
        assertEquals(KerningAxis.VERTICAL, kernings.get(1).getAxis());
        assertEquals("a=2,b", kernings.get(1).getOptions());
    }

    @Test
    public void kerningQuadratsAreRecognised() throws MDCSyntaxError {
        HieroglyphicText text = parse("p-%hkern-q:%vkern:t");
        Cadrat kern = (Cadrat) text.getChildAt(1);
        assertEquals(KerningAxis.HORIZONTAL, kern.getKerningAxis().orElseThrow());
        Cadrat other = (Cadrat) text.getChildAt(2);
        assertTrue(other.getKerningAxis().isEmpty());
        assertTrue(other.containsKerning());
        assertEquals(KerningAxis.VERTICAL, other.getHBox(1).getKerningAxis().orElseThrow());
        assertFalse(((Cadrat) text.getChildAt(0)).containsKerning());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "p*%hkern*q",
            "p:%vkern:q",
            "p-%hkern-q",
            "p-%vkern[a=2]-q",
            "p:%hkern[x,y=z]:q*%vkern*t",
            "<-p-%hkern-q->",
    })
    public void writingAndReadingBackGivesTheSameModel(String mdc) throws MDCSyntaxError {
        HieroglyphicText first = parse(mdc);
        String written = new MdCModelWriter().toMdC(first);
        assertTrue(written.contains("kern"), written);
        HieroglyphicText second = parse(written);
        assertTrue(first.equalsIgnoreId(second), mdc + " -> " + written);
    }

    @Test
    public void deepCopyKeepsEverything() throws MDCSyntaxError {
        Kerning kerning = findKernings(parse("p-%vkern[a=2]-q")).get(0);
        Kerning copy = kerning.deepCopy();
        assertTrue(kerning.equalsIgnoreId(copy));
        assertEquals(0, kerning.compareTo(copy));
        assertFalse(kerning.equalsIgnoreId(new Kerning(KerningAxis.HORIZONTAL, "a=2")));
    }
}
