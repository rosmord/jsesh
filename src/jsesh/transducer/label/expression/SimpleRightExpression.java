/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 31 mars 2005
 *
 */
package jsesh.transducer.label.expression;

/**
 * @author Serge Rosmorduc
 */
public class SimpleRightExpression extends RightExpression {
    private String functor;
    private RightExpression args[];
    
    /**
     * @param functor
     * @param args
     */
    public SimpleRightExpression(String functor, RightExpression[] args) {
        super();
        this.functor = functor;
        this.args = args;
    }
    
    /**
     * A simple contructor for unary expression.
     * Mainly useful in tests.
     * @param functor
     */
    public SimpleRightExpression(String functor) {
        this.functor= functor;
        this.args= new RightExpression[0];        
    }
    
    /**
     * A simple contructor for unary expression.
     * Mainly useful in tests.
     * @param functor
     * @param arg0
     */
    public SimpleRightExpression(String functor, RightExpression arg0) {
        this.functor= functor;
        this.args= new RightExpression[1];
        args[0]= arg0;
    }
    
    /**
     * A simple contructor for binary expression.
     * Mainly useful in tests.
     * @param functor
     * @param arg0
     */
    public SimpleRightExpression(String functor, RightExpression arg0, RightExpression arg1) {
        this.functor= functor;
        this.args= new RightExpression[2];
        args[0]= arg0;
        args[1]= arg1;
    }
    
    /**
     * Simple constructor for test purporses
     * @param functor
     * @param args  an array of strings representing constant arguments
     */
    public SimpleRightExpression(String functor, String[] args) {
        this.functor = functor;
        this.args = new RightExpression[args.length];
        for (int i=0; i< args.length; i++)
            this.args[i]= new SimpleRightExpression(args[i]);
    }
    
    /**
     * @return Returns the args.
     */
    public RightExpression[] getArgs() {
        return args;
    }
    /**
     * @return Returns the functor.
     */
    public String getFunctor() {
        return functor;
    }
}
