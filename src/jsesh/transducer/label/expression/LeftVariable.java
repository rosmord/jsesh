/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 31 mars 2005
 *
 */
package jsesh.transducer.label.expression;

/**
 * @author Serge Rosmorduc
 */
public class LeftVariable extends LeftExpression {
    private String name;
    
    /**
     * @param name
     */
    public LeftVariable(String name) {
        super();
        this.name = name;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
}
