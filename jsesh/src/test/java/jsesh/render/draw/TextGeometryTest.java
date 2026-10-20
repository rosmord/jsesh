package jsesh.render.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.Test;

import jsesh.model.MDCPosition;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.render.view.MDCView;
import jsesh.ui.editor.JMDCEditor;

/// The mapping between points and top-level positions (mouse clicks, scrolling
/// to the cursor).
class TextGeometryTest {

    private static final double EPSILON = 1e-6;

    private final JMDCEditor editor = new JMDCEditor();

    private TextGeometry geometry(String mdc) {
        editor.setMDCText(mdc);
        return new TextGeometry(editor.getView(), editor.getJSeshStyle());
    }

    /// A point inside a top-level view, in drawing coordinates.
    ///
    /// @param i the index of the view.
    /// @param fx the horizontal fraction of the view's width.
    /// @param fy the vertical fraction of the view's height.
    private Point2D pointIn(int i, double fx, double fy) {
        MDCView root = editor.getView();
        Rectangle2D bounds = root.getSubViewBounds(i);
        Point2D local = new Point2D.Double(bounds.getX() + fx * bounds.getWidth(),
                bounds.getY() + fy * bounds.getHeight());
        return root.getAffineTransform().transform(local, null);
    }

    private int positionAt(TextGeometry geometry, Point2D point) {
        return geometry.positionAt(point).getIndex();
    }

    @Test
    void clickInLeftHalfGoesBeforeTheSign() {
        TextGeometry geometry = geometry("A1-N5-D21");
        assertEquals(1, positionAt(geometry, pointIn(1, 0.25, 0.5)));
        assertEquals(2, positionAt(geometry, pointIn(1, 0.75, 0.5)));
    }

    @Test
    void rightToLeftClicksAreMirrored() {
        editor.setTextDirection(TextDirection.RIGHT_TO_LEFT);
        TextGeometry geometry = geometry("A1-N5-D21");
        // Drawn mirrored: the left half of the sign is its end.
        assertEquals(2, positionAt(geometry, pointIn(1, 0.25, 0.5)));
        assertEquals(1, positionAt(geometry, pointIn(1, 0.75, 0.5)));
    }

    @Test
    void columnClicksDependOnTheVerticalHalf() {
        editor.setTextOrientation(TextOrientation.VERTICAL);
        TextGeometry geometry = geometry("A1-N5-D21");
        assertEquals(1, positionAt(geometry, pointIn(1, 0.5, 0.25)));
        assertEquals(2, positionAt(geometry, pointIn(1, 0.5, 0.75)));
    }

    @Test
    void clickAfterEverythingGoesToTheEndOfTheText() {
        TextGeometry geometry = geometry("A1-N5-D21");
        assertEquals(3, positionAt(geometry, new Point2D.Double(10000, 10000)));
    }

    @Test
    void clickInEmptyText() {
        TextGeometry geometry = geometry("");
        assertEquals(0, positionAt(geometry, new Point2D.Double(10, 10)));
    }

    @Test
    void clickAfterTheEndOfALineGoesBeforeTheBreak() {
        // Views: A1, N5, the line break, D21 (on the second line).
        TextGeometry geometry = geometry("A1-N5-!-D21");
        Point2D inN5 = pointIn(1, 0.5, 0.5);
        assertEquals(2, positionAt(geometry, new Point2D.Double(inN5.getX() + 10000, inN5.getY())));
    }

    @Test
    void areaAroundSpansTheGapBetweenTwoSigns() {
        TextGeometry geometry = geometry("A1-N5");
        MDCView root = editor.getView();
        Rectangle2D first = root.getSubViewBounds(0);
        Rectangle2D second = root.getSubViewBounds(1);
        Rectangle2D area = geometry.areaAround(new MDCPosition(1));
        assertEquals(first.getMaxX(), area.getMinX(), EPSILON);
        assertEquals(second.getMinX() + 1, area.getMaxX(), EPSILON);
    }

    @Test
    void areaAroundTheEndOfALineIgnoresTheBreak() {
        TextGeometry geometry = geometry("A1-!-N5");
        Rectangle2D first = editor.getView().getSubViewBounds(0);
        Rectangle2D area = geometry.areaAround(new MDCPosition(1));
        assertEquals(first.getMaxX(), area.getMinX(), EPSILON);
        assertEquals(first.getMinY(), area.getMinY(), EPSILON);
        assertEquals(first.getHeight(), area.getHeight(), EPSILON);
    }
}
