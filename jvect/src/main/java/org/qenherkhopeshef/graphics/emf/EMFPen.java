/**
 * 
 */
package org.qenherkhopeshef.graphics.emf;

/**
 * A pen for drawing.
 * @author rosmord
 *
 */
public class EMFPen {
	 /*
	  *  The pen and brush id. Package visibility.
	  */
	short penNum;
	short brushNum;
	
	/**
	 * @param penNum
	 * @param brushNum
	 */
	public EMFPen(short penNum, short brushNum) {
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
