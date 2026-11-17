/*
 * Created on 22 févr. 2005 by rosmord
 *
 * This file is distributed according to the GNU LESSER PUBLIC LICENCE
 */
package jsesh.transducer.old;

import java.util.Iterator;
import java.util.List;

/**
 * @author rosmord A Rule like A,B->C will be transformed into a transducer :
 * 
 * <pre>
 * 
 *       A          B       epsilon
 *  *---------&gt;*---------&gt;*---------&gt;*
 *    epsilon    epsilon       C
 * 
 * </pre>
 * 
 * The cost will be 0 for the first two arcs, and the cost of the rule (or 0)
 * for the last one.
 *  
 */

public class Rule {
    List left; // Expr

    List right; // PlainExpr

    private int cost;

    private VarTable varNames;

    // Computed variables
    private int numLeft;

    private int numRight;

    // Only ExprBuilder can build rules :
    public Rule() {
        left = null;
        right = null;
        cost = 0;
        varNames = new VarTable();
        // Computed variables
        numLeft = 0;
        numRight = 0;
    }

    public void setLeft(List left) {
        this.left = left;
    }

    public void setRight(List right) {
        this.right = right;
    }

    public List getLeft() {
        return left;
    }

    public List getRight() {
        return right;
    }

    void setCost(int c) {
        cost = c;
    }

    public int getCost() {
        return cost;
    }

    public VarTable getVarTable() {
        return varNames;
    }

    Bindings makeBindings() {
        return new Bindings(getVarTable());
    }

    String toString(Bindings b) {
        StringBuffer res = new StringBuffer();
        Iterator i;

        if (startsWithEpsilon())
            res.append("epsilon");
        else {
            for (i = getLeft().iterator(); i.hasNext();)
                res.append(((Expr) (i.next())).toString(b)).append(", ");
            res.delete(res.length() - 2, res.length());
        }
        res.append(" -> ");

        if (getRight().size() != 0) {
            for (Iterator j = getRight().iterator(); j.hasNext();) {
                PlainExpr element = (PlainExpr) j.next();
                res.append(element.toString(b)).append(", ");
            }
            res.delete(res.length() - 2, res.length());
        }
        res.append(" / ").append(getCost());
        return res.toString();

    }

    // Information about the rule
    // Left part is epsilon ???
    boolean startsWithEpsilon() {
        return numLeft == 0;
    }

    // Number of elements in left part of the rule
    int getNumLeft() {
        return numLeft;
    }

    // Number of elements in right part of the rule
    int getNumRight() {
        return numRight;
    }

}