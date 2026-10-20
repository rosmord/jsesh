package org.qenherkhopeshef.viewToolKit.drawing.element;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CompositeElementTest {

    private static SpacerElement spacer(double w, double h) {
        return new SpacerElement(w, h);
    }

    @Test
    public void verticalLayoutStacksAndStretches() {
        CompositeElement c = new CompositeElement();
        SpacerElement a = spacer(10, 5), b = spacer(30, 7);
        c.addElement(a);
        c.addElement(b);
        c.pack();
        assertEquals(30, c.getBounds().getWidth());
        assertEquals(12, c.getBounds().getHeight());
        assertEquals(5, b.getBounds().getMinY());
        // a is stretched to the composite width.
        assertEquals(30, a.getBounds().getWidth());
    }

    @Test
    public void horizontalLayoutPlacesSideBySide() {
        CompositeElement c = new CompositeElement();
        c.setAxis(CompositeAxis.HORIZONTAL);
        SpacerElement a = spacer(10, 5), b = spacer(30, 7);
        c.addElement(a);
        c.addElement(b);
        c.pack();
        assertEquals(40, c.getBounds().getWidth());
        assertEquals(7, c.getBounds().getHeight());
        assertEquals(10, b.getBounds().getMinX());
        assertEquals(7, a.getBounds().getHeight());
    }

    @Test
    public void preferredSizeDoesNotDependOnPacking() {
        CompositeElement c = new CompositeElement();
        c.addElement(spacer(10, 5));
        c.addElement(spacer(10, 5));
        assertEquals(10, c.getPreferredSize().getHeight());
    }

    @Test
    public void nestedChangesRelayoutTheComposite() {
        CompositeElement c = new CompositeElement();
        TextElement t = new TextElement("short");
        t.setMargin(Margin.NONE);
        c.addElement(t);
        c.pack();
        double before = c.getBounds().getWidth();
        t.setText("a much, much longer text");
        assertTrue(c.getBounds().getWidth() > before);
    }

    @Test
    public void unsizedElementsFollowTheirPreferredSize() {
        TextElement t = new TextElement("abc");
        double before = t.getBounds().getWidth();
        t.setText("abcdefghijkl");
        assertTrue(t.getBounds().getWidth() > before);
    }
}
