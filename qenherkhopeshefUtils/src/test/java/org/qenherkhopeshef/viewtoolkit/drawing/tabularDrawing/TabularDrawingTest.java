package org.qenherkhopeshef.viewtoolkit.drawing.tabularDrawing;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.swingUtils.DoubleDimensions;
import org.qenherkhopeshef.viewtoolkit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewtoolkit.drawing.element.Margin;
import org.qenherkhopeshef.viewtoolkit.drawing.element.property.BoundsProperty;
import org.qenherkhopeshef.viewtoolkit.drawing.event.DrawingEvent;
import org.qenherkhopeshef.viewtoolkit.drawing.event.PropertyChangedEvent;

public class TabularDrawingTest {

    private static final double EPS = 1e-9;

    /// An element whose preferred size can change (and tells the drawing).
    private static class Box extends GraphicalElement {
        private Dimension2D size;

        Box(double width, double height) {
            size = new DoubleDimensions(width, height);
            setMargin(Margin.NONE);
        }

        void resize(double width, double height) {
            size = new DoubleDimensions(width, height);
            fireDrawingEvent(new PropertyChangedEvent(this, new BoundsProperty()));
        }

        @Override
        protected void drawElement(java.awt.Graphics2D g) {
            // Nothing to draw.
        }

        @Override
        public Dimension2D getPreferredSize() {
            return size;
        }
    }

    /// A 2x2 drawing.
    ///
    /// ```
    ///           col 0         col 1
    ///   row 0   a (10 x 5)    b (30 x 7)
    ///   row 1   c (20 x 9)    d ( 8 x 3)
    /// ```
    ///
    /// Column widths are 20 and 30, row heights 7 and 9.
    private static class Fixture {
        final TabularDrawing drawing = new TabularDrawing();
        final Box a = new Box(10, 5), b = new Box(30, 7), c = new Box(20, 9), d = new Box(8, 3);
        final List<DrawingEvent> events = new ArrayList<>();

        Fixture() {
            drawing.add(a, 0, 0, 1, 1);
            drawing.add(b, 1, 0, 1, 1);
            drawing.add(c, 0, 1, 1, 1);
            drawing.add(d, 1, 1, 1, 1);
            drawing.addDrawingListener(events::add);
        }

        List<Box> all() {
            return List.of(a, b, c, d);
        }

        List<String> eventNames() {
            return events.stream().map(e -> e.getClass().getSimpleName()).toList();
        }
    }

    private static void assertBounds(GraphicalElement e, double x, double y, double w, double h) {
        Rectangle2D r = e.getBounds();
        assertEquals(x, r.getX(), EPS, "x of " + e);
        assertEquals(y, r.getY(), EPS, "y of " + e);
        assertEquals(w, r.getWidth(), EPS, "width of " + e);
        assertEquals(h, r.getHeight(), EPS, "height of " + e);
    }

    /// Every element must have room for its preferred size.
    private static void assertAllFit(List<? extends GraphicalElement> elements) {
        for (GraphicalElement e : elements) {
            assertTrue(e.getBounds().getWidth() >= e.getPreferredSize().getWidth() - EPS,
                    "too narrow: " + e.getBounds() + " for " + e.getPreferredSize().getWidth());
            assertTrue(e.getBounds().getHeight() >= e.getPreferredSize().getHeight() - EPS,
                    "too short: " + e.getBounds() + " for " + e.getPreferredSize().getHeight());
        }
    }

    /// Force a from-scratch layout, and check that it changes nothing: the
    /// incremental layout must already be the canonical one.
    private static void assertIsCanonicalLayout(TabularDrawing drawing, List<? extends GraphicalElement> elements) {
        List<Rectangle2D> before = elements.stream().map(GraphicalElement::getBounds).toList();
        double width = drawing.getWidth(), height = drawing.getHeight();
        drawing.setBeingChanged(true);
        drawing.setBeingChanged(false);
        assertEquals(width, drawing.getWidth(), EPS, "width");
        assertEquals(height, drawing.getHeight(), EPS, "height");
        for (int i = 0; i < elements.size(); i++)
            assertEquals(before.get(i), elements.get(i).getBounds(), "bounds of element " + i);
    }

    // ---------------------------------------------------------------------
    // Layout
    // ---------------------------------------------------------------------

