/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mai 2005
 *
 */
package jsesh.transducer.tests;

import java.util.ArrayList;

/**
 * @author Serge Rosmorduc
 * This class represents the bindings of variables when applying RuleTransducer.
 * Bindings are kept in an incremental way, so that they can be shared.
 * 
 * <p> An important note about binding :
 * 	We consider that (by creation design), variable number 0 will be the first
 * variable seen in a rule, variable 1 the second and so on.
 */
public class Bindings {
    private Bindings parent; 
    private int firstIndice;
    private ArrayList values;
    
    /**
     * 
     */
    public Bindings() {
        parent= null;
        firstIndice= 0;
        values= new ArrayList(2);
    }
    
    public Bindings(Bindings parent) {
        this.parent= parent;
        firstIndice= parent.firstIndice+ parent.values.size();
        values= new ArrayList(2);
    }
    
    /**
     * Get the value of variable number i.
     * @param i
     * @return the value.
     */
    public Object getValue(int i) {
        if (i >= firstIndice)
            return values.get(i - firstIndice);
        else
            return
            	parent.getValue(i);
    }
    
    public void setValue(int i, Object o) {
        if (i >= firstIndice) {
            int realI= i - firstIndice;
            ensureHasSpace(realI);
            values.set(realI, o);
        } else {
            parent.setValue(i,o);
        }
    }
    
    /**
     * Create empty elements, if needed, to accomodate a index.
     * @param i : the "real" index in the value array (not the logical one).
     */
    private void ensureHasSpace(int i) {
        while (i >= values.size())
            values.add(null);
    }
}
