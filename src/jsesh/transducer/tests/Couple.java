/*
 * Created on 27 mai 2005 by rosmord
 *
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.transducer.tests;

/**
 * A couple (x,y) of values.
 * @author rosmord
 * */
public class Couple implements Comparable {
    private Object first, second;
    
    

    /**
     * @param first
     * @param second
     */
    public Couple(Object first, Object second) {      
        this.first = first;
        this.second = second;
    }
    
    
    /**
     * @return Returns the first.
     */
    public Object getFirst() {
        return first;
    }
    
    /**
     * @return Returns the second.
     */
    public Object getSecond() {
        return second;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof Couple) {
            Couple c1= (Couple) obj;
            return first.equals(c1.first) && second.equals(c1.second);
        } else
            return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return first.hashCode() + second.hashCode() << 16;
    }
    
    
    /* Only works if both first and second are comparable.
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg0) {
        Couple c= (Couple) arg0;
        int result= 0;
        Comparable c1= (Comparable) first;
        Comparable c2= (Comparable) second;       
        
        result= c1.compareTo(c.first);
        if (result == 0)
            result= c2.compareTo(c.second);
        
        return result;
    }
}
