package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.Test;

import jsesh.model.MDCPosition;
import jsesh.model.TextScope;
import jsesh.render.view.MDCView;

/// Mouse clicks place the cursor in the text, and inside cartouches, but not
/// inside quadrats.
class JMDCEditorMouseTest {

    private final JMDCEditor editor = new JMDCEditor();

    /// The centre of a view of the text, as a mouse position in the editor.
    ///
    /// @param path the child indices of the view.
    /// @param fx the horizontal fraction of the view's width.
    private Point mouseIn(double fx, int... path) {
        MDCView view = editor.getView();
        AffineTransform toRoot = new AffineTransform(view.getAffineTransform());
        for (int i : path) {
            Rectangle2D bounds = view.getSubViewBounds(i);
            view = view.getSubView(i);
            toRoot.translate(bounds.getMinX(), bounds.getMinY());
            toRoot.concatenate(view.getAffineTransform());
        }
        Point2D p = toRoot.transform(new Point2D.Double(fx * view.getInternalWidth(), view.getInternalHeight() / 2),
                null);
        return new Point((int) Math.round(p.getX() * editor.getScale()), (int) Math.round(p.getY() * editor.getScale()));
    }

    private MDCPosition caret() {
        return editor.getWorkflow().getCaret().getInsertExactPosition();
    }

    @Test
    void clickInACartouchePlacesTheCursorInIt() {
        editor.setMDCText("<-A1-N5->-D21");
        editor.moveCursorToMouse(mouseIn(0.75, 0, 0, 0, 0, 1));
        assertEquals(TextScope.of(0, 0, 0, 0).positionAt(2), caret());
    }

    @Test
    void clickOutsideTheCartoucheMovesTheCursorBackToTheTopLevel() {
        editor.setMDCText("<-A1-N5->-D21");
        editor.moveCursorToMouse(mouseIn(0.25, 0, 0, 0, 0, 0));
        editor.moveCursorToMouse(mouseIn(0.75, 1));
        assertEquals(new MDCPosition(2), caret());
    }

    @Test
    void clickInAQuadratDoesNotEnterIt() {
        editor.setMDCText("A1*N5-D21");
        editor.moveCursorToMouse(mouseIn(0.25, 0));
        assertEquals(new MDCPosition(0), caret());
    }
}
