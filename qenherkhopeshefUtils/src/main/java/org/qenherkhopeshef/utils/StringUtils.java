package org.qenherkhopeshef.utils;

import java.util.function.Consumer;

/**
 * Utility class for string manipulation.
 */

public class StringUtils {
    private StringUtils() {        
    }    

    /** 
     * Checks that a string is not empty in any sense, not null, not empty, not only spaces.
     * @param s the string to check
     * @return true if the string is not empty, false otherwise.
     */
    public static boolean isNotEmpty(String s) {
        return s != null && ! "".equals(s.trim());
    }


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



    /**
     * Performs an action on a string iff it's not empty.
     * <p> The action will only be performed if:
     * <ul>
     * <li> the string is not null
     * <li> it's not empty
     * <li> it contains non space characters.
     * </ul>
     * The consumer will be called with <code>s.trim()</code> as argument.
     * @param s a string
     * @param consumer an action on the string
     */
    public static void doIfNotEmpty(String s, Consumer<String> consumer) {
        if (s != null) {
            String trimmed = s.trim();
            if (! "".equals(trimmed)) {
                consumer.accept(trimmed);
            }
        }
    }

}
