package jsesh.render.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import jsesh.model.MDCPosition;
import jsesh.model.constants.TextOrientation;
import jsesh.render.view.MDCView;
import jsesh.ui.editor.JMDCEditor;

/// The cursor geometry is computed from the views only: no `Graphics2D` needed.
class CursorLocatorTest {

    private static final double EPSILON = 1e-6;

    private final JMDCEditor editor = new JMDCEditor();

    private Optional<CursorShape> locate(String mdc, MDCPosition position) {
        editor.setMDCText(mdc);
        return new TextGeometry(editor.getView(), editor.getJSeshStyle()).cursorAt(position);
    }

    private static Rectangle2D bounds(MDCView v, MDCView parent) {
        return new Rectangle2D.Double(v.getPosition().x, v.getPosition().y, v.getWidth(), v.getHeight());
    }

    @Test
    void topLevelCursorStandsBetweenTwoViews() {
        Optional<CursorShape> location = locate("A1-N5-D21", new MDCPosition(1));
        assertTrue(location.isPresent());
        assertTrue(location.get().wide());
        MDCView root = editor.getView();
        Rectangle2D first = bounds(root.getSubView(0), root);
        Rectangle2D second = bounds(root.getSubView(1), root);
        // A single line, in the gap between the two views.
        assertEquals(1, location.get().lines().size());
        double x = location.get().lines().get(0).getX1();
        assertTrue(x >= first.getMaxX() - EPSILON && x <= second.getMinX() + EPSILON);
    }

    @Test
    void columnCursorBetweenTwoViewsIsASingleLineInTheGap() {
        editor.setTextOrientation(TextOrientation.VERTICAL);
        Optional<CursorShape> location = locate("A1-N5-D21", new MDCPosition(1));
        assertTrue(location.isPresent());
        assertEquals(1, location.get().lines().size());
        MDCView root = editor.getView();
        MDCView first = root.getSubView(0);
        MDCView second = root.getSubView(1);
        Line2D line = location.get().lines().get(0);
        // Horizontal line, in the gap between the two quadrats.
        assertEquals(line.getY1(), line.getY2(), EPSILON);
        assertTrue(line.getY1() >= first.getPosition().y + first.getHeight() - EPSILON);
        assertTrue(line.getY1() <= second.getPosition().y + EPSILON);
    }

    @Test
    void cursorAtStartAndEndOfText() {
        Optional<CursorShape> start = locate("A1-N5", new MDCPosition(0));
        assertTrue(start.isPresent());
        assertEquals(1, start.get().lines().size());
        assertEquals(editor.getView().getSubView(0).getPosition().x, start.get().lines().get(0).getX1(), EPSILON);

        Optional<CursorShape> end = locate("A1-N5", new MDCPosition(2));
        assertTrue(end.isPresent());
        MDCView last = editor.getView().getSubView(1);
        assertEquals(last.getPosition().x + last.getWidth(), end.get().lines().get(0).getX1(), EPSILON);
    }

    @Test
    void cursorInEmptyText() {
        Optional<CursorShape> location = locate("", new MDCPosition(0));
        assertTrue(location.isPresent());
        Line2D line = location.get().lines().get(0);
        assertEquals(0, line.getX1(), EPSILON);
        assertEquals(0, line.getY1(), EPSILON);
    }

    @Test
    void noCursorAfterLineBreak() {
        // Position 2 stands after the break: the cursor is at the start of the next line, not after the break.
        Optional<CursorShape> location = locate("A1-!-N5", new MDCPosition(2));
        assertTrue(location.isPresent());
        MDCView after = editor.getView().getSubView(2);
        assertEquals(1, location.get().lines().size());
        assertEquals(after.getPosition().x, location.get().lines().get(0).getX1(), EPSILON);
        assertEquals(after.getPosition().y, location.get().lines().get(0).getY1(), EPSILON);
    }

