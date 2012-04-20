package jsesh.mdc.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing transliteration alphabetic order in manual de codage ASCII format.
 * <p> This code should move to TransliterationUtilities for simplicity and coherence sake.
 * @author rosmord
 */
public class MDCOrderingUtil {
	private static final Map<Character, Integer> charMap = new HashMap<Character, Integer>();
	static {
		char[] t = { ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'i', 'y', 'a', 'w', 'b', 'p', 'f', 'm', 'n', 'r', 'l',
				'h', 'H', 'x', 'X', 'z', 's', 'S', 'q', 'k', 'g', 't', 'T',
				'd', 'D' };
		for (int i = 0; i < t.length; i++) {
			charMap.put(new Character(t[i]), new Integer(' ' + i));
		}
	}
	
	/**
	 * Return a string suitable for ordering translitterations.
	 * <p>
	 * The string will be an ASCII string.
	 * 
	 * @param translitteration
	 * @return
	 */
	public static String getOrderingForm(String translitteration) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < translitteration.length(); i++) {
			Character c = new Character(translitteration.charAt(i));
			if (c.charValue() == 'y') {
				buff.append((char) charMap.get(new Character('i'))
						.intValue());
				buff.append((char) charMap.get(new Character('i'))
						.intValue());
			} else if (charMap.containsKey(c)) {
				buff.append((char) charMap.get(c).intValue());
			}
		}
		return buff.toString();
	}

}
