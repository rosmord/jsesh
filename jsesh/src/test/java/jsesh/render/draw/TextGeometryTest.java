package jsesh.render.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.Test;

import jsesh.model.MDCPosition;
import jsesh.model.TextScope;
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

    /// Wraps the lines of the editor (soft wrapping), narrow enough for two signs per row.
    private void wrapAfterTwoSigns() {
        MDCView probe = geometryView("A1-A1");
        float width = (float) (probe.getWidth() + 1);
        editor.setJSeshStyle(editor.getJSeshStyle().copy().geometry(g -> g.wrapWidth(width)).build());
    }

    private MDCView geometryView(String mdc) {
        editor.setMDCText(mdc);
        return editor.getView();
    }

    @Test
    void clickAfterTheEndOfAWrappedRowGoesAfterItsLastSign() {
        wrapAfterTwoSigns();
        // Views: A1, A1 on the first row, A1, A1 on the second one.
        TextGeometry geometry = geometry("A1-A1-A1-A1");
        MDCView root = editor.getView();
        assertTrue(root.getSubView(2).getPosition().y > root.getSubView(1).getPosition().y, "wrapped");
        Point2D inSecond = pointIn(1, 0.5, 0.5);
        assertEquals(2, positionAt(geometry, new Point2D.Double(inSecond.getX() + 10000, inSecond.getY())));
    }

    @Test
    void areaAroundAWrapPointIsOnTheNextRow() {
        wrapAfterTwoSigns();
        TextGeometry geometry = geometry("A1-A1-A1-A1");
        Rectangle2D third = editor.getView().getSubViewBounds(2);
        Rectangle2D area = geometry.areaAround(new MDCPosition(2));
        assertEquals(third.getMinX(), area.getMinX(), EPSILON);
        assertEquals(third.getMinY(), area.getMinY(), EPSILON);
    }

    // --- Positions inside cartouches ---------------------------------------

    /// The content of the cartouche, when it is the first item of the text.
    private static final TextScope CARTOUCHE = TextScope.of(0, 0, 0, 0);

    /// A point inside a view designated by its model path, in drawing
    /// coordinates.
    private Point2D pointInView(double fx, double fy, int... path) {
        PlacedView placed = PlacedView.root(editor.getView()).follow(path).orElseThrow();
        MDCView view = placed.view();
        Point2D local = new Point2D.Double(fx * view.getInternalWidth(), fy * view.getInternalHeight());
        return placed.toRoot(local);
    }

    private MDCPosition inCartouche(double fx, int item) {
        return geometryPosition(pointInView(fx, 0.5, 0, 0, 0, 0, item));
    }

    private MDCPosition geometryPosition(Point2D point) {
        return new TextGeometry(editor.getView(), editor.getJSeshStyle()).positionAt(point);
    }

    @Test
    void clickInsideACartoucheGivesAPositionOfItsContent() {
        editor.setMDCText("<-A1-N5->-D21");
        assertEquals(CARTOUCHE.positionAt(0), inCartouche(0.25, 0));
        assertEquals(CARTOUCHE.positionAt(1), inCartouche(0.75, 0));
        assertEquals(CARTOUCHE.positionAt(1), inCartouche(0.25, 1));
        assertEquals(CARTOUCHE.positionAt(2), inCartouche(0.75, 1));
    }

    @Test
    void clickOnTheFrameOfACartoucheStaysAtTheTopLevel() {
        editor.setMDCText("<-A1-N5->-D21");
        PlacedView cartouche = PlacedView.root(editor.getView()).follow(new int[] { 0, 0, 0 }).orElseThrow();
        // The very left of the cartouche, where the frame is.
        Point2D onFrame = cartouche.toRoot(new Point2D.Double(0.5, cartouche.view().getInternalHeight() / 2));
        MDCPosition p = geometryPosition(onFrame);
        assertTrue(p.isTop(), p.toString());
    }

    @Test
    void clickInsideAQuadratGivesATopLevelPosition() {
        editor.setMDCText("A1*N5-D21");
        MDCPosition p = geometryPosition(pointIn(0, 0.25, 0.25));
        assertEquals(new MDCPosition(0), p);
        p = geometryPosition(pointIn(0, 0.75, 0.75));
        assertEquals(new MDCPosition(1), p);
    }

    @Test
    void clickInsideACartoucheQuadratGivesAPositionOfTheCartoucheNotOfTheQuadrat() {
        editor.setMDCText("<-A1*N5->-D21");
        MDCPosition p = geometryPosition(pointInView(0.75, 0.75, 0, 0, 0, 0, 0));
        assertEquals(CARTOUCHE.positionAt(1), p);
    }

    @Test
    void clickInsideASubQuadratIsNotEntered() {
        editor.setMDCText("(A1-N5)-D21");
        // Anywhere in the sub-quadrat's content.
        PlacedView list = PlacedView.root(editor.getView()).follow(new int[] { 0, 0, 0, 0 }).orElseThrow();
        Point2D inside = list.toRoot(new Point2D.Double(list.view().getInternalWidth() * 0.25,
                list.view().getInternalHeight() / 2));
        assertTrue(geometryPosition(inside).isTop());
    }

    @Test
    void rightToLeftCartoucheClicksAreMirrored() {
        editor.setTextDirection(TextDirection.RIGHT_TO_LEFT);
        editor.setMDCText("<-A1-N5->-D21");
        // In a right-to-left text, the first sign is at the right.
        assertEquals(CARTOUCHE.positionAt(1), inCartouche(0.25, 0));
        assertEquals(CARTOUCHE.positionAt(0), inCartouche(0.75, 0));
    }

    @Test
    void verticalCartoucheClicksDependOnTheVerticalHalf() {
        editor.setTextOrientation(TextOrientation.VERTICAL);
        editor.setMDCText("<-A1-N5->-D21");
        assertEquals(CARTOUCHE.positionAt(0), geometryPosition(pointInView(0.5, 0.25, 0, 0, 0, 0, 0)));
        assertEquals(CARTOUCHE.positionAt(1), geometryPosition(pointInView(0.5, 0.75, 0, 0, 0, 0, 0)));
    }
}