    @Test
    public void emptyDrawingHasNoSize() {
        TabularDrawing drawing = new TabularDrawing();
        assertTrue(drawing.isEmpty());
        assertEquals(0, drawing.getWidth());
        assertEquals(0, drawing.getHeight());
        assertEquals(0, drawing.getNumberOfColumns());
        assertEquals(0, drawing.getNumberOfRows());
        assertTrue(drawing.getSquareContent(0, 0).isEmpty());
    }

    @Test
    public void columnsAndRowsTakeTheSizeOfTheirLargestElement() {
        Fixture f = new Fixture();
        assertEquals(50, f.drawing.getWidth(), EPS);
        assertEquals(16, f.drawing.getHeight(), EPS);
        assertEquals(2, f.drawing.getNumberOfColumns());
        assertEquals(2, f.drawing.getNumberOfRows());
    }

    @Test
    public void elementsFillTheirCell() {
        Fixture f = new Fixture();
        assertBounds(f.a, 0, 0, 20, 7);
        assertBounds(f.b, 20, 0, 30, 7);
        assertBounds(f.c, 0, 7, 20, 9);
        assertBounds(f.d, 20, 7, 30, 9);
        assertAllFit(f.all());
    }

    @Test
    public void elementsKnowTheirCell() {
        Fixture f = new Fixture();
        ElementInfo info = f.drawing.getElementCell(f.d);
        assertEquals(1, info.getFirstColumn());
        assertEquals(1, info.getFirstRow());
        assertEquals(1, info.getHorizontalSpan());
        assertEquals(List.of(f.c), List.copyOf(f.drawing.getSquareContent(0, 1)));
        assertEquals(2, f.drawing.getColumnContent(1).size());
    }

    @Test
    public void sparseElementsLeaveEmptyColumnsAndRows() {
        TabularDrawing drawing = new TabularDrawing();
        Box x = new Box(10, 4);
        drawing.add(x, 2, 1, 1, 1);
        assertEquals(3, drawing.getNumberOfColumns());
        assertEquals(2, drawing.getNumberOfRows());
        assertBounds(x, 0, 0, 10, 4);
        assertEquals(10, drawing.getWidth(), EPS);
        assertEquals(4, drawing.getHeight(), EPS);
    }

    @Test
    public void spanningElementIsSharedBetweenItsColumns() {
        TabularDrawing drawing = new TabularDrawing();
        Box wide = new Box(40, 4), x = new Box(10, 6), y = new Box(10, 6);
        drawing.add(wide, 0, 0, 2, 1);
        drawing.add(x, 0, 1, 1, 1);
        drawing.add(y, 1, 1, 1, 1);
        assertEquals(40, drawing.getWidth(), EPS);
        assertBounds(wide, 0, 0, 40, 4);
        assertBounds(x, 0, 4, 20, 6);
        assertBounds(y, 20, 4, 20, 6);
        assertEquals(List.of(wide), List.copyOf(drawing.getSquareContent(1, 0)));
    }

    @Test
    public void growingAnElementPushesItsNeighbours() {
        Fixture f = new Fixture();
        f.a.resize(30, 12);
        assertBounds(f.a, 0, 0, 30, 12);
        assertBounds(f.b, 30, 0, 30, 12);
        assertBounds(f.c, 0, 12, 30, 9);
        assertBounds(f.d, 30, 12, 30, 9);
        assertEquals(60, f.drawing.getWidth(), EPS);
        assertEquals(21, f.drawing.getHeight(), EPS);
        assertAllFit(f.all());
    }

    @Test
    public void growingAnElementIsReportedOnce() {
        Fixture f = new Fixture();
        f.events.clear();
        f.a.resize(30, 12);
        // The element's own event goes through; the moves it causes don't.
        assertEquals(List.of("PropertyChangedEvent"), f.eventNames());
    }

    @Test
    public void relayoutOfAllTheDrawingCompacts() {
        Fixture f = new Fixture();
        f.b.resize(5, 2);
        f.drawing.setBeingChanged(true);
        f.drawing.setBeingChanged(false);
        // column 1 is now 8 wide (d), row 0 is 5 high (a).
        assertEquals(28, f.drawing.getWidth(), EPS);
        assertBounds(f.b, 20, 0, 8, 5);
        assertBounds(f.c, 0, 5, 20, 9);
    }

