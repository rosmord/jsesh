/*
 * Created on 25 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import jsesh.mdc.interfaces.OptionListInterface;

/**
 * @author S. Rosmorduc
 * 
 */
public class OptionsMap implements OptionListInterface, Comparable,
		Serializable {
	TreeMap map;

	public OptionsMap() {
		map = new TreeMap();
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
	public Set entrySet() {
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
		Set entries = map.entrySet();
		for (Iterator i = entries.iterator(); i.hasNext();) {
			Map.Entry e = (Entry) i.next();
			result.map.put(e.getKey(), e.getValue());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		OptionsMap o1 = (OptionsMap) o;
		int result = 0;
		TreeSet s;
		Iterator i = map.entrySet().iterator();
		Iterator j = o1.map.entrySet().iterator();
		while (result == 0 && i.hasNext() && j.hasNext()) {
			Map.Entry opt1 = (Entry) i.next();
			Map.Entry opt2 = (Entry) j.next();
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
