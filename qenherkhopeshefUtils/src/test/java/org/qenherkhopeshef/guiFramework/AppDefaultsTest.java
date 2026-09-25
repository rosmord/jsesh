package org.qenherkhopeshef.guiFramework;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AppDefaultsTest {

    @Test
    public void firstDefinedKeyReturnsFirstKeyWhenBothDefined() {
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.text", "New");
        defaults.put("foo.Name", "Old");
        assertEquals("foo.text", defaults.firstDefinedKey("foo.text", "foo.Name"));
    }

    @Test
    public void firstDefinedKeyReturnsFirstKeyWhenOnlyFirstDefined() {
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.text", "New");
        assertEquals("foo.text", defaults.firstDefinedKey("foo.text", "foo.Name"));
    }

    @Test
    public void firstDefinedKeyReturnsFallbackKeyWhenOnlyFallbackDefined() {
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.Name", "Old");
        assertEquals("foo.Name", defaults.firstDefinedKey("foo.text", "foo.Name"));
    }

    @Test
    public void firstDefinedKeyReturnsFallbackKeyWhenNeitherDefined() {
        AppDefaults defaults = new AppDefaults();
        assertEquals("foo.Name", defaults.firstDefinedKey("foo.text", "foo.Name"));
    }

    @Test
    public void firstDefinedKeySupportsMoreThanTwoCandidates() {
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.middle", "Middle");
        assertEquals("foo.middle",
                defaults.firstDefinedKey("foo.first", "foo.middle", "foo.last"));
    }
}
