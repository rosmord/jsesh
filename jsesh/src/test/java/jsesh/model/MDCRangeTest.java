package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/// Tests [MDCRange].
class MDCRangeTest {

    @Test
    void ofUnorderedSwapsReversedPositions() {
        MDCPosition a = MDCPosition.of(1, 0, 1);
        MDCPosition b = MDCPosition.of(2);
        MDCRange expected = new MDCRange(a, b);
        assertEquals(expected, MDCRange.ofUnordered(a, b));
        assertEquals(expected, MDCRange.ofUnordered(b, a));
    }

    @Test
    void reversedConstructorArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new MDCRange(MDCPosition.of(2), MDCPosition.of(1)));
    }

    @Test
    void emptyRange() {
        assertTrue(MDCRange.ofUnordered(MDCPosition.of(1), MDCPosition.of(1)).isEmpty());
        assertFalse(MDCRange.ofUnordered(MDCPosition.of(1), MDCPosition.of(2)).isEmpty());
    }
}
