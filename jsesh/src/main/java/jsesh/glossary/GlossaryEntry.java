package jsesh.glossary;

import java.util.ArrayList;
import java.util.List;

import jsesh.mdc.model.TopItem;
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
	private List<TopItem> topItems;

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
	public GlossaryEntry(String key, List<TopItem> items) {
		super();
		if (!isCorrectKey(key))
			throw new BadGlossaryEntryException();
		this.key = key;
		this.topItems = new ArrayList<TopItem>();
		for (TopItem item: items) {
			this.topItems.add(item.deepCopy());
		}
	}

	public GlossaryEntry(String key, TopItemList items) {
		super();
		if (!isCorrectKey(key))
			throw new BadGlossaryEntryException();
		this.key = key;
		this.topItems = items.getTopItemListBetween(0, topItems.size());
	}

	public String getKey() {
		return key;
	}

	public List<TopItem> getTopItems() {
		return topItems;
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
