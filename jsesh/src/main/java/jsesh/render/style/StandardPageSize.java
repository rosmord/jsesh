package jsesh.render.style;

import java.util.Optional;

/// Usual paper sizes, in layout units (points: 1 inch = 72 units).
///
/// The sizes are given in portrait orientation.
public enum StandardPageSize {
	A3("A3", 297, 420),
	A4("A4", 210, 297),
	A5("A5", 148, 210),
	B5("B5", 176, 250),
	LETTER("Letter", 215.9, 279.4),
	LEGAL("Legal", 215.9, 355.6);

	/// How close two sizes must be (in layout units) to be considered the same.
	private static final double TOLERANCE = 0.6;

	private final String label;
	private final double width;
	private final double height;

	StandardPageSize(String label, double widthInMm, double heightInMm) {
		this.label = label;
		this.width = toUnits(widthInMm);
		this.height = toUnits(heightInMm);
	}

	// Rounded to two decimals, so that saved values stay readable (595.28).
	private static double toUnits(double mm) {
		return Math.round(mm * 72 / 25.4 * 100) / 100.0;
	}

	/// @return the width in portrait orientation, in layout units.
	public double width() {
		return width;
	}

	/// @return the height in portrait orientation, in layout units.
	public double height() {
		return height;
	}

	/// Does a page have this size, in either orientation?
	///
	/// @param pageWidth the width of the page.
	/// @param pageHeight the height of the page.
	/// @return true if the dimensions are (nearly) those of this size.
	public boolean matches(double pageWidth, double pageHeight) {
		return close(pageWidth, width) && close(pageHeight, height)
				|| close(pageWidth, height) && close(pageHeight, width);
	}

	private static boolean close(double a, double b) {
		return Math.abs(a - b) <= TOLERANCE;
	}

	/// Finds the standard size of a page.
	///
	/// @param pageWidth the width of the page.
	/// @param pageHeight the height of the page.
	/// @return the matching size, in either orientation, if any.
	public static Optional<StandardPageSize> find(double pageWidth, double pageHeight) {
		for (StandardPageSize size : values()) {
			if (size.matches(pageWidth, pageHeight)) {
				return Optional.of(size);
			}
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return label;
	}
}