    @Test
    void innerCursorStandsBetweenTheSubviewsOfItsContainer() {
        // Quadrat 1 is "N5*N5*N5": a row of three signs inside a horizontal box.
        Optional<CursorShape> location = locate("A1-N5*N5*N5-D21:D21", MDCPosition.of(1, 0, 1));
        assertTrue(location.isPresent());
        assertFalse(location.get().wide());
        assertEquals(1, location.get().lines().size());

        MDCView quadrat = editor.getView().getSubView(1);
        MDCView hbox = quadrat.getSubView(0);
        Line2D line = location.get().lines().get(0);
        // Vertical line.
        assertEquals(line.getX1(), line.getX2(), EPSILON);
        // The line is expressed in the root's coordinates: offset the container's subviews.
        double offset = quadrat.getPosition().x + hbox.getPosition().x;
        double leftOfGap = offset + hbox.getSubView(0).getPosition().x + hbox.getSubView(0).getWidth();
        double rightOfGap = offset + hbox.getSubView(1).getPosition().x;
        assertTrue(line.getX1() >= leftOfGap - EPSILON && line.getX1() <= rightOfGap + EPSILON);
        // And it is inside the quadrat.
        Rectangle2D quadratBounds = new Rectangle2D.Double(quadrat.getPosition().x, quadrat.getPosition().y,
                quadrat.getWidth(), quadrat.getHeight());
        assertTrue(quadratBounds.contains(line.getX1(), (line.getY1() + line.getY2()) / 2));

        // Moving the gap moves the line.
        Optional<CursorShape> next = new TextGeometry(editor.getView(), editor.getJSeshStyle())
                .cursorAt(MDCPosition.of(1, 0, 2));
        assertTrue(next.get().lines().get(0).getX1() > line.getX1());
    }

    @Test
    void innerCursorBetweenRowsIsHorizontal() {
        Optional<CursorShape> location = locate("A1-N5*N5*N5-D21:D21", MDCPosition.of(2, 1));
        assertTrue(location.isPresent());
        Line2D line = location.get().lines().get(0);
        assertEquals(line.getY1(), line.getY2(), EPSILON);
    }

    @Test
    void positionsOutsideOfTheViewGiveNothing() {
        assertTrue(locate("A1-N5", new MDCPosition(3)).isEmpty());
        assertTrue(locate("A1-N5", MDCPosition.of(5, 0, 1)).isEmpty());
        // A1 is a single sign: no gap inside it.
        assertTrue(locate("A1-N5", MDCPosition.of(0, 0, 1)).isEmpty());
    }

    @Test
    void cursorInAbsoluteGroupMarksTheEndOfThePrecedingSign() {
        // An absolute group: signs are placed explicitly, the cursor stands in their order.
        String mdc = "A1-n**x{{967,79,100}}**N5{{0,0,100}}-D21";
        Optional<CursorShape> location = locate(mdc, MDCPosition.of(1, 0, 0, 1));
        assertTrue(location.isPresent());
        assertFalse(location.get().wide());
        assertEquals(1, location.get().lines().size());
        Line2D line = location.get().lines().get(0);
        assertEquals(line.getX1(), line.getX2(), EPSILON);

        MDCView quadrat = editor.getView().getSubView(1);
        MDCView group = quadrat.getSubView(0).getSubView(0);
        MDCView sign = group.getSubView(0);
        double offset = quadrat.getPosition().x + quadrat.getSubView(0).getPosition().x + group.getPosition().x;
        assertEquals(offset + sign.getPosition().x + sign.getWidth(), line.getX1(), EPSILON);

        // The next gap marks another sign.
        Optional<CursorShape> next = locate(mdc, MDCPosition.of(1, 0, 0, 2));
        assertTrue(next.isPresent());
        assertTrue(Math.abs(next.get().lines().get(0).getX1() - line.getX1()) > EPSILON);
    }

    @Test
    void cursorInLigatureFollowsThePlacementOfTheSigns() {
        // In the ligature table, the two signs of i&i are side by side.
        Optional<CursorShape> location = locate("A1-i&i-D21", MDCPosition.of(1, 0, 0, 1));
        assertTrue(location.isPresent());
        assertFalse(location.get().wide());
        Line2D line = location.get().lines().get(0);
        // Vertical line, between the two signs.
        assertEquals(line.getX1(), line.getX2(), EPSILON);
        MDCView quadrat = editor.getView().getSubView(1);
        MDCView hbox = quadrat.getSubView(0);
        MDCView ligature = hbox.getSubView(0);
        double offset = quadrat.getPosition().x + hbox.getPosition().x + ligature.getPosition().x;
        MDCView first = ligature.getSubView(0);
        MDCView second = ligature.getSubView(1);
        assertTrue(line.getX1() >= offset + first.getPosition().x + first.getWidth() - EPSILON);
        assertTrue(line.getX1() <= offset + second.getPosition().x + EPSILON);
    }
}
