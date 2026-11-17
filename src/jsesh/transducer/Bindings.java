/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mars 2005
 *
 */
package jsesh.transducer;

/**
 * @author Serge Rosmorduc
 */

public class Bindings implements Cloneable {
    private BoundExpr tabValues[];
    private VarTable tabNames;
    
    public Bindings(VarTable t) {
        tabNames= t;
        tabValues= new BoundExpr[t.getNbVars()];
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        Bindings result= (Bindings) super.clone();
        result.tabNames= tabNames;
        result.tabValues= (BoundExpr[]) tabValues.clone();
        return result;
    }
    
    public void set( VarRep v,  BoundExpr value) {
        tabValues[v.getPosition()]= value;
    }
    
    public  BoundExpr get( VarRep v) {
        return tabValues[v.getPosition()];        
    }
    
    public boolean hasvalue( VarRep v) {
        return (get(v) != null);
    }
    
    public VarRep getRep(String varname) {
        return tabNames.getVar(varname);
    }

}
