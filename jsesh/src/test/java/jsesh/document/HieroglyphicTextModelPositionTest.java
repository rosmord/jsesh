package jsesh.document;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jsesh.io.mdc.MdCModelWriter;
import jsesh.model.MDCPosition;

/// Tests the position-based editing API of [HieroglyphicTextModel].
class HieroglyphicTextModelPositionTest {

    /// Positions: `0 |A1| 1 |A2| 2 |!| 3 |B1| 4 |B2| 5 |!| 6 |C1| 7`
    private HieroglyphicTextModel model;

    @BeforeEach
    void setUp() throws Exception {
        model = new HieroglyphicTextModel();
        model.setMDCCode("A1-A2-!B1-B2-!C1");
    }

    private static MDCPosition pos(int i) {
        return new MDCPosition(i);
    }

    private String mdc() {
        return new MdCModelWriter().toMdC(model.getHieroglyphicText());
    }

    @Test
    void insertMDCTextAtAPosition() throws Exception {
        model.insertMDCText(pos(4), "D1-D2");
        assertEquals("A1-A2-!\nB1-D1-D2-B2-!\nC1", mdc());
        model.undo();
        assertEquals("A1-A2-!\nB1-B2-!\nC1", mdc());
    }

    @Test
    void replaceWithMDCTextIgnoresLimitsOrder() throws Exception {
        model.replaceWithMDCText(pos(6), pos(3), "D1");
        assertEquals("A1-A2-!\nD1-C1", mdc());
        model.undo();
        assertEquals("A1-A2-!\nB1-B2-!\nC1", mdc());
    }

    @Test
    void removeElementsCanBeUndoneAndRedone() {
        model.removeElements(pos(5), pos(1));
        assertEquals("A1-!\nC1", mdc());
        model.undo();
        assertEquals("A1-A2-!\nB1-B2-!\nC1", mdc());
        model.redo();
        assertEquals("A1-!\nC1", mdc());
    }

    @Test
    void lineAndPageLimits() {
        assertEquals(List.of(pos(3), pos(5)), model.getLineLimitsAround(pos(4)));
        // No page break: the whole text is one page.
        assertEquals(List.of(pos(0), pos(7)), model.getPageLimitsAround(pos(4)));
    }
}
