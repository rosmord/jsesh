package jsesh.hieroglyphs.data;

import static org.junit.Assert.*;

import org.junit.Test;

import jsesh.hieroglyphs.data.coreMdC.GardinerCode;

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
}
