package jsesh.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

public class TclImporter {
	private StreamTokenizer tok;

	/**
	 * Reads a Tcl list from a file, and build the corresponding Java list.
	 * any token is rendered as a String, except numeric data which are rendered as Doubles.
	 * @param r a tcl list.	  
	 * @return a java list.
	 * @throws IOException
	 */
	public List parseTclList(Reader r) throws IOException {
		List result;
		tok = new StreamTokenizer(r);
		tok.nextToken();
		result = parseTclListAux();
		if (tok.ttype != StreamTokenizer.TT_EOF)
			throw new IOException("Bad Tcl list");
		;
		tok = null;
		return result;
	}

	private List parseTclListAux() throws IOException {
		ArrayList result = new ArrayList();
		boolean ended = false;
		while (!ended) {
			switch (tok.ttype) {
			case StreamTokenizer.TT_WORD: {
				result.add(tok.sval);
				tok.nextToken();
			}
				break;
			case StreamTokenizer.TT_NUMBER: {
				result.add(tok.nval);
				tok.nextToken();
			}
				break;
			case '{':
				tok.nextToken();
				result.add(parseTclListAux());
				if (tok.ttype != '}')
					throw new IOException("'}' expected");
				tok.nextToken();
				break;
			case '}':
				ended = true;
				break;
			case StreamTokenizer.TT_EOF:
				ended = true;
				break;
			default:
				result.add("" + (char) tok.ttype);
				tok.nextToken();
				break;
			}
		}
		return result;
	}
	
//	public static void main(String[] args) throws IOException {
//		TclImporter importer= new TclImporter();
//		List result= importer.parseTclList(new StringReader("a b c {z z1} 3 {d e cf { 23.2 -1.4}} {h i}"));
//		System.out.println(result);
//		result= importer.parseTclList(new StringReader("a b c {d e cf { 23.2 -1.4} {h i"));
//		System.out.println(result);
//	}
}
