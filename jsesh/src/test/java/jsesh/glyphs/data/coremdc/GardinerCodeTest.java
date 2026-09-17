package jsesh.glyphs.data.coremdc;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.signcodes.GardinerCode;

public class GardinerCodeTest {

	@Test
	public void testcreateGardinerCodeSimple() {
		checkCodeCreationOk("G32", 0, "G", 32, "");
	}

	@Test
	public void testcreateGardinerCodeAA() {
		checkCodeCreationOk("Aa10", 0, "Aa", 10, "");
	}

	@Test
	public void testcreateGardinerCodeFf() {
		checkCodeCreationOk("Ff5", 0, "Ff", 5, "");
	}

	@Test
	public void testcreateGardinerCodeWithVar() {
		checkCodeCreationOk("G32VARX", 0, "G", 32, "VARX");
	}

	@Test
	public void testcreateGardinerCodeWithUser() {
		checkCodeCreationOk("US122A32", 122, "A", 32, "");
	}

	private void checkCodeCreationOk(String code, int userId, String family, int number, String varPart) {
		GardinerCode gardinerCode = GardinerCode.createGardinerCode(code);
		assertEquals(userId, gardinerCode.getUserId());
		assertEquals(family, gardinerCode.getFamily());
		assertEquals(number, gardinerCode.getNumber());
		assertEquals(varPart, gardinerCode.getVariantPart());
	}

	@Test
	public void testCodeForFileName1() {
		String fname = "aAv.svg";
		assertEquals(null, GardinerCode.getCodeForFileName(fname));
	}

	@Test
	public void testCodeForFileName2() {
		String fname = "O29v.svg";
		assertEquals("O29v", GardinerCode.getCodeForFileName(fname));
	}

	@Test
	public void testCodeForFileNameNormalizesAaFamily() {
		assertEquals("Aa10", GardinerCode.getCodeForFileName("AA10.svg"));
	}

	@Test
	public void testIsWellFormedGardinerCode_valid() {
		assertTrue(GardinerCode.isWellFormedGardinerCode("G17"));
	}

	@Test
	public void testIsWellFormedGardinerCode_rejectsLowercaseFamily() {
		assertFalse(GardinerCode.isWellFormedGardinerCode("g17"));
	}

	@Test
	public void testIsWellFormedCodeIgnoreCase_acceptsLowercase() {
		assertTrue(GardinerCode.isWellFormedCodeIgnoreCase("g17"));
	}

	@Test
	public void testVariantPart_singleLetterHIsLowercased() {
		GardinerCode code = new GardinerCode("A", 23, "H");
		assertEquals("h", code.getVariantPart());
		assertEquals("A23h", code.toString());
	}

	@Test
	public void testVariantPart_singleLetterVIsLowercased() {
		GardinerCode code = new GardinerCode("D", 1, "v");
		assertEquals("v", code.getVariantPart());
		assertEquals("D1v", code.toString());
	}

	@Test
	public void testVariantPart_multiCharVariantStaysUppercase() {
		GardinerCode code = new GardinerCode("G", 32, "vara");
		assertEquals("VARA", code.getVariantPart());
	}

	@Test
	public void testCompareTo_familyOrder() {
		GardinerCode d21 = new GardinerCode("D", 21, "");
		GardinerCode g43 = new GardinerCode("G", 43, "");
		assertTrue(d21.compareTo(g43) < 0);
		assertTrue(g43.compareTo(d21) > 0);
	}

	@Test
	public void testCompareTo_userIdTiebreak() {
		GardinerCode user1 = new GardinerCode(1, "A", 1, "");
		GardinerCode user2 = new GardinerCode(2, "A", 1, "");
		assertTrue(user1.compareTo(user2) < 0);
	}

	@Test
	public void testGetGardinerFamilies() {
		List<String> families = GardinerCode.getGardinerFamilies();
		assertEquals(29, families.size());
		assertEquals("A", families.get(0));
		assertTrue(families.contains("Aa"));
		assertTrue(families.contains("Ff"));
		assertTrue(families.contains("NU"));
		assertTrue(families.contains("NL"));
	}
}
