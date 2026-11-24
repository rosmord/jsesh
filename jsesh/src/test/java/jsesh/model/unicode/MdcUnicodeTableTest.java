package jsesh.model.unicode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MdcUnicodeTableTest {

    private String unicodeFor(int codepoint) {
        return new String(Character.toChars(codepoint));
    }

    @Test
    public void testGetUnicodeFor_knownGardinerCodes() {
        assertEquals(unicodeFor(0x131CB), MdcUnicodeTable.INSTANCE.getUnicodeFor("M17"));
        assertEquals(unicodeFor(0x13000), MdcUnicodeTable.INSTANCE.getUnicodeFor("A1"));
        assertEquals(unicodeFor(0x13171), MdcUnicodeTable.INSTANCE.getUnicodeFor("G43"));
        assertEquals(unicodeFor(0x1308B), MdcUnicodeTable.INSTANCE.getUnicodeFor("D21"));
    }

    @Test
    public void testGetUnicodeFor_phoneticCodeResolvesViaCanonicalCode() {
        // "i" is the phonetic MdC shortcut for the Gardiner code M17.
        assertEquals(MdcUnicodeTable.INSTANCE.getUnicodeFor("M17"), MdcUnicodeTable.INSTANCE.getUnicodeFor("i"));
    }

    @Test
    public void testHasUnicode_knownCode() {
        assertTrue(MdcUnicodeTable.INSTANCE.hasUnicode("M17"));
    }

    @Test
    public void testHasUnicode_unknownCode() {
        assertFalse(MdcUnicodeTable.INSTANCE.hasUnicode("ZZZ999"));
    }

    @Test
    public void testGetUnicodeFor_fallsBackToInputWhenUnmapped() {
        assertEquals("ZZZ999", MdcUnicodeTable.INSTANCE.getUnicodeFor("ZZZ999"));
    }

    @Test
    public void testSupplementaryTableAddsSigns() {
        // A1A is not in mdc2unicode.txt, but is in namemdc.csv.
        assertTrue(MdcUnicodeTable.INSTANCE.hasUnicode("A1A"));
        assertEquals(unicodeFor(0x1359D), MdcUnicodeTable.INSTANCE.getUnicodeFor("A1A"));
        assertTrue(MdcUnicodeTable.INSTANCE.getSupplementaryCount() > 1000);
    }

    @Test
    public void testCuratedTableHasPriority() {
        // M17 and E34 are in both files: the result must be the same as in mdc2unicode.txt.
        assertEquals(unicodeFor(0x130F9), MdcUnicodeTable.INSTANCE.getUnicodeFor("E34"));
        // N33 used to be shadowed by a duplicate line (D067).
        assertEquals(unicodeFor(0x13212), MdcUnicodeTable.INSTANCE.getUnicodeFor("N33"));
    }

    @Test
    public void testFormatControlsAreNotLoaded() {
        // A80A is a group made with format controls in namemdc.csv: layout, not handled yet.
        assertFalse(MdcUnicodeTable.INSTANCE.hasUnicode("A80A"));
    }
}
