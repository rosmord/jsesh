/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mars 2005
 *
 */
package jsesh.transducer;

import java.util.HashMap;

/**
 * @author Serge Rosmorduc
 */
public class VarTable {

    private HashMap map = new HashMap();

    /**
     * @return
     */
    public int getNbVars() {
        return map.size();
    }

    /**
     * @param varname
     * @return
     */
    public VarRep getVar(String varname) {
        VarRep result; 
        if (map.containsKey(varname)) {
            result= (VarRep) map.get(varname);
        } else {
            int num= getNbVars();
            result= new VarRep(varname, num);
            map.put(varname, result);
        }
        return result;
    }

}