/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mars 2005
 *
 */
package jsesh.transducer.old;

/**
 * @author Serge Rosmorduc
 */
public class BoundExpr {
    
    public final static BoundExpr EPSILON_EXPRESSION= 
        new BoundExpr(new EpsilonExpr(), null);

    
    private PlainExpr expr;
    private Bindings bindings;

    
    /**
     * @param expr
     * @param bindings
     */
    public BoundExpr(PlainExpr expr, Bindings bindings) {
        super();
        this.expr = expr;
        this.bindings = bindings;
    }
    
    /**
     * @return Returns the expr.
     */
    public PlainExpr getExpr() {
        return expr;
    }
    
    /**
     * @return Returns the bindings.
     */
    public Bindings getBindings() {
        return bindings;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (expr != null)
        return getExpr().toString(getBindings());
        else
          return "epsilon";

    } 
    
    /**
     * Matches the bound expression with the unbound expression v. new bindings
     * for v go in newBindings.
     */
    boolean match(PlainExpr e, Bindings newBindings) {
        return expr.match(e, bindings, newBindings);
    }
}
