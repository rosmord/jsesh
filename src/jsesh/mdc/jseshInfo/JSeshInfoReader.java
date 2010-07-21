package jsesh.mdc.jseshInfo;

import jsesh.mdc.constants.JSeshInfoConstants;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.file.MDCDocument;
import jsesh.utils.StringBufferUtils;

public class JSeshInfoReader {

	int pos = 0;
	StringBuffer buffer;
	MDCDocument document;

	public JSeshInfoReader() {

	}

	public void process(StringBuffer buffer, MDCDocument document) {
		this.buffer = buffer;
		this.document = document;
		// Line-by-line read of the text.

		while (StringBufferUtils.startsWith(buffer, pos, "++"
				+ JSeshInfoConstants.JSESH_INFO_PREFIX)) {
			String line = readLine();
			String[] tab = line.split(" ");
			String property = tab[0].substring(2);
			String value = tab.length > 1 ? tab[1] : "";
			processInfo(property, value);
		}
	}

	/**
	 * Read document related info
	 * 
	 * @param property
	 * @param value
	 */
	private void processInfo(String property, String value) {
		if (JSeshInfoConstants.JSESH_INFO_PREFIX.equals(property)) {
			// test if value is "1.0" ?
		} else if (JSeshInfoConstants.JSESH_PAGE_WIDTH.equals(property)) {

		} else if (JSeshInfoConstants.JSESH_PAGE_HEIGHT.equals(property)) {

		} else if (JSeshInfoConstants.JSESH_SMALL_SIGNS_CENTRED.equals(property)) {
			boolean centered= "1".equals(value);
			document.setSmallSignCentered(centered);
		}  else if (JSeshInfoConstants.JSESH_MAIN_ORIENTATION.equals(property)) {
			if (TextOrientation.VERTICAL.toString().equals(value))
				document.setMainOrientation(TextOrientation.VERTICAL);
			else
				document.setMainOrientation(TextOrientation.HORIZONTAL);
		} else if (JSeshInfoConstants.JSESH_MAIN_DIRECTION.equals(property)) {
			if (TextDirection.RIGHT_TO_LEFT.toString().equals(value)) {
				document.setMainDirection(TextDirection.RIGHT_TO_LEFT);
			} else {
				document.setMainDirection(TextDirection.LEFT_TO_RIGHT);
			}
		}
	}

	public int getNextPos() {
		return pos;
	}

	/**
	 * Reads the next line. Advances too.
	 * 
	 * @return the next line (minus the end of line stuff).
	 */
	private String readLine() {
		int lastPosInLine = pos;
		char c;
		while ((c = charAt(lastPosInLine)) != 0 && c != '\n' && c != '\r')
			lastPosInLine++;

		// The content of the line...
		String line = buffer.substring(pos, lastPosInLine);
		// Next position:
		// Now, we are at the end of line.
		// We need to get past \r\n, \n and \r
		switch (c) {
		case '\n':
			pos = lastPosInLine + 1;
			break;
		case '\r':
			if (charAt(lastPosInLine + 1) == '\n')
				pos = lastPosInLine + 2;
			else
				pos = lastPosInLine + 1;
			break;
		default:
			break;
		}
		return line;
	}

	private char charAt(int i) {
		if (i >= buffer.length())
			return 0;
		else
			return buffer.charAt(i);
	}
}
