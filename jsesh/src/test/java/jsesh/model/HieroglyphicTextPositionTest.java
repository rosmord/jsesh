package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
        assertEquals(pos(7), mark.getPosition());
        assertSame(text, mark.getHieroglyphicText());
        assertSame(text.getTopItemAt(6), mark.getElementBefore());
        mark.release();
    }

    @Test
    void markFollowsInsertionAccordingToGravity() {
        MDCMark forward = new MDCMark(text, pos(3));
        MDCMark backward = new MDCMark(text, pos(3), MDCMark.Gravity.BACKWARD);
        MDCMark after = new MDCMark(text, pos(5));
        text.addTopItemAt(pos(3), new Cadrat());
        assertEquals(pos(4), forward.getPosition());
        assertEquals(pos(3), backward.getPosition());
        assertEquals(pos(6), after.getPosition());
        forward.release();
        backward.release();
        after.release();
    }

    @Test
    void markFollowsDeletion() {
        MDCMark inside = new MDCMark(text, pos(2));
        MDCMark after = new MDCMark(text, pos(6));
        text.removeTopItems(pos(1), pos(4));
        assertEquals(pos(1), inside.getPosition());
        assertEquals(pos(3), after.getPosition());
        inside.release();
        after.release();
    }

    @Test
    void lineLimitsAround() {
        assertEquals(List.of(pos(0), pos(2)), text.getLineLimitsAround(pos(0)));
        assertEquals(List.of(pos(0), pos(2)), text.getLineLimitsAround(pos(2)));
        assertEquals(List.of(pos(3), pos(5)), text.getLineLimitsAround(pos(4)));
        assertEquals(List.of(pos(6), pos(7)), text.getLineLimitsAround(pos(7)));
        // Out of bounds: clamped to the last line.
        assertEquals(List.of(pos(6), pos(7)), text.getLineLimitsAround(pos(100)));
    }

    @Test
    void pageLimitsAround() throws Exception {
        // 0 |A1| 1 |!!| 2 |B1| 3 |B2| 4 |!!| 5 |C1| 6
        HieroglyphicText paged = new MDCParserModelGenerator().parse("A1-!!B1-B2-!!C1");
        assertEquals(List.of(pos(0), pos(1)), paged.getPageLimitsAround(pos(1)));
        assertEquals(List.of(pos(2), pos(4)), paged.getPageLimitsAround(pos(2)));
        assertEquals(List.of(pos(2), pos(4)), paged.getPageLimitsAround(pos(4)));
        assertEquals(List.of(pos(5), pos(6)), paged.getPageLimitsAround(pos(6)));
        assertEquals(List.of(pos(5), pos(6)), paged.getPageLimitsAround(pos(100)));
    }

    @Test
    void topItemsBetweenIgnoresOrderAndReturnsCopies() {
        List<TopItem> items = text.getTopItemsBetween(pos(4), pos(1));
        assertEquals(3, items.size());
        assertNotSame(text.getTopItemAt(1), items.get(0));
        assertEquals(0, text.getTopItemAt(1).compareTo(items.get(0)));
        assertEquals(items.size(), text.getTopItemsBetween(pos(1), pos(4)).size());
        assertTrue(text.getTopItemsBetween(pos(3), pos(3)).isEmpty());
    }

    @Test
    void removeTopItemsReturnsWhatAddAllAtPutsBack() {
        TopItem second = text.getTopItemAt(1);
        List<TopItem> removed = text.removeTopItems(pos(1), pos(4));
        assertEquals(3, removed.size());
        assertSame(second, removed.get(0));
        assertEquals(4, text.getNumberOfChildren());
        text.addAllAt(pos(1), removed);
        assertEquals(7, text.getNumberOfChildren());
        assertSame(second, text.getTopItemAt(1));
    }

    @Test
    void zoneModificationsIgnoreLimitsOrder() {
        text.setRed(pos(4), pos(1), true);
        text.shade(pos(1), pos(4), true);
        for (int i = 0; i < text.getNumberOfChildren(); i++) {
            boolean inZone = 1 <= i && i < 4;
            assertEquals(inZone, text.getTopItemAt(i).getState().isRed(), "red " + i);
            assertEquals(inZone, text.getTopItemAt(i).getState().isShaded(), "shaded " + i);
        }
    }

    @Test
    void originalDocumentCoordinates() {
        text.addTopItemAt(pos(3), new Superscript("vo, 3"));
        assertEquals("", text.getOriginalDocumentCoordinates(pos(3)));
        assertEquals("vo, 3", text.getOriginalDocumentCoordinates(pos(4)));
        assertEquals("vo, 3", text.getOriginalDocumentCoordinates(text.getLastPosition()));
        assertEquals("vo, 3", text.getOriginalDocumentCoordinates(pos(100)));
    }
}
