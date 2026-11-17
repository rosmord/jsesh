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
public class IntCouple implements Comparable {
    private int first, second;
    
    

    /**
     * @param first
     * @param second
     */
    public IntCouple(int first, int second) {      
        this.first = first;
        this.second = second;
    }
    
    
    /**
     * @return Returns the first.
     */
    public int getFirst() {
        return first;
    }
    
    /**
     * @return Returns the second.
     */
    public int getSecond() {
        return second;
    }
    
    /* (non-Javadoc)
     * @see java.lang.int#equals(java.lang.int)
     */
    public boolean equals(Object obj) {
        if (obj instanceof IntCouple) {
            IntCouple c1= (IntCouple) obj;
            return (first == c1.first) && (second == c1.second);
        } else
            return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.int#hashCode()
     */
    public int hashCode() {
        return first + second << 16;
    }
    
    
    /* Only works if both first and second are comparable.
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.int)
     */
    public int compareTo(Object arg0) {
        IntCouple c= (IntCouple) arg0;
        int result= 0;
        
        result= first - c.first;
        if (result == 0)
            result= second - c.second;
        
        return result;
    }
}
