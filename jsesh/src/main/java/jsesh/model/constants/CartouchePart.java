package jsesh.model.constants;

/**
 * How one extremity of a {@link jsesh.model.Cartouche} should be drawn, as
 * the Manuel de Codage digit (0-3) found there.
 * <p>For a plain cartouche, a serekh, a castle or a circular enclosure:
 * {@link #NONE} is nothing, {@link #FIRST} a normal start, {@link #SECOND}
 * an ending part (e.g. the node of an actual cartouche).
 * <p>For a Hwt-sign, the same digits mean something different: {@link #NONE}
 * is unused, {@link #FIRST} is "no square", {@link #SECOND} a square in the
 * lower part (for text in lines), {@link #THIRD} a square in the upper part.
 *
 * @author rosmord
 */
public enum CartouchePart {

    NONE(0),
    FIRST(1),
    SECOND(2),
    THIRD(3);

    private final int digit;

    CartouchePart(int digit) {
        this.digit = digit;
    }

    /**
     * @return the Manuel de Codage digit (0-3) for this part.
     */
    public int digit() {
        return digit;
    }

    /**
     * @return false for {@link #NONE}, true otherwise: whether anything at
     * all should be drawn at this extremity.
     */
    public boolean isPresent() {
        return this != NONE;
    }

    /**
     * @param digit a Manuel de Codage cartouche part digit (0-3).
     * @return the matching part.
     */
    public static CartouchePart forDigit(int digit) {
        for (CartouchePart part : values()) {
            if (part.digit == digit) {
                return part;
            }
        }
        throw new IllegalArgumentException("Not a valid cartouche part digit: " + digit);
    }
}
