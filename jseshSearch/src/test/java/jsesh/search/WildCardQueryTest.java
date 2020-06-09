/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.search.simple.SignStringSearchQuery;
import jsesh.search.wildcard.VariantLevelForSearch;
import jsesh.search.wildcard.WildCardQuery;
import org.junit.Test;

import static org.junit.Assert.*;

public class WildCardQueryTest {

    private TopItemList parse(String mdc) {
        try {
            return new MDCParserModelGenerator().parse(mdc);
        } catch (MDCSyntaxError ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Auxiliary method for writing tests.
     *
     * @param message
     * @param mdc
     * @param codes
     * @param expected
     */
    private void doSearch(String message, String mdc, String codes, VariantLevelForSearch variantLevel, Integer... expected) {
        TopItemList text = parse(mdc);
        TopItemList toSearch = parse(codes);
        WildCardQuery searcher = new WildCardQuery(toSearch, 0, variantLevel);
        List<MDCPosition> actualResult = searcher.doSearch(text);
        List<MDCPosition> expectedResult
                = Arrays.asList(expected).stream()
                        .map(pos -> new MDCPosition(text, pos))
                        .collect(Collectors.toList());
        assertEquals(message, expectedResult, actualResult);
    }

    @Test
    public void signForSignSearch() {
        doSearch("Simple search", "A1-A1-A2-A3", "A2", VariantLevelForSearch.EXACT_SEARCH, 2);
    }

    @Test
    public void searchStar() {
        doSearch("QuerySkip", "m-m-n:r-s-g-p", "n QUERYSKIP g", VariantLevelForSearch.EXACT_SEARCH, 2);
    }

    @Test
    public void searchApprox() {
        doSearch("Approximative search", "m-C1B", "C1", VariantLevelForSearch.EXTENDED_VARIANTS, 1);
    }

    /**
     * Note : this test shows that transitive dependencies go a bit too far...
     */
    @Test
    public void searchApproxMultipleSteps() {
        doSearch("Approximative search", "m-C268", "C1", VariantLevelForSearch.EXTENDED_VARIANTS, 1);
    }

    /**
     * Note : this test shows that transitive dependencies go a bit too far...
     */
    @Test
    public void searchApproxMultipleStepsReversed() {
        doSearch("Approximative search", "m-C1", "C268", VariantLevelForSearch.EXTENDED_VARIANTS, 1);
    }

    @Test
    public void testSimpleSearchGardinerCodes() {
        doSearch("Simple search", "i-w-r:a-C1-r-ir:t", "D21", VariantLevelForSearch.EXACT_SEARCH, 2, 4);
    }

    @Test
    public void testSimpleSearchPhonCodes() {
        doSearch("Simple search", "i-w-r:a-C1-r-ir:t", "r", VariantLevelForSearch.EXACT_SEARCH, 2, 4);
    }

    @Test
    public void testInQuadrant() {
        doSearch("test In Quadrants", "i-n:n:n:k:w-m", "n n k", VariantLevelForSearch.EXACT_SEARCH, 1);
    }

    @Test
    public void testLast() {
        doSearch("last Pos", "i-w-r:a-C1-r-ir:t", "t", VariantLevelForSearch.EXACT_SEARCH, 5);
    }

    @Test
    public void testFirst() {
        doSearch("first", "i-w-r:a-C1-r-ir:t", "i", VariantLevelForSearch.EXACT_SEARCH, 0);
    }

    @Test
    public void testSecond() {
        doSearch("second", "i-w-r:a-C1-r-ir:t", "w r", VariantLevelForSearch.EXACT_SEARCH, 1);
    }

    @Test
    public void testPartiel() {
        doSearch("testPartiel", "i-w-r:a-C1-r-ir:t", "w b", VariantLevelForSearch.EXACT_SEARCH);
    }

    @Test
    public void testSeq() {
        doSearch("testSeq", "i-w-r:a-C1-r-ir:t", "r ir", VariantLevelForSearch.EXACT_SEARCH, 4);

    }

    @Test
    public void testLongSeq() {
        doSearch("testLongSeq", "i-w-r:a#12-C1-r-ir:t", "r a C1 r ir", VariantLevelForSearch.EXACT_SEARCH, 2);
    }

    @Test
    public void testNo() {
        doSearch("testNo", "i-w-r:a-C1-r-ir:t", "r C1", VariantLevelForSearch.EXACT_SEARCH);
    }

    @Test
    public void doWildCardSearch() {
        String searchString = "r-a-QUERYSKIP-m";
        String mdc = "i-w-r:a-C1-m-pt:p*t";

    }

    @Test
    public void doApproximativeSearch() {

    }

    @Test
    public void doSetSearch() {

    }
}
