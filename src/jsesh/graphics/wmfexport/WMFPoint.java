/*
 * 
 * Created on 5 nov. 2004
 *
 * This file is distributed under the LGPL.
 * 
 */
package jsesh.graphics.wmfexport;

/**
 * A point in a WMF file.
 * @author rosmord
 *
 */
public class WMFPoint {
	public short x, y;

	public WMFPoint(short x, short y) {
		this.x = x;
		this.y = y;
	}
}