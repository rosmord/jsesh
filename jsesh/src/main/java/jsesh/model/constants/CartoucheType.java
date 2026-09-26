package jsesh.model.constants;

/**
 * The kind of cartouche, serekh, hwt-sign, castle or circular enclosure a
 * {@link jsesh.model.Cartouche} denotes, keyed by its Manuel de Codage
 * letter.
 *
 * @author rosmord
 */
public enum CartoucheType {

    CARTOUCHE('c'),
    SEREKH('s'),
    HWT('h'),
    CASTLE('f'),
    CIRCULAR_ENCLOSURE('g');

    private final char code;

    CartoucheType(char code) {
        this.code = code;
    }

    /**
     * @return the Manuel de Codage letter for this cartouche type.
     */
    public char code() {
        return code;
    }

    /**
     * @param code a Manuel de Codage cartouche letter, in either case (the
     * "old style" MacScribe syntax uses uppercase letters for the same
     * types).
     * @return the matching cartouche type.
     */
    public static CartoucheType forCode(char code) {
        char lower = Character.toLowerCase(code);
        for (CartoucheType type : values()) {
            if (type.code == lower) {
                return type;
            }
        }
        throw new IllegalArgumentException("Not a valid cartouche type code: " + code);
    }
}
