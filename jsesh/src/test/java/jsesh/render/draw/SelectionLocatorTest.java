package jsesh.render.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.model.TextScope;
import jsesh.render.view.MDCView;
import jsesh.ui.editor.JMDCEditor;

/// The area of a selection inside a cartouche is computed from the views only.
class SelectionLocatorTest {

    private static final double EPSILON = 1e-6;

    /// The content of the cartouche, when it is the first item of the text.
    private static final TextScope CARTOUCHE = TextScope.of(0, 0, 0, 0);

    private final JMDCEditor editor = new JMDCEditor();

    private List<Rectangle2D> locate(String mdc, TextScope scope, int start, int end) {
        editor.setMDCText(mdc);
        return SelectionLocator.locate(editor.getView(), scope, start, end);
    }

    private Rectangle2D topLevelBounds(int i) {
        MDCView v = editor.getView().getSubView(i);
        return new Rectangle2D.Double(v.getPosition().x, v.getPosition().y, v.getWidth(), v.getHeight());
    }

    private static boolean contains(Rectangle2D outer, Rectangle2D inner) {
        return inner.getMinX() >= outer.getMinX() - EPSILON && inner.getMaxX() <= outer.getMaxX() + EPSILON
                && inner.getMinY() >= outer.getMinY() - EPSILON && inner.getMaxY() <= outer.getMaxY() + EPSILON;
    }

    @Test
    void oneQuadratOfACartouche() {
        List<Rectangle2D> areas = locate("<-A1-N5-D21->", CARTOUCHE, 1, 2);
        assertEquals(1, areas.size());
        Rectangle2D cartouche = topLevelBounds(0);
        // Inside the cartouche, and much narrower.
        assertTrue(contains(cartouche, areas.get(0)), areas + " / " + cartouche);
        assertTrue(areas.get(0).getWidth() < cartouche.getWidth() / 2);
    }

    @Test
    void consecutiveQuadratsMakeOneArea() {
        List<Rectangle2D> one = locate("<-A1-N5-D21->", CARTOUCHE, 0, 1);
        List<Rectangle2D> all = locate("<-A1-N5-D21->", CARTOUCHE, 0, 3);
        assertEquals(1, all.size());
        assertTrue(all.get(0).getWidth() > one.get(0).getWidth());
        assertTrue(contains(all.get(0), one.get(0)));
    }

    @Test
    void cartoucheAfterOtherItems() {
        List<Rectangle2D> areas = locate("A1-<-N5-D21->", TextScope.of(1, 0, 0, 0), 1, 2);
        assertEquals(1, areas.size());
        assertTrue(contains(topLevelBounds(1), areas.get(0)));
    }

    @Test
    void nothingToHighlight() {
        assertTrue(locate("<-A1-N5->", CARTOUCHE, 1, 1).isEmpty());
        // The path doesn't lead to a view.
        assertTrue(locate("A1", TextScope.of(3, 0, 0, 0), 0, 1).isEmpty());
    }
}
