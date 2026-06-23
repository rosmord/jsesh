package org.qenherkhopeshef.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class StringUtilsTest {

    @Test
    public void isNotEmptyReturnsFalseForNull() {
        assertFalse(StringUtils.isNotEmpty(null));
    }

    @Test
    public void isNotEmptyReturnsFalseForEmptyString() {
        assertFalse(StringUtils.isNotEmpty(""));
    }

    @Test
    public void isNotEmptyReturnsFalseForBlankString() {
        assertFalse(StringUtils.isNotEmpty("   "));
    }

    @Test
    public void isNotEmptyReturnsTrueForNonEmpty() {
        assertTrue(StringUtils.isNotEmpty("hello"));
    }

    @Test
    public void isNotEmptyReturnsTrueForStringWithSpaces() {
        assertTrue(StringUtils.isNotEmpty("  a  "));
    }

    @Test
    public void replaceSimple() {
        assertEquals("hello Java", StringUtils.replace("hello world", "world", "Java"));
    }

    @Test
    public void replaceAll() {
        assertEquals("bbb", StringUtils.replace("aaa", "a", "b"));
    }

    @Test
    public void replaceNoMatch() {
        assertEquals("hello", StringUtils.replace("hello", "x", "y"));
    }

    @Test
    public void replaceEmptyPattern() {
        // empty t1: inserts replacement before each character
        assertEquals("XaXb", StringUtils.replace("ab", "", "X"));
    }

    @Test
    public void replaceMultiCharPattern() {
        assertEquals("hi world", StringUtils.replace("hello world", "hello", "hi"));
    }

    @Test
    public void getUpperLimitStartsWithInput() {
        String limit = StringUtils.getUpperLimit("abc");
        assertTrue(limit.startsWith("abc"));
    }

    @Test
    public void getUpperLimitEndsWithMaxChar() {
        String limit = StringUtils.getUpperLimit("abc");
        assertEquals('￿', limit.charAt(limit.length() - 1));
    }

    @Test
    public void getUpperLimitSortsAfterAllStartingStrings() {
        String prefix = "abc";
        String limit = StringUtils.getUpperLimit(prefix);
        assertTrue((prefix + "zzzzz").compareTo(limit) < 0);
    }

    @Test
    public void doIfNotEmptyCallsConsumer() {
        List<String> received = new ArrayList<>();
        StringUtils.doIfNotEmpty("hello", received::add);
        assertEquals(List.of("hello"), received);
    }

    @Test
    public void doIfNotEmptySkipsNull() {
        List<String> received = new ArrayList<>();
        StringUtils.doIfNotEmpty(null, received::add);
        assertTrue(received.isEmpty());
    }

    @Test
    public void doIfNotEmptySkipsBlank() {
        List<String> received = new ArrayList<>();
        StringUtils.doIfNotEmpty("   ", received::add);
        assertTrue(received.isEmpty());
    }

    @Test
    public void doIfNotEmptyTrimsBeforeConsumer() {
        List<String> received = new ArrayList<>();
        StringUtils.doIfNotEmpty("  hello  ", received::add);
        assertEquals(List.of("hello"), received);
    }
}
