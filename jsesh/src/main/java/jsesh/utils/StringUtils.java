package jsesh.utils;

/**
 * Utility class for string manipulation.
 * 
 * @author rosmord
 * 
 */
public class StringUtils {

	/**
	 * Replaces all occurrences of t1 by t2 in <code>in</code>. jdk1.5 has a
	 * method for this, but not jdk1.4.
	 * 
	 * @param in
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static String replace(String in, String t1, String t2) {
		StringBuffer result = new StringBuffer();
		int i = 0;
		while (i < in.length()) {
			if (in.startsWith(t1, i)) {
				result.append(t2);
				if (t1.length() == 0) {
					result.append(in.charAt(i));
					i++;
				} else
					i += t1.length();
			} else {
				result.append(in.charAt(i));
				i++;
			}
		}
		return result.toString();
	}

	/**
	 * Return a string suitable as the upper limit for the set of strings which
	 * start with the argument.
	 * 
	 * @param start
	 * @return
	 */
	public static String getUpperLimit(String start) {
		return start + "\uFFFF";
		
	}

//	public static void main(String[] args) {
//		String t = "aadsfsdaafsdfdaaaqdfdsaaddd";
//		System.out.println(replace(t, "aa", " hello "));
//		System.out.println(replace(t, "aa", ""));
//		System.out.println(replace(t, "hello", "world"));
//		System.out.println(replace(t, "", " good "));
//	}
}