    @Test
    public void changesAreBatchedWhileTheDrawingIsBeingChanged() {
        TabularDrawing drawing = new TabularDrawing();
        List<DrawingEvent> events = new ArrayList<>();
        drawing.addDrawingListener(events::add);
        Box x = new Box(10, 5), y = new Box(30, 7);
        drawing.setBeingChanged(true);
        assertTrue(drawing.isBeingChanged());
        drawing.add(x, 0, 0, 1, 1);
        drawing.add(y, 1, 0, 1, 1);
        // Nothing is laid out yet...
        assertEquals(0, drawing.getWidth(), EPS);
        drawing.setBeingChanged(false);
        assertFalse(drawing.isBeingChanged());
        // ... until the end of the change.
        assertEquals(40, drawing.getWidth(), EPS);
        assertBounds(x, 0, 0, 10, 7);
        assertBounds(y, 10, 0, 30, 7);
        assertTrue(events.stream().anyMatch(e -> e.getClass().getSimpleName().equals("DrawingChangedEvent")));
    }

    @Test
    public void layoutReportsItsAdvancement() {
        TabularDrawing drawing = new TabularDrawing();
        List<Integer> percents = new ArrayList<>();
        int[] finished = { 0 };
        drawing.setAdvancementListener(new AdvancementListener() {
            public void notifyCompletion(int percent) {
                percents.add(percent);
            }

            public void notifyCompletion() {
                finished[0]++;
            }
        });
        drawing.add(new Box(10, 5), 0, 0, 1, 1);
        drawing.add(new Box(10, 5), 1, 0, 1, 1);
        assertFalse(percents.isEmpty());
        assertEquals(100, percents.get(percents.size() - 1));
        assertEquals(2, finished[0], "one per layout");
        drawing.removeAdvancementListener();
        percents.clear();
        drawing.add(new Box(10, 5), 2, 0, 1, 1);
        assertTrue(percents.isEmpty());
    }

    @Test
    public void emptyDrawingCanBeLaidOutWithAListener() {
        TabularDrawing drawing = new TabularDrawing();
        int[] finished = { 0 };
        drawing.setAdvancementListener(new AdvancementListener() {
            public void notifyCompletion(int percent) {
            }

            public void notifyCompletion() {
                finished[0]++;
            }
        });
        drawing.setBeingChanged(true);
        drawing.setBeingChanged(false);
        assertEquals(1, finished[0]);
    }

    // ---------------------------------------------------------------------
    // Hit testing
    // ---------------------------------------------------------------------

    @Test
    public void pointsAreMappedToTheirCell() {
        Fixture f = new Fixture();
        assertEquals(new Coordinates(0, 0), f.drawing.getPointCoordinates(5, 3));
        assertEquals(new Coordinates(1, 0), f.drawing.getPointCoordinates(25, 3));
        assertEquals(new Coordinates(-1, 0), f.drawing.getPointCoordinates(-5, 3));
        assertEquals(new Coordinates(2, 0), f.drawing.getPointCoordinates(80, 3));
    }

    @Test
    public void pointsAreMappedToTheirRow() {
        Fixture f = new Fixture(); // rows: [0, 7) and [7, 16)
        assertEquals(new Coordinates(0, 0), f.drawing.getPointCoordinates(5, 3));
        assertEquals(new Coordinates(0, 1), f.drawing.getPointCoordinates(5, 7.5));
        assertEquals(new Coordinates(0, 1), f.drawing.getPointCoordinates(5, 15));
        assertEquals(new Coordinates(0, -1), f.drawing.getPointCoordinates(5, -1));
        assertEquals(new Coordinates(0, 2), f.drawing.getPointCoordinates(5, 16));
        assertEquals(new Coordinates(0, 2), f.drawing.getPointCoordinates(5, 100));
    }

    @Test
    public void pointsAreMappedToTheirRowWithManyRows() {
        TabularDrawing drawing = new TabularDrawing();
        for (int r = 0; r < 7; r++)
            drawing.add(new Box(10, 10), 0, r, 1, 1); // row r is [10r, 10r+10)
        for (int r = 0; r < 7; r++) {
            assertEquals(r, drawing.getPointCoordinates(5, 10 * r + 5).getRow());
            assertEquals(r, drawing.getPointCoordinates(5, 10 * r).getRow());
        }
        assertEquals(7, drawing.getPointCoordinates(5, 70).getRow());
    }

