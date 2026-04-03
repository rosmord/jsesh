package jsesh.mdc.translitteration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author rosmord
 */
public class TransliterationUtilitiesTest {
    
   
    /**
     * Tests the method simplifyKey, which removes non MdC chars.
     */

    @Test
    public void testSimplifyKeyEmpty() {
        String entry = "";
        String expected = "";
        
        String actual = TransliterationUtilities.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyAllOk() {
        String entry = "sxmnṯrꜥꜣ𓐍";
        String expected = "sxmnṯrꜥꜣ𓐍";
        String actual = TransliterationUtilities.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyWithHyphen() {
        String entry = "Hwt-Hr";
        String expected = "HwtHr";
        String actual = TransliterationUtilities.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyWithHyphens() {
        String entry = "Hr-m-Ax-bit";
        String expected = "HrmAxbit";
        String actual = TransliterationUtilities.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyWithSpaces() {
        String entry = "Hwt Hr";
        String expected = "HwtHr";
        String actual = TransliterationUtilities.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyWithSUnderscores() {
        String entry = "Hwt_Hr";
        String expected = "HwtHr";
        String actual = TransliterationUtilities.removeHyphens(entry);
        assertEquals(expected, actual);
    }
}
