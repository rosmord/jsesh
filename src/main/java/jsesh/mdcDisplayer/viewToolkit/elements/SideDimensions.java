package jsesh.mdcDisplayer.viewToolkit.elements;

/**
 * Dimensions attached to the sides of a rectangular area.
 * Suitable for margins, paddings, etc.
 * @author rosmord
 *
 */
public class SideDimensions {
	private double top, left, right, bottom;

	public SideDimensions(double left, double right, double top, double bottom) {
		super();
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}

	public SideDimensions(double dim) {
		this(dim,dim,dim,dim);
	}
	
	public SideDimensions() {
		this(0);
	}
	
	public double getTop() {
		return top;
	}

	public void setTop(double top) {
		this.top = top;
	}

	public double getLeft() {
		return left;
	}

	public void setLeft(double left) {
		this.left = left;
	}

	public double getRight() {
		return right;
	}

	public void setRight(double right) {
		this.right = right;
	}

	public double getBottom() {
		return bottom;
	}

	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	public double getWidth() {
		return left+right;
	}

	public double getHeight() {
		return top+bottom;
	}
	
	
	
	
}
