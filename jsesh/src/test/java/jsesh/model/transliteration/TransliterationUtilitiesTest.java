package jsesh.model.transliteration;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import jsesh.model.tools.YODChoice;
import jsesh.utils.datatypes.StringUtils;

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
        
        String actual = StringUtils.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyAllOk() {
        String entry = "sxmnṯrꜥꜣ𓐍";
        String expected = "sxmnṯrꜥꜣ𓐍";
        String actual = StringUtils.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyWithHyphen() {
        String entry = "Hwt-Hr";
        String expected = "HwtHr";
        String actual = StringUtils.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyWithHyphens() {
        String entry = "Hr-m-Ax-bit";
        String expected = "HrmAxbit";
        String actual = StringUtils.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyWithSpaces() {
        String entry = "Hwt Hr";
        String expected = "HwtHr";
        String actual = StringUtils.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimplifyKeyWithSUnderscores() {
        String entry = "Hwt_Hr";
        String expected = "HwtHr";
        String actual = StringUtils.removeHyphens(entry);
        assertEquals(expected, actual);
    }

    /**
     * Tests for getActualTransliterationString, using
     * "iw ^Imn Ra ^Xnmw m-a=k qn=k Ax ^Ax" ("Amun-Ra-Khnum is with you, you
     * are powerful, akh") as the running example.
     */

    private static final String SAMPLE = "iw ^Imn Ra ^Xnmw m-a=k qn=k Ax ^Ax";

    @Test
    public void testSampleWithDefaultYodChoice() {
        TransliterationEncoding encoding = new TransliterationEncoding(true, YODChoice.U0313, false);
        String expected = "i̓w Imn Rꜥ H̱nmw m-ꜥ=k qn=k ꜣḫ Ꜣḫ";
        assertEquals(expected, TransliterationUtilities.getActualTransliterationString(SAMPLE, encoding));
    }

    @Test
    public void testSampleWithUseJYodChoice() {
        TransliterationEncoding encoding = new TransliterationEncoding(true, YODChoice.PLAIN_J, false);
        // Only "iw" changes: the yod is rendered as plain "j" instead of "i" + a diacritic.
        String expected = "jw Imn Rꜥ H̱nmw m-ꜥ=k qn=k ꜣḫ Ꜣḫ";
        assertEquals(expected, TransliterationUtilities.getActualTransliterationString(SAMPLE, encoding));
    }

    @Test
    public void testSampleWithGardinerQof() {
        TransliterationEncoding encoding = new TransliterationEncoding(true, YODChoice.U0313, true);
        // Only "qn=k" changes: q becomes the dotted-k Gardiner qof.
        String expected = "i̓w Imn Rꜥ H̱nmw m-ꜥ=k ḳn=k ꜣḫ Ꜣḫ";
        assertEquals(expected, TransliterationUtilities.getActualTransliterationString(SAMPLE, encoding));
    }

    @Test
    public void testSampleWithNonUnicodeEncoding() {
        // The non-unicode (MdC font) branch just strips '^' markers and leaves every other char untouched.
        TransliterationEncoding encoding = new TransliterationEncoding(false, YODChoice.U0313, false);
        String expected = "iw Imn Ra Xnmw m-a=k qn=k Ax Ax";
        assertEquals(expected, TransliterationUtilities.getActualTransliterationString(SAMPLE, encoding));
    }

    @Test
    public void testCaretOnlyCapitalizesTheImmediatelyFollowingChar() {
        // "^Xnmw": only the X is affected by the caret; n, m, w are unchanged.
        TransliterationEncoding encoding = new TransliterationEncoding(true, YODChoice.U0313, false);
        assertEquals("H̱nmw", TransliterationUtilities.getActualTransliterationString("^Xnmw", encoding));
    }

    @Test
    public void testRawUppercaseLettersOutsideSpecialCasesPassThroughUnchanged() {
        // 'R' has no dedicated switch case, so toUpperCase()/verbatim-char logic makes
        // '^' a no-op on it: both with and without the caret it stays "R".
        TransliterationEncoding encoding = new TransliterationEncoding(true, YODChoice.U0313, false);
        assertEquals("R", TransliterationUtilities.getActualTransliterationString("R", encoding));
        assertEquals("R", TransliterationUtilities.getActualTransliterationString("^R", encoding));
    }

    @Test
    public void testCapitalAAndCapitalXAreDistinctLettersFromLowercase() {
        // 'A' (aleph) and 'X' are their own phonemes in MdC, not just an uppercase
        // spelling of 'a'/'x' - each still has its own upper/lower display form,
        // selected by '^', independently of the case already used in the source text.
        TransliterationEncoding encoding = new TransliterationEncoding(true, YODChoice.U0313, false);
        assertEquals("ꜣ", TransliterationUtilities.getActualTransliterationString("A", encoding));
        assertEquals("Ꜣ", TransliterationUtilities.getActualTransliterationString("^A", encoding));
        assertEquals("ẖ", TransliterationUtilities.getActualTransliterationString("X", encoding));
        assertEquals("H̱", TransliterationUtilities.getActualTransliterationString("^X", encoding));
    }

    @Test
    public void testGardinerQofToggle() {
        TransliterationEncoding gardiner = new TransliterationEncoding(true, YODChoice.U0313, true);
        assertEquals("ḳ", TransliterationUtilities.getActualTransliterationString("q", gardiner));
        assertEquals("Ḳ", TransliterationUtilities.getActualTransliterationString("^q", gardiner));

        TransliterationEncoding plain = new TransliterationEncoding(true, YODChoice.U0313, false);
        assertEquals("q", TransliterationUtilities.getActualTransliterationString("q", plain));
        assertEquals("Q", TransliterationUtilities.getActualTransliterationString("^q", plain));
    }

    @ParameterizedTest
    @EnumSource(YODChoice.class)
    public void testYodChoiceAffectsOnlyTheYodLetter(YODChoice yodChoice) {
        TransliterationEncoding encoding = new TransliterationEncoding(true, yodChoice, false);
        String lower = TransliterationUtilities.getActualTransliterationString("i", encoding);
        String upper = TransliterationUtilities.getActualTransliterationString("^i", encoding);
        String expectedLower = switch (yodChoice) {
            case U0313 -> "i̓";
            case U0486 -> "i҆";
            case UA7BD -> "ꞽ";
            case PLAIN_J -> "j";
        };
        String expectedUpper = switch (yodChoice) {
            case U0313 -> "I̓";
            case U0486 -> "I҆";
            case UA7BD -> "Ꞽ";
            case PLAIN_J -> "J";
        };
        assertEquals(expectedLower, lower);
        assertEquals(expectedUpper, upper);
    }

    @Test
    public void testEmptyString() {
        TransliterationEncoding encoding = new TransliterationEncoding(true, YODChoice.U0313, false);
        assertEquals("", TransliterationUtilities.getActualTransliterationString("", encoding));
    }
}
