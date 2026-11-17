/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 31 mars 2005
 *
 */
package jsesh.transducer.label.expression;

import java.io.Serializable;

/**
 * An expression in which everything is known.
 * 
 * @author Serge Rosmorduc
 */

public class GroundedExpression implements Comparable, Serializable{

    private String functor;

    private GroundedExpression args[];

    public GroundedExpression(String functor) {
        this.functor = functor;
        this.args = new GroundedExpression[0];
    }

    /**
     * @param functor
     * @param args
     */
    public GroundedExpression(String functor, GroundedExpression[] args) {
        super();
        this.functor = functor;
        this.args = args;
    }

    public int getArity() {
        if (args != null)
            return args.length;
        else
            return 0;
    }

    /**
     * @return Returns the args.
     */
    public GroundedExpression[] getArgs() {
        return args;
    }

    /**
     * @param args
     *            The args to set.
     */
    public void setArgs(GroundedExpression[] args) {
        this.args = args;
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buff = new StringBuffer(functor);
        if (getArity() > 0) {
            buff.append('(');
            int i = 0;
            while (i < args.length - 1) {
                buff.append(args[i].toString()).append(", ");
                i++;
            }
            buff.append(args[i].toString());
            buff.append(')');
        }
        return super.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof GroundedExpression) {
            GroundedExpression e = (GroundedExpression) obj;
            // Same functor
            boolean result = functor.equals(e.functor);
            // Same arity
            result &= (args.length == e.args.length);
            // Same arguments
            int i = 0;
            while (result && i < args.length) {
                result &= args[i].equals(e.args[i]);
            }
            return result;
        } else {
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        int result= functor.hashCode();
        for (int i= 0; i < args.length; i++) {
            result= (result << 8) + args[i].hashCode();
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        GroundedExpression e= (GroundedExpression) o;
        // First, compare functors
        int result= functor.compareTo(e.functor);
        
        // Now compare args values
        int i= 0;
        while (i < args.length && i < e.args.length && result == 0)
        {
            result= args[i].compareTo(e.args[i]);
            i++;
        }
        // Now compare args length :
        if (result == 0) {
            result= args.length - e.args.length;
        }
        return result;
    }
    
}