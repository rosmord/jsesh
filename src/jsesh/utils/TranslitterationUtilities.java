package jsesh.utils;

/**
 * Various utility methods to help importing texts from other softwares.
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
	 *            a string using the encoding of Utrecht's translitteration
	 *            fonts
	 * @return proper MdC translitteration, with ^ for uppercase.
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
	 * Suppress all uppercase markers (^) from a MdC translitteration.
	 * @param entry
	 * @return
	 */
	public static String toLowerCase(String entry) {
		return StringUtils.replace(entry, "^", "");
	}
}
