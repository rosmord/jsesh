package jsesh.render.style;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import jsesh.render.style.PaintingSpecifications;
import jsesh.render.style.ShadingMode;

public class ColorSpecTest {

    @Test
    public void testImmutabilityA() {
        // Check that the color map is unmodifiable.
        HashMap<String, Color> map = new HashMap<>();
        PaintingSpecifications s = new PaintingSpecifications(Color.BLACK, Color.RED, Color.BLUE, Color.LIGHT_GRAY,
                Color.WHITE,
                ShadingMode.GRAY_SHADING,
                map);

        map.put("yellow", Color.YELLOW);
        assertTrue(s.colorMap().isEmpty());
        // TODO : move to JUnit 5
    }

    @Test
    public void testImmutabilityB() {
        assertThrows(UnsupportedOperationException.class,
                () -> {
                    // Check that the color map is unmodifiable.
                    HashMap<String, Color> map = new HashMap<>();
                    map.put("yellow", Color.YELLOW);
                    PaintingSpecifications s = new PaintingSpecifications(Color.BLACK, Color.RED, Color.BLUE,
                            Color.LIGHT_GRAY, Color.WHITE, ShadingMode.GRAY_SHADING,
                            map);

                    s.colorMap().put("green", Color.GREEN);
                });

    }
}
