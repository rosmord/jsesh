package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link ShadingCode#parse(String)}, the decode direction of the MdC
 * {@code "#1234"} shading syntax (the encode direction is
 * {@link ShadingCode#toString(String, int)}).
 */
class ShadingCodeTest {

    @Test
    void parseDecodesEachDigitToItsBit() {
        assertEquals(ShadingCode.TOP_START, ShadingCode.parse("1"));
        assertEquals(ShadingCode.TOP_END, ShadingCode.parse("2"));
        assertEquals(ShadingCode.BOTTOM_START, ShadingCode.parse("3"));
        assertEquals(ShadingCode.BOTTOM_END, ShadingCode.parse("4"));
    }

    @Test
    void parseOrsTogetherAllDigitsPresent() {
        assertEquals(ShadingCode.FULL, ShadingCode.parse("1234"));
        assertEquals(3, ShadingCode.parse("12"));
        assertEquals(5, ShadingCode.parse("13"));
    }

    @Test
    void parseIgnoresUnknownCharactersAndEmptyInput() {
        assertEquals(ShadingCode.NONE, ShadingCode.parse(""));
        assertEquals(ShadingCode.TOP_START, ShadingCode.parse("1x"));
    }

    @Test
    void parseIsTheInverseOfToString() {
        for (int sh = 0; sh <= ShadingCode.FULL; sh++) {
            String digits = ShadingCode.toString("", sh);
            assertEquals(sh, ShadingCode.parse(digits));
        }
    }
}
