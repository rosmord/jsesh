package org.qenherkhopeshef.graphics.eps;

/**
 * Color used at low level in our EPS files.
 * we do use integer values, as they are easier to manipulate.
 * @author rosmord
 *
 */
public class EPSColor {
	private int r,g,b;

	public EPSColor(int r, int g, int b) {
		super();
		this.r = r;
		this.g = g;
		this.b = b;
	}

	/**
	 * @return the r
	 */
	public int getR() {
		return r;
	}

	/**
	 * @return the g
	 */
	public int getG() {
		return g;
	}

	/**
	 * @return the b
	 */
	public int getB() {
		return b;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + b;
		result = prime * result + g;
		result = prime * result + r;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final EPSColor other = (EPSColor) obj;
		if (b != other.b)
			return false;
		if (g != other.g)
			return false;
		if (r != other.r)
			return false;
		return true;
	}
	
	
}
