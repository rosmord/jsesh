/*
 * Created on 25 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.constants;

import jsesh.utils.EnumBase;

/**
 * Writing direction for the text : left to right or right to left.
 * @author S. Rosmorduc
 *
 */
public class TextDirection extends EnumBase {

	public static final TextDirection LEFT_TO_RIGHT= new TextDirection(0,"LEFT_TO_RIGHT");
	public static final TextDirection RIGHT_TO_LEFT= new TextDirection(1, "RIGHT_TO_LEFT");
	
	/**
	 * @param id
	 * @param designation
	 */
	
	public TextDirection(int id, String designation) {
		super(id, designation);
	}
	
	public boolean isLeftToRight() {
	    return getId() == 0;
	}
}