    @Test
    public void emptyRowsAreSkippedWhenMappingPoints() {
        TabularDrawing drawing = new TabularDrawing();
        drawing.add(new Box(10, 10), 0, 0, 1, 1);
        drawing.add(new Box(10, 10), 0, 2, 1, 1); // row 1 is empty (height 0).
        assertEquals(2, drawing.getPointCoordinates(5, 15).getRow());
    }

    @Test
    public void elementsAreFoundInTheirZone() {
        Fixture f = new Fixture();
        assertEquals(List.of(f.a), List.copyOf(f.drawing.getElementsInZone(new Rectangle2D.Double(1, 1, 5, 3))));
        assertEquals(2, f.drawing.getElementsInZone(new Rectangle2D.Double(15, 1, 10, 3)).size());
        assertEquals(4, f.drawing.getElementsInZone(new Rectangle2D.Double(-10, -10, 500, 500)).size());
        assertTrue(f.drawing.getElementsInZone(new Rectangle2D.Double(200, 200, 5, 5)).isEmpty());
    }

    // ---------------------------------------------------------------------
    // Removal
    // ---------------------------------------------------------------------

    @Test
    public void removingTheWidestElementShrinksItsColumn() {
        Fixture f = new Fixture();
        f.drawing.removeElement(f.b);
        // column 1 is now as wide as d; row 0 as high as a.
        assertEquals(28, f.drawing.getWidth(), EPS);
        assertEquals(14, f.drawing.getHeight(), EPS);
        assertBounds(f.a, 0, 0, 20, 5);
        assertBounds(f.c, 0, 5, 20, 9);
        assertBounds(f.d, 20, 5, 8, 9);
    }

    @Test
    public void removingTheTallestElementShrinksItsRow() {
        Fixture f = new Fixture();
        f.drawing.removeElement(f.c);
        // row 1 is now as high as d; column 0 as wide as a.
        assertEquals(10 + 30, f.drawing.getWidth(), EPS);
        assertEquals(7 + 3, f.drawing.getHeight(), EPS);
        assertBounds(f.a, 0, 0, 10, 7);
        assertBounds(f.b, 10, 0, 30, 7);
        assertBounds(f.d, 10, 7, 30, 3);
    }

    @Test
    public void removingAnElementWhichDoesNotSetTheSizeMovesNothing() {
        Fixture f = new Fixture();
        f.events.clear();
        f.drawing.removeElement(f.a); // neither the widest of col 0 (c) nor the tallest of row 0 (b)
        assertEquals(List.of("ElementRemoved"), f.eventNames());
        assertBounds(f.b, 20, 0, 30, 7);
        assertBounds(f.c, 0, 7, 20, 9);
        assertBounds(f.d, 20, 7, 30, 9);
        assertEquals(50, f.drawing.getWidth(), EPS);
    }

    @Test
    public void removingOneOfTwoEquallyTallElementsMovesNothing() {
        TabularDrawing drawing = new TabularDrawing();
        Box x = new Box(10, 7), y = new Box(10, 7), below = new Box(10, 2);
        drawing.add(x, 0, 0, 1, 1);
        drawing.add(y, 1, 0, 1, 1);
        drawing.add(below, 0, 1, 1, 1);
        drawing.removeElement(x);
        assertEquals(9, drawing.getHeight(), EPS);
        assertBounds(below, 0, 7, 10, 2);
    }

    @Test
    public void removalTellsTheListenersAboutTheShrinkOnlyIfThereIsOne() {
        Fixture f = new Fixture();
        f.events.clear();
        f.drawing.removeElement(f.b);
        assertEquals(List.of("ElementRemoved", "DrawingChangedEvent"), f.eventNames());
        assertSame(f.b, f.events.get(0).getSource());
    }

    @Test
    public void removalForgetsTheElement() {
        Fixture f = new Fixture();
        f.drawing.removeElement(f.d);
        assertNull(f.d.getManager());
        assertNull(f.drawing.getElementCell(f.d));
        assertTrue(f.drawing.getSquareContent(1, 1).isEmpty());
        assertFalse(f.drawing.getElementsInZone(new Rectangle2D.Double(-10, -10, 500, 500)).contains(f.d));
        assertFalse(f.drawing.isEmpty());
    }

    @Test
    public void removedElementsNoLongerAffectTheDrawing() {
        Fixture f = new Fixture();
        f.drawing.removeElement(f.b);
        f.events.clear();
        double width = f.drawing.getWidth();
        f.b.resize(500, 500);
        assertTrue(f.events.isEmpty());
        assertEquals(width, f.drawing.getWidth(), EPS);
    }

