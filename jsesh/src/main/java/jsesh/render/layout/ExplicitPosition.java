/*
 * Created on 25 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.render.layout;

/**
 * Describes a sign explicit placement in a ligature.
 * @author S. Rosmorduc
 *
 */
public class ExplicitPosition {
	
	private double x,y, scale;
	
	public ExplicitPosition(double x, double y, double scale) {
		this.x= x;
		this.y= y;
		this.scale= scale;
	}
	
	
	/**
	 * @return the scale.
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * @return x.
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return y.
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param f
	 */
	public void setScale(double f) {
		scale = f;
	}

	/**
	 * @param f
	 */
	public void setX(double f) {
		x = f;
	}

	/**
	 * @param f
	 */
	public void setY(double f) {
		y = f;
	}

}
