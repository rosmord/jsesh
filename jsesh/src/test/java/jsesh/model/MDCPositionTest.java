package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/// Tests [MDCPosition] as a path of integers.
class MDCPositionTest {

    @Test
    void topLevelFactoriesAgree() {
        assertEquals(new MDCPosition(2), MDCPosition.of(2));
        assertEquals(new MDCPosition(2).hashCode(), MDCPosition.of(2).hashCode());
        assertTrue(MDCPosition.of(2).isTop());
        assertEquals(2, MDCPosition.of(2).getIndex());
    }

    @Test
    void negativeValuesAreClamped() {
        assertEquals(MDCPosition.of(0, 0, 0), MDCPosition.of(-1, -4, -2));
        assertEquals(new MDCPosition(0), new MDCPosition(-3));
    }

    @Test
    void emptyPathIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MDCPosition.of());
    }

    @Test
    void nestedAccessors() {
        MDCPosition p = MDCPosition.of(1, 0, 1);
        assertFalse(p.isTop());
        assertEquals(3, p.depth());
        assertEquals(1, p.getLastIndex());
        assertArrayEquals(new int[] { 1, 0 }, p.getParentPath());
        assertArrayEquals(new int[] { 1, 0, 1 }, p.getPath());
        assertThrows(UnsupportedOperationException.class, p::getIndex);
    }

    @Test
    void pathIsDefensivelyCopied() {
        int[] a = { 1, 2 };
        MDCPosition p = MDCPosition.of(a);
        a[0] = 7;
        p.getPath()[0] = 9;
        assertEquals(MDCPosition.of(1, 2), p);
    }

    @Test
    void documentOrder() {
        List<MDCPosition> expected = List.of(MDCPosition.of(0), MDCPosition.of(1), MDCPosition.of(1, 0, 0),
                MDCPosition.of(1, 0, 1), MDCPosition.of(1, 1, 0), MDCPosition.of(2));
        List<MDCPosition> shuffled = new ArrayList<>(expected);
        Collections.reverse(shuffled);
        Collections.sort(shuffled);
        assertEquals(expected, shuffled);
    }

    @Test
    void previousPositionStaysInSameElement() {
        assertEquals(MDCPosition.of(1, 0, 0), MDCPosition.of(1, 0, 1).getPreviousPosition(1));
        assertEquals(MDCPosition.of(1, 0, 0), MDCPosition.of(1, 0, 1).getPreviousPosition(5));
        assertTrue(MDCPosition.of(1, 0, 1).hasPrevious());
        assertFalse(MDCPosition.of(1, 0, 0).hasPrevious());
    }

    @Test
    void toStringShowsPath() {
        assertEquals("pos 3", new MDCPosition(3).toString());
        assertEquals("pos (1,0,1)", MDCPosition.of(1, 0, 1).toString());
    }
}
