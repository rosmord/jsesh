package jsesh.mdc.jseshInfo;

import java.util.HashMap;

import jsesh.mdc.constants.JSeshInfoConstants;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.file.DocumentPreferences;
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

		HashMap<String, String> propertyMap = new HashMap<String, String>();
		while (StringBufferUtils.startsWith(buffer, pos, "++"
				+ JSeshInfoConstants.JSESH_INFO_PREFIX)) {
			String line = readLine();
			String[] tab = line.split(" ");
			String property = tab[0].substring(2);
			String value = tab.length > 1 ? tab[1] : "";
			propertyMap.put(property, value);
		}
		document.setDocumentPreferences(DocumentPreferences.fromStringMap(propertyMap));
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
