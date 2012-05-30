/*
 * Created on 25 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import jsesh.mdc.interfaces.OptionListInterface;

/**
 * Map of attributes given to a glyph or construct.
 * <p> An attribut has a name, and a value (a string or an integer).
 * <p> Note that the inner representation is not very good. We should have an "attribute value" class.
 * @author S. Rosmorduc
 * 
 */
public class OptionsMap implements OptionListInterface, Comparable<OptionsMap>,
		Serializable {

	
	private static final long serialVersionUID = -4388928774717325300L;
	
	TreeMap<String, Comparable<?>> map;

	public OptionsMap() {
		map = new TreeMap<String, Comparable<?>>();
	}

	/**
	 *  
	 */
	public void clear() {
		map.clear();
	}

	/**
	 * @param key
	 * @return true if the map contains key.
	 */
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	/*
	 * Returns all couples of options and values.
	 * 
	 * @return
	 */
	public Set<Entry<String, Comparable<?>>> entrySet() {
		return map.entrySet();
	}

	/**
	 * Define a boolean option.
	 * 
	 * @param name
	 * @param value
	 */
	public void setOption(String name, boolean value) {
		if (value) {
			map.put(name, null);
		} else {
			map.remove(name);
		}
	}

	public void setOption(String name, String value) {
		map.put(name, value);
	}

	public void setOption(String name, int value) {
		map.put(name, value);
	}

	/**
	 * Gets an option
	 * 
	 * @param name
	 * @return the option value.
	 */

	public Object get(String name) {
		return map.get(name);
	}

	/**
	 * Test if an option is defined.
	 * 
	 * @param option
	 * @return true if defined.
	 * 
	 */

	public boolean isDefined(String option) {
		return map.containsKey(option);
	}

	public String getString(String name, String defaultValue) {
		if (isDefined(name)) {
			return (String) (map.get(name));
		} else
			return defaultValue;
	}

	public OptionsMap deepCopy() {
		OptionsMap result = new OptionsMap();
		Set<Entry<String, Comparable<?>>> entries = map.entrySet();
		for (Iterator<Entry<String, Comparable<?>>> i = entries.iterator(); i.hasNext();) {
			Entry<String, Comparable<?>> e = i.next();
			result.map.put(e.getKey(), e.getValue());
		}
		return result;
	}

	
	public int compareTo(OptionsMap o1) {
		int result = 0;
		Iterator<Entry<String, Comparable<?>>> i = map.entrySet().iterator();
		Iterator<Entry<String, Comparable<?>>> j = o1.map.entrySet().iterator();
		while (result == 0 && i.hasNext() && j.hasNext()) {
			Entry<String, Comparable<?>> opt1 = i.next();
			Entry<String, Comparable<?>> opt2 = j.next();
			// Compare option names
			result = ((String) opt1.getKey()).compareTo((String) opt2.getKey());
			// Compare options values.
			if (result == 0) {
				result = ((String) opt1.getValue()).compareTo((String) opt2
						.getValue());
			}
		}
		if (result == 0) {
			int a = (i.hasNext() ? 1 : 0);
			int b = (j.hasNext() ? 1 : 0);
			result = a - b;
		}
		return result;
	}

	/**
	 * Returns the value of the said integer, or 0 if none.
	 * 
	 * @param string
	 * @return
	 */
	public int getInt(String string) {
		try {
			return (Integer) map.get(string);
		} catch (Exception e) {
			return 0;
		}
	}
}
