/*
 * Created on 14 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.hieroglyphs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jsesh.mdc.model.TopItem;
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

	public void add(String gardinerCode) {
		Possibility newPossibility = new Possibility(gardinerCode);
		if (!signs.contains(newPossibility))
			signs.add(newPossibility);
	}

	public void add(TopItemList item) {
		signs.add(new Possibility(item));
	}

	void add(Possibility possibility) {
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
		for (Possibility o : p1.asList()) {
			result.add(o);
		}
		return result;
	}

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
	public static final class Possibility {
		private String code;
		private TopItemList topItemList;

		public Possibility(String signCode) {
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
		public Possibility(TopItemList topItems) {
			if (topItems == null) {
				throw new NullPointerException();
			}
			this.topItemList = topItems.deepCopy();
		}

		public boolean isSingleSign() {
			return code != null;
		}

		/**
		 * Returns the sign code, if defined.
		 * 
		 * @return a sign code
		 * @throws NullPointerException
		 *             if {@link #isSingleSign()} returns false.
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
			if (isSingleSign())
				throw new NullPointerException();
			return topItemList;
		}
	}
}
