package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.mdcreader.MDCParserModelGenerator;

/// Tests for [PositionNavigator].
class PositionNavigatorTest {

    private static HieroglyphicText parse(String mdc) throws Exception {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private static MDCPosition p(int... path) {
        return MDCPosition.of(path);
    }

    /// All positions visited by repeated calls to next.
    private static List<MDCPosition> walkForward(HieroglyphicText t, CaretGranularity g) {
        List<MDCPosition> result = new ArrayList<>();
        MDCPosition current = p(0);
        result.add(current);
        while (true) {
            MDCPosition next = PositionNavigator.next(t, current, g);
            if (next.equals(current)) {
                return result;
            }
            result.add(next);
            current = next;
        }
    }

    @Test
    void quadratsWithOneSignHaveNoInnerStops() throws Exception {
        HieroglyphicText t = parse("A1-B1");
        assertTrue(PositionNavigator.innerStops(t, 0).isEmpty());
        assertTrue(PositionNavigator.innerStops(t, 1).isEmpty());
        assertTrue(PositionNavigator.innerStops(t, 2).isEmpty());
        assertEquals(List.of(p(0), p(1), p(2)), walkForward(t, CaretGranularity.INNER));
    }

    @Test
    void stopsBetweenRowsOfAQuadrat() throws Exception {
        HieroglyphicText t = parse("A1:B1-C1");
        assertEquals(List.of(p(0, 1)), PositionNavigator.innerStops(t, 0));
    }

    @Test
    void stopsBetweenElementsOfARow() throws Exception {
        HieroglyphicText t = parse("A1*B1*C1");
        assertEquals(List.of(p(0, 0, 1), p(0, 0, 2)), PositionNavigator.innerStops(t, 0));
    }

    @Test
    void walkingForwardVisitsAllStopsInDocumentOrder() throws Exception {
        HieroglyphicText t = parse("A1*B1*C1-D1:E1-F1");
        List<MDCPosition> expected = List.of(p(0), p(0, 0, 1), p(0, 0, 2), p(1), p(1, 1), p(2), p(3));
        assertEquals(expected, walkForward(t, CaretGranularity.INNER));
        List<MDCPosition> sorted = new ArrayList<>(expected);
        sorted.sort(null);
        assertEquals(sorted, expected);
    }

    @Test
    void previousIsTheInverseOfNext() throws Exception {
        HieroglyphicText t = parse("A1*B1*C1-D1:E1-F1:G1*H1");
        List<MDCPosition> forward = walkForward(t, CaretGranularity.INNER);
        for (int i = 1; i < forward.size(); i++) {
            assertEquals(forward.get(i - 1), PositionNavigator.previous(t, forward.get(i), CaretGranularity.INNER));
        }
    }

    @Test
    void quadratGranularityIgnoresInnerStops() throws Exception {
        HieroglyphicText t = parse("A1*B1*C1-D1:E1");
        assertEquals(List.of(p(0), p(1), p(2)), walkForward(t, CaretGranularity.QUADRAT));
        // a nested position is first snapped to the position before its item.
        assertEquals(p(1), PositionNavigator.next(t, p(0, 0, 1), CaretGranularity.QUADRAT));
        assertEquals(p(0), PositionNavigator.previous(t, p(1, 1), CaretGranularity.QUADRAT));
    }

    @Test
    void movesAreClampedAtTheTextBounds() throws Exception {
        HieroglyphicText t = parse("A1:B1");
        assertEquals(p(0), PositionNavigator.previous(t, p(0), CaretGranularity.INNER));
        assertEquals(p(1), PositionNavigator.next(t, p(1), CaretGranularity.INNER));
        assertEquals(p(1), PositionNavigator.move(t, p(0), 50, CaretGranularity.INNER));
        assertEquals(p(0), PositionNavigator.move(t, p(1), -50, CaretGranularity.INNER));
        assertEquals(p(0, 1), PositionNavigator.move(t, p(0), 1, CaretGranularity.INNER));
    }

    @Test
    void nestedStopsAreValidPositions() throws Exception {
        HieroglyphicText t = parse("A1*B1*C1-D1:E1-F1:G1*H1");
        for (int i = 0; i < t.getNumberOfChildren(); i++) {
            for (MDCPosition stop : PositionNavigator.innerStops(t, i)) {
                assertTrue(t.isValidPosition(stop), stop.toString());
            }
        }
    }

    @Test
    void cursorEntersCartouches() throws Exception {
        HieroglyphicText t = parse("<-A1-B1->-C1");
        // Both edges of the cartouche content are stops, as they differ from the
        // positions around the cartouche.
        List<MDCPosition> expected = List.of(p(0), p(0, 0, 0, 0, 0), p(0, 0, 0, 0, 1), p(0, 0, 0, 0, 2), p(1), p(2));
        assertEquals(expected, walkForward(t, CaretGranularity.INNER));
        assertEquals(List.of(p(0), p(1), p(2)), walkForward(t, CaretGranularity.QUADRAT));
    }

    @Test
    void cursorEntersTheQuadratsInsideACartouche() throws Exception {
        HieroglyphicText t = parse("<-A1:B1-C1->");
        assertEquals(List.of(p(0, 0, 0, 0, 0), p(0, 0, 0, 0, 0, 1), p(0, 0, 0, 0, 1), p(0, 0, 0, 0, 2)),
                PositionNavigator.innerStops(t, 0));
    }

    @Test
    void cursorEntersNestedCartouches() throws Exception {
        HieroglyphicText t = parse("<-A1-<-B1->->");
        List<MDCPosition> stops = PositionNavigator.innerStops(t, 0);
        assertEquals(5, stops.size());
        assertTrue(stops.contains(p(0, 0, 0, 0, 1, 0, 0, 0, 0)));
        assertTrue(stops.contains(p(0, 0, 0, 0, 1, 0, 0, 0, 1)));
    }

    @Test
    void cursorEntersSubQuadrats() throws Exception {
        HieroglyphicText t = parse("(A1-B1)");
        assertEquals(List.of(p(0, 0, 0, 0, 0), p(0, 0, 0, 0, 1), p(0, 0, 0, 0, 2)),
                PositionNavigator.innerStops(t, 0));
    }

    @Test
    void cursorEntersLigatures() throws Exception {
        HieroglyphicText t = parse("A1&B1&C1-D1");
        assertEquals(List.of(p(0, 0, 0, 1), p(0, 0, 0, 2)), PositionNavigator.innerStops(t, 0));
        assertEquals(List.of(p(0), p(0, 0, 0, 1), p(0, 0, 0, 2), p(1), p(2)), walkForward(t, CaretGranularity.INNER));
    }

    @Test
    void cursorEntersComplexLigatures() throws Exception {
        // t^^^A&&&n : a sign, with a group before and a group after.
        HieroglyphicText t = parse("t^^^A&&&n");
        assertEquals(List.of(p(0, 0, 0, 1), p(0, 0, 0, 2)), PositionNavigator.innerStops(t, 0));
        // a complex ligature with only one extra group.
        t = parse("t&&&A");
        assertEquals(List.of(p(0, 0, 0, 1)), PositionNavigator.innerStops(t, 0));
    }

    @Test
    void cursorEntersLigaturesInsideCartouches() throws Exception {
        HieroglyphicText t = parse("<-A1&B1-C1->");
        List<MDCPosition> expected = List.of(p(0, 0, 0, 0, 0), p(0, 0, 0, 0, 0, 0, 0, 1), p(0, 0, 0, 0, 1),
                p(0, 0, 0, 0, 2));
        assertEquals(expected, PositionNavigator.innerStops(t, 0));
    }

    @Test
    void emptyCartoucheHasOneStop() throws Exception {
        HieroglyphicText t = parse("<-->");
        assertEquals(List.of(p(0, 0, 0, 0, 0)), PositionNavigator.innerStops(t, 0));
    }

    @Test
    void previousIsTheInverseOfNextInsideCartouchesAndLigatures() throws Exception {
        HieroglyphicText t = parse("<-A1&B1-C1:D1->-t^^^A&&&n-(A1-B1)-D1:E1");
        List<MDCPosition> forward = walkForward(t, CaretGranularity.INNER);
        for (int i = 1; i < forward.size(); i++) {
            assertEquals(forward.get(i - 1), PositionNavigator.previous(t, forward.get(i), CaretGranularity.INNER));
            assertTrue(t.isValidPosition(forward.get(i)), forward.get(i).toString());
        }
        List<MDCPosition> sorted = new ArrayList<>(forward);
        sorted.sort(null);
        assertEquals(sorted, forward);
    }

    @Test
    void stopsBetweenSignsOfAnAbsoluteGroup() throws Exception {
        HieroglyphicText t = parse("n**x{{967,79,100}}**N5{{0,0,100}}-A1");
        List<MDCPosition> stops = PositionNavigator.innerStops(t, 0);
        assertEquals(2, stops.size());
        assertEquals(p(0, 0, 0, 1), stops.get(0));
        assertEquals(p(0, 0, 0, 2), stops.get(1));
    }
}
