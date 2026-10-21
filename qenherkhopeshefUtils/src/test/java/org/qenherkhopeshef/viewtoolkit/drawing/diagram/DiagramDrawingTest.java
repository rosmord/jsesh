package org.qenherkhopeshef.viewtoolkit.drawing.diagram;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.viewtoolkit.drawing.diagram.ConnectorElement;
import org.qenherkhopeshef.viewtoolkit.drawing.diagram.DiagramDrawing;
import org.qenherkhopeshef.viewtoolkit.drawing.element.SpacerElement;

public class DiagramDrawingTest {

    @Test
    public void connectorEndsOnTheBordersOfItsNodes() {
        DiagramDrawing drawing = new DiagramDrawing();
        SpacerElement a = new SpacerElement(100, 50);
        SpacerElement b = new SpacerElement(100, 50);
        drawing.putAt(a, 0, 0);
        drawing.putAt(b, 300, 0);
        ConnectorElement c = new ConnectorElement(a, b);
        drawing.addConnector(c);
        // The line goes from x=100 to x=300, at y=25.
        assertTrue(c.contains(new Point2D.Double(200, 25), 1));
        assertFalse(c.contains(new Point2D.Double(50, 25), 1));
    }

    @Test
    public void connectorFollowsMovedNodes() {
        DiagramDrawing drawing = new DiagramDrawing();
        SpacerElement a = new SpacerElement(100, 50);
        SpacerElement b = new SpacerElement(100, 50);
        drawing.putAt(a, 0, 0);
        drawing.putAt(b, 300, 0);
        ConnectorElement c = new ConnectorElement(a, b);
        drawing.addConnector(c);
        b.setOrigin(300, 1000);
        Rectangle2D bounds = c.getBounds();
        assertTrue(bounds.getMaxY() > 900);
        assertTrue(drawing.getElementsInZone(new Rectangle2D.Double(0, 500, 400, 10)).contains(c));
    }

    @Test
    public void removingANodeRemovesItsConnectors() {
        DiagramDrawing drawing = new DiagramDrawing();
        SpacerElement a = new SpacerElement(10, 10);
        SpacerElement b = new SpacerElement(10, 10);
        drawing.putAt(a, 0, 0);
        drawing.putAt(b, 100, 0);
        ConnectorElement c = new ConnectorElement(a, b);
        drawing.addConnector(c);
        drawing.remove(a);
        assertFalse(drawing.containsElement(c));
        assertTrue(drawing.getConnectors(b).isEmpty());
    }

    @Test
    public void selfConnector() {
        DiagramDrawing drawing = new DiagramDrawing();
        SpacerElement a = new SpacerElement(100, 100);
        drawing.putAt(a, 100, 100);
        ConnectorElement c = new ConnectorElement(a, a);
        drawing.addConnector(c);
        assertTrue(c.getBounds().getMaxX() > 200);
        assertTrue(c.getBounds().getMinY() < 100);
    }
}
