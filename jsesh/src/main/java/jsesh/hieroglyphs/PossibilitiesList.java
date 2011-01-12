/*
 * Created on 14 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.hieroglyphs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A list of possible signs for a given phonetic phonCode.
 * Created and updated only by the hieroglyphsManager.
 * In each list, a sign is selected as the current prefered sign.
 * @author S. Rosmorduc
 *
 */
public class PossibilitiesList {
	private String phonCode;
	private List<String> signs;
	int pos;
	
	PossibilitiesList(String phonCode) {
		this.phonCode = phonCode;
		this.signs= new ArrayList<String>();
		pos= 0;
	}

	/**
	 * Copy constructor.
	 * @param possibilitiesList
	 */
	public PossibilitiesList(PossibilitiesList possibilitiesList) {
		this(possibilitiesList.getPhonCode());
		signs.addAll(possibilitiesList.signs);
	}

	public void add(String gardinerCode) {
		if (! signs.contains(gardinerCode))
			signs.add(gardinerCode);
	}
	
	/**
	 * Select the next possible sign.
	 */
	public void next() {
		if (signs.size() != 0)
			pos= (pos + 1) % signs.size();
		else
			pos= 0;
	}
	
	public String getCurrentSign() {
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
	 * @return a list of Strings.
	 */
	public List<String> asList() {
		return Collections.unmodifiableList(signs);
	}

	public boolean isEmpty() {
		return signs.isEmpty();
	}

	/**
	 * Create a new possibility list which combines this and p1.
	 * this and p1 are left unchanged.
	 * @param p1
	 */
	public PossibilitiesList add(PossibilitiesList p1) {
		PossibilitiesList result= new PossibilitiesList(this);
		for (Object o : p1.asList()) {
			result.add((String) o);
		}
		return result;
	}
}
