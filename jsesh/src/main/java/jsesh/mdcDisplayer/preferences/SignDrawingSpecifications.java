package jsesh.mdcDisplayer.preferences;

public record SignDrawingSpecifications(
		/**
		 * return the ratio to base size a sign should have to be considered a "large"
		 * sign.
		 */
		float largeSignSizeRatio,
		 /**
	     * Return the ratio to base size a sign should have to be considered a
	     * "small" sign. i.e. if a sign size is smaller than <code>getBaseLength() *
	     * getSmallSize(),</code> this sign is small.	     
	     */
		float smallSignSizeRatio,
		/**
		 * Returns the size (in points) below which we will start using small body
		 * fonts. For instance, if we set it to 10, any sign drawn with a scale such
		 * that the A1 sign height would be below the said limit will be drawn in the
		 * small body (bold) font.
		 */
		float smallBodyScaleLimit) {

}
