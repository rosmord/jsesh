package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jsesh.mdcreader.MDCParserModelGenerator;

/// Tests the position-based navigation of [HieroglyphicText], and [MDCMark]s
/// following text modifications, now that [MDCPosition] is a plain value.
class HieroglyphicTextPositionTest {

    /// Positions: `0 |A1| 1 |A2| 2 |!| 3 |B1| 4 |B2| 5 |!| 6 |C1| 7`
    private HieroglyphicText text;

    @BeforeEach
    void setUp() throws Exception {
        text = new MDCParserModelGenerator().parse("A1-A2-!B1-B2-!C1");
        assertEquals(7, text.getNumberOfChildren());
        assertTrue(text.getTopItemAt(2).isBreak());
        assertTrue(text.getTopItemAt(5).isBreak());
    }

    private static MDCPosition pos(int i) {
        return new MDCPosition(i);
    }

    @Test
    void positionIsAValue() {
        assertEquals(pos(3), pos(3));
        assertEquals(pos(3).hashCode(), pos(3).hashCode());
        assertNotEquals(pos(3), pos(4));
        assertEquals(0, pos(-5).getIndex());
        assertTrue(pos(2).compareTo(pos(5)) < 0);
        assertEquals(pos(1), pos(4).getPreviousPosition(3));
        assertEquals(pos(0), pos(1).getPreviousPosition(3));
    }

    @Test
    void getPositionAtClampsToTextBounds() {
        assertEquals(pos(0), text.getPositionAt(-2));
        assertEquals(pos(4), text.getPositionAt(4));
        assertEquals(pos(7), text.getPositionAt(100));
        assertEquals(pos(7), text.getLastPosition());
        assertEquals(pos(7), text.getNextPosition(pos(5), 10));
        assertEquals(pos(0), text.getNextPosition(pos(2), -10));
    }

    @Test
    void elementsAroundPositions() {
        assertNull(text.getElementBefore(pos(0)));
        assertSame(text.getTopItemAt(0), text.getElementAfter(pos(0)));
        assertSame(text.getTopItemAt(6), text.getElementBefore(pos(7)));
        assertNull(text.getElementAfter(pos(7)));
        assertTrue(text.hasNext(pos(6)));
        assertFalse(text.hasNext(pos(7)));
    }

    @Test
    void lineFirstAndLastPositions() {
        assertEquals(pos(0), text.getLineFirstPosition(pos(1)));
        assertEquals(pos(2), text.getLineLastPosition(pos(1)));
        assertEquals(pos(3), text.getLineFirstPosition(pos(3)));
        assertEquals(pos(5), text.getLineLastPosition(pos(3)));
        assertEquals(pos(6), text.getLineFirstPosition(pos(7)));
        assertEquals(pos(7), text.getLineLastPosition(pos(6)));
    }

    @Test
    void downGoesToTheStartOfTheNextLine() {
        assertEquals(pos(3), text.getDownPosition(pos(0)));
        assertEquals(pos(3), text.getDownPosition(pos(1)));
        assertEquals(pos(6), text.getDownPosition(pos(4)));
        // Last line: goes to the end of the text.
        assertEquals(pos(7), text.getDownPosition(pos(6)));
    }

    @Test
    void upGoesToTheStartOfThePreviousLine() {
        assertEquals(pos(0), text.getUpPosition(pos(4)));
        assertEquals(pos(0), text.getUpPosition(pos(3)));
        assertEquals(pos(3), text.getUpPosition(pos(7)));
        // First line: stays on it.
        assertEquals(pos(0), text.getUpPosition(pos(1)));
    }

    @Test
    void navigationInAnEmptyText() {
        HieroglyphicText empty = new HieroglyphicText();
        assertEquals(pos(0), empty.getUpPosition(pos(0)));
        assertEquals(pos(0), empty.getDownPosition(pos(0)));
        assertEquals(pos(0), empty.getLineFirstPosition(pos(0)));
        assertEquals(pos(0), empty.getLineLastPosition(pos(0)));
        assertNull(empty.getElementAfter(pos(0)));
        assertNull(empty.getElementBefore(pos(0)));
    }

    @Test
    void markIsClampedAndKnowsItsText() {
        MDCMark mark = new MDCMark(text, pos(100));
        assertEquals(7, mark.getIndex());
        assertSame(text, mark.getHieroglyphicText());
        assertSame(text.getTopItemAt(6), mark.getElementBefore());
        mark.release();
    }

    @Test
    void markFollowsInsertionAccordingToGravity() {
        MDCMark forward = new MDCMark(text, pos(3));
        MDCMark backward = new MDCMark(text, pos(3), MDCMark.Gravity.BACKWARD);
        MDCMark after = new MDCMark(text, pos(5));
        text.addTopItemAt(3, new Cadrat());
        assertEquals(4, forward.getIndex());
        assertEquals(3, backward.getIndex());
        assertEquals(6, after.getIndex());
        forward.release();
        backward.release();
        after.release();
    }

    @Test
    void markFollowsDeletion() {
        MDCMark inside = new MDCMark(text, pos(2));
        MDCMark after = new MDCMark(text, pos(6));
        text.removeTopItems(1, 4);
        assertEquals(1, inside.getIndex());
        assertEquals(3, after.getIndex());
        inside.release();
        after.release();
    }
}
