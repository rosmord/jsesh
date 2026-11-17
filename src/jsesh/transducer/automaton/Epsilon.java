/*
 * Created on 2 mai 2005 by rosmord
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.transducer.automaton;


/**
 * Singleton class for Epsilon values.
 * @author rosmord
 */
public class Epsilon {
    public static final Epsilon EPSILON= new Epsilon();
    
    private Epsilon() {
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        // Non trivial equals method, because we can actually have two different Epsilon objects due to RMI.
        return obj instanceof Epsilon;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return getClass().hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        // code for "epsilon".
        return "\u03B5";
    }
}
