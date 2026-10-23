package org.qenherkhopeshef.viewtoolkit.drawing.element;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.viewtoolkit.drawing.element.CompositeAxis;
import org.qenherkhopeshef.viewtoolkit.drawing.element.CompositeElement;
import org.qenherkhopeshef.viewtoolkit.drawing.element.Margin;
import org.qenherkhopeshef.viewtoolkit.drawing.element.SpacerElement;
import org.qenherkhopeshef.viewtoolkit.drawing.element.TextElement;
import org.qenherkhopeshef.viewtoolkit.drawing.element.layout.CompositeLayout;
import org.qenherkhopeshef.viewtoolkit.drawing.element.layout.VerticalCompositeLayout;

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

    @Test
    public void addingTheSameElementTwiceIsRefused() {
        CompositeElement c = new CompositeElement();
        SpacerElement a = spacer(10, 5);
        c.addElement(a);
        assertThrows(IllegalArgumentException.class, () -> c.addElement(a));
        assertEquals(1, c.getNumberOfChildren());
    }

    @Test
    public void elementOwnedByAnotherCompositeIsRefused() {
        CompositeElement c1 = new CompositeElement(), c2 = new CompositeElement();
        SpacerElement a = spacer(10, 5);
        c1.addElement(a);
        assertThrows(IllegalArgumentException.class, () -> c2.addElement(a));
        // Once removed, it can move.
        c1.removeElement(a);
        c2.addElement(a);
        assertEquals(1, c2.getNumberOfChildren());
    }

    @Test
    public void failedLayoutDoesNotLeaveTheCompositeDeaf() {
        CompositeElement c = new CompositeElement();
        TextElement t = new TextElement("short");
        t.setMargin(Margin.NONE);
        c.addElement(t);
        VerticalCompositeLayout vertical = new VerticalCompositeLayout();
        c.setLayout(new CompositeLayout() {
            public java.awt.geom.Dimension2D minimalSize(CompositeElement composite) {
                return vertical.minimalSize(composite);
            }
            public void layoutComposite(CompositeElement composite) {
                throw new IllegalStateException("boom");
            }
            public void fitChildren(CompositeElement composite, double width, double height) {
                vertical.fitChildren(composite, width, height);
            }
        });
        assertThrows(IllegalStateException.class, c::pack);
        c.setLayout(vertical);
        c.pack();
        double before = c.getBounds().getWidth();
        // Would be ignored if the guard had stayed raised.
        t.setText("a much, much longer text");
        assertTrue(c.getBounds().getWidth() > before);
    }
}
