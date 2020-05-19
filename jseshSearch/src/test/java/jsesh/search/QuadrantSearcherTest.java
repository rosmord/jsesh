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
import jsesh.search.quadrant.QuadrantSearchQuery;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test of searches for Specific quadrants.
 * @author rosmord
 */
public class QuadrantSearcherTest {
    
    private TopItemList parse(String mdc) {
        try {
            return new MDCParserModelGenerator().parse(mdc);
        } catch (MDCSyntaxError ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Auxiliary method for writing tests.
     * @param message
     * @param mdc
     * @param codes
     * @param expected 
     */
    private void doSearch(String message, String mdc, String mdcToSearch, Integer... expected) {
        TopItemList text = parse(mdc);
        TopItemList toSearch= parse(mdcToSearch);
        QuadrantSearchQuery searcher = new QuadrantSearchQuery(toSearch);
        List<MDCPosition> actualResult = searcher.doSearch(text);
        List<MDCPosition> expectedResult
                = Arrays.asList(expected).stream()
                        .map(pos -> new MDCPosition(text, pos))
                        .collect(Collectors.toList());
        assertEquals(message, expectedResult, actualResult);
    }

    @Test
    public void testSimpleSearchGardinerCodes() {
        doSearch("Simple search", "i-w-r:a-C1-r-ir:t", "D21", 4);
    }

    @Test
    public void testSearchQuadrant() {
        doSearch("Simple search", "i-w-r:a-C1-r-ir:t", "D21", 4);
    }
    
     @Test
    public void testNone() {
        doSearch("Simple search", "i-w-r:a-C1-Hr:r-ir:t", "D21");
    }
    
    // The following specification is not yet implemented.
    // (case pending).
//    @Test
//    public void testSubQuadrant() {
//        doSearch("testSubQuadrant", 
//                "G40-tA:N23*1-n:km:niwt*t-m-anx-DA-s",
//                "km:niwt*X1", 2
//                );
//    }
}
