package jsesh.mdc.translitteration;

import jsesh.glossary.JSeshGlossary;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rosmord
 */
public class TranslitterationUtilitiesTest {
    
   
    /**
     * Tests the method simplifyKey, which removes non MdC chars.
     */

    @Test
    public void testSimplifyKeyEmpty() {
        String entry = "";
        String expected = "";
        
        String actual = TranslitterationUtilities.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyAllOk() {
        String entry = "sxmnṯrꜥꜣ𓐍";
        String expected = "sxmnṯrꜥꜣ𓐍";
        String actual = TranslitterationUtilities.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyWithHyphen() {
        String entry = "Hwt-Hr";
        String expected = "HwtHr";
        String actual = TranslitterationUtilities.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyWithHyphens() {
        String entry = "Hr-m-Ax-bit";
        String expected = "HrmAxbit";
        String actual = TranslitterationUtilities.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyWithSpaces() {
        String entry = "Hwt Hr";
        String expected = "HwtHr";
        String actual = TranslitterationUtilities.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyWithSUnderscores() {
        String entry = "Hwt_Hr";
        String expected = "HwtHr";
        String actual = TranslitterationUtilities.removeHyphens(entry);
        assertEquals(expected, actual);
    }
}
