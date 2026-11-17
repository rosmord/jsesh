/*
 * Created on 2 mai 2005 by rosmord
 *
 * TODO document the file SimpleLabel.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.transducer.label.simple;

import java.util.ArrayList;
import java.util.List;

/**
 * A functionnal label for links.
 * <p>
 * symbols are considered as functor of arity 0.
 * 
 * @author rosmord
 */
public class SimpleLabel {
    private List args;

    private String functor;

    public SimpleLabel(String functor) {
        this.functor = functor;
    }

    public int getNumberOfArgs() {
        if (args == null)
            return 0;
        else
            return getArgs().size();
    }

    public SimpleLabel getArg(int i) {
        return (SimpleLabel) getArgs().get(i);
    }

    public void addArg(SimpleLabel arg) {
        getArgs().add(arg);
    }

    public void setArg(int i, SimpleLabel arg) {
        getArgs().set(i, arg);
    }
    
    private List getArgs() {
        if (args == null) {
            args = new ArrayList();
        }
        return args;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer result= new StringBuffer(functor);
        if (getNumberOfArgs() > 0) {
            result.append('(');
            result.append(getArg(0));
            for (int i=1; i < getNumberOfArgs(); i++) {                
                result.append(", ").append(getArg(i));
            }
            result.append(')');
        }
        return result.toString();
    }
}
