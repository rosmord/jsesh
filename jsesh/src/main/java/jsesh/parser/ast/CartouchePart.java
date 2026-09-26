package jsesh.parser.ast;

/**
 * How one extremity of an {@link AstCartouche} should be drawn, as the
 * Manuel de Codage digit (0-3) found there. The exact meaning of each value
 * depends on the cartouche's {@link CartoucheType}; see {@link AstCartouche}.
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
