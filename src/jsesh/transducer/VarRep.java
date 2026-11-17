/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mars 2005
 *
 */
package jsesh.transducer;

/**
 * @author Serge Rosmorduc
 */
public class VarRep {
    private int position;
    private String name;
  	
    /**
     * @param name
     * @param position
     */
    public VarRep(String name, int position) {
        super();
        this.position = position;
        this.name = name;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public int getPosition() {
        // TODO Auto-generated method stub
        return 0;
    }
}
