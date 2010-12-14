/*
 * Created on 29 mai 2004
 *
 * This code is published under the LGPL.
 */
package jsesh.utils;

import java.awt.geom.Dimension2D;

/**
 * DoubleDimensions
 * @author rosmord
 *
 *(a similar class also exists in jvectClipboard. But this introduces a quite artificial dependency between the two packages.
 * As basically, those objects are simple implementations of Dimension2D (and should be part of the standard java library in the first place),
 * this is not a huge issue.
 *This code is published under the GNU LGPL.
 */
public class DoubleDimensions extends Dimension2D {

	private double height;
	private double width;
	
	public DoubleDimensions(double width, double height) {
		setSize(width, height);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.geom.Dimension2D#setSize(double, double)
	 */
	public void setSize(double width, double height) {
		this.width= width;
		this.height= height;
	}
	

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}

	public String toString() {
		return "("+ width + ", "+ height+ ")";
	}
}
