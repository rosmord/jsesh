package org.qenherkhopeshef.viewToolKit.drawing.textOriented;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import org.qenherkhopeshef.swingUtils.DoubleDimensions;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.Margin;

/// One laid-out line of a paragraph.
///
/// Offsets used by the methods of this class are *paragraph* offsets.
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public class TextLineElement extends GraphicalElement {

	/// The layout, or null for an empty line.
	private final TextLayout layout;

	private final int start;

	private final int length;

	private final double ascent;

	private final double height;

	private final Color color;

	/// A line of text.
	TextLineElement(TextLayout layout, int start, int length, Color color) {
		this.layout = layout;
		this.start = start;
		this.length = length;
		this.ascent = layout.getAscent();
		this.height = layout.getAscent() + layout.getDescent() + layout.getLeading();
		this.color = color;
		setMargin(Margin.NONE);
	}

	/// An empty line (for an empty paragraph).
	TextLineElement(int start, double ascent, double height) {
		this.layout = null;
		this.start = start;
		this.length = 0;
		this.ascent = ascent;
		this.height = height;
		this.color = Color.BLACK;
		setMargin(Margin.NONE);
	}

	@Override
	protected void drawElement(Graphics2D g) {
		if (layout != null) {
			g.setColor(color);
			layout.draw(g, 0, (float) ascent);
		}
	}

	@Override
	public Dimension2D getPreferredSize() {
		return new DoubleDimensions(layout == null ? 0 : layout.getAdvance(), height);
	}

	/// The offset of the first character of the line, in the paragraph.
	public int getStart() {
		return start;
	}

	/// The offset after the last character of the line, in the paragraph.
	public int getEnd() {
		return start + length;
	}

	public int getLength() {
		return length;
	}

	/// Is the last character of the line a space (on which the line was broken) ?
	boolean endsWithSpace(Paragraph paragraph) {
		return length > 0 && Character.isWhitespace(paragraph.charAt(getEnd() - 1));
	}

	/// The paragraph offset closest to a horizontal position in the line.
	int offsetAt(double x) {
		if (layout == null)
			return start;
		TextHitInfo hit = layout.hitTestChar((float) x, (float) ascent);
		return start + hit.getInsertionIndex();
	}

	/// The horizontal position of the caret for a paragraph offset.
	double caretX(int offset) {
		if (layout == null)
			return 0;
		int local = Math.max(0, Math.min(length, offset - start));
		TextHitInfo hit = local < length ? TextHitInfo.leading(local) : TextHitInfo.trailing(length - 1);
		return layout.getCaretInfo(hit)[0];
	}

	/// The highlight shape for the paragraph offsets [from, to), in the line
	/// coordinates.
	Shape highlight(int from, int to) {
		int a = Math.max(0, from - start);
		int b = Math.min(length, to - start);
		if (layout == null || a >= b)
			return new Rectangle2D.Double();
		Shape s = layout.getLogicalHighlightShape(a, b);
		return AffineTransform.getTranslateInstance(0, ascent).createTransformedShape(s);
	}

	@Override
	public String toString() {
		return "[line " + start + "+" + length + "]";
	}
}
