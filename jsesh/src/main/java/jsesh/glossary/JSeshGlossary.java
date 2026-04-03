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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import jsesh.mdc.translitteration.MDCTranslitterationComparator;
import static jsesh.mdc.translitteration.TransliterationUtilities.removeHyphens;

public class JSeshGlossary implements Iterable<GlossaryEntry> {

	private final EventSupport eventSupport = new EventSupport();

	private GlossaryMap map = new GlossaryMap();

	/**
	 * A list of entries, used to present a list-like representation of the
	 * glossary for editing purposes. Built (and rebuilt) on demand, as
	 * glossaries won't be very large.
	 */
	private List<GlossaryEntry> entryList = null;

	public JSeshGlossary() {
		map = new GlossaryMap();
	}

	/**
	 * Insert a new text in the glossary.
	 * 
	 * @param key
	 *             an MdC transliteration used as key (no space allowed, as it
	 *             won't be possible to retrieve the text).
	 * @param mdc a mdc text (the glossary will have its own copy of it).
	 */
	public void add(String key, String mdc) {
		GlossaryEntry entry = new GlossaryEntry(key, mdc);
		if (map.add(entry)) {
			eventSupport.fireEvent(new GlossaryEntryAdded(this, entry));
			invalidateList();
		}
	}

	public void remove(GlossaryEntry entry) {
		if (map.remove(entry)) {
			eventSupport.fireEvent(new GlossaryEntryRemoved(this, entry));
			invalidateList();

		}
	}

	public List<GlossaryEntry> get(String key) {
		List<GlossaryEntry> result = map.get(key);
		if (result == null)
			return Collections.emptyList();
		else
			return result;
	}


	/**
	 * @param eventClass
	 * @param object
	 * @param methodName
	 * @see jsesh.glossary.EventSupport#addEventLink(java.lang.Class,
	 *      java.lang.Object, java.lang.String)
	 */
	public void addEventLink(Class<? extends EventObject> eventClass,
			Object object, String methodName) {
		eventSupport.addEventLink(eventClass, object, methodName);
	}

	/**
	 * @param eventClass
	 * @param object
	 * @param methodName
	 * @see jsesh.glossary.EventSupport#removeEventLink(java.lang.Class,
	 *      java.lang.Object, java.lang.String)
	 */
	public void removeEventLink(Class<? extends EventObject> eventClass,
			Object object, String methodName) {
		eventSupport.removeEventLink(eventClass, object, methodName);
	}

	public int getNumberOfEntries() {
		return getEntryList().size();
	}

        @Override
	public Iterator<GlossaryEntry> iterator() {
		return getEntryList().iterator();
	}

	/**
	 * Returns the entry in position "pos" in the list of entries.
	 * <p>
	 * Note that the position is not a reliable identifier for an entry (as new
	 * entries might be inserted later).
	 * 
	 * @param pos
	 * @return
	 */
	public GlossaryEntry getEntry(int pos) {
		return getEntryList().get(pos);
	}

	private List<GlossaryEntry> getEntryList() {
		if (entryList == null) {
			entryList = map.allEntries();
		}
		return entryList;
	}

	/**
	 * Method to be called by anything which changes the list of entries.
	 */
	private void invalidateList() {
		entryList = null;
	}

	

	/**
	 * Low level class to encapsulate access to the glossary internals.
	 * 
	 * The keys used are always simplified keys.
	 */
	private static class GlossaryMap {
		

		/**
		 * The map actually used when searching for entries.
		 * 
		 * We could replace list with a LinkedHashSet.
		 */

		private final TreeMap<String, ArrayList<GlossaryEntry>> map;

		public GlossaryMap() {
			map = new TreeMap<>(
					new MDCTranslitterationComparator());
		}

		public List<GlossaryEntry> get(String key) {
			return map.get(removeHyphens(key));
		}

		/**
		 * Attempts to remove an entry.
		 * @param entry
		 * @return true iff the glossary was modified (i.e. if the entry was actually removed).
		 */
		public boolean remove(GlossaryEntry entry) {
			String simplifiedKey = removeHyphens(entry.getKey());
			ArrayList<GlossaryEntry> l = map.get(simplifiedKey);
			if (l != null) {
				boolean removed = l.remove(entry);
				if (l.isEmpty()) {
					map.remove(simplifiedKey);
				}
				return removed;
			} else {
				return false;
			}
		}

		/**
		 * Add a new entry in the glossary.
		 * If the glossary was not modified, returns false. Otherwise, returns true.
		 * 
		 * @param entry
		 * @return true iff the glossary was modified.
		 */
		public boolean add(GlossaryEntry entry) {
			String simplifiedKey = removeHyphens(entry.getKey());
			if (!map.containsKey(simplifiedKey)) {
				map.put(simplifiedKey, new ArrayList<>());
			}
			ArrayList<GlossaryEntry> list = map.get(simplifiedKey);
			if (list.contains(entry)) {
				return false;
			} else {
				list.add(entry);
				return true;
			}
		}

		public List<GlossaryEntry> allEntries() {
			return map.values().stream().flatMap(list -> list.stream()).toList();
		}
	}

}
