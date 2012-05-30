package jsesh.mdc.utils;

import java.util.HashMap;
import java.util.Map;

import jsesh.utils.StringUtils;

/**
 * Various utility methods to deal with transliteration strings.
 * 
 * @author rosmord
 * 
 */

public class TranslitterationUtilities {

	/**
	 * Converts a translitteration written using the mapping used in the Utrecht
	 * fonts into proper Manuel de Codage.
	 * <p>
	 * It seems quite a few users tend to use non-standard MdC for uppercase
	 * letters.
	 * <p>
	 * The corresponding codes for upper case are:
	 * <ul>
	 * <li> h = !
	 * <li> H = @
	 * <li> x = #
	 * <li> X = £
	 * <li> s = %
	 * <li> S = ^ ; quite impossible in a MdC file.
	 * <li> t = &amp;
	 * <li> T = ß or *
	 * <li> d = ?
	 * <li> D = + ; quite impossible in a MdC file.
	 * <li> s with spirit = c
	 * </ul>
	 * </ul>
	 * @param entry :
	 *            a string using the encoding of Utrecht's transliteration
	 *            fonts
	 * @return proper MdC transliteration, with ^ for uppercase.
	 */
	public static String convertWindowsTranslitteration(String entry) {
		// entry= StringUtils.replace(entry, "^", "^S");
		entry = StringUtils.replace(entry, "!", "^h");
		entry = StringUtils.replace(entry, "@", "^H");
		entry = StringUtils.replace(entry, "#", "^x");
		entry = StringUtils.replace(entry, "£", "^X");
		entry = StringUtils.replace(entry, "%", "^s");
		entry = StringUtils.replace(entry, "&", "^t");
		entry = StringUtils.replace(entry, "ß", "^T");
		entry = StringUtils.replace(entry, "*", "^T");
		entry = StringUtils.replace(entry, "\"", "^d");
		//entry = StringUtils.replace(entry, "+", "^D"); not possible
		entry = StringUtils.replace(entry, "c", "s");
		return entry;
	}
	
	/**
	 * Suppress all uppercase markers (^) from a MdC transliteration.
	 * @param entry a proper standard MdC transliteration text.
	 * @return
	 */
	public static String toLowerCase(String entry) {
		return StringUtils.replace(entry, "^", "");
	}

	/**
	 * Transform a mdc String into a String which can be displayed.
	 * @param mdcCode a text, using the MdC encoding for transliteration.
	 * @param transliterationEncoding the way transliteration will be encoded in display.
	 * @return a string ready for display (provided the fonts used respect transliterationEncoding)
	 */
	public static String getActualTransliterationString(String mdcCode, TransliterationEncoding transliterationEncoding) {
		if (transliterationEncoding.isTranslitUnicode()) {
			StringBuilder builder= new StringBuilder();
			boolean nextIsUpper= false;
			for (int i= 0; i < mdcCode.length(); i++) {
				char c= mdcCode.charAt(i);			
				if (c == '^')
					nextIsUpper= true;
				else {
					String cDown, cUp;
					switch (c) {
					case 'A':
						cUp= "\uA722";
						cDown= "\uA723";
						//cUp= "" + (char)0x722;
						//cDown= ""+(char)0x723;
						break;
					case 'i':
						if (transliterationEncoding.getYodChoice() == YODChoice.U0313) {
							cUp= "I\u0313";
							cDown= "i\u0313";
						} else {
							cUp= "I\u0486";
							cDown= "i\u0486";
						}
						break;
					case 'a':
						cUp="\uA724";
						cDown= "\uA725";
						//cUp=""+ (char)0xA724;
						//cDown= ""+ (char)0xA725;
						//cUp= "Ꜥ";
						//cDown= "ꜥ";
						//cUp= new String(new char[] {0x724});
						//cDown= new String(new char[] {0x725});
						//System.out.println(Integer.toHexString(cUp.charAt(0)));
						break;
					case 'H':
						cUp="\u1e24";
						cDown= "\u1e25";						
						break;
					case 'x':
						cUp="\u1e2a";
						cDown= "\u1e2b";						
						break;
					case 'X':
						cUp="H\u0331";
						cDown= "\u1e96";
						break;
					case 'S':
						cUp="\u0160";
						cDown= "\u0161";
						break;
					case 'q':					
						if (transliterationEncoding.isGardinerQofUsed()) {
							cUp= "\u1e32";
							cDown= "\u1e33";
						} else {
							cUp= "Q";
							cDown= "q";
						}
						break;
					case 'T':
						cUp= "\u1e6e";
						cDown= "\u1e6f";
						break;
					case 'D':
						cUp= "\u1e0e";
						cDown= "\u1e0f";
						break;
					default:
						cUp= ""+Character.toUpperCase(c);
						cDown= ""+c;
						break;
					}
					if (nextIsUpper) 
						builder.append(cUp);
					else
						builder.append(cDown);
					nextIsUpper= false;
				}
			}
			String result= builder.toString();
			return result;
		} else {
			// We don't do capitals in our mdc font.
			return mdcCode.replace("^", "");
		}
	}
	
	/**
	 * Static data used by the ordering system.
	 */
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
	 * @param translitteration an MdC transliteration.
	 * @return the transliterated text.
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
