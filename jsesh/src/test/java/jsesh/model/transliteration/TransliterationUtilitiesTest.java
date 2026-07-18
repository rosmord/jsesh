package jsesh.model.transliteration;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import jsesh.model.transliteration.TransliterationUtilities;

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
