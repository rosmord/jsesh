/*
 * Created on 25 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.layout;

/**
 * Describes a sign explicit placement in a ligature.
 * @author S. Rosmorduc
 *
 */
public class ExplicitPosition {
	
	private float x,y, scale;
	
	public ExplicitPosition(float x, float y, float scale) {
		this.x= x;
		this.y= y;
		this.scale= scale;
	}
	
	
	/**
	 * @return the scale.
	 */
	public float getScale() {
		return scale;
	}

	/**
	 * @return x.
	 */
	public float getX() {
		return x;
	}

	/**
	 * @return y.
	 */
	public float getY() {
		return y;
	}

	/**
	 * @param f
	 */
	public void setScale(float f) {
		scale = f;
	}

	/**
	 * @param f
	 */
	public void setX(float f) {
		x = f;
	}

	/**
	 * @param f
	 */
	public void setY(float f) {
		y = f;
	}

}
