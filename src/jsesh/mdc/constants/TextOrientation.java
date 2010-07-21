/*
 * Created on 25 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.constants;

import jsesh.utils.EnumBase;

/**
 * Constants for text general orientation : horizontal or vertical.
 * @author S. Rosmorduc
 *
 */
public class TextOrientation extends EnumBase {

	public  static final TextOrientation HORIZONTAL= new TextOrientation(0, "HORIZONTAL");
	public static final TextOrientation VERTICAL= new TextOrientation(1,"VERTICAL");

	/**
	 * @param id
	 * @param designation
	 */
	private TextOrientation(int id, String designation) {
		super(id, designation);
	}

    /**
     * @return true if this is the horizontal orientation.
     */
    public boolean isHorizontal() {      
        return getId() == HORIZONTAL.getId();
    }
}
