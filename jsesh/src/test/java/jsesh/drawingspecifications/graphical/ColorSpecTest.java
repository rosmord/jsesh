package jsesh.drawingspecifications.graphical;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.HashMap;

import org.junit.Test;

import jsesh.drawingspecifications.PaintingSpecifications;
import jsesh.drawingspecifications.ShadingMode;

public class ColorSpecTest {

    @Test
    public void testImmutabilityA() {
        // Check that the color map is unmodifiable.
        HashMap<String, Color> map = new HashMap<>();
        PaintingSpecifications s = new PaintingSpecifications(Color.BLACK, Color.RED, Color.BLUE, Color.LIGHT_GRAY, Color.WHITE,
            ShadingMode.GRAY_SHADING,
                map);

        map.put("yellow", Color.YELLOW);
        assertTrue(s.colorMap().isEmpty());
        // TODO : move to JUnit 5
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutabilityB() {
        // Check that the color map is unmodifiable.
        HashMap<String, Color> map = new HashMap<>();
        map.put("yellow", Color.YELLOW);
        PaintingSpecifications s = new PaintingSpecifications(Color.BLACK, Color.RED, Color.BLUE, Color.LIGHT_GRAY, Color.WHITE, ShadingMode.GRAY_SHADING,
                map);

        s.colorMap().put("green", Color.GREEN);       
    }
}
