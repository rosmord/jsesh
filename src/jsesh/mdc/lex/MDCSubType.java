package jsesh.mdc.lex;

/**
 * MDCSubType is used to represent types that have an variety of
 * subtypes. The code sent back by the lexer will give the main type,
 * and the MDCSubType value will give the subtype.
 *
 *
 * Created: Sun May 12 10:33:35 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 */

public class MDCSubType {
    private int subType;
    
    /**
     * Get the value of subType.
     * @return value of subType.
     */
    public int getSubType() {
	return subType;
    }
    
    /**
     * Set the value of subType.
     * @param v  Value to assign to subType.
     */
    public void setSubType(int  v) {
	this.subType = v;
    }
    
    public MDCSubType (int sub){
	subType= sub;
    }
    
}// MDCSubType
