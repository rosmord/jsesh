/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mars 2005
 *
 */
package jsesh.transducer.old;

/**
 * @author Serge Rosmorduc
 */
public class Expr {
    private AndExpr rep;
    
    public static final Expr EPSILON= new Expr();
    
    public Expr() {
        rep= new AndExpr();
    }
    
    public Expr(AndExpr a) {
        rep= a;
    }
        
    public  String toString(Bindings b) {
        return rep.toString(b);
    }
    
    public boolean match(PlainExpr e, Bindings b1, Bindings b2) {
        return rep.match(e,b1,b2);
    }
    
    public boolean isEpsilon() {
        return rep.getTerms().size() == 0;
    }

}
