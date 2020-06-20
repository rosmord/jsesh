/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rosmord
 */
public class LineNumberHelperTest {

    /**
     * Test of incrementLineNumber method, of class LineNumberHelper.
     */
    @Test
    public void testEmpty() {
        String input = "";
        String expectedOutput = "";
        assertEquals(expectedOutput, LineNumberHelper.incrementLineNumber(input));
    }

    @Test
    public void testSimple() {
        String input = "24";
        String expectedOutput = "25";
        assertEquals(expectedOutput, LineNumberHelper.incrementLineNumber(input));
    }

    @Test
    public void testSimpleWholeNumber() {
        // Check it does work on multiple digits !
        String input = "99";
        String expectedOutput = "100";
        assertEquals(expectedOutput, LineNumberHelper.incrementLineNumber(input));
    }

    @Test
    public void testVerySimple() {
        String input = "2";
        String expectedOutput = "3";
        assertEquals(expectedOutput, LineNumberHelper.incrementLineNumber(input));
    }

    @Test
    public void testWithContext() {
        String input = "[24]";
        String expectedOutput = "[25]";
        assertEquals(expectedOutput, LineNumberHelper.incrementLineNumber(input));
    }

    @Test
    public void testWithMultipleNumbers() {
        String input = "12 vo 5";
        String expectedOutput = "12 vo 6";
        assertEquals(expectedOutput, LineNumberHelper.incrementLineNumber(input));
    }

    @Test
    public void testWithMultipleNumbersA() {
        String input = "12 vo 29";
        String expectedOutput = "12 vo 30";
        assertEquals(expectedOutput, LineNumberHelper.incrementLineNumber(input));
    }

}
