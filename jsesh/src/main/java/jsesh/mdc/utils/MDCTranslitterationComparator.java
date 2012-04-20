package jsesh.mdc.utils;

import java.util.Comparator;

public class MDCTranslitterationComparator implements Comparator<String> {

	/**
	 * Compare two translitterations.
	 */
	public int compare(String s1, String s2) {
		return MDCOrderingUtil.getOrderingForm(s1).compareTo(
				MDCOrderingUtil.getOrderingForm(s2));
	}

}
