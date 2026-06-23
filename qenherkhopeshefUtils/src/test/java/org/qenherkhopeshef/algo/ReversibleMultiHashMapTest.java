package org.qenherkhopeshef.algo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class ReversibleMultiHashMapTest {

    @Test
    public void putAndGetBasic() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        map.put("a", "x");
        assertTrue(map.get("a").contains("x"));
    }


    @Test
    public void getKeysForBasic() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        map.put("a", "x");
        assertTrue(map.getKeysFor("x").contains("a"));
    }

    @Test
    public void getReturnsEmptySetForMissingKey() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        Set<String> result = map.get("missing");
        assertAll(
            () -> assertNotNull(result),
            () -> assertTrue(result.isEmpty())
        );
    }


    @Test
    public void getKeysForEmptyForMissingValue() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        Set<String> result = map.getKeysFor("missing");
        assertAll(
            () -> assertNotNull(result),
            () -> assertTrue(result.isEmpty())
        );        
    }

    @Test
    public void multipleValuesPerKey() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        map.put("a", "x");
        map.put("a", "y");
        Set<String> values = map.get("a");
        assertEquals(Set.of("x", "y"), values);        
    }

    @Test
    public void multipleKeysPerValue() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        map.put("a", "x");
        map.put("b", "x");
        Set<String> keys = map.getKeysFor("x");
        assertEquals(Set.of("a", "b"), keys);        
    }

    @Test
    public void removeValue() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        map.put("a", "x");
        map.put("b", "x");
        map.removeValue("x");
        assertTrue(map.get("a").isEmpty());
        assertTrue(map.get("b").isEmpty());
        assertTrue(map.getKeysFor("x").isEmpty());
    }

    @Test
    public void removeValueCleansEmptyKey() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        map.put("a", "x");
        map.removeValue("x");
        assertFalse(map.keySet().contains("a"));
    }

    @Test
    public void removeKey() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        map.put("a", "x");
        map.put("a", "y");
        map.removeKey("a");
        assertAll(
            () -> assertFalse(map.keySet().contains("a")),
            () -> assertTrue(map.get("a").isEmpty()),
            () -> assertTrue(map.getKeysFor("x").isEmpty()),
            () -> assertTrue(map.getKeysFor("y").isEmpty())
        );        
    }

    @Test
    public void removeKeyCleansEmptyValue() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        map.put("a", "x");
        map.removeKey("a");
        assertFalse(map.keySet().contains("a"));
        assertTrue(map.getKeysFor("x").isEmpty());
    }

    @Test
    public void removeKeyKeepsOtherKeysForSharedValue() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        map.put("a", "x");
        map.put("b", "x");
        map.removeKey("a");
        assertTrue(map.getKeysFor("x").contains("b"));
        assertFalse(map.getKeysFor("x").contains("a"));
    }

    @Test
    public void clear() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        map.put("a", "x");
        map.put("b", "y");
        map.clear();
        assertTrue(map.get("a").isEmpty());
        assertTrue(map.get("b").isEmpty());
        assertTrue(map.getKeysFor("x").isEmpty());
        assertTrue(map.keySet().isEmpty());
    }

    @Test
    public void keySet() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        map.put("a", "x");
        map.put("b", "y");
        Set<String> keys = map.keySet();
        assertTrue(keys.contains("a"));
        assertTrue(keys.contains("b"));
        assertEquals(2, keys.size());
    }

    @Test
    public void putKeysForValue() {
        ReversibleMultiHashMap<String, String> map = new ReversibleMultiHashMap<>();
        Set<String> keys = Set.of("k1", "k2", "k3");
        map.putKeysForValue(keys, "v");
        for (String k : keys) {
            assertTrue(map.get(k).contains("v"), "expected key " + k + " to map to v");
        }
        assertEquals(keys, map.getKeysFor("v"));
    }
}
