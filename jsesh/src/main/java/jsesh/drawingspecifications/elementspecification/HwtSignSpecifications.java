package jsesh.drawingspecifications.elementspecification;

public record HwtSignSpecifications(
		float smallMargin,
		float squareSize
		) {

	/** Create a copy. */
	public Builder copy() {
		return new Builder(this);
	}
	/** copy builder class */
	public static class Builder {
		private float smallMargin;
		private float squareSize;
		public Builder(HwtSignSpecifications specs) {
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
		public HwtSignSpecifications build() {
			return new HwtSignSpecifications(smallMargin, squareSize);
		}
	}
}

