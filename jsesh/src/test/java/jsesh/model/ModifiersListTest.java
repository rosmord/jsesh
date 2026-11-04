package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/// Tests of the special numeric modifiers (rotations, scale) of [ModifiersList].
public class ModifiersListTest {

    private ModifiersList list(Modifier... modifiers) {
        ModifiersList result = new ModifiersList();
        for (Modifier m : modifiers) {
            result.includeModifier(m);
        }
        return result;
    }

    @Test
    public void defaults() {
        ModifiersList l = new ModifiersList();
        assertEquals(0, l.getAngle());
        assertEquals(100, l.getScale());
        assertFalse(l.isReversed());
    }

    // --- Old-style rotation: r<n> (quarter turns, clockwise) ---

    @Test
    public void quarterTurnRotation() {
        assertEquals(270, list(new Modifier("r", 1)).getAngle());
        assertEquals(180, list(new Modifier("r", 2)).getAngle());
        assertEquals(90, list(new Modifier("r", 3)).getAngle());
        assertEquals(0, list(new Modifier("r", 4)).getAngle());
        assertEquals(0, list(new Modifier("r", 0)).getAngle());
    }

    @Test
    public void quarterTurnRotationDoesNotReverse() {
        assertFalse(list(new Modifier("r", 1)).isReversed());
    }

    @Test
    public void quarterTurnRotationIsNotStoredAsChild() {
        assertEquals(0, list(new Modifier("r", 1)).getNumberOfChildren());
    }

    @Test
    public void removeQuarterTurnRotation() {
        ModifiersList l = list(new Modifier("r", 1));
        l.removeModifier(new Modifier("r", 1));
        assertEquals(0, l.getAngle());
    }

    // --- t<n>: rotation + reversal ---

    @Test
    public void rotationWithReversal() {
        ModifiersList l = list(new Modifier("t", 1));
        assertEquals(270, l.getAngle());
        assertTrue(l.isReversed());
    }

    @Test
    public void removeRotationWithReversal() {
        ModifiersList l = list(new Modifier("t", 1));
        l.removeModifier(new Modifier("t", 1));
        assertEquals(0, l.getAngle());
        assertFalse(l.isReversed());
    }

    // --- R<n>: rotation in degrees ---

    @Test
    public void rotationInDegrees() {
        assertEquals(10, list(new Modifier("R", 10)).getAngle());
        assertEquals(90, list(new Modifier("R", 90)).getAngle());
    }

    @Test
    public void rotationInDegreesIsNormalized() {
        assertEquals(10, list(new Modifier("R", 370)).getAngle());
        assertEquals(0, list(new Modifier("R", 360)).getAngle());
    }

    @Test
    public void negativeRotationInDegrees() {
        assertEquals(350, list(new Modifier("R", -10)).getAngle());
        assertEquals(270, list(new Modifier("R", -90)).getAngle());
    }

    @Test
    public void rotationInDegreesDoesNotReverse() {
        assertFalse(list(new Modifier("R", 10)).isReversed());
    }

    @Test
    public void rotationReplacesPreviousRotation() {
        ModifiersList l = list(new Modifier("R", 10), new Modifier("R", 20));
        assertEquals(20, l.getAngle());
        l = list(new Modifier("r", 1), new Modifier("R", 20));
        assertEquals(20, l.getAngle());
    }

    @Test
    public void removeRotationInDegrees() {
        ModifiersList l = list(new Modifier("R", 10));
        l.removeModifier(new Modifier("R", 10));
        assertEquals(0, l.getAngle());
    }

    @Test
    public void setIntegerRotation() {
        ModifiersList l = new ModifiersList();
        l.setInteger("R", 45);
        assertEquals(45, l.getAngle());
    }

    @Test
    public void setAnglePositive() {
        ModifiersList l = new ModifiersList();
        l.setAngle(450);
        assertEquals(90, l.getAngle());
    }

    // --- Scale: \<n> (percentage) ---

    @Test
    public void scaleInPercent() {
        ModifiersList l = list(new Modifier("", 50));
        assertEquals(50, l.getScale());
        assertFalse(l.isReversed());
    }

    @Test
    public void scaleReplacesPreviousScale() {
        assertEquals(70, list(new Modifier("", 50), new Modifier("", 70)).getScale());
    }

    @Test
    public void removeScaleResetsToFull() {
        ModifiersList l = list(new Modifier("", 50));
        l.removeModifier(new Modifier("", 50));
        assertEquals(100, l.getScale());
    }

    @Test
    public void scaleIsNotStoredAsChild() {
        assertEquals(0, list(new Modifier("", 50)).getNumberOfChildren());
    }

    @Test
    public void reversalWithoutValue() {
        ModifiersList l = list(new Modifier("", null));
        assertTrue(l.isReversed());
        assertEquals(100, l.getScale());
        l.removeModifier(new Modifier("", null));
        assertFalse(l.isReversed());
    }

    // --- s<n>: scale by successive divisions by sqrt(2) ---

    @Test
    public void sqrt2Scale() {
        assertEquals(100, list(new Modifier("s", 0)).getScale());
        // 100/1.4142136 = 70.71...
        assertEquals(70, list(new Modifier("s", 1)).getScale());
        // 100/2 = 49.99999... (truncated)
        int s2 = list(new Modifier("s", 2)).getScale();
        assertTrue(s2 == 49 || s2 == 50, "unexpected scale " + s2);
        assertEquals(35, list(new Modifier("s", 3)).getScale());
        int s4 = list(new Modifier("s", 4)).getScale();
        assertTrue(s4 == 24 || s4 == 25, "unexpected scale " + s4);
    }

    @Test
    public void sqrt2ScaleReplacesPercentScale() {
        assertEquals(70, list(new Modifier("", 30), new Modifier("s", 1)).getScale());
    }

    // --- interaction with other modifiers, copies and comparison ---

    @Test
    public void otherIntegerModifiersAreStored() {
        ModifiersList l = list(new Modifier("x", 12));
        assertTrue(l.hasInteger("x"));
        assertEquals(12, l.getInteger("x"));
        assertEquals(0, l.getAngle());
        assertEquals(100, l.getScale());
    }

    @Test
    public void deepCopyKeepsAngleAndScale() {
        ModifiersList l = list(new Modifier("t", 1), new Modifier("R", 30), new Modifier("", 60));
        ModifiersList copy = l.deepCopy();
        assertEquals(30, copy.getAngle());
        assertEquals(60, copy.getScale());
        assertTrue(copy.isReversed());
    }

    @Test
    public void equalsIgnoreIdTakesAngleAndScaleIntoAccount() {
        ModifiersList a = list(new Modifier("R", 30), new Modifier("", 60));
        ModifiersList b = list(new Modifier("R", 30), new Modifier("", 60));
        assertTrue(a.equalsIgnoreId(b));
        assertFalse(a.equalsIgnoreId(list(new Modifier("R", 31), new Modifier("", 60))));
        assertFalse(a.equalsIgnoreId(list(new Modifier("R", 30), new Modifier("", 61))));
    }
}
