/*
 * Created on 7 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package org.qenherkhopeshef.graphics.wmf;



/**
 * An Utility class, representing a complete pen (pen + brush).
 * @author S. Rosmorduc
 *
 */
public class WMFPen {
	/**
	 * The pen and brush id. Package visibility.
	 */
	short penNum;
	short brushNum;
	
	/**
	 * @param penNum
	 * @param brushNum
	 */
	public WMFPen(short penNum, short brushNum) {
		this.penNum = penNum;
		this.brushNum = brushNum;
	}
	

	
	short getBrushNum() {
		return brushNum;
	}
	
	short getPenNum() {
		return penNum;
	}
}
