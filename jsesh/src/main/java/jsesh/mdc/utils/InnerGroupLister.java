/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 7 dec. 2004
 *
 */
package jsesh.mdc.utils;

import java.util.ArrayList;
import java.util.List;

import jsesh.mdc.model.InnerGroup;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementDeepAdapter;

/**
 * A visitor able to extract all inner groups from an element.
 * @author Serge Rosmorduc
 */
public class InnerGroupLister {
    
    /**
     * Return all inner groups contained in elt.
     * @param elt
     * @return the groups contained in elt.
     */
    static public InnerGroup [] getInnerGroups(ModelElement elt) {
        InnerGroupListerAux aux= new InnerGroupListerAux();
        elt.accept(aux);
        return (InnerGroup[]) aux.innerGroups.toArray(new InnerGroup[aux.innerGroups.size()]);
    }
    
    /**
     * Return all inner groups contained in elt's children, between indexes a and b.
     * @param elt 
     * @param a
     * @param b
     * @return all inner groups contained in elt's children, between indexes a and b.
     */
    static public InnerGroup [] getInnerGroups(ModelElement elt, int a, int b) {
        InnerGroupListerAux aux= new InnerGroupListerAux();
        for (int i=a; i < b; i++) {
            elt.getChildAt(i).accept(aux);
        }
        return (InnerGroup[]) aux.innerGroups.toArray(new InnerGroup[aux.innerGroups.size()]);
    }
    
    static private class InnerGroupListerAux extends ModelElementDeepAdapter {
        private List innerGroups= new ArrayList();
        
        /* (non-Javadoc)
         * @see jsesh.mdc.model.ModelElementDeepAdapter#visitInnerGroup(jsesh.mdc.model.InnerGroup)
         */
        public void visitInnerGroup(InnerGroup g) {
            innerGroups.add(g);
        }
    }
    
}
