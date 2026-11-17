/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mars 2005
 *
 */
package jsesh.transducer.old;

/**
 * @author Serge Rosmorduc
 */
public class VarExpr extends PlainExpr {
    private VarRep var;

    /**
     * @param var
     */
    public VarExpr(VarRep var) {
        super();
        this.var = var;
    }

    /**
     * @param var
     *            The var to set.
     */
    public void setVar(VarRep var) {
        this.var = var;
    }

    /**
     * @return Returns the var.
     */
    public VarRep getVar() {
        return var;
    }

    /**
     * Sets the value of this expression in b1 as v.
     */
    public void setVal(BoundExpr val, Bindings b) {
        b.set(var, val);
    }

    /**
     * Get this variable value according to bindings b.
     * 
     * @param b
     * @return
     */
    public BoundExpr getVal(Bindings b) {
        return b.get(var);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.PlainExpr#match(jsesh.transducer.PlainExpr,
     *      jsesh.transducer.Bindings, jsesh.transducer.Bindings)
     */
    boolean match(PlainExpr e, Bindings b1, Bindings b2) {
        return e.matchVar(this, b2, b1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.PlainExpr#matchVar(jsesh.transducer.VarExpr,
     *      jsesh.transducer.Bindings, jsesh.transducer.Bindings)
     */
    boolean matchVar(VarExpr e, Bindings b1, Bindings b2) {
        // One of the two variables should have a value
        if (hasValue(b1))
            return getVal(b1).match(e, b2);
        else if (e.hasValue(b2))
            return e.getVal(b2).match(this, b1);
        else
            throw new RuntimeException("Normalement Impossible");

    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.PlainExpr#matchNeg(jsesh.transducer.NegExpr,
     *      jsesh.transducer.Bindings, jsesh.transducer.Bindings)
     */
    boolean matchNeg(NegExpr e, Bindings b1, Bindings b2) {
        if (hasValue(b1)) {
            return getVal(b1).match(e, b2);
        } else {
            throw new RuntimeException("Unset Variable in negative context.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.PlainExpr#matchFunc(jsesh.transducer.FuncExpr,
     *      jsesh.transducer.Bindings, jsesh.transducer.Bindings)
     */
    boolean matchFunc(FuncExpr e, Bindings b1, Bindings b2) {
        if (hasValue(b1)) {
            return getVal(b1).match(e, b2);
        } else {
            BoundExpr be;
            be = new BoundExpr(e, b2);
            setVal(be, b1);
        }
        return true;

    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.PlainExpr#toString(jsesh.transducer.Bindings)
     */
    String toString(Bindings b) {
        if (hasValue(b))
            return b.get(var).toString();
        else
            return var.getName();

    }

    public boolean hasValue(Bindings b) {
        if (b != null)
            return b.hasvalue(var);
        else
            return false;
    }
}