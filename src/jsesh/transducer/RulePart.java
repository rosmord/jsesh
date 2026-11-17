/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 10 mars 2005
 *
 */
package jsesh.transducer;

/**
 * @author Serge Rosmorduc
 */
public class RulePart {
    
    public static final RulePart LEFT= new RulePart(0, "LEFT");
    public static final RulePart MIDDLE= new RulePart(1, "MIDDLE");
    public static final RulePart RIGHT= new RulePart(2, "RIGHT");
    
    private int var;
    private String name;
    
    
    /**
     * @param var
     * @param name
     */
    private RulePart(int var, String name) {
        super();
        this.var = var;
        this.name = name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof RulePart) {
            return ((RulePart)obj).var== var;
        } else return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return var;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
     return name;
    }
}
