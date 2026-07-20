package jsesh.glyphs.data.coremdc;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
