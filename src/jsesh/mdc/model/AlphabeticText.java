package jsesh.mdc.model;

/**
 * AlphabeticText represents text written in an alphabetic system, as Coptic,
 * Greek, or Latin.
 * 
 * @author rosmord
 * 
 * This code is published under the GNU LGPL.
 */

public class AlphabeticText extends BasicItem {

	private char scriptCode;

	private String text;

	public AlphabeticText(char scriptCode, String text) {
		this.text = text;
		this.scriptCode = scriptCode;
	}

	public String getText() {
		return text;
	}

	public void setText(String _text) {
		text = _text;
		notifyModification();
	}

	public void accept(ModelElementVisitor v) {
		v.visitAlphabeticText(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(text " + text.toString() + ")";
	}

	/**
	 * returns a code which describes the script to use. codes are
	 * <ul>
	 * <li>'l' : latin
	 * <li>'b' : bold
	 * <li>'i' : italic
	 * <li>'t' : transliteration
	 * <li>'c' : coptic
	 * <li>'g' : greek
	 * <li>'h' : hebrew
	 * <li>'r' : cyrillic
	 * </ul>
	 * 
	 * @return
	 */
	public char getScriptCode() {
		return scriptCode;
	}

	/**
	 * @param c
	 */
	public void setScriptCode(char c) {
		scriptCode = c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		AlphabeticText at = (AlphabeticText) e;
		// Compare strings, then code letters.
		int result = getText().toString().compareTo(at.getText().toString());
		if (result == 0)
			result = getScriptCode() - at.getScriptCode();
		if (result == 0) {
			result = getState().compareTo(at.getState());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public ModelElement deepCopy() {
		// String are immutable ! no need to copy them !
		AlphabeticText t= new AlphabeticText(scriptCode, text);
		copyStateTo(t);
		return t;
	}
} // end AlphabeticText
