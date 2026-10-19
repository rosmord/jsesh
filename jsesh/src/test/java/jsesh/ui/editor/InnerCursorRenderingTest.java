package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import jsesh.model.CaretGranularity;
import jsesh.model.MDCPosition;

/// Draws a [JMDCEditor] with the cursor inside a quadrat, in headless mode.
class InnerCursorRenderingTest {

    private static BufferedImage render(JMDCEditor editor) {
        Dimension d = editor.getPreferredSize();
        BufferedImage image = new BufferedImage(Math.max(d.width, 100), Math.max(d.height, 100),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        editor.setSize(image.getWidth(), image.getHeight());
        editor.paint(g);
        g.dispose();
        try {
            File dir = new File(System.getProperty("buildDir", "build"), "test-images");
            dir.mkdirs();
            String name = editor.getWorkflow().getCaret().getInsertExactPosition().toString()
                    .replaceAll("[^0-9a-z]+", "_");
            ImageIO.write(image, "png", new File(dir, "cursor-" + name + ".png"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return image;
    }

    private static boolean sameImage(BufferedImage a, BufferedImage b) {
        return Arrays.equals(a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0, a.getWidth()),
                b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0, b.getWidth()));
    }

    @Test
    void innerCursorIsDrawnAndDiffersFromOtherPositions() throws Exception {
        JMDCEditor editor = new JMDCEditor();
        editor.setMDCText("A1-N5*N5*N5-D21:D21");
        editor.getWorkflow().setCaretGranularity(CaretGranularity.INNER);

        editor.getWorkflow().setCursor(new MDCPosition(1));
        BufferedImage before = render(editor);
        editor.getWorkflow().setCursor(MDCPosition.of(1, 0, 1));
        BufferedImage inRow = render(editor);
        editor.getWorkflow().setCursor(MDCPosition.of(1, 0, 2));
        BufferedImage inRow2 = render(editor);
        editor.getWorkflow().setCursor(MDCPosition.of(2, 1));
        BufferedImage inColumn = render(editor);

        assertFalse(sameImage(before, inRow));
        assertFalse(sameImage(inRow, inRow2));
        assertFalse(sameImage(inRow, inColumn));
        assertTrue(inRow.getWidth() > 0);
    }

    @Test
    void cursorIsDrawnInsideCartouchesAndLigatures() throws Exception {
        JMDCEditor editor = new JMDCEditor();
        editor.setMDCText("<-A1-N5->-t^^^A&&&n");
        editor.getWorkflow().setCaretGranularity(CaretGranularity.INNER);

        editor.getWorkflow().setCursor(new MDCPosition(1));
        BufferedImage outside = render(editor);
        editor.getWorkflow().setCursor(MDCPosition.of(0, 0, 0, 0, 0));
        BufferedImage cartoucheStart = render(editor);
        editor.getWorkflow().setCursor(MDCPosition.of(0, 0, 0, 0, 1));
        BufferedImage cartoucheMiddle = render(editor);
        editor.getWorkflow().setCursor(MDCPosition.of(0, 0, 0, 0, 2));
        BufferedImage cartoucheEnd = render(editor);
        editor.getWorkflow().setCursor(MDCPosition.of(1, 0, 0, 1));
        BufferedImage inComplexLigature = render(editor);

        assertFalse(sameImage(outside, cartoucheStart));
        assertFalse(sameImage(cartoucheStart, cartoucheMiddle));
        assertFalse(sameImage(cartoucheMiddle, cartoucheEnd));
        assertFalse(sameImage(cartoucheStart, cartoucheEnd));
        assertFalse(sameImage(outside, inComplexLigature));
    }
}
