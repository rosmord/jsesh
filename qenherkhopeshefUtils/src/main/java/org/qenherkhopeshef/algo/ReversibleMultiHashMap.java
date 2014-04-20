package org.qenherkhopeshef.algo;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * An hashmap which maintains a reverse index from values to keys.
 * 
 * Note that in this hashmap, we have both multiple values for one key and
 * multiple keys for one value.
 * 
 * Important remark for developers : only put, clear and removeValue should
 * change this map. That is, a get for a non existing value will return an empty
 * set, but won't create a new cell.
 * 
 * In this map, keys without values and values without keys are automatically removed.
 * 
 * <p>(question to self : why is this thing serializable ?????)
 * @author Serge Rosmorduc
 * @version 20100417
 * 
 * @param <K>
 * @param <V>
 */
public class ReversibleMultiHashMap<K, V> implements Serializable {

	private static final long serialVersionUID = -1379144229315950937L;
	
	private final HashMap<K, HashSet<V>> map = new HashMap<K, HashSet<V>>();
	// The reverse index.
	private final HashMap<V, HashSet<K>> reverseMap = new HashMap<V, HashSet<K>>();

	public void clear() {
		map.clear();
		reverseMap.clear();
	}

	/**
	 * Add a mapping from key to value.
	 * <p>
	 * A key can be associated to various values, and a value to various keys
	 * (hence <em>add</em>, not <em>put</em>).
	 * 
	 * @param key
	 * @param value
	 */
	public void put(K key, V value) {
		// map
		getMapList(key).add(value);
		// reverse map
		getReverseMapList(value).add(key);
	};

	public Set<V> get(K key) {
		if (map.containsKey(key)) {
			return Collections.unmodifiableSet(map.get(key));
		} else {
			return Collections.emptySet();
		}
	}

	public Set<K> getKeysFor(V value) {
		if (reverseMap.containsKey(value)) {
			return Collections.unmodifiableSet(reverseMap.get(value));
		} else
			return Collections.emptySet();
	}

	public void removeValue(V value) {
		Set<K> keys = getKeysFor(value);
		for (K key : keys) {
			// Remove the value from the map.
			HashSet<V> valueSet = getMapList(key);
			valueSet.remove(value);
			if (valueSet.isEmpty()) {
				map.remove(key);
			}
		}
		reverseMap.remove(value);
	}

	public void removeKey(K key) {
		for (V v : getMapList(key)) {
			HashSet<K> keySet = getReverseMapList(v);
			keySet.remove(key);
			if (keySet.isEmpty())
				reverseMap.remove(v);
		}
		map.remove(key);
	}

	private HashSet<V> getMapList(K key) {
		if (!map.containsKey(key)) {
			map.put(key, new HashSet<V>());
		}
		return map.get(key);
	}

	private HashSet<K> getReverseMapList(V value) {
		if (!reverseMap.containsKey(value)) {
			reverseMap.put(value, new HashSet<K>());
		}
		return reverseMap.get(value);
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public void putKeysForValue(Set<K> keys, V value) {
		getReverseMapList(value).addAll(keys);
		for (K key : keys) {
			getMapList(key).add(value);
		}
	}
}
