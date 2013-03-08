package jsesh.mdc.utils;

import java.util.Comparator;

public class MDCTranslitterationComparator implements Comparator<String> {

	/**
	 * Compare two translitterations.
	 */
	public int compare(String s1, String s2) {
		return TranslitterationUtilities.getOrderingForm(s1).compareTo(
				TranslitterationUtilities.getOrderingForm(s2));
	}

}
