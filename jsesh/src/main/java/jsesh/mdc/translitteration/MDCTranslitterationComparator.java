package jsesh.mdc.translitteration;

import java.util.Comparator;

public class MDCTranslitterationComparator implements Comparator<String> {

	/**
	 * Compare two translitterations.
	 */
	public int compare(String s1, String s2) {
		return TransliterationUtilities.getOrderingForm(s1).compareTo(
				TransliterationUtilities.getOrderingForm(s2));
	}

}
