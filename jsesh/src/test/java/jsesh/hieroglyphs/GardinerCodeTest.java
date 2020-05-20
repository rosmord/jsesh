package jsesh.hieroglyphs;

import org.junit.Test;

import static org.junit.Assert.*;
import static jsesh.hieroglyphs.data.GardinerCode.*;

public class GardinerCodeTest {

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
	public void isCanonicalCodenn() {
		assertTrue(isCanonicalCode("nn"));
	}


	@Test
	public void isCanonicalCodenTrw() {
		assertTrue(isCanonicalCode("nTrw"));
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