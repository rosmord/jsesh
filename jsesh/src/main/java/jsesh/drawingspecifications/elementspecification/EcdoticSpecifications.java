package jsesh.drawingspecifications.elementspecification;

/**
 * 
 */
public record EcdoticSpecifications(
			/**
			 * The with of a philological bracket.
			 */
			float philologyWidth
		) {

		/**
		 * Create a copy.
		 */
		public Builder copy() {
			return new Builder(this);
		}

		/** copy builder class */
		public static class Builder {
			private float philologyWidth;

			public Builder(EcdoticSpecifications specs) {
				this.philologyWidth = specs.philologyWidth;
			}

			public Builder philologyWidth(float philologyWidth) {
				this.philologyWidth = philologyWidth;
				return this;
			}

			public EcdoticSpecifications build() {
				return new EcdoticSpecifications(philologyWidth);
			}
		}

}
