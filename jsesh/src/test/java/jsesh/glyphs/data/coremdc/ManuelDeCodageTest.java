package jsesh.glyphs.data.coremdc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jsesh.signcodes.CanonicalCode;
import jsesh.signcodes.ManuelDeCodage;

public class ManuelDeCodageTest {

    @Test
    public void testR8A() {
        String mdc = "nTrw";
        String expected = "R8A";
        CanonicalCode actual = ManuelDeCodage.getInstance().getCanonicalCode(mdc);
        assertEquals(expected, actual.code());
        assertEquals(expected, actual.toString());
    }

    @Test
    public void testR8AIdempotent() {
        String mdc = "R8A";
        String expected = "R8A";
        CanonicalCode actual = ManuelDeCodage.getInstance().getCanonicalCode(mdc);
        assertEquals(expected, actual.code());
        assertEquals(expected, actual.toString());
    }

    @Test
    public void testM22B() {
        String mdc = "nn";
        String expected = "M22B";
        CanonicalCode actual = ManuelDeCodage.getInstance().getCanonicalCode(mdc);
        assertEquals(expected, actual.code());
        assertEquals(expected, actual.toString());
    }

    @Test
    public void testM22BIdempotent() {
        String mdc = "M22B";
        String expected = "M22B";
        CanonicalCode actual = ManuelDeCodage.getInstance().getCanonicalCode(mdc);
        assertEquals(expected, actual.code());
        assertEquals(expected, actual.toString());
    }

    @Test
    public void testCodeWithH() {
        String mdc = "A23H";
        String expected = "A23h";
        CanonicalCode actual = ManuelDeCodage.getInstance().getCanonicalCode(mdc);
        assertEquals(expected, actual.code());
        assertEquals(expected, actual.toString());
    }

    /**
     * isKnownCode only checks the raw phonetic/alias table, not every Gardiner
     * code: "nTrw" is registered as a key (mapping to R8A), but the Gardiner
     * code "R8A" itself never is.
     */
    @Test
    public void testIsKnownCode_registeredPhoneticCode() {
        assertTrue(ManuelDeCodage.getInstance().isKnownCode("nTrw"));
    }

    @Test
    public void testIsKnownCode_unregisteredCode() {
        assertFalse(ManuelDeCodage.getInstance().isKnownCode("ZZZ999"));
    }

    @Test
    public void testEqualsCanonical_phoneticVsGardiner() {
        assertTrue(ManuelDeCodage.getInstance().equalsCanonical("nTrw", "R8A"));
    }

    @Test
    public void testEqualsCanonical_differentSigns() {
        assertFalse(ManuelDeCodage.getInstance().equalsCanonical("M17", "G43"));
    }

    @Test
    public void testGetA1Code() {
        assertEquals("A1", ManuelDeCodage.getInstance().getA1Code().code());
    }

    @Test
    public void testCompareCodes_familyOrder() {
        assertTrue(ManuelDeCodage.compareCodes("D21", "G43") < 0);
    }
}
