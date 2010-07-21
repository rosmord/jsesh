/*
 * Created on 27 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jsesh.mdc.constants;

import jsesh.utils.EnumBase;

/**
 * @author rosmord
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WordEndingCode extends EnumBase {

	public static final WordEndingCode NONE= new WordEndingCode(0, "NONE");
	public static final WordEndingCode WORD_END= new WordEndingCode(1, "WORD_END");
	public static final WordEndingCode SENTENCE_END= new WordEndingCode(2, "SENTENCE_END");
	
	/**
	 * @param id
	 * @param designation
	 */
	private WordEndingCode(int id, String designation) {
		super(id, designation);
	}

	
}
