package org.qenherkhopeshef.viewToolKit.drawing.textOriented;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import org.qenherkhopeshef.viewToolKit.drawing.element.CompositeElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.Margin;

/// The view of one paragraph: a vertical composite of [TextLineElement]s.
///
/// A paragraph element is an immutable snapshot of its paragraph: when the
/// paragraph changes, the [TextDrawing] builds a new one.
///
/// Coordinates used by the query methods are relative to the paragraph origin.
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public class ParagraphElement extends CompositeElement {

	private final Paragraph paragraph;

	private final List<TextLineElement> lines = new ArrayList<>();

	/// Lays out a paragraph.
	///
	/// @param paragraph the paragraph.
	/// @param font the text font.
	/// @param color the text color.
	/// @param width the line width.
	/// @param frc the context used for measuring text.
	ParagraphElement(Paragraph paragraph, Font font, Color color, double width, FontRenderContext frc) {
		this.paragraph = paragraph;
		setMargin(Margin.NONE);
		if (paragraph.isEmpty()) {
			TextLayout reference = new TextLayout(" ", font, frc);
			lines.add(new TextLineElement(0, reference.getAscent(),
					reference.getAscent() + reference.getDescent() + reference.getLeading()));
		} else {
			String text = paragraph.getText();
			AttributedString attributed = new AttributedString(text);
			attributed.addAttribute(TextAttribute.FONT, font);
			List<GraphicalElement> elements = paragraph.getElements();
			int k = 0;
			for (int i = 0; i < text.length() && k < elements.size(); i++) {
				if (text.charAt(i) == Paragraph.OBJECT_REPLACEMENT_CHARACTER) {
					attributed.addAttribute(TextAttribute.CHAR_REPLACEMENT,
							new EmbeddedElementGraphic(elements.get(k++)), i, i + 1);
				}
			}
			LineBreakMeasurer measurer = new LineBreakMeasurer(attributed.getIterator(), frc);
			float wrappingWidth = (float) Math.max(1, width);
			while (measurer.getPosition() < text.length()) {
				int start = measurer.getPosition();
				TextLayout layout = measurer.nextLayout(wrappingWidth);
				lines.add(new TextLineElement(layout, start, measurer.getPosition() - start, color));
			}
		}
		for (TextLineElement line : lines)
			addElement(line);
		pack();
	}

	/// Computes the height a paragraph will have, without building its view.
	///
	/// For a paragraph of plain text, all lines have the same height, so it
	/// suffices to count the lines, which is much cheaper than building them.
	/// Paragraphs with embedded elements are laid out completely.
	///
	/// @param lineHeight the height of a line of plain text in this font.
	static double measureHeight(Paragraph paragraph, Font font, Color color, double width,
			FontRenderContext frc, double lineHeight) {
		if (!paragraph.getElements().isEmpty())
			return new ParagraphElement(paragraph, font, color, width, frc).getBounds().getHeight();
		if (paragraph.isEmpty())
			return lineHeight;
		AttributedString attributed = new AttributedString(paragraph.getText());
		attributed.addAttribute(TextAttribute.FONT, font);
		LineBreakMeasurer measurer = new LineBreakMeasurer(attributed.getIterator(), frc);
		float wrappingWidth = (float) Math.max(1, width);
		int lineCount = 0;
		while (measurer.getPosition() < paragraph.length()) {
			measurer.setPosition(measurer.nextOffset(wrappingWidth));
			lineCount++;
		}
		return lineCount * lineHeight;
	}

	public Paragraph getParagraph() {
		return paragraph;
	}

	public int getLineCount() {
		return lines.size();
	}

	public TextLineElement getLine(int index) {
		return lines.get(index);
	}

	/// The index of the line which displays a given offset.
	///
	/// An offset which is both the end of a line and the start of the next
	/// one belongs to the next line.
	public int getLineIndexFor(int offset) {
		int a = 0, b = lines.size() - 1;
		while (a < b) {
			int m = (a + b + 1) / 2;
			if (lines.get(m).getStart() <= offset)
				a = m;
			else
				b = m - 1;
		}
		return a;
	}

	/// The index of the line at a vertical position (clamped to the existing lines).
	public int getLineIndexAt(double y) {
		for (int i = 0; i < lines.size(); i++) {
			if (y < lines.get(i).getBounds().getMaxY())
				return i;
		}
		return lines.size() - 1;
	}

	/// The offset closest to a point.
	public int getOffsetAt(double x, double y) {
		return getOffsetInLine(getLineIndexAt(y), x);
	}

	/// The offset closest to a horizontal position, in a given line.
	public int getOffsetInLine(int lineIndex, double x) {
		TextLineElement line = lines.get(lineIndex);
		int offset = line.offsetAt(x - line.getBounds().getMinX());
		// Clicking after the end of a line which was broken on a space:
		// stay on this line, before the space, rather than going to the start
		// of the next line.
		if (offset == line.getEnd() && lineIndex < lines.size() - 1 && line.endsWithSpace(paragraph))
			offset--;
		return offset;
	}

	/// The caret rectangle for an offset (with a zero width).
	public Rectangle2D getCaretRectangle(int offset) {
		TextLineElement line = lines.get(getLineIndexFor(offset));
		Rectangle2D b = line.getBounds();
		return new Rectangle2D.Double(b.getMinX() + line.caretX(offset), b.getMinY(), 0, b.getHeight());
	}

	/// The highlight shape for the offsets [from, to).
	///
	/// @param extendToEnd if true, the highlight is extended a bit after the
	///  end of the paragraph (to show that the paragraph break is selected).
	public Shape getHighlightShape(int from, int to, boolean extendToEnd) {
		Path2D result = new Path2D.Double();
		for (TextLineElement line : lines) {
			if (line.getEnd() < from || line.getStart() > to)
				continue;
			Rectangle2D b = line.getBounds();
			AffineTransform t = AffineTransform.getTranslateInstance(b.getMinX(), b.getMinY());
			result.append(t.createTransformedShape(line.highlight(from, to)), false);
		}
		if (extendToEnd) {
			TextLineElement last = lines.get(lines.size() - 1);
			Rectangle2D b = last.getBounds();
			double x = b.getMinX() + last.caretX(paragraph.length());
			result.append(new Rectangle2D.Double(x, b.getMinY(), 6, b.getHeight()), false);
		}
		return result;
	}
}
