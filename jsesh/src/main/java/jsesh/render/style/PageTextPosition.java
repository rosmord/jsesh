package jsesh.render.style;

/// Where a text (page number, title) is written in the margins of a page.
public enum PageTextPosition {
	/// The text is not written.
	NONE,
	TOP_LEFT,
	TOP_CENTER,
	TOP_RIGHT,
	BOTTOM_LEFT,
	BOTTOM_CENTER,
	BOTTOM_RIGHT;

	/// The position on the other side of the page.
	///
	/// @return the position with left and right exchanged. The centre positions and
	/// [#NONE] stay as they are.
	public PageTextPosition mirrored() {
		return switch (this) {
			case TOP_LEFT -> TOP_RIGHT;
			case TOP_RIGHT -> TOP_LEFT;
			case BOTTOM_LEFT -> BOTTOM_RIGHT;
			case BOTTOM_RIGHT -> BOTTOM_LEFT;
			default -> this;
		};
	}

	/// @return true for the positions in the top margin.
	public boolean isTop() {
		return this == TOP_LEFT || this == TOP_CENTER || this == TOP_RIGHT;
	}

	/// @return true for the positions in the bottom margin.
	public boolean isBottom() {
		return this == BOTTOM_LEFT || this == BOTTOM_CENTER || this == BOTTOM_RIGHT;
	}

	/// @return true for the positions at the left of the margin.
	public boolean isLeft() {
		return this == TOP_LEFT || this == BOTTOM_LEFT;
	}

	/// @return true for the positions at the right of the margin.
	public boolean isRight() {
		return this == TOP_RIGHT || this == BOTTOM_RIGHT;
	}
}
