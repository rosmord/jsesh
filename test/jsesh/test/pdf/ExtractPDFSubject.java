package jsesh.test.pdf;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.lowagie.text.pdf.PdfReader;


/**
 * Test Extraction of information from a PDF file.
 * Valuable to get data from Cut and Paste.
 * @author rosmord
 *
 */
public class ExtractPDFSubject {
	public static void main(String[] args) throws IOException {
		PdfReader reader = new PdfReader("default.pdf");
		System.out.println(reader);
		HashMap info = reader.getInfo();
		System.out.println(info.get("Subject"));
		Iterator iter = info.entrySet().iterator();
		
		
		
		while (iter.hasNext()) {
			Map.Entry next = (Entry) iter.next();
			
			System.out.println("Key "+ next.getKey());
			System.out.println("Value "+ next.getValue());

		}
		System.out.println(info.get("Subject"));
		reader.close();
	}
}
