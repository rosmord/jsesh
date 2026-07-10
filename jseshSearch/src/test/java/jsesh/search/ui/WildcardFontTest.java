package jsesh.search.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import jsesh.hieroglyphs.data.coremdc.ManuelDeCodage;

public class WildcardFontTest {

    @Test
    public void testCheckWildCard() {
        WildcardFont font = WildcardFont.getInstance();
        var res = font.get(ManuelDeCodage.getInstance().getCanonicalCode("QUERYSKIP"));
        assertNotNull(res);
    }
}
