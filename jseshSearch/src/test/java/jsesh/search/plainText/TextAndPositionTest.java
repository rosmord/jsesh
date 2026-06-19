/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.plainText;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 *
 * @author rosmord
 */
public class TextAndPositionTest {

    TextAndPosition textAndPosition;

    @BeforeEach    
    public void prepare() {
        textAndPosition = TextAndPosition.builder(10).
                add("abcde").add("fg").add("hi").build();
    }

    @Test
    public void testMatchFirst() {
        List<Integer> res = textAndPosition.match("bc");
        assertEquals(Arrays.asList(10), res);
    }

    @Test
    public void testMatchSecondA() {
        List<Integer> res = textAndPosition.match("f");
        assertEquals(Arrays.asList(11), res);
    }

    @Test
    public void testMatchSecondB() {
        List<Integer> res = textAndPosition.match("fg");
        assertEquals(Arrays.asList(11), res);
    }

    @Test
    public void testMatchSecondC() {
        List<Integer> res = textAndPosition.match("g");
        assertEquals(Arrays.asList(11), res);
    }

    @Test
    public void testMatchLastA() {
        List<Integer> res = textAndPosition.match("hi");
        assertEquals(Arrays.asList(12), res);
    }

    @Test
    public void testMatchLastB() {
        List<Integer> res = textAndPosition.match("h");
        assertEquals(Arrays.asList(12), res);
    }

    @Test
    public void testMatchLastC() {
        List<Integer> res = textAndPosition.match("i");
        assertEquals(Arrays.asList(12), res);
    }

    @Test
    public void testMatchOverlap() {
        List<Integer> res = textAndPosition.match("ghi");
        assertEquals(Arrays.asList(11), res);
    }

    @Test
    public void testMatchOverlapA() {
        List<Integer> res = textAndPosition.match("efghi");
        assertEquals(Arrays.asList(10), res);
    }

    @Test
    public void testMatchOverlapAll() {
        List<Integer> res = textAndPosition.match("abcdefghi");
        assertEquals(Arrays.asList(10), res);
    }

    @Test
    public void testMatchMultiple() {
        TextAndPosition textAndPosition = TextAndPosition.builder(10)
                .add("un un").add("pour ").add("un autre").build();

        assertEquals(Arrays.asList(10,10, 12), textAndPosition.match("un"));
    }

   
}
