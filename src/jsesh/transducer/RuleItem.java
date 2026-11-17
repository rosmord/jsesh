/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mars 2005
 *
 */
package jsesh.transducer;

import java.util.Iterator;

/**
 * @author Serge Rosmorduc
 * 
 * Note about RuleItems
 * 
 * 
 * When requiring a ruleItem Upper and Lower part, there should be an overlap :
 * 
 * 
 * for instance for a rule A B C -> D E F :
 * 
 * <pre>
 * 
 *       A, B, C, 0, 0
 *       0, 0, D, E, F
 *  part l  l  m  r  r
 * 
 * </pre>
 * 
 * (0 means epsilon)
 * 
 * Now, there are special cases :
 * 
 * <pre>
 * 
 *  a) 0 -&gt; D =&gt; 
 *     part = middle
 *     
 *  b) D -&gt; 0 =&gt;
 *  c) 0 -&gt; 0
 * 
 * </pre>
 */

/* Could be a subclass of Rule !!! */

public class RuleItem implements Cloneable {

    // Instance variables
    //  position in rule
    private Rule rule; /* rule */

    private Iterator lpos; /* iterator for left part */

    private Iterator rpos; /* iterator for right part */

    private RulePart part; /* part of the rule (see drawing above) */

    private Bindings bindings; /* bindings for the variables used in rules */

    // Auxiliary instance variables
    private int itemNumber; /* rank of item in left part */

    private boolean first; /* are we at the beginning of a rule ?? */

    private BoundExpr low;

    //  Create a Rule Item corresponding to the beginning of a rule.
    public RuleItem(Rule rule) {
        this.rule = rule;
        lpos = rule.left.iterator();
        rpos = rule.right.iterator();
        itemNumber = 1;
        bindings = rule.makeBindings();
        if (rule.getNumLeft() > 1)
            part = RulePart.LEFT;
        else if (rule.getNumLeft() == 1)
            part = RulePart.MIDDLE;
        else
            part = RulePart.RIGHT;
        fixLow();
        first = true;
    }

    // can we call makeNext ?
    // operator bool();

    // makeNext(...) : If e can be matched, makeNext returns a new
    // RuleItem which corresponds to the rule after matching expression
    // e. If not, it returns 0.
    // BoundExpr & should be "const BoundExpr &",
    // But we are faced with an annoying problem :
    // We call the match() function from Expr, which
    // might change the bindings, but won't

    // BEWARE : TEMPORARY PATCH
    // Well, there's a problem with RuleItems and links.
    //

    public RuleItem makeMatch(BoundExpr expr) {
        RuleItem res = null;

        try {
            res = (RuleItem) clone();
        } catch (CloneNotSupportedException e) {
            // Won't happen.
            e.printStackTrace();
        }

        if (res.getUpper().match(expr.getExpr(), res.bindings,
                expr.getBindings())) {

        } else {

            res = null;
        }
        return res;

    }

    // Accessors :
    Expr getUpper() {
        if (!part.equals(RulePart.RIGHT))
            return lpos;
        else
            return Expr.EPSILON;
    }

    BoundExpr getLower() {
        return low;
    }

    int getCost() {
        if (first)
            return rule.getCost();
        else
            return 0;

    }

    Bindings getBindings() {
        return bindings;
    }

    // next : advances in the rule, regardless of matching anything.
    void next() {
        first = false;
        if (part.equals(RulePart.LEFT)) {
            lpos++;
            itemNumber++;
            if (itemNumber == rule.getNumLeft())
                part = RulePart.MIDDLE;
        } else if (part.equals(RulePart.RIGHT)) {
            itemNumber++;
            rpos++;
        } else {
            // MIDDLE

            // The first item of the right part is used by the middle element
            // as its lower part. Hence, middle should fix itemNumber at 2,
            // not one.
            part = RulePart.RIGHT;
            itemNumber = 2;
            rpos++;
        }        
        fixLow();
    }

    // true if this RuleItem is the last for its rule
boolean isFinal() {
         bool res= false;
         switch (part) {
         case left:
           res= false;
           break;
         case middle:
           res= (rule.getNumRight() < 2);
           break;
         case right:
           res= (rule.getNumRight() == itemNumber);
           break;
         default:
           throw string("This shouldn't be!!!!!!");
         }
         return res;
    
     }
    Rule getRule() {
        return rule;
    }

    //   gives the low part its value
private void fixLow() {
         if (part != RulePart.LEFT)
             low= new BoundExpr(rpos, bindings);
         else 
           low=null;

     }
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
public String toString() {
    StringBuffer res= new StringBuffer();
        res .append("Rule Item ")
        	.append(getRule().toString(getBindings()))
        	.append(" pos ");
      
      switch(part)
        {
        case RuleItem::left: cout << "left "; break;
        case RuleItem::right: cout << "right "; break;
        case RuleItem::middle: cout << "middle "; break;
        }
      
      if (r.part != RuleItem::middle)
        cout << r.itemNumber;
      */
      return res.toString();
     
    }};
