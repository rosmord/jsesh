package jsesh.drawingspecifications;

import java.awt.Stroke;
import java.awt.print.PageFormat;
import java.awt.BasicStroke;

/**
 * Specification for rendering strokes.
 * 
 * This class has a huge number of parameters, but grouping them in smaller
 * classes is confusing. We have tried it.
 * Using a more generic approach (i.e. a map of parameters) could work, but we
 * would loose type safety, autocompletion and
 * some speed.
 * 
 * Note: currently, textWidth, textHeight, leftMargin and topMargin
 * and PageFormat are not very well defined.
 * 
 * @param fineLineWidth       the width for fine lines (drawn with {lx1,x2})
 * @param wideLineWidth       the width for wide lines (drawn with {lx1,x2})
 * @param lineSkip            space between text lines
 * @param columnSkip          space between text columns (in column orientation)
 * @param tabUnitWidth        the size of a unit in mdc tabulation (?x-)
 * @param cartoucheLineWidth  the width of the cartouche line
 * @param cartoucheLoopLength the size of the cartouche loop
 * @param cartoucheMargin     the margin between the cartouche and its content.
 * @param bastionDepth        the depth of a bastion (for square fortress
 *                            enclosures)
 * @param bastionLength       the length of a bastion (for square fortress
 *                            enclosures)
 * @param hwtSmallMargin      the margin between the hwt and its content.
 * @param hwtSquareSize       the size of the small square in the hwt.
 * @param serekhDoorSize      the size of the door in the serekh.
 * @param textWidth           the width of the text area (not very well defined)
 * @param textHeight          the height of the text area (not very well
 *                            defined)
 * @param leftMargin          the left margin (not very well defined)
 * @param topMargin           the top margin (not very well defined)
 * @param format              the page format (not very well defined)
 * @param maxCadratHeight     the maximum height of a quadrat (in points)
 * @param maxCadratWidth      the maximum width of a quadrat (in points)
 * @param smallSkip           a small space (used in various places)
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
public record GeometrySpecification(
		float fineLineWidth, // 0.5f,
		float wideLineWidth, // 3f,
		float lineSkip, // 6f,
		float columnSkip, // 10f
		float tabUnitWidth, // 18f/ 200f,
		float cartoucheLineWidth, // 1,
		float cartoucheLoopLength, // 10,
		float cartoucheMargin, // 2
		float bastionDepth, // 3
		float bastionLength, // 4
		float hwtSmallMargin, // 3
		float hwtSquareSize, // 10
		float serekhDoorSize, // 20
		float textWidth, // 538
		float textHeight, // 760
		float leftMargin, // 6
		float topMargin, // 5
		PageFormat format, // null
		float maxCadratHeight, // 18f,
		float maxCadratWidth, // 22f,
		float smallSkip, // 2f,
		float largeSignSizeRatio, // 0.8f,
		float smallSignSizeRatio, // 0.4f,
		float smallBodyScaleLimit, // 12f,
		float standardSignHeight // 18f);
) {

	public static final GeometrySpecification DEFAULT = new GeometrySpecification(
			0.5f,
			3f,
			6f,
			10f,
			18f / 200f,
			1,
			10,
			2,
			3,
			4,
			3,
			10,
			20,
			538,
			760,
			6,
			5,
			null,
			18f,
			22f,
			2f,
			0.8f,
			0.4f,
			12f,
			18f);

	/**
	 * Auxiliary method to build a stroke for fine lines.
	 * @return a stroke for fine lines.
	 */
	public Stroke fineStroke() {
		return new BasicStroke(fineLineWidth);
	}

	/**
	 * Auxiliary method to build a stroke for wide lines.
	 * @return a stroke for wide lines.
	 */
	public Stroke wideStroke() {
		return new BasicStroke(wideLineWidth);
	}

	/**
	 * Auxiliary method to build a stroke for cartouches.
	 * @return a stroke for cartouches.
	 */
	public Stroke cartoucheStroke() {
		return new BasicStroke(cartoucheLineWidth);
	}

	public Builder copy() {
		return new Builder(this);
	}

	/** copy builder class */
	public static class Builder {
		private float fineLineWidth; // 0.5f,
		private float wideLineWidth; // 3f,
		private float lineSkip; // 6f,
		private float columnSkip; // 10f
		private float tabUnitWidth; // 18f/ 200f,
		private float cartoucheLineWidth; // 1,
		private float cartoucheLoopLength; // 10,
		private float cartoucheMargin; // 2
		private float bastionDepth; // 3
		private float bastionLength; // 4
		private float hwtSmallMargin; // 3
		private float hwtSquareSize; // 10
		private float serekhDoorSize; // 20
		private float textWidth; // 538
		private float textHeight; // 760
		private float leftMargin; // 6
		private float topMargin; // 5
		private PageFormat format; // null
		private float maxCadratHeight; // 18f,
		private float maxCadratWidth; // 22f,
		private float smallSkip; // 2f,
		private float largeSignSizeRatio; // 0.8f,
		private float smallSignSizeRatio; // 0.4f,
		private float smallBodyScaleLimit; // 12f,
		private float standardSignHeight; // 18f;

		public Builder(GeometrySpecification specs) {
			this.fineLineWidth = specs.fineLineWidth;
			this.wideLineWidth = specs.wideLineWidth;
			this.lineSkip = specs.lineSkip;
			this.columnSkip = specs.columnSkip;
			this.tabUnitWidth = specs.tabUnitWidth;
			this.cartoucheLineWidth = specs.cartoucheLineWidth;
			this.cartoucheLoopLength = specs.cartoucheLoopLength;
			this.cartoucheMargin = specs.cartoucheMargin;
			this.bastionDepth = specs.bastionDepth;
			this.bastionLength = specs.bastionLength;
			this.hwtSmallMargin = specs.hwtSmallMargin;
			this.hwtSquareSize = specs.hwtSquareSize;
			this.serekhDoorSize = specs.serekhDoorSize;
			this.textWidth = specs.textWidth;
			this.textHeight = specs.textHeight;
			this.leftMargin = specs.leftMargin;
			this.topMargin = specs.topMargin;
			this.format = specs.format;
			this.maxCadratHeight = specs.maxCadratHeight;
			this.maxCadratWidth = specs.maxCadratWidth;
			this.smallSkip = specs.smallSkip;
			this.largeSignSizeRatio = specs.largeSignSizeRatio;
			this.smallSignSizeRatio = specs.smallSignSizeRatio;
			this.smallBodyScaleLimit = specs.smallBodyScaleLimit;
			this.standardSignHeight = specs.standardSignHeight;
		}

		public Builder fineLineWidth(float fineLineWidth) {
			this.fineLineWidth = fineLineWidth;
			return this;
		}

		public Builder wideLineWidth(float wideLineWidth) {
			this.wideLineWidth = wideLineWidth;
			return this;
		}

		public Builder lineSkip(float lineSkip) {
			this.lineSkip = lineSkip;
			return this;
		}

		public Builder columnSkip(float columnSkip) {
			this.columnSkip = columnSkip;
			return this;
		}

		public Builder tabUnitWidth(float tabUnitWidth) {
			this.tabUnitWidth = tabUnitWidth;
			return this;
		}

		public Builder cartoucheLineWidth(float cartoucheLineWidth) {
			this.cartoucheLineWidth = cartoucheLineWidth;
			return this;
		}

		public Builder cartoucheLoopLength(float cartoucheLoopLength) {
			this.cartoucheLoopLength = cartoucheLoopLength;
			return this;
		}

		public Builder cartoucheMargin(float cartoucheMargin) {
			this.cartoucheMargin = cartoucheMargin;
			return this;
		}

		public Builder bastionDepth(float bastionDepth) {
			this.bastionDepth = bastionDepth;
			return this;
		}

		public Builder bastionLength(float bastionLength) {
			this.bastionLength = bastionLength;
			return this;
		}

		public Builder hwtSmallMargin(float hwtSmallMargin) {
			this.hwtSmallMargin = hwtSmallMargin;
			return this;
		}

		public Builder hwtSquareSize(float hwtSquareSize) {
			this.hwtSquareSize = hwtSquareSize;
			return this;
		}

		public Builder serekhDoorSize(float serekhDoorSize) {
			this.serekhDoorSize = serekhDoorSize;
			return this;
		}

		public Builder textWidth(float textWidth) {
			this.textWidth = textWidth;
			return this;
		}

		public Builder textHeight(float textHeight) {
			this.textHeight = textHeight;
			return this;
		}

		public Builder leftMargin(float leftMargin) {
			this.leftMargin = leftMargin;
			return this;
		}

		public Builder topMargin(float topMargin) {
			this.topMargin = topMargin;
			return this;
		}

		public Builder format(PageFormat format) {
			this.format = format;
			return this;
		}

		public Builder maxCadratHeight(float maxCadratHeight) {
			this.maxCadratHeight = maxCadratHeight;
			return this;
		}

		public Builder maxCadratWidth(float maxCadratWidth) {
			this.maxCadratWidth = maxCadratWidth;
			return this;
		}

		public Builder smallSkip(float smallSkip) {
			this.smallSkip = smallSkip;
			return this;
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

		public GeometrySpecification build() {
			return new GeometrySpecification(
					fineLineWidth,
					wideLineWidth,
					lineSkip,
					columnSkip,
					tabUnitWidth,
					cartoucheLineWidth,
					cartoucheLoopLength,
					cartoucheMargin,
					bastionDepth,
					bastionLength,
					hwtSmallMargin,
					hwtSquareSize,
					serekhDoorSize,
					textWidth,
					textHeight,
					leftMargin,
					topMargin,
					format,
					maxCadratHeight,
					maxCadratWidth,
					smallSkip,
					largeSignSizeRatio,
					smallSignSizeRatio,
					smallBodyScaleLimit,
					standardSignHeight);
		}
	}
}
