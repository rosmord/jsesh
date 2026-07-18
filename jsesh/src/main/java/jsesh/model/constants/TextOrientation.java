/*
 * Created on 25 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.model.constants;


/**
 * Constants for text general orientation : horizontal or vertical.
 * 
 * @author S. Rosmorduc
 * 
 */
public enum TextOrientation {

	HORIZONTAL, VERTICAL;

	/**
	 * @return true if this is the horizontal orientation.
	 */
	public boolean isHorizontal() {
		return this == HORIZONTAL;
	}
}
