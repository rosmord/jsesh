package org.qenherkhopeshef.viewToolKit.drawing.element;

/**
 * Margins for a page or similar stuff.
 * Note : this is an immutable object / Value semantics.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class Margin {
	private double top, bottom, left, right;

	/**
	 * Empty margin.
	 */
	public Margin() {
		this(2,2,2,2);
	}
	
	/**
	 * Create a uniform margin.
	 * <p> the same value will be used for top, bottom, left and right margins.
	 * @param margin
	 */
	public Margin(double margin) {
		this(margin,margin,margin,margin);
	}
	
	/**
	 * Create a margin giving all sides dimensions.
	 * @param top
	 * @param bottom
	 * @param left
	 * @param right
	 */
	public Margin(double top, double bottom, double left, double right) {
		super();
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}

	/**
	 * @return the top
	 */
	public double getTop() {
		return top;
	}

	/**
	 * @return the bottom
	 */
	public double getBottom() {
		return bottom;
	}

	/**
	 * @return the left
	 */
	public double getLeft() {
		return left;
	}

	/**
	 * @return the right
	 */
	public double getRight() {
		return right;
	}

	public double getTotalMarginWidth() {
		return left+right;
	}

	public double getTotalMarginHeight() {
		return top+ bottom;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(bottom);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(left);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(right);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(top);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Margin other = (Margin) obj;
		if (Double.doubleToLongBits(bottom) != Double
				.doubleToLongBits(other.bottom))
			return false;
		if (Double.doubleToLongBits(left) != Double
				.doubleToLongBits(other.left))
			return false;
		if (Double.doubleToLongBits(right) != Double
				.doubleToLongBits(other.right))
			return false;
		if (Double.doubleToLongBits(top) != Double.doubleToLongBits(other.top))
			return false;
		return true;
	}
	
	
	
	
}
