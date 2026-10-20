package org.qenherkhopeshef.viewToolKit.drawing.textOriented;

/// A position in a [TextDocument]: a paragraph index, and an offset in this
/// paragraph.
///
/// The offset is a *gap* position: offset 0 is before the first character,
/// offset `length()` is after the last one.
///
/// @param paragraph the paragraph index.
/// @param offset the position in the paragraph.
public record TextPosition(int paragraph, int offset) implements Comparable<TextPosition> {

	/// The start of any document.
	public static final TextPosition START = new TextPosition(0, 0);

	public TextPosition {
		if (paragraph < 0 || offset < 0)
			throw new IllegalArgumentException("negative position " + paragraph + ", " + offset);
	}

	@Override
	public int compareTo(TextPosition o) {
		if (paragraph != o.paragraph)
			return Integer.compare(paragraph, o.paragraph);
		return Integer.compare(offset, o.offset);
	}

	public boolean isBefore(TextPosition o) {
		return compareTo(o) < 0;
	}

	public static TextPosition min(TextPosition a, TextPosition b) {
		return a.compareTo(b) <= 0 ? a : b;
	}

	public static TextPosition max(TextPosition a, TextPosition b) {
		return a.compareTo(b) >= 0 ? a : b;
	}
}
