package jsesh.drawingspecifications.elementspecification;

public record CartoucheSpecifications(
		float knotLength,
		float lineWidth,
		float loopLength,
		float margin) {
	/**
	 * Create a copy
	 * 
	 * @return a builder initialized with the current values.
	 */
	public Builder copy() {
		return new Builder(this);
	}

	/** copy builder class */
	public static class Builder {
		private float knotLength;
		private float lineWidth;
		private float loopLength;
		private float margin;

		public Builder(CartoucheSpecifications specs) {
			this.knotLength = specs.knotLength;
			this.lineWidth = specs.lineWidth;
			this.loopLength = specs.loopLength;
			this.margin = specs.margin;
		}

		public Builder knotLength(float knotLength) {
			this.knotLength = knotLength;
			return this;
		}

		public Builder lineWidth(float lineWidth) {
			this.lineWidth = lineWidth;
			return this;
		}

		public Builder loopLength(float loopLength) {
			this.loopLength = loopLength;
			return this;
		}

		public Builder margin(float margin) {
			this.margin = margin;
			return this;
		}

		public CartoucheSpecifications build() {
			return new CartoucheSpecifications(knotLength, lineWidth, loopLength, margin);
		}

	}
}