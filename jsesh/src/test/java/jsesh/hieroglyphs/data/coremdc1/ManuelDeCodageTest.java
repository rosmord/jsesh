package jsesh.hieroglyphs.data.coreMdC;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jsesh.hieroglyphs.data.coremdc1.ManuelDeCodage;

public class ManuelDeCodageTest {

    @Test
    public void testR8A() {
        String mdc = "nTrw";
        String expected = "R8A";
        String actual = ManuelDeCodage.getInstance().getCanonicalCode(mdc);
        assertEquals(expected, actual);
    }

    @Test
    public void testR8AIdempotent() {
        String mdc = "R8A";
        String expected = "R8A";
        String actual = ManuelDeCodage.getInstance().getCanonicalCode(mdc);
        assertEquals(expected, actual);
    }

    @Test
    public void testM22B() {
        String mdc = "nn";
        String expected = "M22B";
        String actual = ManuelDeCodage.getInstance().getCanonicalCode(mdc);
        assertEquals(expected, actual);
    }

    @Test
    public void testM22BIdempotent() {
        String mdc = "M22B";
        String expected = "M22B";
        String actual = ManuelDeCodage.getInstance().getCanonicalCode(mdc);
        assertEquals(expected, actual);
    }

}
