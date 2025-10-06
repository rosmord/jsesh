package jsesh.drawingspecifications.elements.enclosureframe;

/**
 * Hwt sign specifications.
 */
public record HwtSignSpec(
		float smallMargin,
		float squareSize
		) {

	public static final HwtSignSpec DEFAULT = 
			new HwtSignSpec(3, 10);

	/** Create a copy. */
	public Builder copy() {
		return new Builder(this);
	}
	/** copy builder class */
	public static class Builder {
		private float smallMargin;
		private float squareSize;
		public Builder(HwtSignSpec specs) {
			this.smallMargin = specs.smallMargin;
			this.squareSize = specs.squareSize;
		}
		public Builder smallMargin(float smallMargin) {
			this.smallMargin = smallMargin;
			return this;
		}
		public Builder squareSize(float squareSize) {
			this.squareSize = squareSize;
			return this;
		}
		public HwtSignSpec build() {
			return new HwtSignSpec(smallMargin, squareSize);
		}
	}
}

