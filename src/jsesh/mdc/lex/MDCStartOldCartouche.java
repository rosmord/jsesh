package jsesh.mdc.lex;

/**
 * Represents the opening token for a cartouche according to the Manuel de Codage.
 * 
 */
public class MDCStartOldCartouche implements MDCSymbols {

    private int cartoucheType;
    
    /**
     * Get the value of cartoucheType.
     * @see MDCCartoucheType
     * @return value of cartoucheType.
     */
    
    public int getCartoucheType() {
	return cartoucheType;
    }
    
    char part;
    
    /**
     * Indicates which part of the <em>whole</em> cartouche to draw.
     * other codes :
     *  for standard MDC cartouches, the whole information
     *  is in the opening part. with possible values of 
     *  b, m, e or 'a' for the whole cartouche.
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
     * @return value of part.
     */
    public char getPart() {
	return part;
    }
    
    public MDCStartOldCartouche(char type, char part) {
	cartoucheType= Character.toLowerCase(type);
	part= Character.toLowerCase(part);
    }
}
