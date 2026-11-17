/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mars 2005
 *
 */
package jsesh.transducer.old;

import java.util.Iterator;
import java.util.List;

/**
 * @author Serge Rosmorduc
 */
public class AndExpr {

    private List terms; // List of PlainExpr

    public boolean match(PlainExpr e, Bindings b1, Bindings b2) {
        boolean res = true;
        Iterator i;
        for (i = terms.iterator(); res && i.hasNext();) {
            PlainExpr exp = (PlainExpr) i.next();
            res &= exp.match(e, b1, b2);
        }
        return res;
    }

    /**
     * @return Returns the terms.
     */
    public List getTerms() {
        return terms;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString(Bindings bindings) {
        StringBuffer result= new StringBuffer();
        Iterator i;
        for(i= terms.iterator(); i.hasNext(); ) {
            PlainExpr expr= (PlainExpr) i.next();
            result.append( expr.toString(bindings) + " & ");  
        }
          
        // Suppress the final " & " 
        return result.substring(0, result.length() - 3);
    }
}