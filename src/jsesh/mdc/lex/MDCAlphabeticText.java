/*
 * Created on 5 oct. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdc.lex;

/**
 * Text in an alphabetic script.
 *   @author rosmord
 *
 */
public class MDCAlphabeticText
{
	private char scriptCode;
	private String text;
	
	public MDCAlphabeticText() {
		scriptCode= 'l';
		text="";
	}
	
	public MDCAlphabeticText(char scriptCode, String text) {
		this.scriptCode= scriptCode;
		this.text= text;
	}
	
	/**
	 * Returns a code that describe the script to use.
	 * Possible codes are 'l' for latin, 'b' for latin bold,
	 * 'i' for latin italic, '+' for comments,
	 * 't' for transliteration, 'c' for coptic, 'g' for 
	 * greek, 'r' for cyrillic, 'h' for hebrew.
	 * 
	 * @return the script code.
	 */
	public char getScriptCode()
	{
		return scriptCode;
	}

	/**
	 * @return the text.
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * @param c
	 */
	public void setScriptCode(char c)
	{
		scriptCode= c;
	}

	/**
	 * @param string
	 */
	public void setText(String string)
	{
		text= string;
	}

}
