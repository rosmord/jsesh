/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.io.mdc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.HieroglyphicText;
import jsesh.parser.MDCSyntaxError;

/// Sub-quadrats are syntax: useless ones (`i-(mn:n)`) are neither kept in the model nor written.
public class MdCModelWriterSubCadratTest {

    private HieroglyphicText parse(String mdc) throws MDCSyntaxError {
        return new MDCParserModelGenerator().parse(mdc);
    }

    @ParameterizedTest
    @ValueSource(strings = { "i-(mn:n)", "i-((mn:n))", "i-(((mn:n)))" })
    public void uselessSubCadratsAreRemoved(String mdc) throws MDCSyntaxError {
        HieroglyphicText text = parse(mdc);
        assertTrue(text.equalsIgnoreId(parse("i-mn:n")), mdc);
        assertEquals("i-mn:n", new MdCModelWriter().toMdC(text).trim());
    }

    @ParameterizedTest
    @CsvSource({ "a:(b:c), a:b:c", "x*(a*b), x*a*b", "a:(b*c:d), a:b*c:d", "a:((b:c)), a:b:c",
            "x*(a*(b*c)), x*a*b*c", "a:(b:(c:d)), a:b:c:d", "n:(x:t)*(a*b), n:(x:t)*a*b" })
    public void flattenedSubCadrats(String mdc, String expected) throws MDCSyntaxError {
        HieroglyphicText text = parse(mdc);
        assertTrue(text.equalsIgnoreId(parse(expected)), mdc);
        assertEquals(expected, new MdCModelWriter().toMdC(text).trim());
    }

    @ParameterizedTest
    @ValueSource(strings = { "n:(x:t)*nxt", "x*(a:b)", "(i-w)", "(a:b)^^w", "i-(mn:n)*i", "a*(b:c)*d", "a:(b:c)*t" })
    public void usefulSubCadratsAreKept(String mdc) throws MDCSyntaxError {
        HieroglyphicText text = parse(mdc);
        String written = new MdCModelWriter().toMdC(text).trim();
        assertTrue(written.contains("("), mdc + " -> " + written);
        assertTrue(text.equalsIgnoreId(parse(written)), mdc + " -> " + written);
    }
}
