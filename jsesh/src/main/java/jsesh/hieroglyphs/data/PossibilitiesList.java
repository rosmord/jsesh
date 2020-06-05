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
  Created on 14 nov. 2004
 */
package jsesh.hieroglyphs.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import jsesh.mdc.model.TopItemList;

/**
 * A list of possible signs for a given phonetic phonCode. Created and updated
 * only by the hieroglyphsManager. In each list, a sign is selected as the
 * current preferred sign.
 * 
 * @author S. Rosmorduc
 * 
 */
public class PossibilitiesList {
	private String phonCode;
	private List<Possibility> signs;
	int pos;

	/**
	 * Create a possibility list for a given key
	 * 
	 * @param phonCode
	 *            : a phonetic code for signs or texts.
	 */
	public PossibilitiesList(String phonCode) {
		this.phonCode = phonCode;
		this.signs = new ArrayList<Possibility>();
		pos = 0;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param possibilitiesList
	 */
	public PossibilitiesList(PossibilitiesList possibilitiesList) {
		this(possibilitiesList.getPhonCode());
		signs.addAll(possibilitiesList.signs);
	}

	/**
	 * Add a code in the list, but only if it isn't already there.
	 * @param gardinerCode
	 */
	public void addSign(String gardinerCode) {
		Possibility newPossibility = Possibility
				.createSignPossibility(gardinerCode);
		if (!signs.contains(newPossibility))
			signs.add(newPossibility);
	}

	public void addMdCText(String mdcText) {
		signs.add(Possibility.createMdCPossibility(mdcText));
	}

	void add(Possibility possibility) {
		if(! signs.contains(possibility))
			signs.add(possibility);
	}

	/**
	 * Select the next possible sign.
	 */
	public void next() {
		if (signs.size() != 0)
			pos = (pos + 1) % signs.size();
		else
			pos = 0;
	}

	public Possibility getCurrentSign() {
		return signs.get(pos);
	}

	/**
	 * @return Returns the phonCode.
	 */
	public String getPhonCode() {
		return phonCode;
	}

	/**
	 * Returns the possibilityList as an immutable list of codes.
	 * 
	 * @return a list of Strings.
	 */
	public List<Possibility> asList() {
		return Collections.unmodifiableList(signs);
	}

	public boolean isEmpty() {
		return signs.isEmpty();
	}

	/**
	 * Create a new possibility list which combines this and p1. this and p1 are
	 * left unchanged.
	 * 
	 * @param p1
	 */
	public PossibilitiesList add(PossibilitiesList p1) {
		PossibilitiesList result = new PossibilitiesList(this);
		HashSet<Possibility> p1Content = new HashSet<Possibility>(p1.asList());
		for (Possibility o : p1.asList()) {			
				result.add(o);
		}
		return result;
	}
}
