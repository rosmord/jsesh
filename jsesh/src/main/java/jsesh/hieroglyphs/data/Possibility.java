/*
 Copyright Serge Rosmorduc
 contributor(s) : Serge J. P. Thomas for the fonts
 serge.rosmorduc@qenherkhopeshef.org

 This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

 This software is governed by the CeCILL license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.hieroglyphs.data;

import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.TopItemList;

/**
 * A candidate text in the possibility list.
 * <p>
 * A choice in the possibility list may be two things:
 * <ul>
 * <li>a single sign (a simple code)
 * <li>a word, coming from the glossary at time being.
 * </ul>
 * Hence this class, to encapsulate both cases. Use of inheritance here
 * would probably be overkill.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public final class Possibility {
	private String code;
	private TopItemList topItemList;
	private boolean singleSign;

	
	/**
	 * Create a possibility representing a single sign.
	 * @param signCode
	 * @return
	 */
	public static Possibility createSignPossibility(String signCode) {
		return new Possibility(signCode, true);
	}
	
	/**
	 * Create a possibility representing a manuel de codage text.
	 * @param mdcText
	 * @return
	 */
	public static Possibility createMdCPossibility(String mdcText) {
		return new Possibility(mdcText, false);
	}
	
	private Possibility(String code, boolean singleSign) {
		this.code= code;
		this.singleSign= singleSign;
		this.topItemList= null;
		
	}
	private Possibility(String signCode) {
		if (signCode == null)
			throw new NullPointerException();
		this.code = signCode;
	}

	/**
	 * Create a possibility corresponding to some text.
	 * 
	 * @param topItems
	 *            a list of items (will be defensively copied).
	 */
	private Possibility(TopItemList topItems) {
		if (topItems == null) {
			throw new NullPointerException();
		}
		this.topItemList = topItems.deepCopy();
	}

	public boolean isSingleSign() {
		return singleSign;
	}

	/**
	 * Returns the sign code, if defined (or, alternatively, the manuel de codage code).
	 * 
	 * @return a sign code
	 */
	public String getCode() {
		if (!isSingleSign())
			throw new NullPointerException();
		return code;
	}

	/**
	 * Returns the text, if defined.
	 * 
	 * @return a text
	 * @throw {@link NullPointerException} if {@link #isSingleSign()}
	 *        returns true.
	 */
	public TopItemList getTopItemList() {
		if (topItemList == null) {
			MDCParserModelGenerator gen= new MDCParserModelGenerator();
			try {
				topItemList= gen.parse(code);
			} catch (MDCSyntaxError e) {
				e.printStackTrace();				
			}
		}
		return topItemList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + (singleSign ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Possibility))
			return false;
		Possibility other = (Possibility) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (singleSign != other.singleSign)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return code;
	}

    public boolean hasCode(String code) {
       return this.code.equals(code);
    }
	
}