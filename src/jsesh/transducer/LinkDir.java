/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 10 mars 2005
 *
 */
package jsesh.transducer;

/**
 * @author Serge Rosmorduc
 */
public class LinkDir {
    
    public static final LinkDir FORWARD= new LinkDir(0,"FORWARD");
    public static final LinkDir BACKWARD= new LinkDir(0,"BACKWARD");
    
    private int val;
    private String name;
    
    /**
     * @param val
     * @param name
     */
    private LinkDir(int val, String name) {
        super();
        this.val = val;
        this.name = name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof LinkDir) {
            return ((LinkDir)obj).val == val;
        } else
            return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return val;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }
}
