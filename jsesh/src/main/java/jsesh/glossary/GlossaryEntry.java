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
package jsesh.glossary;

import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.TopItemList;

/**
 * A glossary entry.
 * <p>
 * Glossary entries have a key, which is a sequence of ascii chars (ascii
 * letters and numbers), without space, and a value, which is hieroglyphic
 * topItems.
 * <p>
 * Note that more than one entry may share the same key.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class GlossaryEntry {
	private String key;
	private String mdc;
	private TopItemList topItems;

	/**
	 * Create a new glossary entry.
	 * 
	 * @param key
	 *            the key (a sequence of ascii letters and digits.
	 * @param items
	 *            the hieroglyphic topItems.
	 * @throws BadGlossaryEntryException
	 *             if the key is not correct (better check before, with the
	 *             static method provided).
	 */
	public GlossaryEntry(String key, String mdc) {
		super();
		if (!isCorrectKey(key))
			throw new BadGlossaryEntryException();
		this.key = key;
		this.mdc= mdc;
	}


	public String getKey() {
		return key;
	}

	public TopItemList getTopItems() {
		if (topItems == null) {
			MDCParserModelGenerator parser = new MDCParserModelGenerator();
			try {
				topItems= parser.parse(mdc);				
			} catch (MDCSyntaxError e) {
				System.out.println("error reading "+ mdc);
				topItems= new TopItemList();
				AlphabeticText text= new AlphabeticText('l', "error reading : "+mdc);
				topItems.addTopItem(text);				
			}
		}
		return this.topItems;
	}

	public String getMdc() {
		return mdc;
	}
	/**
	 * Method to check a key before element creation.
	 * 
	 * @param key
	 * @return true if the key is [a-zA-Z0-9]+
	 */
	public static boolean isCorrectKey(String key) {
		return key.matches("^[a-zA-Z0-9]+$");
	}

}
