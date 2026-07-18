package jsesh.glyphs.data.coremdc;

import static jsesh.glyphs.data.coremdc.GardinerCode.isCanonicalCode;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class GardinerCodeCanonicalCodeTest {

	@Test
	public void isCanonicalCodeA1() {
		assertTrue(isCanonicalCode("A1"));
	}

	@Test
	public void isCanonicalNotCodem() {
		assertFalse(isCanonicalCode("m"));
	}


	@Test
	public void isCanonicalNotCodeaa() {
		assertFalse(isCanonicalCode("Aa"));
	}

	@Test
	public void isCanonicalCodeG17() {
		assertTrue(isCanonicalCode("G17"));
	}

	

	@Test
	public void isCanonicalCode2() {
		assertTrue(isCanonicalCode("2"));
	}


	@Test
	public void isCanonicalCode5() {
		assertTrue(isCanonicalCode("5"));
	}


	@Test
	public void isCanonicalCode3() {
		assertTrue(isCanonicalCode("3"));
	}


	@Test
	public void isCanonicalCode40() {
		assertTrue(isCanonicalCode("40"));
	}
}