/*
 * Created on 14 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.hieroglyphs;

import java.util.ArrayList;
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
	private List signs;
	int pos;
	
	PossibilitiesList(String phonCode) {
		this.phonCode = phonCode;
		this.signs= new ArrayList();
		pos= 0;
	}

	void add(String gardinerCode) {
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
		return (String)signs.get(pos);
	}
}
