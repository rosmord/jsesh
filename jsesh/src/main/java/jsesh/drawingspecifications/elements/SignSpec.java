package jsesh.drawingspecifications.elements;

/**
 * Specification for the rendering of signs.
 * 
 * @param largeSignSizeRatio  the ratio to base size a sign should have to be
 *                            considered a "large" sign.
 * @param smallSignSizeRatio  the ratio to base size a sign should have to be
 *                            considered a "small" sign. i.e. if a sign size is
 *                            smaller than
 *                            <code>getBaseLength() * getSmallSize(),</code>
 *                            this
 *                            sign is small.
 * @param smallBodyScaleLimit the size (in points) below which we will start
 *                            using
 *                            small body fonts. For instance, if we set it to
 *                            10,
 *                            any sign drawn with a scale such that the A1 sign
 *                            height would be below the said limit will be drawn
 *                            in
 *                            the small body (bold) font.
 * @param standardSignHeight  the expected height of A1, in points.
 */
public record SignSpec(
		float largeSignSizeRatio,
		float smallSignSizeRatio,
		float smallBodyScaleLimit,
		float standardSignHeight

) {

	public static final SignSpec DEFAULT = new SignSpec(
			0.8f,
			0.4f,
			12f,
			18f);

	public Builder copy() {
		return new Builder(this);
	}

	/** Copy builder class */
	public static class Builder {
		private float largeSignSizeRatio;
		private float smallSignSizeRatio;
		private float smallBodyScaleLimit;
		private float standardSignHeight;

		public Builder(SignSpec specs) {
			this.largeSignSizeRatio = specs.largeSignSizeRatio;
			this.smallSignSizeRatio = specs.smallSignSizeRatio;
			this.smallBodyScaleLimit = specs.smallBodyScaleLimit;
			this.standardSignHeight = specs.standardSignHeight;
		}

		public Builder largeSignSizeRatio(float largeSignSizeRatio) {
			this.largeSignSizeRatio = largeSignSizeRatio;
			return this;
		}

		public Builder smallSignSizeRatio(float smallSignSizeRatio) {
			this.smallSignSizeRatio = smallSignSizeRatio;
			return this;
		}

		public Builder smallBodyScaleLimit(float smallBodyScaleLimit) {
			this.smallBodyScaleLimit = smallBodyScaleLimit;
			return this;
		}
		
		public Builder standardSignHeight(float standardSignHeight) {
			this.standardSignHeight = standardSignHeight;
			return this;
		}

		public SignSpec build() {
			return new SignSpec(largeSignSizeRatio, smallSignSizeRatio, smallBodyScaleLimit, standardSignHeight);
		}
	}

}
