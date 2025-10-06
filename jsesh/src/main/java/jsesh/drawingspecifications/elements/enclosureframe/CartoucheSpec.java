package jsesh.drawingspecifications.elements.enclosureframe;

/**
 * Specifications for cartouches.
 * 
 * (done)
 * @author rosmord
 */
public record CartoucheSpec(
		float lineWidth,
		float loopLength,
		float margin) {


	public static final CartoucheSpec DEFAULT = 
			new CartoucheSpec(1, 10, 2);
		
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
		private float lineWidth;
		private float loopLength;
		private float margin;

		public Builder(CartoucheSpec specs) {
			this.lineWidth = specs.lineWidth;
			this.loopLength = specs.loopLength;
			this.margin = specs.margin;
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

		public CartoucheSpec build() {
			return new CartoucheSpec(lineWidth, loopLength, margin);
		}

	}
}