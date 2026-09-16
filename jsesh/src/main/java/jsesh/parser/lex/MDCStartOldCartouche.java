package jsesh.parser.lex;

/**
 * Represents the opening token for a cartouche according to the Manuel de
 * Codage.
 * 
 */
public class MDCStartOldCartouche implements MDCSymbols {

    private int cartoucheType;

    char part;

    public MDCStartOldCartouche(char type, char part) {
        this.cartoucheType = Character.toLowerCase(type); // locale insensitive
        this.part = Character.toLowerCase(part); // locale insensitive
    }

    /**
     * Get the value of cartoucheType.
     * 
     * @see MDCCartoucheType
     * @return value of cartoucheType.
     */

    public int getCartoucheType() {
        return cartoucheType;
    }

    /**
     * Indicates which part of the <em>whole</em> cartouche to draw.
     * other codes :
     * for standard MDC cartouches, the whole information
     * is in the opening part. with possible values of
     * b, m, e or 'a' for the whole cartouche.
     *
     * <dl>
     * <dt>b</dt>
     * <dd>beginnning of a cartouche</dd>
     * <dt>m</dt>
     * <dd>middle of a cartouche</dd>
     * <dt>e</dt>
     * <dd>end of a cartouche</dd>
     * <dt>a</dt>
     * <dd>the whole cartouche. Not in the "manual"</dd>
     * <dl>
     * 
     * @return value of part.
     */
    public char getPart() {
        return part;
    }

}
