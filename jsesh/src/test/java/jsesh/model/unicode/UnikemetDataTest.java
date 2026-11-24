package jsesh.model.unicode;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UnikemetDataTest {

    @Test
    public void variationSequences() {
        assertTrue(UnikemetData.INSTANCE.hasVariationSequence(0x13091, 0xFE00));
        assertFalse(UnikemetData.INSTANCE.hasVariationSequence(0x13091, 0xFE01));
    }
}
