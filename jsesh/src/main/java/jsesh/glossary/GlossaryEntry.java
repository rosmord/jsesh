package jsesh.glossary;

import jsesh.mdc.model.TopItemList;

/**
 * A glossary entry.
 * <p>Glossary entries have a key, which is a sequence of ascii chars (normally MdC transliteration codes),
 * without space, and a value, which is hieroglyphic text.
 * <p> Note that more than one entry may share the same key.   
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class GlossaryEntry {
	private String key;
	private TopItemList text;
	
	public String getKey() {
		return key;
	}
	
	public TopItemList getText() {
		return text;
	}
	
	public void setText(TopItemList text) {
		this.text = text;
	}
	
	
}
