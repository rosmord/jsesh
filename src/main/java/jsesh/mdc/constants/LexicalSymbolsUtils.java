/*
 * Created on 25 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.constants;

import java.util.HashMap;

/**
 * @author S. Rosmorduc
 *  
 */
public class LexicalSymbolsUtils {

	static private String[] codesForSymbols = null;
	static private HashMap integerCodesForString= new HashMap();
	
	static {
		codesForSymbols = new String[256];
		codesForSymbols[SymbolCodes.HALFSPACE] = ".";
		codesForSymbols[SymbolCodes.FULLSPACE] = "..";
		codesForSymbols[SymbolCodes.REDPOINT] = "o";
		codesForSymbols[SymbolCodes.BLACKPOINT] = "O";
		codesForSymbols[SymbolCodes.FULLSHADE] = "//";
		codesForSymbols[SymbolCodes.VERTICALSHADE] = "v/";
		codesForSymbols[SymbolCodes.HORIZONTALSHADE] = "h/";
		codesForSymbols[SymbolCodes.QUATERSHADE] = "/";
		codesForSymbols[SymbolCodes.BEGINERASE] = "[[";
		codesForSymbols[SymbolCodes.ENDERASE] = "]]";
		codesForSymbols[SymbolCodes.BEGINEDITORADDITION] = "[&";
		codesForSymbols[SymbolCodes.ENDEDITORADDITION] = "&]";
		codesForSymbols[SymbolCodes.BEGINMINORADDITION] = "[(";
		codesForSymbols[SymbolCodes.ENDMINORADDITION] = ")]";	
		codesForSymbols[SymbolCodes.BEGINDUBIOUS] = "[?";
		codesForSymbols[SymbolCodes.ENDDUBIOUS] = "?]";	
		codesForSymbols[SymbolCodes.BEGINEDITORSUPERFLUOUS] = "[{";
		codesForSymbols[SymbolCodes.ENDEDITORSUPERFLUOUS] = "}]";
		codesForSymbols[SymbolCodes.BEGINPREVIOUSLYREADABLE] = "[\"";
		codesForSymbols[SymbolCodes.ENDPREVIOUSLYREADABLE] = "\"]";
		codesForSymbols[SymbolCodes.BEGINSCRIBEADDITION] = "['";
		codesForSymbols[SymbolCodes.ENDSCRIBEADDITION] = "']";
		codesForSymbols[SymbolCodes.SMALLTEXT]= "\"\"";
		
		for (int i= 0; i < 256; i++) {
			if (codesForSymbols[i] != null)
				integerCodesForString.put(codesForSymbols[i], new Integer(i));
		}
	}

	private static String[] getCodesForSymbols() {
		return codesForSymbols;
	}

	/**
	 * Return the actual brackets represented by the manuel de codage code.
	 * 
	 * @param code :
	 *            the code for the bracket. Note that this can be computed (see
	 *            symbolcode).
	 * @return the string for a given Philological code.
	 */
	public static String getStringForPhilology(int code) {
		String result = "??";
		switch (code) {
		case SymbolCodes.BEGINERASE:
			result = "[";
			break;
		case SymbolCodes.ENDERASE:
			result = "]";
			break;
		case SymbolCodes.BEGINEDITORADDITION:
			result = "<";
			break;
		case SymbolCodes.ENDEDITORADDITION:
			result = ">";
			break;
		case SymbolCodes.BEGINEDITORSUPERFLUOUS:
			result = "{";
			break; // [{ }] => { }
		case SymbolCodes.ENDEDITORSUPERFLUOUS:
			result = "}";
			break; // [{ }] => { }
		case SymbolCodes.BEGINPREVIOUSLYREADABLE:
			result = "[|";
			break; // [" "] => [| |]
		case SymbolCodes.ENDPREVIOUSLYREADABLE:
			result = "|]";
			break; // [" "] => [| |]
		case SymbolCodes.BEGINSCRIBEADDITION:
			result = "'";
			break; // [' '] => ' '
		case SymbolCodes.ENDSCRIBEADDITION:
			result = "'";
			break; // [' '] => ' '
		}
		return result;

	}

	/**
	 * Return the Manuel de codage code for opening brackets.
	 * 
	 * @param code
	 * @return the opening construct fot a given philological parenthesis.
	 */
	public static String getOpenCodeForPhilology(int code) {
		String result = null;
		switch (code) {
		case SymbolCodes.ERASEDSIGNS:
			result = "[[";
			break;
		case SymbolCodes.EDITORADDITION:
			result = "[&";
			break;
		case SymbolCodes.EDITORSUPERFLUOUS:
			result = "[{";
			break; // [{ }] => { }
		case SymbolCodes.PREVIOUSLYREADABLE:
			result = "[\"";
			break; // [" "] => [| |]
		case SymbolCodes.SCRIBEADDITION:
			result = "['";
			break; // [' '] => ' '
		}
		return result;
	}

	/**
	 * Return the Manuel de codage code for closing brackets.
	 * 
	 * @param code
	 * @return the closing construct fot a given philological parenthesis.
	 */

	public static String getCloseCodeForPhilology(int code) {
		String result = null;
		switch (code) {
		case SymbolCodes.ERASEDSIGNS:
			result = "]]";
			break;
		case SymbolCodes.EDITORADDITION:
			result = "&]";
			break;
		case SymbolCodes.EDITORSUPERFLUOUS:
			result = "}]";
			break; // [{ }] => { }
		case SymbolCodes.PREVIOUSLYREADABLE:
			result = "\"]";
			break; // [" "] => [| |]
		case SymbolCodes.SCRIBEADDITION:
			result = "']";
			break; // [' '] => ' '
		}
		return result;
	}

	/**
	 * return the string for simple lexical item, like space, halfspace, red
	 * point, etc.
	 * 
	 * @param itemType :
	 *            the code from SymbolCodes
	 * @return a string
	 * @see SymbolCodes
	 */
	public static String getStringForLexicalItem(int itemType) {
		return getCodesForSymbols()[itemType];
	}
	
	/**
	 * Returns the code for a certain string.
	 * @param mdcString the MdC code for an editorial mark (like "[[").
	 */
	public static int getCodeForString(String mdcString) {
		if (integerCodesForString.containsKey(mdcString)) {
			return ((Integer)integerCodesForString.get(mdcString)).intValue();
		} else 
			return -1;
	}
}