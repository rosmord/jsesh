package jsesh.test.rtfReaderTest;

import java.io.IOException;
import java.io.InputStream;

import jsesh.io.importer.rtf.RTFReader;

public class RTFReaderTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		InputStream in = RTFLexerTest.class.getResourceAsStream("test.rtf");
		RTFReader reader= new RTFReader(in);
		
		System.out.println(reader.getMDC());
	}

}
