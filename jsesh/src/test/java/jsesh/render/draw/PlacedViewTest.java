package jsesh.render.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.Test;

import jsesh.model.constants.TextDirection;
import jsesh.render.view.MDCView;
import jsesh.ui.editor.JMDCEditor;

/// Following model paths down the view tree.
class PlacedViewTest {

    private static final double EPSILON = 1e-6;

    private final JMDCEditor editor = new JMDCEditor();

    private PlacedView root(String mdc) {
        editor.setMDCText(mdc);
        return PlacedView.root(editor.getView());
    }

    @Test
    void anEmptyPathLeadsToTheViewItself() {
        PlacedView root = root("A1-N5");
        assertSame(editor.getView(), root.follow(new int[0]).orElseThrow().view());
    }

    @Test
    void pathsFollowTheModel() {
        PlacedView root = root("A1-N5*N5");
        MDCView hbox = editor.getView().getSubView(1).getSubView(0);
        PlacedView reached = root.follow(new int[] { 1, 0 }).orElseThrow();
        assertSame(hbox, reached.view());
        assertSame(hbox.getModel(), reached.model());
        assertEquals(2, reached.childCount());
    }

    @Test
    void pathsWhichDontMatchTheViewsLeadNowhere() {
        PlacedView root = root("A1-N5");
        assertTrue(root.follow(new int[] { 5 }).isEmpty());
        assertTrue(root.follow(new int[] { 0, 7 }).isEmpty());
        assertTrue(root.child(-1).isEmpty());
    }

    @Test
    void childBoundsAreInTheLocalSystem() {
        PlacedView root = root("A1-N5");
        MDCView second = editor.getView().getSubView(1);
        Rectangle2D bounds = root.childBounds(1);
        assertEquals(second.getPosition().x, bounds.getMinX(), EPSILON);
        assertEquals(second.getPosition().y, bounds.getMinY(), EPSILON);
        assertEquals(second.getWidth(), bounds.getWidth(), EPSILON);
    }

    @Test
    void childBoundsAreMirroredInRightToLeftViews() {
        editor.setTextDirection(TextDirection.RIGHT_TO_LEFT);
        PlacedView root = root("A1-N5");
        MDCView rootView = editor.getView();
        assertTrue(!rootView.getDirection().isLeftToRight());
        MDCView first = rootView.getSubView(0);
        double expected = rootView.getInternalWidth() - first.getPosition().x - first.getWidth();
        assertEquals(expected, root.childBounds(0).getMinX(), EPSILON);
    }

    @Test
    void nestedBoundsAreConvertedToTheRootSystem() {
        PlacedView root = root("A1-N5*N5");
        PlacedView hbox = root.follow(new int[] { 1, 0 }).orElseThrow();
        Rectangle2D inRoot = hbox.toRoot(hbox.childBounds(0));
        // The first sign of the second quadrat lies inside that quadrat.
        Rectangle2D quadrat = root.childBounds(1);
        assertTrue(inRoot.getMinX() >= quadrat.getMinX() - EPSILON && inRoot.getMaxX() <= quadrat.getMaxX() + EPSILON,
                inRoot + " / " + quadrat);
    }
}
