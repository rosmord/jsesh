package jsesh.render.style;

import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
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
 * The margins ({@code leftMargin}, {@code topMargin}) surround the text
 * area. The (optional) page of the document is described by
 * {@link PageSpecification}.
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
 * @param leftMargin          the margin on the left (and right) of the text, in
 *                            points. Lines start at this distance of the edge,
 *                            and the usable width of a wrapped line is
 *                            {@code wrapWidth - 2 * leftMargin}. When a page is
 *                            defined, the page's own margins are used instead
 *                            for the wrapping width.
 * @param topMargin           the margin above (and below) the text, in points.
 *                            Columns start at this distance of the top edge, and
 *                            the usable height of a column is
 *                            {@code columnHeight - 2 * topMargin}.
 * @param page                the page size and margins ({@link PageSpecification#NONE}
 *                            for a text as large as it needs to be). When the page
 *                            is defined, it determines the wrapping width, and
 *                            {@code wrapWidth} is ignored.
 * @param pageDecoration     the texts (page number, title) written in the margins
 *                            of each page ({@link PageDecoration#NONE} for none).
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
 * @param alphabeticTextBaseline the height of the baseline of alphabetic text
 *                            (latin, transliteration...) above the bottom line
 *                            of quadrats, in points. 0 puts the text on the
 *                            quadrats' baseline.
 * @param underlineGap        the space between a quadrat and its underline, in
 *                            points (see {@link PaintingSpecifications#underlineRed()}).
 *                            It is independent of {@code smallSkip}.
 * @param wrapWidth           the total width (margins included) to which lines
 *                            of horizontal text are soft-wrapped, in layout
 *                            units. 0 (or less) means no wrapping: lines only
 *                            end at explicit line breaks.
 * @param columnHeight        the total height (margins included) in which the
 *                            columns of vertical text are aligned, in layout
 *                            units. 0 (or less) means none: they are aligned
 *                            on the longest column. Columns are never cut.
 *                            When a page with a height is defined, its usable
 *                            height is used instead.
 * @param columnQuadratSkip   the space between two successive quadrats of a
 *                            column (the space between two quadrats of a line is
 *                            {@code smallSkip}).
 * @param unit                the reference length of the document, in points:
 *                            the lengths of the document preferences and of
 *                            zones ({@code %zone[...]}) are percents of it (see
 *                            {@link jsesh.model.LayoutSetting}).
 */
public record GeometrySpecification(
		double fineLineWidth, // 0.5,
		double wideLineWidth, // 3.0,
		double lineSkip, // 6.0,
		double columnSkip, // 10.0
		double tabUnitWidth, // 18.0/ 200.0,
		double cartoucheLineWidth, // 1,
		double cartoucheLoopLength, // 10,
		double cartoucheMargin, // 2
		double bastionDepth, // 3
		double bastionLength, // 4
		double hwtSmallMargin, // 3
		double hwtSquareSize, // 10
		double serekhDoorSize, // 20
		double leftMargin, // 6
		double topMargin, // 5
		PageSpecification page, // PageSpecification.NONE
		PageDecoration pageDecoration, // PageDecoration.NONE
		double maxCadratHeight, // 18.0,
		double maxCadratWidth, // 22.0,
		double smallSkip, // 2.0,
		double largeSignSizeRatio, // 0.8,
		double smallSignSizeRatio, // 0.4,
		double smallBodyScaleLimit, // 12.0,
		double standardSignHeight, // 18.0
		double alphabeticTextBaseline, // 4.0
		double underlineGap, // 2.0
		double wrapWidth, // 0.0 (no wrapping)
		double columnHeight, // 0.0 (natural height)
		double columnQuadratSkip, // 6.0
		double unit // 18.0
) {

	public static final GeometrySpecification DEFAULT = new GeometrySpecification(
			0.5,
			3.0,
			6.0,
			10.0,
			18.0 / 200.0,
			1,
			10,
			2,
			3,
			4,
			3,
			10,
			20,
			6,
			5,
			PageSpecification.NONE,
			PageDecoration.NONE,
			18.0,
			22.0,
			2.0,
			0.8,
			0.4,
			12.0,
			18.0,
			4.0,
			2.0,
			0.0,
			0.0,
			6.0,
			18.0);

	/// The unit of the lengths of zones and of the document preferences: one
	/// percent of [#unit()].
	///
	/// @return the length of 1 %, in points.
	public double percentUnit() {
		return unit / 100.0;
	}

	/**
	 * Auxiliary method to build a stroke for fine lines.
	 * @return a stroke for fine lines.
	 */
	public Stroke fineStroke() {
		return new BasicStroke((float) fineLineWidth);
	}

	/**
	 * Auxiliary method to build a stroke for wide lines.
	 * @return a stroke for wide lines.
	 */
	public Stroke wideStroke() {
		return new BasicStroke((float) wideLineWidth);
	}

	/**
	 * Auxiliary method to build a stroke for cartouches.
	 * @return a stroke for cartouches.
	 */
	public Stroke cartoucheStroke() {
		return new BasicStroke((float) cartoucheLineWidth);
	}

	/// Is soft wrapping of lines active?
	///
	/// A soft wrap is a line end made by the layout, as opposed to an explicit
	/// line break of the text. See "Soft wrap" and "Wrap width" in
	/// `00_Documents/documentation/technical_glossary.md`.
	///
	/// @return true if a page is defined, or if [#wrapWidth()] is positive.
	public boolean isWrapping() {
		return hasPage() || wrapWidth > 0;
	}

	/// Is a page defined?
	///
	/// @return true if [#page()] is defined.
	public boolean hasPage() {
		return page != null && page.isDefined();
	}

	/// The width available for the content of a wrapped line.
	///
	/// With a page, it's the page's usable width. Otherwise, it's the wrapping
	/// width minus the margin on both sides.
	///
	/// @return the maximal width of a line's content. Meaningful only if
	/// [#isWrapping()].
	public double maxLineWidth() {
		return hasPage() ? page.usableWidth() : wrapWidth - 2 * leftMargin;
	}

	/// The height in which the content of columns is aligned (see "Line
	/// alignment" in `00_Documents/documentation/technical_glossary.md`).
	///
	/// With a page which has a height, it's the page's usable height.
	/// Otherwise, it's [#columnHeight()] minus the top margin on both sides.
	///
	/// @return the height, or 0 if none is imposed (the columns are then
	/// aligned on the longest one).
	public double maxColumnHeight() {
		if (hasPage() && page.usableHeight() > 0) {
			return page.usableHeight();
		}
		return columnHeight > 0 ? Math.max(0, columnHeight - 2 * topMargin) : 0;
	}

	/// The page rectangle, if a page is defined.
	///
	/// @return the whole page.
	/// @throws NullPointerException if there is no page.
	public Rectangle2D getPageRectangle() {
		if (hasPage()) {
			return page.pageRectangle();
		}
		throw new NullPointerException("No page available");
	}

	public Builder copy() {
		return new Builder(this);
	}

	/** copy builder class */
	public static class Builder {
		private double fineLineWidth; // 0.5,
		private double wideLineWidth; // 3.0,
		private double lineSkip; // 6.0,
		private double columnSkip; // 10.0
		private double tabUnitWidth; // 18.0/ 200.0,
		private double cartoucheLineWidth; // 1,
		private double cartoucheLoopLength; // 10,
		private double cartoucheMargin; // 2
		private double bastionDepth; // 3
		private double bastionLength; // 4
		private double hwtSmallMargin; // 3
		private double hwtSquareSize; // 10
		private double serekhDoorSize; // 20
		private double leftMargin; // 6
		private double topMargin; // 5
		private PageSpecification page; // NONE
		private PageDecoration pageDecoration; // NONE
		private double maxCadratHeight; // 18.0,
		private double maxCadratWidth; // 22.0,
		private double smallSkip; // 2.0,
		private double largeSignSizeRatio; // 0.8,
		private double smallSignSizeRatio; // 0.4,
		private double smallBodyScaleLimit; // 12.0,
		private double standardSignHeight; // 18.0;
		private double alphabeticTextBaseline; // 4.0;
		private double underlineGap; // 2.0;
		private double wrapWidth; // 0.0;
		private double columnHeight; // 0.0;
		private double columnQuadratSkip; // 6.0;
		private double unit; // 18.0;

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
			this.leftMargin = specs.leftMargin;
			this.topMargin = specs.topMargin;
			this.page = specs.page;
			this.pageDecoration = specs.pageDecoration;
			this.maxCadratHeight = specs.maxCadratHeight;
			this.maxCadratWidth = specs.maxCadratWidth;
			this.smallSkip = specs.smallSkip;
			this.largeSignSizeRatio = specs.largeSignSizeRatio;
			this.smallSignSizeRatio = specs.smallSignSizeRatio;
			this.smallBodyScaleLimit = specs.smallBodyScaleLimit;
			this.standardSignHeight = specs.standardSignHeight;
			this.alphabeticTextBaseline = specs.alphabeticTextBaseline;
			this.underlineGap = specs.underlineGap;
			this.wrapWidth = specs.wrapWidth;
			this.columnHeight = specs.columnHeight;
			this.columnQuadratSkip = specs.columnQuadratSkip;
			this.unit = specs.unit;
		}

		public Builder fineLineWidth(double fineLineWidth) {
			this.fineLineWidth = fineLineWidth;
			return this;
		}

		public Builder wideLineWidth(double wideLineWidth) {
			this.wideLineWidth = wideLineWidth;
			return this;
		}

		public Builder lineSkip(double lineSkip) {
			this.lineSkip = lineSkip;
			return this;
		}

		public Builder columnSkip(double columnSkip) {
			this.columnSkip = columnSkip;
			return this;
		}

		public Builder tabUnitWidth(double tabUnitWidth) {
			this.tabUnitWidth = tabUnitWidth;
			return this;
		}

		public Builder cartoucheLineWidth(double cartoucheLineWidth) {
			this.cartoucheLineWidth = cartoucheLineWidth;
			return this;
		}

		public Builder cartoucheLoopLength(double cartoucheLoopLength) {
			this.cartoucheLoopLength = cartoucheLoopLength;
			return this;
		}

		public Builder cartoucheMargin(double cartoucheMargin) {
			this.cartoucheMargin = cartoucheMargin;
			return this;
		}

		public Builder bastionDepth(double bastionDepth) {
			this.bastionDepth = bastionDepth;
			return this;
		}

		public Builder bastionLength(double bastionLength) {
			this.bastionLength = bastionLength;
			return this;
		}

		public Builder hwtSmallMargin(double hwtSmallMargin) {
			this.hwtSmallMargin = hwtSmallMargin;
			return this;
		}

		public Builder hwtSquareSize(double hwtSquareSize) {
			this.hwtSquareSize = hwtSquareSize;
			return this;
		}

		public Builder serekhDoorSize(double serekhDoorSize) {
			this.serekhDoorSize = serekhDoorSize;
			return this;
		}

		public Builder leftMargin(double leftMargin) {
			this.leftMargin = leftMargin;
			return this;
		}

		public Builder topMargin(double topMargin) {
			this.topMargin = topMargin;
			return this;
		}

		public Builder pageDecoration(PageDecoration pageDecoration) {
			this.pageDecoration = pageDecoration == null ? PageDecoration.NONE : pageDecoration;
			return this;
		}

		public Builder page(PageSpecification page) {
			this.page = page == null ? PageSpecification.NONE : page;
			return this;
		}

		/// Keeps the page's width and margins, but doesn't cut the text into pages
		/// (see [PageSpecification#withoutHeight()]).
		///
		/// @return this builder, for chaining.
		public Builder withoutPagination() {
			this.page = page.withoutHeight();
			return this;
		}

		public Builder maxCadratHeight(double maxCadratHeight) {
			this.maxCadratHeight = maxCadratHeight;
			return this;
		}

		public Builder maxCadratWidth(double maxCadratWidth) {
			this.maxCadratWidth = maxCadratWidth;
			return this;
		}

		public Builder smallSkip(double smallSkip) {
			this.smallSkip = smallSkip;
			return this;
		}

		public Builder largeSignSizeRatio(double largeSignSizeRatio) {
			this.largeSignSizeRatio = largeSignSizeRatio;
			return this;
		}

		public Builder smallSignSizeRatio(double smallSignSizeRatio) {
			this.smallSignSizeRatio = smallSignSizeRatio;
			return this;
		}

		public Builder smallBodyScaleLimit(double smallBodyScaleLimit) {
			this.smallBodyScaleLimit = smallBodyScaleLimit;
			return this;
		}

		public Builder standardSignHeight(double standardSignHeight) {
			this.standardSignHeight = standardSignHeight;
			return this;
		}

		public Builder alphabeticTextBaseline(double alphabeticTextBaseline) {
			this.alphabeticTextBaseline = alphabeticTextBaseline;
			return this;
		}

		public Builder underlineGap(double underlineGap) {
			this.underlineGap = underlineGap;
			return this;
		}

		public Builder wrapWidth(double wrapWidth) {
			this.wrapWidth = wrapWidth;
			return this;
		}

		public Builder columnHeight(double columnHeight) {
			this.columnHeight = columnHeight;
			return this;
		}

		/// @param columnQuadratSkip the space between two quadrats of a column.
		/// @return this builder, for chaining.
		public Builder columnQuadratSkip(double columnQuadratSkip) {
			this.columnQuadratSkip = columnQuadratSkip;
			return this;
		}

		/// @param unit the reference length of the document, in points.
		/// @return this builder, for chaining.
		public Builder unit(double unit) {
			this.unit = unit;
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
					leftMargin,
					topMargin,
					page,
					pageDecoration,
					maxCadratHeight,
					maxCadratWidth,
					smallSkip,
					largeSignSizeRatio,
					smallSignSizeRatio,
					smallBodyScaleLimit,
					standardSignHeight,
					alphabeticTextBaseline,
					underlineGap,
					wrapWidth,
					columnHeight,
					columnQuadratSkip,
					unit);
		}

		/**
		 * Sets a new maxcadrat height, but scales all other parameters accordingly (page included).
		 * @param newHeight the new maxcadrat height.
		 * @return this builder, for chaining.
		 */
        public Builder scaleToHeight(double newHeight) {
			double scale = newHeight / standardSignHeight;
			fineLineWidth *= scale;
			wideLineWidth *= scale;
			lineSkip *= scale;
			columnSkip *= scale;
			tabUnitWidth *= scale;
			cartoucheLineWidth *= scale;
			cartoucheLoopLength *= scale;
			cartoucheMargin *= scale;
			bastionDepth *= scale;
			bastionLength *= scale;
			hwtSmallMargin *= scale;
			hwtSquareSize *= scale;
			serekhDoorSize *= scale;
			leftMargin *= scale;
			topMargin *= scale;
			page = page.scaled(scale);
			maxCadratHeight *= scale;
			maxCadratWidth *= scale;
			smallSkip *= scale;
			largeSignSizeRatio *= scale;
			smallSignSizeRatio *= scale;
			smallBodyScaleLimit *= scale;
			standardSignHeight *= scale;
			alphabeticTextBaseline *= scale;
			underlineGap *= scale;
			wrapWidth *= scale;
			columnHeight *= scale;
			columnQuadratSkip *= scale;
			unit *= scale;
			return this;
        }
	}
}
