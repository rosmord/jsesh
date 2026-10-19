package jsesh.document.caret;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.HieroglyphicText;
import jsesh.model.CaretGranularity;
import jsesh.model.MDCPosition;

/// Tests the position-based API of [MDCCaret].
class MDCCaretTest {

    /// Positions: `0 |A1| 1 |A2| 2 |!| 3 |B1| 4 |B2| 5 |!| 6 |C1| 7`
    private HieroglyphicText text;
    private MDCCaret caret;

    @BeforeEach
    void setUp() throws Exception {
        text = new MDCParserModelGenerator().parse("A1-A2-!B1-B2-!C1");
        caret = new MDCCaret(text);
    }

    private static MDCPosition pos(int i) {
        return new MDCPosition(i);
    }

    @Test
    void withoutMarkMinAndMaxAreTheInsertPosition() {
        caret.setInsertPosition(pos(3));
        assertFalse(caret.hasMark());
        assertFalse(caret.hasSelection());
        assertEquals(pos(3), caret.getMinPosition());
        assertEquals(pos(3), caret.getMaxPosition());
    }

    @Test
    void minAndMaxDoNotDependOnTheMarkSide() {
        caret.setInsertPosition(pos(5));
        caret.setMarkPosition(pos(2));
        assertTrue(caret.hasSelection());
        assertEquals(pos(2), caret.getMinPosition());
        assertEquals(pos(5), caret.getMaxPosition());

        caret.setInsertPosition(pos(1));
        assertEquals(pos(1), caret.getMinPosition());
        assertEquals(pos(2), caret.getMaxPosition());
    }

    @Test
    void markOnTheInsertPositionIsNoSelection() {
        caret.setInsertPosition(pos(4));
        caret.setMarkPosition(pos(4));
        assertTrue(caret.hasMark());
        assertFalse(caret.hasSelection());
    }

    @Test
    void positionsAreClampedToTheText() {
        caret.setMarkPosition(pos(100));
        assertEquals(text.getLastPosition(), caret.getMarkPosition());
        caret.setInsertPosition(pos(100));
        assertEquals(text.getLastPosition(), caret.getInsertPosition());
    }

    @Test
    void moveInsertByIsClamped() {
        caret.moveInsertBy(3);
        assertEquals(pos(3), caret.getInsertPosition());
        caret.moveInsertBy(-1);
        assertEquals(pos(2), caret.getInsertPosition());
        caret.moveInsertBy(100);
        assertEquals(pos(7), caret.getInsertPosition());
        caret.moveInsertBy(-100);
        assertEquals(pos(0), caret.getInsertPosition());
    }

    @Test
    void wholeTextCaretSelectsEverything() {
        MDCCaret whole = MDCCaret.buildWholeTextCaret(text);
        assertEquals(pos(0), whole.getMinPosition());
        assertEquals(text.getLastPosition(), whole.getMaxPosition());
        assertTrue(whole.hasSelection());
    }

    @Test
    void marksFollowTheText() {
        caret.setInsertPosition(pos(4));
        caret.setMarkPosition(pos(6));
        text.removeTopItems(pos(0), pos(3));
        assertEquals(pos(1), caret.getInsertPosition());
        assertEquals(pos(3), caret.getMarkPosition());
    }

    @Test
    void innerInsertIsSnappedToTheTopLevelByTheStandardAccessors() throws Exception {
        MDCCaret c = new MDCCaret(new MDCParserModelGenerator().parse("A1-B1*C1-D1:E1"));
        c.setInsertPosition(MDCPosition.of(1, 0, 1));
        assertTrue(c.isInsertInner());
        assertEquals(MDCPosition.of(1, 0, 1), c.getInsertExactPosition());
        assertEquals(pos(1), c.getInsertPosition());
        assertEquals(pos(1), c.getMinPosition());
        assertEquals(pos(1), c.getMaxPosition());
        assertFalse(c.hasSelection());

        c.snapToScopeLevel();
        assertFalse(c.isInsertInner());
        assertEquals(pos(1), c.getInsertExactPosition());
    }

    @Test
    void moveInsertWalksThroughTheQuadrats() throws Exception {
        MDCCaret c = new MDCCaret(new MDCParserModelGenerator().parse("A1-B1*C1-D1:E1"));
        c.moveInsert(1, CaretGranularity.INNER);
        assertEquals(pos(1), c.getInsertExactPosition());
        c.moveInsert(1, CaretGranularity.INNER);
        assertEquals(MDCPosition.of(1, 0, 1), c.getInsertExactPosition());
        c.moveInsert(1, CaretGranularity.INNER);
        assertEquals(pos(2), c.getInsertExactPosition());
        c.moveInsert(1, CaretGranularity.INNER);
        assertEquals(MDCPosition.of(2, 1), c.getInsertExactPosition());
        c.moveInsert(-2, CaretGranularity.INNER);
        assertEquals(MDCPosition.of(1, 0, 1), c.getInsertExactPosition());
        c.moveInsert(-100, CaretGranularity.INNER);
        assertEquals(pos(0), c.getInsertExactPosition());
    }

    @Test
    void moveInsertByStillMovesByTopLevelItems() throws Exception {
        MDCCaret c = new MDCCaret(new MDCParserModelGenerator().parse("A1-B1*C1-D1:E1"));
        c.setInsertPosition(MDCPosition.of(1, 0, 1));
        c.moveInsertBy(1);
        assertEquals(pos(2), c.getInsertExactPosition());
    }
}
