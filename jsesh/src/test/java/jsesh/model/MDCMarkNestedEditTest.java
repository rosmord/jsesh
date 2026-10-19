package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.mdcreader.MDCParserModelGenerator;

/// [MDCMark]s follow the edits made inside nested scopes (cartouches...).
class MDCMarkNestedEditTest {

    /// The content of the cartouche, when it is the first item of the text.
    private static final TextScope CARTOUCHE = TextScope.of(0, 0, 0, 0);

    private static HieroglyphicText parse(String mdc) throws Exception {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private static MDCPosition p(int... path) {
        return MDCPosition.of(path);
    }

    private static List<TopItem> quadrat(String code) {
        return List.of(new Cadrat(new Hieroglyph(code)));
    }

    @Test
    void insertionBeforeTheMarkShiftsIt() throws Exception {
        HieroglyphicText t = parse("<-A1-B1->");
        MDCMark mark = new MDCMark(t, CARTOUCHE.positionAt(1));
        CARTOUCHE.resolve(t).insertItemsAt(0, quadrat("C1"));
        assertEquals(CARTOUCHE.positionAt(2), mark.getPosition());
    }

    @Test
    void insertionAtTheMarkFollowsGravity() throws Exception {
        HieroglyphicText t = parse("<-A1-B1->");
        MDCMark forward = new MDCMark(t, CARTOUCHE.positionAt(1), MDCMark.Gravity.FORWARD);
        MDCMark backward = new MDCMark(t, CARTOUCHE.positionAt(1), MDCMark.Gravity.BACKWARD);
        CARTOUCHE.resolve(t).insertItemsAt(1, quadrat("C1"));
        assertEquals(CARTOUCHE.positionAt(2), forward.getPosition());
        assertEquals(CARTOUCHE.positionAt(1), backward.getPosition());
    }

    @Test
    void insertionAfterTheMarkLeavesIt() throws Exception {
        HieroglyphicText t = parse("<-A1-B1->");
        MDCMark mark = new MDCMark(t, CARTOUCHE.positionAt(1));
        CARTOUCHE.resolve(t).insertItemsAt(2, quadrat("C1"));
        assertEquals(CARTOUCHE.positionAt(1), mark.getPosition());
    }

    @Test
    void deletionAroundTheMark() throws Exception {
        HieroglyphicText t = parse("<-A1-B1-C1-D1->");
        MDCMark before = new MDCMark(t, CARTOUCHE.positionAt(1));
        MDCMark inside = new MDCMark(t, CARTOUCHE.positionAt(2));
        MDCMark after = new MDCMark(t, CARTOUCHE.positionAt(4));
        CARTOUCHE.resolve(t).removeItems(1, 3);
        assertEquals(CARTOUCHE.positionAt(1), before.getPosition());
        assertEquals(CARTOUCHE.positionAt(1), inside.getPosition());
        assertEquals(CARTOUCHE.positionAt(2), after.getPosition());
    }

    @Test
    void markInsideARemovedQuadratFallsBackToItsGap() throws Exception {
        HieroglyphicText t = parse("<-A1-B1*C1->");
        // Between B1 and C1, inside the second quadrat of the cartouche.
        MDCMark mark = new MDCMark(t, p(0, 0, 0, 0, 1, 0, 1));
        CARTOUCHE.resolve(t).removeItems(1, 2);
        assertEquals(CARTOUCHE.positionAt(1), mark.getPosition());
    }

    @Test
    void markInsideAQuadratOfTheCartoucheIsShifted() throws Exception {
        HieroglyphicText t = parse("<-A1-B1*C1->");
        MDCMark mark = new MDCMark(t, p(0, 0, 0, 0, 1, 0, 1));
        CARTOUCHE.resolve(t).insertItemsAt(1, quadrat("D1"));
        assertEquals(p(0, 0, 0, 0, 2, 0, 1), mark.getPosition());
    }

    @Test
    void editsInAnotherCartoucheDontMoveTheMark() throws Exception {
        HieroglyphicText t = parse("<-A1-B1->-<-C1->");
        MDCMark mark = new MDCMark(t, CARTOUCHE.positionAt(1));
        TextScope.of(1, 0, 0, 0).resolve(t).insertItemsAt(0, quadrat("D1"));
        assertEquals(CARTOUCHE.positionAt(1), mark.getPosition());
    }

    @Test
    void topLevelMarkAfterTheCartoucheIsNotMoved() throws Exception {
        HieroglyphicText t = parse("<-A1->-B1");
        MDCMark mark = new MDCMark(t, p(1));
        CARTOUCHE.resolve(t).insertItemsAt(1, quadrat("C1"));
        assertEquals(p(1), mark.getPosition());
    }
}
