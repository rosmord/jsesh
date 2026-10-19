package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jsesh.mdcreader.MDCParserModelGenerator;

/// [MDCMark]s standing inside a quadrat.
class MDCMarkInnerPositionTest {

    private HieroglyphicText text;

    @BeforeEach
    void setUp() throws Exception {
        text = new MDCParserModelGenerator().parse("A1-B1*C1-D1");
    }

    @Test
    void innerMarkKeepsItsExactPositionAndExposesItsTopLevelPosition() {
        MDCMark mark = new MDCMark(text, MDCPosition.of(1, 0, 1));
        assertTrue(mark.isInner());
        assertEquals(MDCPosition.of(1, 0, 1), mark.getPosition());
        assertEquals(new MDCPosition(1), mark.getTopLevelPosition());
        // top-level services work on the quadrat's position.
        assertEquals(text.getTopItemAt(1), mark.getElementAfter());
        assertEquals(text.getTopItemAt(0), mark.getElementBefore());
        assertEquals(new MDCPosition(2), mark.getNextPosition(1));
    }

    @Test
    void invalidInnerPositionFallsBackToTopLevel() {
        MDCMark mark = new MDCMark(text, MDCPosition.of(1, 0, 9));
        assertFalse(mark.isInner());
        assertEquals(new MDCPosition(1), mark.getPosition());
        mark = new MDCMark(text, MDCPosition.of(9, 0, 1));
        assertFalse(mark.isInner());
        assertEquals(text.getLastPosition(), mark.getPosition());
    }

    @Test
    void editsAtTopLevelResetTheMarkToTopLevel() {
        MDCMark mark = new MDCMark(text, MDCPosition.of(1, 0, 1));
        text.addTopItemAt(new MDCPosition(0), new Superscript("1"));
        assertFalse(mark.isInner());
        // the mark followed the text, as any top-level mark.
        assertEquals(new MDCPosition(2), mark.getPosition());
    }

    @Test
    void deletingTheQuadratResetsTheMark() {
        MDCMark mark = new MDCMark(text, MDCPosition.of(1, 0, 1));
        text.removeTopItems(new MDCPosition(1), new MDCPosition(2));
        assertFalse(mark.isInner());
        assertEquals(new MDCPosition(1), mark.getPosition());
    }

    @Test
    void releasedMarksAreNotUpdated() {
        MDCMark mark = new MDCMark(text, MDCPosition.of(1, 0, 1));
        mark.release();
        text.addTopItemAt(new MDCPosition(0), new Superscript("1"));
    }
}
