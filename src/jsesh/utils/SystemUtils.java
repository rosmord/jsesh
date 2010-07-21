package jsesh.utils;

/**
 * Various utility methods.
 * @author rosmord
 *
 */
public class SystemUtils {
	/**
	 * Return the system default text encoding.
	 * The jdk 1.5 has a method for this, not jdk 1.4. Hence this method. 
	 * @return the system default text encoding.
	 */
	public static String getDefaultEncoding() {
		return System.getProperty("file.encoding");
	}

}
