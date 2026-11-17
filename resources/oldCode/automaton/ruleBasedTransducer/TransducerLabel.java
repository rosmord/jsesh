/*
 * Created on 4 mai 2005 by rosmord
 *
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.finiteState.ruleBasedTransducer;


/**
 * Label of links in transducers.
 * Useful if considering the transducer as an automaton.
 * @author rosmord
 *
 */

public class TransducerLabel {
    private Object top;
    private Object bottom;
    
    /**
     * Create a label. None of top and bottom should be null.
     * @param top
     * @param bottom
     */
    public TransducerLabel(Object top, Object bottom) {      
        this.top = top;
        this.bottom = bottom;
    }
    /**
     * @return Returns the bottom.
     */
    public Object getBottom() {
        return bottom;
    }
    /**
     * @param bottom The bottom to set.
     */
    public void setBottom(Object bottom) {
        this.bottom = bottom;
    }
    /**
     * @return Returns the top.
     */
    public Object getTop() {
        return top;
    }
    /**
     * @param top The top to set.
     */
    public void setTop(Object top) {
        this.top = top;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof TransducerLabel) {
            TransducerLabel tl= (TransducerLabel) obj;
            return tl.getTop().equals(top) && tl.getBottom().equals(bottom);
        } else
            return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return top.hashCode() + (bottom.hashCode() << 16);
    }
}