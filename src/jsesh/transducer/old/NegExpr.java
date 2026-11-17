/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mars 2005
 *
 */
package jsesh.transducer.old;

/**
 * @author Serge Rosmorduc
 */
public class NegExpr extends PlainExpr {

    private FuncExpr expr;
    
    
    /**
     * @param exp
     */
    public NegExpr(FuncExpr expr) {
        super();
        this.expr = expr;
    }
    
    
    /**
     * @return
     */
    public FuncExpr getExpr() {
        return expr;
    }


    /* (non-Javadoc)
     * @see jsesh.transducer.PlainExpr#match(jsesh.transducer.PlainExpr, jsesh.transducer.Bindings, jsesh.transducer.Bindings)
     */
    boolean match(PlainExpr e, Bindings b1, Bindings b2) {
        return e.matchNeg(this,b2,b1);
    }


    /* (non-Javadoc)
     * @see jsesh.transducer.PlainExpr#matchVar(jsesh.transducer.VarExpr, jsesh.transducer.Bindings, jsesh.transducer.Bindings)
     */
    boolean matchVar(VarExpr e, Bindings b1, Bindings b2) {
        return e.matchNeg(this, b2, b1);
    }


    /* (non-Javadoc)
     * @see jsesh.transducer.PlainExpr#matchNeg(jsesh.transducer.NegExpr, jsesh.transducer.Bindings, jsesh.transducer.Bindings)
     */
    boolean matchNeg(NegExpr e, Bindings b1, Bindings b2) {
        throw new RuntimeException("We shouldn't try to match two negative Items.");
    }


    /* (non-Javadoc)
     * @see jsesh.transducer.PlainExpr#matchFunc(jsesh.transducer.FuncExpr, jsesh.transducer.Bindings, jsesh.transducer.Bindings)
     */
    boolean matchFunc(FuncExpr e, Bindings b1, Bindings b2) {
        return e.matchNeg(this, b2, b1);
    }


    /* (non-Javadoc)
     * @see jsesh.transducer.PlainExpr#toString(jsesh.transducer.Bindings)
     */
    String toString(Bindings b) {
        return "! " +  getExpr().toString(b);

    }

}
