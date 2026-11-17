/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mars 2005
 *
 */
package jsesh.transducer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Serge Rosmorduc
 */
public class FuncExpr extends PlainExpr {
    private String functor;

    private List args; // PlainExpr

    /**
     * @param functor
     * @param args
     */
    public FuncExpr(String functor, List args) {
        super();
        this.functor = functor;
        this.args = args;
    }

    /**
     *  
     */
    public FuncExpr(String functor) {
        this.functor = functor;
        args = new ArrayList();

    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.PlainExpr#match(jsesh.transducer.PlainExpr,
     *      jsesh.transducer.Bindings, jsesh.transducer.Bindings)
     */
    boolean match(PlainExpr e, Bindings b1, Bindings b2) {
        return e.matchFunc(this, b2, b1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.PlainExpr#matchVar(jsesh.transducer.VarExpr,
     *      jsesh.transducer.Bindings, jsesh.transducer.Bindings)
     */
    boolean matchVar(VarExpr e, Bindings b1, Bindings b2) {
        return e.matchFunc(this, b2, b1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.PlainExpr#matchNeg(jsesh.transducer.NegExpr,
     *      jsesh.transducer.Bindings, jsesh.transducer.Bindings)
     */
    boolean matchNeg(NegExpr e, Bindings b1, Bindings b2) {
        Bindings b = null;
        VarTable v;
        try {
            // cout << "trying not to match : " << this->toString(b1) << endl;
            b = (Bindings) b2.clone();
        } catch (CloneNotSupportedException e1) {
            throw new RuntimeException(e1);
        }
        boolean res = (!e.getExpr().match(this, b, b1));
        return res;

    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.PlainExpr#matchFunc(jsesh.transducer.FuncExpr,
     *      jsesh.transducer.Bindings, jsesh.transducer.Bindings)
     */
    boolean matchFunc(FuncExpr e, Bindings b1, Bindings b2) {
        boolean res= false;
        if (getFunctor().equals(e.getFunctor()) && args.size() == e.args.size()) {
            res = true;
            // All arguments should match.
            for (Iterator i = args.iterator(), j = e.args.iterator(); res
                    && i.hasNext() && j.hasNext();) {
                PlainExpr argA, argB;
                argA = (PlainExpr) i.next();
                argB= (PlainExpr) j.next();
                if (!argA.match(argB, b1, b2)) {
                    res = false;
                } else
                    res = false;
            }
        }
        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.PlainExpr#toString(jsesh.transducer.Bindings)
     */
    String toString(Bindings b) {
        StringBuffer s= new StringBuffer(getFunctor());
        if (getArgs().size() != 0)
          {
            s.append("(");
            Iterator i;
            for (i=getArgs().iterator(); i.hasNext(); )
      	{
                PlainExpr expr= (PlainExpr) i.next();
                s.append(expr.toString(b));
      	  if (i.hasNext())
      	    s.append( ", ");
      	}

            s.append(")");
          }
        return s.toString();
    }

    /**
     * @return Returns the functor.
     */
    public String getFunctor() {
        return functor;
    }

    /**
     * @param functor
     *            The functor to set.
     */
    public void setFunctor(String functor) {
        this.functor = functor;
    }

    /**
     * @return Returns the args.
     */
    public List getArgs() {
        return args;
    }

    /**
     * @param args
     *            The args to set.
     */
    public void setArgs(List args) {
        this.args = args;
    }
}