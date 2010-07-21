package jsesh.utils;

public class StringBufferUtils {

	/**
	 * returns the index of the next line. Helper method (to move in an utility
	 * class)
	 * 
	 * If there is no next line, returns buffer's length.
	 * 
	 * @param buffer
	 * @param currentPos
	 */
	public static int nextLineIndex(StringBuffer buff, int currentPos) {
		int pos = currentPos;
		boolean inFirstLine = true;
		while (pos < buff.length() && inFirstLine) {
			char c = buff.charAt(pos);
			// try \r\n, \r, and \n
			if (c == '\r') {
				pos++;
				if (pos < buff.length() && buff.charAt(pos) == '\n') {
					pos++;
				}
				inFirstLine = false;
			} else if (c == '\n') {
				pos++;
				inFirstLine = false;
			} else
				pos++;
		}
		return pos;
	}

	/**
	 * Method for working in ASCII.
	 * 
	 * @param bytes
	 * @param firstByte
	 * @param string
	 * @return
	 */
	public static boolean startsWith(StringBuffer buff, int currentPos,
			String string) {
		boolean result = true;
	
		// Compare buffer and string
	
		int i = currentPos;
		int j = 0;
		while (result && i < buff.length() && j < string.length()) {
			if (buff.charAt(i) != string.charAt(j))
				result = false;
			else {
				i++; j++;
			}
		}
	
		return result && j == string.length();
	}

}
