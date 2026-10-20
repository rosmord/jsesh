package org.qenherkhopeshef.viewToolKit.drawing;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.PolylineElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.SpacerElement;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingEvent;

public class GridDrawingTest {

    @Test
    public void elementsAreFoundInTheirZone() {
        GridDrawing drawing = new GridDrawing(50, 50);
        SpacerElement a = new SpacerElement(10, 10);
        SpacerElement b = new SpacerElement(10, 10);
        drawing.putAt(a, 5, 5);
        drawing.putAt(b, 500, 500);
        assertEquals(List.of(a), new ArrayList<>(drawing.getElementsInZone(new Rectangle2D.Double(0, 0, 100, 100))));
        assertEquals(List.of(b), new ArrayList<>(drawing.getElementsInZone(new Rectangle2D.Double(490, 490, 30, 30))));
        assertEquals(2, drawing.getElementsInZone(new Rectangle2D.Double(-1000, -1000, 5000, 5000)).size());
    }

    @Test
    public void movedElementsAreReindexed() {
        GridDrawing drawing = new GridDrawing(50, 50);
        SpacerElement a = new SpacerElement(10, 10);
        drawing.putAt(a, 5, 5);
        List<DrawingEvent> events = new ArrayList<>();
        drawing.addDrawingListener(events::add);
        a.setOrigin(1000, 1000); // moved directly, not through the drawing.
        assertFalse(events.isEmpty());
        assertTrue(drawing.getElementsInZone(new Rectangle2D.Double(0, 0, 100, 100)).isEmpty());
        assertEquals(1, drawing.getElementsAt(new Point2D.Double(1005, 1005)).size());
        assertTrue(drawing.getPreferredSize().getMaxX() >= 1010);
    }

    @Test
    public void largeElementsAreFound() {
        GridDrawing drawing = new GridDrawing(10, 10);
        PolylineElement line = new PolylineElement(List.of(new Point2D.Double(0, 0), new Point2D.Double(5000, 5000)), false);
        drawing.addElement(line);
        assertEquals(1, drawing.getElementsInZone(new Rectangle2D.Double(2500, 2500, 5, 5)).size());
        assertTrue(line.contains(new Point2D.Double(2500, 2500), 1));
        assertFalse(line.contains(new Point2D.Double(2500, 2600), 1));
    }

    @Test
    public void drawingOrderFollowsLayerThenInsertion() {
        GridDrawing drawing = new GridDrawing();
        SpacerElement a = new SpacerElement(10, 10);
        SpacerElement b = new SpacerElement(10, 10);
        SpacerElement c = new SpacerElement(10, 10);
        drawing.putAt(a, 0, 0);
        drawing.putAt(b, 0, 0);
        drawing.putAt(c, 0, 0);
        c.setLayer(-1);
        assertEquals(List.of(c, a, b), drawing.getElements());
        drawing.bringToFront(a);
        assertEquals(List.of(c, b, a), new ArrayList<GraphicalElement>(drawing.getElementsInZone(new Rectangle2D.Double(0, 0, 5, 5))));
    }

    @Test
    public void removal() {
        GridDrawing drawing = new GridDrawing();
        SpacerElement a = new SpacerElement(10, 10);
        drawing.putAt(a, 0, 0);
        drawing.remove(a);
        assertTrue(drawing.isEmpty());
        assertTrue(drawing.getElementsInZone(new Rectangle2D.Double(0, 0, 50, 50)).isEmpty());
        assertNull(a.getManager());
    }
}
