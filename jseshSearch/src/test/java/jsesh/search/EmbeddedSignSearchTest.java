/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.defaults.HieroglyphResourcesBuilder;
import jsesh.document.MdCSearchQuery;
import jsesh.document.embeddedsigns.EmbeddedSign;
import jsesh.document.embeddedsigns.EmbeddedSignList;
import jsesh.glyphs.signdata.HieroglyphDatabase;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.HieroglyphicText;
import jsesh.model.MDCPosition;
import jsesh.parser.MDCSyntaxError;
import jsesh.search.quadrant.QuadratSearchQuery;
import jsesh.search.simple.SignStringSearchQuery;
import jsesh.search.wildcard.VariantLevelForSearch;
import jsesh.search.wildcard.WildCardQuery;

/// Embedded signs are found under their declared Gardiner equivalent.
public class EmbeddedSignSearchTest {

    private static final byte[] SVG = "<svg/>".getBytes(StandardCharsets.UTF_8);

    private static final HieroglyphDatabase DATABASE = HieroglyphResourcesBuilder.buildEmbedded().database();

    /// `sign1` is a variant of A5, `sign2` declares nothing.
    private static EmbeddedSignList signs() {
        EmbeddedSignList list = new EmbeddedSignList();
        list.put(new EmbeddedSign("sign1", SVG, "A5", ""));
        list.put(new EmbeddedSign("sign2", SVG, null, ""));
        return list;
    }

    private static HieroglyphicText parse(String mdc) {
        try {
            return new MDCParserModelGenerator().parse(mdc);
        } catch (MDCSyntaxError ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void check(MdCSearchQuery query, String text, Integer... expected) {
        List<MDCPosition> expectedResult = Arrays.stream(expected).map(MDCPosition::new).toList();
        assertEquals(expectedResult, query.doSearch(parse(text), signs()), text);
    }

    private static WildCardQuery wildcard(String query, VariantLevelForSearch level) {
        return new WildCardQuery(parse(query), 0, DATABASE, level);
    }

    @Test
    public void wildcardExactFindsEquivalent() {
        check(wildcard("i-A5", VariantLevelForSearch.EXACT_SEARCH), "n-i-sign1-i-A5", 1, 3);
    }

    @Test
    public void wildcardExtendedFindsEquivalent() {
        check(wildcard("i-A5", VariantLevelForSearch.EXTENDED_VARIANTS), "n-i-sign1", 1);
    }

    @Test
    public void wildcardFindsEmbeddedCode() {
        check(wildcard("sign1", VariantLevelForSearch.EXACT_SEARCH), "A5-sign1", 1);
    }

    @Test
    public void signWithoutEquivalentIsNotFound() {
        check(wildcard("i-A5", VariantLevelForSearch.EXACT_SEARCH), "i-sign2");
    }

    @Test
    public void wildcardSetFindsEquivalent() {
        check(wildcard("QUERYSETB-A5-A1-QUERYSETE", VariantLevelForSearch.EXACT_SEARCH), "n-sign1", 1);
    }

    @Test
    public void wildcardFindsEquivalentInQuadrat() {
        check(wildcard("i-A5-Z1", VariantLevelForSearch.EXACT_SEARCH), "i-sign1:Z1", 0);
    }

    @Test
    public void signStringFindsEquivalent() {
        check(new SignStringSearchQuery(DATABASE, List.of("i", "A5")), "n-i-sign1", 1);
    }

    @Test
    public void quadratFindsEquivalent() {
        check(new QuadratSearchQuery(parse("A5:Z1")), "i-sign1:Z1-A5:Z1", 1, 2);
    }

    @Test
    public void quadratFindsEmbeddedCode() {
        check(new QuadratSearchQuery(parse("sign1:Z1")), "i-sign1:Z1-A5:Z1", 1);
    }

    @Test
    public void quadratIgnoresSignWithoutEquivalent() {
        check(new QuadratSearchQuery(parse("A5:Z1")), "sign2:Z1");
    }
}
