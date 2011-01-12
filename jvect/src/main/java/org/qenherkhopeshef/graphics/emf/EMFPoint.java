/**
 * 
 */
package org.qenherkhopeshef.graphics.emf;

/**
 * Point in an enhanced metafile.
 * @author rosmord
 *
 */
public class EMFPoint {
	private long x;
	private long y;
	
	
	public EMFPoint(long x, long y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public EMFPoint() {
	}
	
	public long getX() {
		return x;
	}
	
	public void setX(long x) {
		this.x = x;
	}
	
	public long getY() {
		return y;
	}
	
	public void setY(long y) {
		this.y = y;
	}

}
