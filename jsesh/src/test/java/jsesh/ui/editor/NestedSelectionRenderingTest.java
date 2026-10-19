package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import jsesh.model.MDCPosition;
import jsesh.model.TextScope;

/// A selection inside a cartouche highlights the selected quadrats only.
class NestedSelectionRenderingTest {

    private static final TextScope CARTOUCHE = TextScope.of(0, 0, 0, 0);

    private static BufferedImage render(JMDCEditor editor, String name) {
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
            ImageIO.write(image, "png", new File(dir, "selection-" + name + ".png"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return image;
    }

    /// The number of pixels tinted by the (blue) selection colour.
    private static int highlighted(BufferedImage image) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color c = new Color(image.getRGB(x, y));
                if (c.getBlue() > c.getRed() + 20 && c.getBlue() > c.getGreen() + 20) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int highlightedWith(String mdc, MDCPosition cursor, MDCPosition mark) {
        JMDCEditor editor = new JMDCEditor();
        editor.setMDCText(mdc);
        editor.getWorkflow().setCursor(cursor);
        if (mark != null) {
            editor.getWorkflow().setMark(mark);
        }
        String name = (cursor + "_" + mark).replaceAll("[^0-9a-z]+", "_");
        return highlighted(render(editor, name));
    }

    /// The number of pixels highlighted by a selection, the cursor (also blue)
    /// being left out.
    private static int selectionPixels(String mdc, MDCPosition cursor, MDCPosition mark) {
        return highlightedWith(mdc, cursor, mark) - highlightedWith(mdc, cursor, null);
    }

    @Test
    void onlyTheSelectedQuadratsAreHighlighted() {
        String mdc = "<-A1-N5-D21->";
        int oneQuadrat = selectionPixels(mdc, CARTOUCHE.positionAt(1), CARTOUCHE.positionAt(2));
        int whole = selectionPixels(mdc, new MDCPosition(0), new MDCPosition(1));
        assertTrue(oneQuadrat > 0, "nothing highlighted");
        assertTrue(oneQuadrat < whole / 2, oneQuadrat + " vs " + whole);
    }

    @Test
    void aSelectionLeavingTheCartoucheHighlightsItAll() {
        String mdc = "<-A1-N5-D21->-A1";
        int leaving = selectionPixels(mdc, CARTOUCHE.positionAt(1), new MDCPosition(1));
        int whole = selectionPixels(mdc, new MDCPosition(0), new MDCPosition(1));
        assertEquals(whole, leaving, 0.05 * whole);
    }
}
