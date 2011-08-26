/*
 * Created on 25 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.constants;


/**
 * Writing direction for the text : left to right or right to left.
 * 
 * @author S. Rosmorduc
 * 
 */
public enum TextDirection {

	LEFT_TO_RIGHT, RIGHT_TO_LEFT;

	public boolean isLeftToRight() {
		return this == LEFT_TO_RIGHT;
	}

}