    @Test
    public void removingAnUnknownElementDoesNothing() {
        Fixture f = new Fixture();
        f.events.clear();
        Box stranger = new Box(10, 10);
        f.drawing.removeElement(stranger);
        assertTrue(f.events.isEmpty());
        assertEquals(50, f.drawing.getWidth(), EPS);
    }

    @Test
    public void removingTwiceDoesNothingTheSecondTime() {
        Fixture f = new Fixture();
        f.drawing.removeElement(f.b);
        f.events.clear();
        f.drawing.removeElement(f.b);
        assertTrue(f.events.isEmpty());
        assertEquals(28, f.drawing.getWidth(), EPS);
    }

    @Test
    public void removingEverythingEmptiesTheDrawing() {
        Fixture f = new Fixture();
        for (Box e : f.all())
            f.drawing.removeElement(e);
        assertTrue(f.drawing.isEmpty());
        assertEquals(0, f.drawing.getWidth(), EPS);
        assertEquals(0, f.drawing.getHeight(), EPS);
    }

    @Test
    public void removalGivesTheSameLayoutAsLayingOutFromScratch() {
        for (int i = 0; i < 4; i++) {
            Fixture f = new Fixture();
            Box removed = f.all().get(i);
            f.drawing.removeElement(removed);
            List<Box> remaining = new ArrayList<>(f.all());
            remaining.remove(removed);
            assertAllFit(remaining);
            assertIsCanonicalLayout(f.drawing, remaining);
        }
    }

    @Test
    public void removalInAnyOrderGivesTheSameLayoutAsLayingOutFromScratch() {
        // All 24 removal orders.
        int[][] orders = permutations(4);
        for (int[] order : orders) {
            Fixture f = new Fixture();
            List<Box> remaining = new ArrayList<>(f.all());
            for (int i : order) {
                Box removed = f.all().get(i);
                f.drawing.removeElement(removed);
                remaining.remove(removed);
                assertAllFit(remaining);
                assertIsCanonicalLayout(f.drawing, remaining);
            }
        }
    }

    @Test
    public void removingASpanningElementShrinksAllItsColumns() {
        TabularDrawing drawing = new TabularDrawing();
        Box wide = new Box(40, 4), x = new Box(10, 6), y = new Box(10, 6);
        drawing.add(wide, 0, 0, 2, 1);
        drawing.add(x, 0, 1, 1, 1);
        drawing.add(y, 1, 1, 1, 1);
        drawing.removeElement(wide);
        assertEquals(20, drawing.getWidth(), EPS);
        assertEquals(6, drawing.getHeight(), EPS);
        assertBounds(x, 0, 0, 10, 6);
        assertBounds(y, 10, 0, 10, 6);
        assertTrue(drawing.getSquareContent(1, 0).isEmpty());
    }

    @Test
    public void removingAnElementUnderASpanningOneKeepsTheSpanningOneFitting() {
        TabularDrawing drawing = new TabularDrawing();
        Box wide = new Box(40, 4), x = new Box(30, 6);
        drawing.add(wide, 0, 0, 2, 1);
        drawing.add(x, 1, 1, 1, 1);
        // Columns are 20 and 30.
        drawing.removeElement(x);
        assertAllFit(List.of(wide));
        assertIsCanonicalLayout(drawing, List.of(wide));
        assertEquals(40, drawing.getWidth(), EPS);
    }

    @Test
    public void removalNeverMakesTheDrawingLargerOrLeavesElementsTooSmall() {
        // "wide" has grown after having been placed: the extra width went to its first column,
        // so the columns are no longer an even share of it.
        TabularDrawing drawing = new TabularDrawing();
        Box wide = new Box(40, 4), x = new Box(10, 6), y = new Box(10, 6);
        drawing.add(wide, 0, 0, 2, 1);
        drawing.add(x, 0, 1, 1, 1);
        drawing.add(y, 1, 1, 1, 1);
        wide.resize(60, 4);
        List<Box> remaining = List.of(wide, x);
        double widthBefore = drawing.getWidth();
        assertAllFit(List.of(wide, x, y));

        drawing.removeElement(y);

        assertAllFit(remaining);
        assertTrue(drawing.getWidth() <= widthBefore + EPS,
                "removal enlarged the drawing: " + widthBefore + " -> " + drawing.getWidth());
    }

