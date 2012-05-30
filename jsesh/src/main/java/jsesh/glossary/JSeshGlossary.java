package jsesh.glossary;

import java.util.ArrayList;
import java.util.TreeMap;

import jsesh.mdc.model.TopItemList;
import jsesh.mdc.utils.MDCTranslitterationComparator;

public class JSeshGlossary {

	private TreeMap<String, ArrayList<GlossaryEntry>> map;
	
	public JSeshGlossary() {
		map= new TreeMap<String, ArrayList<GlossaryEntry>>(new MDCTranslitterationComparator());
	}
	
	/**
	 * Insert a new text in the glossary.
	 * @param key an MdC transliteration used as key (no space allowed, as it won't be possible to retrieve the text).
	 * @param text an mdc text (the glossary will have its own copy of it).
	 */
	public void add(String key, TopItemList text) {
		text= text.deepCopy();
	}
}
