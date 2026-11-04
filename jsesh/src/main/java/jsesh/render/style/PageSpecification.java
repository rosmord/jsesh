package jsesh.render.style;

import java.awt.geom.Rectangle2D;

/// Size and margins of a page, in layout units (the unit of
/// [GeometrySpecification]: by default, the height of an A1 sign is 18).
///
/// A text has no page by default ([#NONE]): it is as large as its drawing
/// requires. When a page is defined, horizontal lines are soft-wrapped to the
/// [#usableWidth()], and a top-level page break (`!!`) starts a new page.
///
/// The height is not (yet) a layout constraint: text taller than the usable
/// height simply goes on, and automatic pagination is not done.
///
/// @param pageWidth the total width of the page. 0 (or less) means "no page".
/// @param pageHeight the total height of the page. 0 means that the height is not
/// specified.
/// @param marginLeft the left margin.
/// @param marginRight the right margin.
/// @param marginTop the top margin.
/// @param marginBottom the bottom margin.
public record PageSpecification(
		float pageWidth,
		float pageHeight,
		float marginLeft,
		float marginRight,
		float marginTop,
		float marginBottom) {

	/// The space between two pages when they are displayed one below the other,
	/// in layout units.
	public static final float PAGE_GAP = 24f;

	/// The absence of a page: the text is as large as it needs to be.
	public static final PageSpecification NONE = new PageSpecification(0, 0, 0, 0, 0, 0);

	/// Is a page defined? A page needs a positive width (and a usable width).
	///
	/// @return true if lines should be wrapped to this page's usable width.
	public boolean isDefined() {
		return pageWidth > 0 && usableWidth() > 0;
	}

	/// Is the text cut into pages? It is when the page has a height: a line which
	/// does not fit in the page goes to the top of the next one.
	///
	/// In the view, pages are stacked: the page number `k` (from 0) starts at
	/// `k * pageStride()`.
	///
	/// @return true if the page is defined, with a height.
	public boolean isPaginated() {
		return isDefined() && usableHeight() > 0;
	}

	/// The distance between the tops of two consecutive pages in the view.
	///
	/// @return the page height, plus [#PAGE_GAP].
	public float pageStride() {
		return pageHeight + PAGE_GAP;
	}

	/// The number of pages in a view of a given height, when pages are stacked.
	///
	/// @param viewHeight the height of the view.
	/// @return the number of pages, at least 1.
	public int pageCount(double viewHeight) {
		return Math.max(1, (int) Math.ceil((viewHeight + PAGE_GAP - 1e-3) / pageStride()));
	}

	/// The width available for text, margins excluded.
	///
	/// @return the page width minus the left and right margins.
	public float usableWidth() {
		return pageWidth - marginLeft - marginRight;
	}

	/// The height available for text, margins excluded.
	///
	/// @return the page height minus the top and bottom margins, or 0 if the page
	/// height is not specified.
	public float usableHeight() {
		return pageHeight > 0 ? pageHeight - marginTop - marginBottom : 0;
	}

	/// The whole page.
	///
	/// @return the rectangle of the page, with origin (0, 0).
	public Rectangle2D pageRectangle() {
		return new Rectangle2D.Float(0, 0, pageWidth, pageHeight);
	}

	/// The area where text is written.
	///
	/// @return the page rectangle minus the margins. If the page height is not
	/// specified, the height of the result is 0.
	public Rectangle2D usableRectangle() {
		return new Rectangle2D.Float(marginLeft, marginTop, usableWidth(), usableHeight());
	}

	/// The same page, without height: lines still wrap at the usable width, but the
	/// text is not cut into pages.
	///
	/// Useful to draw a part of a text (a selection copied as a picture), which
	/// should not be as tall as a page.
	///
	/// @return the page without its height.
	public PageSpecification withoutHeight() {
		return new PageSpecification(pageWidth, 0, marginLeft, marginRight, marginTop, marginBottom);
	}

	/// The same page, scaled.
	///
	/// @param scale the factor applied to all dimensions.
	/// @return the scaled page.
	public PageSpecification scaled(double scale) {
		return new PageSpecification(
				(float) (pageWidth * scale),
				(float) (pageHeight * scale),
				(float) (marginLeft * scale),
				(float) (marginRight * scale),
				(float) (marginTop * scale),
				(float) (marginBottom * scale));
	}
}