    @Test
    public void removingTheElementWhichEnlargedAFirstColumnKeepsTheSpanningOneFitting() {
        TabularDrawing drawing = new TabularDrawing();
        Box wide = new Box(40, 4), x = new Box(10, 6);
        drawing.add(wide, 0, 0, 2, 1);
        drawing.add(x, 0, 1, 1, 1);
        wide.resize(60, 4); // column 0 gets the 20 extra units: 40 + 20.
        assertAllFit(List.of(wide, x));

        drawing.removeElement(x);

        assertAllFit(List.of(wide));
    }

    @Test
    public void removalWhileTheDrawingIsBeingChangedWaitsForTheEndOfTheChange() {
        Fixture f = new Fixture();
        f.drawing.setBeingChanged(true);
        f.events.clear();
        f.drawing.removeElement(f.b);
        assertEquals(List.of("ElementRemoved"), f.eventNames());
        assertEquals(50, f.drawing.getWidth(), EPS);
        f.drawing.setBeingChanged(false);
        assertEquals(28, f.drawing.getWidth(), EPS);
        assertBounds(f.d, 20, 5, 8, 9);
    }

    @Test
    public void removedElementCanBeAddedAgain() {
        Fixture f = new Fixture();
        f.drawing.removeElement(f.b);
        f.drawing.add(f.b, 1, 0, 1, 1);
        assertEquals(50, f.drawing.getWidth(), EPS);
        assertBounds(f.b, 20, 0, 30, 7);
        assertNotNull(f.b.getManager());
    }

    // ---------------------------------------------------------------------
    // Clear and translate
    // ---------------------------------------------------------------------

    @Test
    public void clearEmptiesTheDrawingAndReleasesTheElements() {
        Fixture f = new Fixture();
        f.events.clear();
        f.drawing.clear();
        assertTrue(f.drawing.isEmpty());
        assertEquals(0, f.drawing.getNumberOfColumns());
        assertEquals(0, f.drawing.getNumberOfRows());
        assertEquals(0, f.drawing.getWidth(), EPS);
        for (Box e : f.all())
            assertNull(e.getManager());
        assertEquals(List.of("DrawingChangedEvent"), f.eventNames());
        // Released elements are deaf.
        f.events.clear();
        f.a.resize(100, 100);
        assertTrue(f.events.isEmpty());
    }

    @Test
    public void drawingCanBeReusedAfterClear() {
        Fixture f = new Fixture();
        f.drawing.clear();
        Box x = new Box(12, 3);
        f.drawing.add(x, 0, 0, 1, 1);
        assertEquals(12, f.drawing.getWidth(), EPS);
        assertEquals(3, f.drawing.getHeight(), EPS);
        assertBounds(x, 0, 0, 12, 3);
    }

    @Test
    public void translateInsertsEmptyColumns() {
        Fixture f = new Fixture();
        f.drawing.translate(1, 2);
        assertEquals(4, f.drawing.getNumberOfColumns());
        assertEquals(3, f.drawing.getElementCell(f.b).getFirstColumn());
        assertEquals(0, f.drawing.getElementCell(f.a).getFirstColumn());
        assertTrue(f.drawing.getColumnContent(1).isEmpty());
        // Empty columns take no room.
        assertEquals(50, f.drawing.getWidth(), EPS);
        assertBounds(f.b, 20, 0, 30, 7);
        assertAllFit(f.all());
    }

    @Test
    public void translateCanRemoveEmptyColumns() {
        Fixture f = new Fixture();
        f.drawing.translate(1, 2);
        f.drawing.translate(1, -2);
        assertEquals(2, f.drawing.getNumberOfColumns());
        assertEquals(1, f.drawing.getElementCell(f.b).getFirstColumn());
        assertBounds(f.b, 20, 0, 30, 7);
    }

    // ---------------------------------------------------------------------

    private static int[][] permutations(int n) {
        List<int[]> result = new ArrayList<>();
        permute(new int[n], new boolean[n], 0, result);
        return result.toArray(new int[0][]);
    }

    private static void permute(int[] current, boolean[] used, int pos, List<int[]> result) {
        if (pos == current.length) {
            result.add(current.clone());
            return;
        }
        for (int i = 0; i < current.length; i++) {
            if (!used[i]) {
                used[i] = true;
                current[pos] = i;
                permute(current, used, pos + 1, result);
                used[i] = false;
            }
        }
    }
}
