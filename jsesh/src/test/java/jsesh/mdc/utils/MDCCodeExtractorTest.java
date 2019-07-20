package jsesh.mdc.utils;

import jsesh.mdc.MDCSyntaxError;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Check MDCCodeExtractor.
 * Done after a bug was found in the search code.
 */


public class MDCCodeExtractorTest {

	/**
	 * Various simple tests.
	 * Original MDC : i-w-r:a-C1-m-pt:p*t
	 * "Gardinerized" MdC : M17-G43-D21:D36-C1-G17-N1:Q3*X1
	 */
	@Test
	public void testSimple() throws MDCSyntaxError {
		String mdc = "i-w-r:a-C1-m-pt:p*t";
		MDCCodeExtractor extractor= new MDCCodeExtractor();
		List<String> codeList = extractor.getCodesAsList(mdc);
		assertEquals(Arrays.asList("M17","G43","D21","D36","C1", "G17", "N1", "Q3", "X1"), codeList);
	}

	/**
	 * Original bug: "3" was not recogized.
	 * The bug was due to a specification change. Originally, "3" was a shortcut for "Z2". At some
	 * point, it was modified to be a different sign. This was taken into account in most places, but not in the method
	 * "isCanonicalCode()". Hence the bug.
	 */
	@Test
	public void test3() throws MDCSyntaxError {
		String mdc = "3";
		MDCCodeExtractor extractor= new MDCCodeExtractor();
		List<String> codeList = extractor.getCodesAsList(mdc);
		assertEquals(Arrays.asList("3"), codeList);
	}

}