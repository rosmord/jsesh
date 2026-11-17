package jsesh.mdc.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java_cup.runtime.Symbol;
import jsesh.mdc.interfaces.MDCBuilder;
import jsesh.mdc.lex.MDCLex;
import jsesh.tmp.simpleDemo.PrintMDCBuilder;

/**
 * ParserTest.java
 *
 *
 * Created: Sun Feb 24 15:15:16 2002
 *
 * @author Serge ROSMORDUC
 * @version
 */

public class ParserTest {
    
    public ParserTest() {
	
    }
    
    public static void main(String[] args) throws IOException {
	InputStream in;
	// Create a tokenizer
	if (args.length == 0)
	    in= System.in;
	else
	    in= new FileInputStream(args[0]);
	
	MDCLex tokenizer= new MDCLex(in);
	// Create a parser
	MDCParse parser= new MDCParse(tokenizer);
	// Or : parser.setScanner(tokenizer);
	
	MDCBuilder s= new PrintMDCBuilder();
	parser.setBuilder(s);
	/* open input files, etc. here */
	Symbol parse_tree = null;
	
	try {
	    parse_tree = parser.debug_parse();
	    System.out.println(parse_tree.value);
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    // Close :
	}
    }
    
} // ParserTest
