package jsesh.test.rtfReaderTest;

import java.io.IOException;
import java.io.InputStream;

import jsesh.io.importer.rtf.RTFLexer;
import jsesh.io.importer.rtf.RTFTokenType;

public class RTFLexerTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
	
		InputStream in = RTFReaderTest.class.getResourceAsStream("test.rtf");
		RTFLexer rtfLexer= new RTFLexer(in);
		rtfLexer.next();
		while (rtfLexer.getTokenType() != RTFTokenType.EOF) {
			System.out.println(rtfLexer.getTokenRepresentation());
			rtfLexer.next();
		}
	}

}
