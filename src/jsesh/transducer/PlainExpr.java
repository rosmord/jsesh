/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mars 2005
 *
 */
package jsesh.transducer;

/**
 * @author Serge Rosmorduc
 */
abstract public class PlainExpr {
    abstract boolean match( PlainExpr e, Bindings b1, Bindings b2);
    abstract boolean matchVar( VarExpr e, Bindings b1, Bindings b2);
    abstract boolean matchNeg( NegExpr e, Bindings b1, Bindings b2);
    abstract boolean matchFunc( FuncExpr e, Bindings b1, Bindings b2);
    abstract String toString(Bindings b);
    boolean isEpsilon()  {return false;};
}
