package jsesh.io.document;

import java.util.HashMap;

import jsesh.model.constants.JSeshInfoConstants;
import jsesh.utils.datatypes.StringBufferUtils;
import jsesh.document.MDCDocument;

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
			propertyMap.put(property, valueOf(line, tab));
		}
		document.setDocumentPreferences(DocumentHeaderCodec.fromHeader(propertyMap));
		document.setEmbeddedSigns(EmbeddedSignsHeaderCodec.fromHeader(propertyMap));
	}

	
	/// The value of a header line: `++JSesh_xxx value +s`.
	///
	/// The value is the text between the property name and the final ` +s`, so it
	/// can contain spaces (a page title for instance). A line without the final
	/// ` +s` gives its first word, as older versions did.
	private static String valueOf(String line, String[] tab) {
		int start = line.indexOf(' ');
		if (start >= 0 && line.endsWith(" +s") && line.length() - 3 >= start + 1) {
			return line.substring(start + 1, line.length() - 3);
		}
		return tab.length > 1 ? tab[1] : "";
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
