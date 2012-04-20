package org.qenherkhopeshef.viewToolKit.drawing.tabularDrawing;

/**
 * Coordinate on a grid.
 * (this could be a point, BTW) 
 * <p>
 * Value class.
 * <p>
 * Immutable.
 * 
 * @author rosmord
 * 
 */
public class Coordinates {
	private int j;
	private int i;

	public Coordinates(int i, int j) {
		this.i= i;
		this.j= j;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Coordinates) {
			Coordinates c = (Coordinates) obj;
			return getColumn() == c.getColumn() && getRow() == c.getRow();
		} else
			return false;
	}

	@Override
	public int hashCode() {
		// 10007 is prime.
		return getColumn() + 10007 * getRow();
	}

	@Override
	public String toString() {
		return "(" + getColumn() + ", " + getRow() + ")";
	}

	

	public int getColumn() {
		return i;
	}

	
	public int getRow() {
		return j;
	}
}