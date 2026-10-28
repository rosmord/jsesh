package jsesh.render.draw;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.ui.editor.JMDCEditor;

/// Explicit line breaks are drawn as marks only when asked.
class LineBreakMarksTest {

    private final JMDCEditor editor = new JMDCEditor();

    private int[] paint(String mdc) {
        editor.setMDCText(mdc);
        Dimension size = new Dimension(600, 400);
        editor.setSize(size);
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, size.width, size.height);
        editor.paint(g);
        g.dispose();
        return ((DataBufferInt) image.getRaster().getDataBuffer()).getData().clone();
    }

    @Test
    void marksAreOffByDefault() {
        assertFalse(editor.isShowLineBreakMarks());
    }

    @Test
    void lineBreakMarkIsDrawnOnlyWhenShown() {
        int[] hidden = paint("A1-N5!D21");
        editor.setShowLineBreakMarks(true);
        int[] shown = paint("A1-N5!D21");
        assertNotEquals(Arrays.hashCode(hidden), Arrays.hashCode(shown));
        editor.setShowLineBreakMarks(false);
        assertArrayEquals(hidden, paint("A1-N5!D21"));
    }

    @Test
    void textWithoutLineBreakIsUnaffected() {
        int[] hidden = paint("A1-N5-D21");
        editor.setShowLineBreakMarks(true);
        assertArrayEquals(hidden, paint("A1-N5-D21"));
    }

    @Test
    void marksAreDrawnInRightToLeftAndVerticalText() {
        editor.setTextDirection(TextDirection.RIGHT_TO_LEFT);
        int[] hidden = paint("A1-N5!D21");
        editor.setShowLineBreakMarks(true);
        assertTrue(!Arrays.equals(hidden, paint("A1-N5!D21")));

        editor.setShowLineBreakMarks(false);
        editor.setTextDirection(TextDirection.LEFT_TO_RIGHT);
        editor.setTextOrientation(TextOrientation.VERTICAL);
        hidden = paint("A1-N5!D21");
        editor.setShowLineBreakMarks(true);
        assertTrue(!Arrays.equals(hidden, paint("A1-N5!D21")));
    }
}
