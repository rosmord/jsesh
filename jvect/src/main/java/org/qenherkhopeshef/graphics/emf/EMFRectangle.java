/**
 * 
 */
package org.qenherkhopeshef.graphics.emf;

/**
 * @author rosmord
 *
 */
public class EMFRectangle {
	private long minx, miny, maxx, maxy;

	
	public EMFRectangle() {
		super();
	}

	public EMFRectangle(long minx, long miny, long maxx, long maxy) {
		super();
		this.minx = minx;
		this.miny = miny;
		this.maxx = maxx;
		this.maxy = maxy;
	}

	public long getMaxx() {
		return maxx;
	}

	public void setMaxx(long maxx) {
		this.maxx = maxx;
	}

	public long getMaxy() {
		return maxy;
	}

	public void setMaxy(long maxy) {
		this.maxy = maxy;
	}

	public long getMinx() {
		return minx;
	}

	public void setMinx(long minx) {
		this.minx = minx;
	}

	public long getMiny() {
		return miny;
	}

	public void setMiny(long miny) {
		this.miny = miny;
	}
	
	

}
