package jsesh.drawingspecifications.graphical;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.HashMap;

import org.junit.Test;

public class ColorSpecTest {

    @Test
    public void testImmutabilityA() {
        // Check that the color map is unmodifiable.
        HashMap<String, Color> map = new HashMap<>();
        ColorSpec s = new ColorSpec(Color.BLACK, Color.RED, Color.BLUE, Color.LIGHT_GRAY, Color.WHITE,
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
        ColorSpec s = new ColorSpec(Color.BLACK, Color.RED, Color.BLUE, Color.LIGHT_GRAY, Color.WHITE,
                map);

        s.colorMap().put("green", Color.GREEN);       
    }
}
