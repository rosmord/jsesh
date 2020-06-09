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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test of searches for sequences of signs.
 * @author rosmord
 */
public class SignStringSearcherTest {

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
    private void doSearch(String message, String mdc, String codes, Integer... expected) {
        TopItemList text = parse(mdc);
        List<String> toSearch = Arrays.asList(codes.split(" "));
        SignStringSearchQuery searcher = new SignStringSearchQuery(toSearch);
        List<MDCPosition> actualResult = searcher.doSearch(text);
        List<MDCPosition> expectedResult
                = Arrays.asList(expected).stream()
                        .map(pos -> new MDCPosition(text, pos))
                        .collect(Collectors.toList());
        assertEquals(message, expectedResult, actualResult);
    }

    @Test
    public void testSimpleSearchGardinerCodes() {
        doSearch("Simple search", "i-w-r:a-C1-r-ir:t", "D21", 2, 4);
    }

    @Test
    public void testSimpleSearchPhonCodes() {
        doSearch("Simple search", "i-w-r:a-C1-r-ir:t", "r", 2, 4);
    }

    @Test
    public void testInQuadrant() {
        doSearch("test In Quadrants", "i-n:n:n:k:w-m", "n n k", 1);
    }

    @Test
    public void testLast() {
        doSearch("last Pos", "i-w-r:a-C1-r-ir:t", "t", 5);
    }

    @Test
    public void testFirst() {
        doSearch("first", "i-w-r:a-C1-r-ir:t", "i", 0);
    }

    @Test
    public void testSecond() {
        doSearch("second", "i-w-r:a-C1-r-ir:t", "w r", 1);
    }

    @Test
    public void testPartiel() {
        doSearch("testPartiel", "i-w-r:a-C1-r-ir:t", "w b");
    }

    @Test
    public void testSeq() {
        doSearch("testSeq", "i-w-r:a-C1-r-ir:t", "r ir", 4);

    }

    @Test
    public void testLongSeq() {
        doSearch("testLongSeq", "i-w-r:a#12-C1-r-ir:t", "r a C1 r ir",2);
    }

    @Test
    public void testNo() {
        doSearch("testNo", "i-w-r:a-C1-r-ir:t", "r C1");
    }


}
