package org.qenherkhopeshef.viewtoolkit.drawing.textOriented;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.viewtoolkit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewtoolkit.drawing.element.SpacerElement;
import org.qenherkhopeshef.viewtoolkit.drawing.textOriented.Paragraph;
import org.qenherkhopeshef.viewtoolkit.drawing.textOriented.ParagraphElement;
import org.qenherkhopeshef.viewtoolkit.drawing.textOriented.TextDocument;
import org.qenherkhopeshef.viewtoolkit.drawing.textOriented.TextDrawing;
import org.qenherkhopeshef.viewtoolkit.drawing.textOriented.TextPosition;

public class TextDrawingTest {

    private static final String LOREM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod "
            + "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud "
            + "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.";

    private TextDrawing drawingWith(int paragraphs) {
        TextDocument doc = new TextDocument();
        TextDocument.Builder builder = new TextDocument.Builder();
        for (int i = 0; i < paragraphs; i++) {
            if (i > 0)
                builder.newParagraph();
            builder.appendText(i + " " + LOREM);
        }
        doc.setContent(builder);
        TextDrawing drawing = new TextDrawing(doc);
        drawing.setPageWidth(300);
        return drawing;
    }

    @Test
    public void longParagraphsAreWrapped() {
        TextDrawing drawing = drawingWith(1);
        ParagraphElement e = drawing.getParagraphElement(0);
        assertTrue(e.getLineCount() > 2, "expected several lines");
        // Lines cover the whole paragraph, without overlap.
        int expectedStart = 0;
        for (int i = 0; i < e.getLineCount(); i++) {
            assertEquals(expectedStart, e.getLine(i).getStart());
            expectedStart = e.getLine(i).getEnd();
        }
        assertEquals(e.getParagraph().length(), expectedStart);
    }

    @Test
    public void onlyVisibleParagraphsAreLaidOut() {
        TextDrawing drawing = drawingWith(100_000);
        assertEquals(0.0, drawing.getLayoutCompletion(), 1e-9);
        double middle = drawing.getPreferredSize().getHeight() / 2;
        Collection<GraphicalElement> visible = drawing.getElementsInZone(
                new Rectangle2D.Double(0, middle, 400, 600));
        assertFalse(visible.isEmpty());
        assertTrue(visible.size() < 50);
        assertTrue(drawing.getLayoutCompletion() < 0.001);
        for (GraphicalElement e : visible)
            assertTrue(e.getBounds().intersects(new Rectangle2D.Double(0, middle, 400, 600)));
    }

    @Test
    public void positionAtIsConsistentWithCaretRectangle() {
        TextDrawing drawing = drawingWith(20);
        for (int p = 0; p < 20; p += 7) {
            for (int offset : new int[] { 0, 10, 57, 120 }) {
                TextPosition pos = new TextPosition(p, offset);
                Rectangle2D caret = drawing.getCaretRectangle(pos);
                Point2D center = new Point2D.Double(caret.getX() + 0.5, caret.getCenterY());
                assertEquals(pos, drawing.getPositionAt(center));
            }
        }
    }

    @Test
    public void upAndDownNavigation() {
        TextDrawing drawing = drawingWith(3);
        TextPosition start = new TextPosition(1, 5);
        double x = drawing.getCaretRectangle(start).getX();
        TextPosition below = drawing.getPositionBelow(start, x);
        assertEquals(1, below.paragraph());
        assertTrue(below.offset() > start.offset());
        assertEquals(start, drawing.getPositionAbove(below, x));
        // From the first line of paragraph 1, up goes into paragraph 0.
        assertEquals(0, drawing.getPositionAbove(start, x).paragraph());
    }

    @Test
    public void editsUpdateTheLayout() {
        TextDrawing drawing = drawingWith(2);
        TextDocument doc = drawing.getDocument();
        double before = drawing.getParagraphTop(1);
        doc.insertText(new TextPosition(0, 0), LOREM + LOREM);
        assertTrue(drawing.getParagraphTop(1) > before);
        doc.insertText(new TextPosition(0, 0), "\n");
        assertEquals(3, drawing.getParagraphCount());
        drawing.setCaret(new TextPosition(2, 10_000));
        assertEquals(doc.getEnd(), drawing.getCaret());
    }

    @Test
    public void embeddedElementsEnlargeTheirLine() {
        TextDocument doc = new TextDocument();
        doc.insertText(TextPosition.START, "abc");
        TextDrawing drawing = new TextDrawing(doc);
        double plain = drawing.getParagraphElement(0).getBounds().getHeight();
        doc.insertElement(new TextPosition(0, 1), new SpacerElement(40, 100));
        double withPicture = drawing.getParagraphElement(0).getBounds().getHeight();
        assertTrue(withPicture >= 100);
        assertTrue(withPicture > plain);
    }

    @Test
    public void backgroundLayoutCompletes() {
        TextDrawing drawing = drawingWith(500);
        while (drawing.layoutPendingParagraphs(50_000_000L)) {
            // continue
        }
        assertEquals(1.0, drawing.getLayoutCompletion(), 1e-9);
        // Now, positions are exact: the top of a paragraph is the sum of the
        // heights before it.
        double y = drawing.getParagraphTop(0);
        for (int i = 0; i < 10; i++) {
            ParagraphElement e = drawing.getParagraphElement(i);
            assertEquals(y, e.getBounds().getMinY(), 1e-6);
            y += e.getBounds().getHeight() + drawing.getParagraphSpacing();
        }
    }

    @Test
    public void fastMeasureMatchesFullLayout() {
        TextDrawing drawing = drawingWith(40);
        java.awt.font.FontRenderContext frc = new java.awt.font.FontRenderContext(null, true, true);
        java.awt.font.TextLayout reference = new java.awt.font.TextLayout("x", drawing.getFont(), frc);
        double lineHeight = reference.getAscent() + reference.getDescent() + reference.getLeading();
        for (int i = 0; i < 40; i++) {
            Paragraph p = drawing.getDocument().getParagraph(i);
            double full = new ParagraphElement(p, drawing.getFont(), java.awt.Color.BLACK, 300, frc).getBounds().getHeight();
            double fast = ParagraphElement.measureHeight(p, drawing.getFont(), java.awt.Color.BLACK, 300, frc, lineHeight);
            assertEquals(full, fast, 1e-6, "paragraph " + i);
        }
    }

    @Test
    public void selection() {
        TextDrawing drawing = drawingWith(3);
        drawing.setCaret(new TextPosition(2, 4));
        drawing.moveCaret(new TextPosition(0, 1), true);
        assertTrue(drawing.hasSelection());
        assertEquals(new TextPosition(0, 1), drawing.getSelectionStart());
        assertEquals(new TextPosition(2, 4), drawing.getSelectionEnd());
        assertFalse(drawing.getDecorationsInZone(drawing.getPreferredSize()).isEmpty());
        drawing.setCaret(new TextPosition(1, 0));
        assertFalse(drawing.hasSelection());
    }
}
