/*
 * Created on 21 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdc.lex;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import java_cup.runtime.Symbol;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.ParserErrorManager;

/**
 * Lexical analyser for manuel de codage files.
 * @author rosmord
 *
 */

// Implementation note : the actual analyser is MDCLexAux. This one is a wrapper, which allows us 
// in particular to have a cleaner treatment of white spaces.

public class MDCLex implements java_cup.runtime.Scanner, ParserErrorManager {

        /**
         *
         */
    
	private MDCLexAux implementation;
	/**
	 * The last token read by the lexer.
	 * May be null.
	 */
	private Symbol lastToken;
	
	/**
	 * @param in
	 */
	public MDCLex(Reader in) {
		implementation= new MDCLexAux(in);
		setDebug(false);
	}

	/**
	 * @param in
	 */
	public MDCLex(InputStream in) {
		implementation= new MDCLexAux(in);
	}

	public  static void main(String argv[]) throws java.io.IOException {
		   MDCLex yy = new MDCLex(System.in);
		   java_cup.runtime.Symbol t;
		   while ((t = yy.next_token()).sym != MDCSymbols.EOF)
			   System.out.println(t);
	   }

	/* (non-Javadoc)
	 * @see java_cup.runtime.Scanner#next_token()
	 */
	public Symbol next_token() throws IOException {
		
		// Get the next symbol
		lastToken= implementation.next_token();
		// see if spaces are now meaningful.
		implementation.fixExpect(lastToken);
		return lastToken;
	}

	

	/* (non-Javadoc)
	 * @see jsesh.mdc.utils.ParserErrorManager#buildError(java.lang.String)
	 */
	public MDCSyntaxError buildError(String message) {
		return implementation.buildError(message, lastToken.toString());
	}

	/**
	 * @return true if we are debugging.
	 */
	public boolean getDebug() {
		return implementation.getDebug();
	}

	/**
	 * @return if philological markers are simple signs.
	 */
	public boolean getPhilologyAsSigns() {
		return implementation.getPhilologyAsSigns();
	}

	/**
	 * 
	 */
	public void reset() {
		implementation.reset();
	}

	/**
	 * @param d
	 */
	public void setDebug(boolean d) {
		implementation.setDebug(d);
	}

	/**
	 * @param p
	 */
	public void setPhilologyAsSigns(boolean p) {
		implementation.setPhilologyAsSigns(p);
	}

}
