package jsesh.mdc.lex;

/**
 * Describe class <code>MDCCartouche</code> here.
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * @version 1.0
 */
public class MDCCartouche implements MDCSymbols {
    
    private char cartoucheType;
    
    private int part;

    /**
     * Builds a cartouche.
     * @param type a code for the kind of cartouche to
     * create. possible values are 'c', 's', 'f' and 'h'. Uppercase
     * letters are allowed.
     * @param part part of the cartouche to draw.
     */
    
    public MDCCartouche(char type, int part) {
	// that old C-like magic :
	cartoucheType= Character.toLowerCase(type);  // locale insensitive
	this.part= part;
    }
    
    /**
     * for a given type of cartouche, indicate how this extremity
     * should be drawn.
     *
     * meaning depends on cartouche.
     *  0 : do not draw this ending.
     * 	for cartouches and serekh 1 : begin 2 : end
     * 		(2 being normally the decorated part)
     *  for Hout signs : 
     *		1 : no square
     *		2 : square in the lower part.
     *		3 : square in the upper part
     *
     *  Note that <-....-> is equivalent to <1-....-2>
     *  and that <H-..-> is equivalent to <h1-...-h2>
     *  
     *
     * @return an <code>int</code> value */
    public int getPart() {
	return part;
    }

    
    /**
     * indicates if this a cartouche, a serekh, etc.
     *  possible values are 'c', 's', 'f' and 'h'.
     * @return the code of the cartouche.
     */
    public int getCartoucheType() {
	return cartoucheType;
    }
}




