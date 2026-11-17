/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 31 mars 2005
 *
 */
package jsesh.transducer.label.expression;

/**
 * @author Serge Rosmorduc
 */
public class SimpleLeftExpression extends LeftExpression {
    private String functor;
    private LeftExpression args[];
    
    
    /**
     * @param functor
     * @param args
     */
    public SimpleLeftExpression(String functor, LeftExpression[] args) {
        this.functor = functor;
        this.args = args;
    }
    
    public SimpleLeftExpression(String functor, LeftExpression arg0) {
        this.functor = functor;
        this.args = new LeftExpression[1];
        args[0]= arg0;
    }
    
    public SimpleLeftExpression(String functor) {
        this.functor = functor;
        this.args = new LeftExpression[0];
    }
    
   
    
    public SimpleLeftExpression(String functor, LeftExpression arg0, LeftExpression arg1) {
        this.functor = functor;
        this.args = new LeftExpression[2];
        args[0]= arg0;
        args[1]= arg1;
    }
    
    /**
     * @return Returns the args.
     */
    public LeftExpression[] getArgs() {
        return args;
    }
    
    /**
     * @return Returns the functor.
     */
    public String getFunctor() {
        return functor;
    }
}
