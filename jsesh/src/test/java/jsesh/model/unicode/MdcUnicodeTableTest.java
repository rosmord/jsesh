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
}
