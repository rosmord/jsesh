package org.qenherkhopeshef.viewtoolkit1.drawing.textOriented;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.qenherkhopeshef.viewtoolkit1.drawing.AbstractDrawing;
import org.qenherkhopeshef.viewtoolkit1.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewtoolkit1.drawing.element.Margin;
import org.qenherkhopeshef.viewtoolkit1.drawing.element.ShapeElement;
import org.qenherkhopeshef.viewtoolkit1.drawing.event.CursorChangedEvent;
import org.qenherkhopeshef.viewtoolkit1.drawing.event.DrawingChangedEvent;
import org.qenherkhopeshef.viewtoolkit1.drawing.event.DrawingEvent;

/// A drawing displaying a [TextDocument] as a column of wrapped paragraphs,
/// with a caret and a selection.
///
/// ## Very long texts
///
/// The drawing is designed for texts of any length (hundreds of thousands of
/// paragraphs):
///
/// - paragraphs are laid out *lazily*, when they are displayed or queried.
///   Until then, their height is estimated from their length;
/// - the paragraph heights are kept in a Fenwick tree, so that finding the
///   paragraph at a given vertical position, or the position of a given
///   paragraph, costs O(log n);
/// - only a bounded number of laid-out paragraphs are kept in memory (a LRU
///   cache);
/// - [#layoutPendingParagraphs(long)] can be called repeatedly (typically
///   from a Swing timer) to replace the estimates by exact heights in the
///   background, which makes the scroll bar more and more accurate.
///
/// Laying out a paragraph never moves the paragraphs *before* it: when the
/// estimate of a displayed paragraph is corrected, only the text below moves.
///
/// ## Coordinates and interaction
///
/// The drawing provides the geometric queries needed by an editor: position
/// under a point ([#getPositionAt(Point2D)]), caret rectangle, positions
/// above/below, line start and end... The editing itself is done on the
/// document; the drawing follows the document's changes.
///
/// The paragraph elements returned by [#getElementsInZone(Rectangle2D)] are
/// views built by the drawing; they are not managed (and should not be
/// modified).
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public class TextDrawing extends AbstractDrawing {

	/// Number of laid-out paragraphs kept in memory.
	private static final int CACHE_SIZE = 2000;

	/// When more paragraphs than this are inserted or changed at once, they
	/// are laid out lazily instead of immediately.
	private static final int IMMEDIATE_LAYOUT_LIMIT = 64;

	private final TextDocument document;

	private final HeightIndex heights = new HeightIndex();

	private final FontRenderContext frc = new FontRenderContext(null, true, true);

	@SuppressWarnings("serial")
	private final LinkedHashMap<Paragraph, ParagraphElement> cache = new LinkedHashMap<>(256, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Paragraph, ParagraphElement> eldest) {
			return size() > CACHE_SIZE;
		}
	};

	private Font font = new Font("Serif", Font.PLAIN, 16);

	private Color textColor = Color.BLACK;

	private Color selectionColor = new Color(70, 130, 255, 80);

	private Color caretColor = Color.BLACK;

	private double pageWidth = 600;

	private Margin pageMargin = new Margin(30, 30, 40, 40);

	private double paragraphSpacing = 6;

	/// Font metrics used for the estimates.
	private double lineHeight;
	private double averageCharWidth;

	private TextPosition caret = TextPosition.START;

	/// The other end of the selection (null if no selection).
	private TextPosition anchor = null;

	private boolean caretVisible = true;

	/// Next paragraph to examine for background layout.
	private int backgroundScan = 0;

	/// Set when a paragraph height changed since the last notification.
	private boolean geometryChanged = false;

	public TextDrawing(TextDocument document) {
		this.document = document;
		document.addTextDocumentListener(this::documentChanged);
		invalidateLayout();
	}

	public TextDocument getDocument() {
		return document;
	}

	// ------------------------------------------------------------------
	// Drawing interface.

	@Override
	public Collection<GraphicalElement> getElementsInZone(Rectangle2D rectangle) {
		List<GraphicalElement> result = new ArrayList<>();
		int n = document.getParagraphCount();
		for (int i = getParagraphIndexAt(rectangle.getMinY()); i < n; i++) {
			if (getParagraphTop(i) > rectangle.getMaxY())
				break;
			ParagraphElement e = getParagraphElement(i);
			if (e.getDecoratedBounds().intersects(rectangle))
				result.add(e);
		}
		notifyGeometryChanges();
		return result;
	}

	/// The caret only (see [#getDecorationsInZone(Rectangle2D)] for the
	/// selection).
	@Override
	public Collection<GraphicalElement> getDecorations() {
		List<GraphicalElement> result = new ArrayList<>();
		addCaret(result);
		return result;
	}

	/// The selection highlight for the visible paragraphs, and the caret.
	@Override
	public Collection<GraphicalElement> getDecorationsInZone(Rectangle2D zone) {
		List<GraphicalElement> result = new ArrayList<>();
		if (hasSelection()) {
			TextPosition start = getSelectionStart();
			TextPosition end = getSelectionEnd();
			int first = Math.max(start.paragraph(), getParagraphIndexAt(zone.getMinY()));
			int last = Math.min(end.paragraph(), getParagraphIndexAt(zone.getMaxY()));
			for (int i = first; i <= last; i++) {
				ParagraphElement pe = getParagraphElement(i);
				int from = i == start.paragraph() ? start.offset() : 0;
				int to = i == end.paragraph() ? end.offset() : pe.getParagraph().length();
				Shape shape = pe.getHighlightShape(from, to, i < end.paragraph());
				ShapeElement highlight = new ShapeElement(shape);
				highlight.setColor(selectionColor);
				highlight.setMargin(Margin.NONE);
				Rectangle2D b = pe.getBounds();
				highlight.setOrigin(b.getMinX(), b.getMinY());
				result.add(highlight);
			}
		}
		addCaret(result);
		notifyGeometryChanges();
		return result;
	}

	private void addCaret(List<GraphicalElement> result) {
		if (caretVisible) {
			Rectangle2D r = getCaretRectangle(caret);
			ShapeElement caretElement = new ShapeElement(new Rectangle2D.Double(0, 0, 1.5, r.getHeight()));
			caretElement.setColor(caretColor);
			caretElement.setMargin(Margin.NONE);
			caretElement.setOrigin(r.getX() - 0.75, r.getY());
			result.add(caretElement);
		}
	}

	@Override
	public Rectangle2D getPreferredSize() {
		return new Rectangle2D.Double(0, 0, pageMargin.getLeft() + pageWidth + pageMargin.getRight(),
				pageMargin.getTop() + heights.total() + pageMargin.getBottom());
	}

	/// The caret area, with some room around it.
	@Override
	public Rectangle2D getCursorBounds() {
		Rectangle2D r = getCaretRectangle(caret);
		notifyGeometryChanges();
		return new Rectangle2D.Double(r.getX() - 10, r.getY() - lineHeight / 2, 20, r.getHeight() + lineHeight);
	}

	/// A text drawing is never empty: there is at least one paragraph.
	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	protected void eventOccurredInDrawing(DrawingEvent ev) {
		// Our elements are not managed; nothing should come here.
		fireDrawingEvent(ev);
	}

	// ------------------------------------------------------------------
	// Layout.

	/// The paragraph count (same as the document's).
	public int getParagraphCount() {
		return document.getParagraphCount();
	}

	/// The vertical position of the top of a paragraph.
	///
	/// It is exact if all paragraphs before i have been laid out, and an
	/// estimate otherwise.
	public double getParagraphTop(int i) {
		return pageMargin.getTop() + heights.prefix(i);
	}

	/// The paragraph displayed at a vertical position (clamped to the
	/// existing paragraphs).
	public int getParagraphIndexAt(double y) {
		return Math.max(0, heights.indexAt(y - pageMargin.getTop()));
	}

	/// The view of a paragraph, laying it out if needed. The view is placed at
	/// the paragraph's current position.
	public ParagraphElement getParagraphElement(int i) {
		Paragraph p = document.getParagraph(i);
		ParagraphElement e = cache.get(p);
		if (e == null) {
			e = new ParagraphElement(p, font, textColor, pageWidth, frc);
			cache.put(p, e);
		}
		double h = e.getBounds().getHeight() + paragraphSpacing;
		if (!heights.isExact(i) || heights.get(i) != h) {
			heights.set(i, h, true);
			geometryChanged = true;
		}
		e.setOrigin(pageMargin.getLeft(), getParagraphTop(i));
		return e;
	}

	/// Lays out paragraphs whose heights are still estimated, for at most
	/// maxNanos nanoseconds.
	///
	/// Meant to be called repeatedly, when the application is idle. Fires a
	/// [DrawingChangedEvent] if some heights changed.
	///
	/// @return true if some paragraphs remain to be laid out.
	public boolean layoutPendingParagraphs(long maxNanos) {
		int n = heights.size();
		long end = System.nanoTime() + maxNanos;
		int scanned = 0;
		while (heights.getInexactCount() > 0 && scanned < n && System.nanoTime() < end) {
			if (backgroundScan >= n)
				backgroundScan = 0;
			if (!heights.isExact(backgroundScan)) {
				Paragraph p = document.getParagraph(backgroundScan);
				// Don't pollute the cache with paragraphs which are not
				// displayed: only measure them.
				ParagraphElement e = cache.get(p);
				double h = e != null ? e.getBounds().getHeight()
						: ParagraphElement.measureHeight(p, font, textColor, pageWidth, frc, lineHeight);
				heights.set(backgroundScan, h + paragraphSpacing, true);
				geometryChanged = true;
			}
			backgroundScan++;
			scanned++;
		}
		notifyGeometryChanges();
		return heights.getInexactCount() > 0;
	}

	/// The proportion of paragraphs whose height is exact (between 0 and 1).
	public double getLayoutCompletion() {
		return heights.size() == 0 ? 1 : 1 - (double) heights.getInexactCount() / heights.size();
	}

	private void notifyGeometryChanges() {
		if (geometryChanged) {
			geometryChanged = false;
			fireDrawingEvent(new DrawingChangedEvent());
		}
	}

	/// Forgets all layouts, and estimates all heights anew.
	private void invalidateLayout() {
		cache.clear();
		TextLayout reference = new TextLayout("The quick brown fox jumps over the lazy dog, 0123456789.", font, frc);
		lineHeight = reference.getAscent() + reference.getDescent() + reference.getLeading();
		// Lines are not completely filled: add a little.
		averageCharWidth = 1.05 * reference.getAdvance() / 56;
		int n = document.getParagraphCount();
		heights.reset(n, 0);
		for (int i = 0; i < n; i++)
			heights.set(i, estimateHeight(document.getParagraph(i)), false);
		backgroundScan = 0;
		geometryChanged = false;
	}

	private double estimateHeight(Paragraph p) {
		double lines = Math.max(1, Math.ceil(p.length() * averageCharWidth / Math.max(1, pageWidth)));
		double h = lines * lineHeight;
		for (GraphicalElement e : p.getElements())
			h += Math.max(0, e.getBounds().getHeight() - lineHeight / 2);
		return h + paragraphSpacing;
	}

	private void documentChanged(TextDocumentEvent ev) {
		int first = ev.firstParagraph();
		int count = ev.count();
		switch (ev.type()) {
		case CHANGED:
			for (int i = first; i < first + count; i++) {
				cache.remove(document.getParagraph(i));
				heights.set(i, estimateHeight(document.getParagraph(i)), false);
				if (count <= IMMEDIATE_LAYOUT_LIMIT)
					getParagraphElement(i);
			}
			break;
		case INSERTED:
			heights.insert(first, count, 0);
			for (int i = first; i < first + count; i++) {
				heights.set(i, estimateHeight(document.getParagraph(i)), false);
				if (count <= IMMEDIATE_LAYOUT_LIMIT)
					getParagraphElement(i);
			}
			break;
		case REMOVED:
			heights.remove(first, count);
			break;
		case RESET:
			caret = TextPosition.START;
			anchor = null;
			invalidateLayout();
			break;
		}
		caret = document.clamp(caret);
		if (anchor != null)
			anchor = document.clamp(anchor);
		geometryChanged = false;
		fireDrawingEvent(new DrawingChangedEvent());
	}

	// ------------------------------------------------------------------
	// Geometric queries.

	/// The text position closest to a point.
	public TextPosition getPositionAt(Point2D point) {
		int n = document.getParagraphCount();
		int i = getParagraphIndexAt(point.getY());
		ParagraphElement e = getParagraphElement(i);
		// The layout of paragraph i may have reduced its height: the point
		// might now be in a following paragraph.
		while (i + 1 < n && point.getY() >= getParagraphTop(i + 1)) {
			i++;
			e = getParagraphElement(i);
		}
		Rectangle2D b = e.getBounds();
		int offset = e.getOffsetAt(point.getX() - b.getMinX(), point.getY() - b.getMinY());
		notifyGeometryChanges();
		return new TextPosition(i, offset);
	}

	/// The caret rectangle for a position (zero width), in drawing coordinates.
	public Rectangle2D getCaretRectangle(TextPosition pos) {
		ParagraphElement e = getParagraphElement(pos.paragraph());
		Rectangle2D r = e.getCaretRectangle(pos.offset());
		Rectangle2D b = e.getBounds();
		return new Rectangle2D.Double(r.getX() + b.getMinX(), r.getY() + b.getMinY(), 0, r.getHeight());
	}

	/// The position on the next line, as close as possible to x (or the end
	/// of the text, on the last line).
	public TextPosition getPositionBelow(TextPosition pos, double x) {
		ParagraphElement e = getParagraphElement(pos.paragraph());
		int line = e.getLineIndexFor(pos.offset());
		TextPosition result;
		if (line + 1 < e.getLineCount()) {
			result = new TextPosition(pos.paragraph(), e.getOffsetInLine(line + 1, x - e.getBounds().getMinX()));
		} else if (pos.paragraph() + 1 < document.getParagraphCount()) {
			ParagraphElement next = getParagraphElement(pos.paragraph() + 1);
			result = new TextPosition(pos.paragraph() + 1, next.getOffsetInLine(0, x - next.getBounds().getMinX()));
		} else {
			result = document.getEnd();
		}
		notifyGeometryChanges();
		return result;
	}

	/// The position on the previous line, as close as possible to x (or the
	/// start of the text, on the first line).
	public TextPosition getPositionAbove(TextPosition pos, double x) {
		ParagraphElement e = getParagraphElement(pos.paragraph());
		int line = e.getLineIndexFor(pos.offset());
		TextPosition result;
		if (line > 0) {
			result = new TextPosition(pos.paragraph(), e.getOffsetInLine(line - 1, x - e.getBounds().getMinX()));
		} else if (pos.paragraph() > 0) {
			ParagraphElement previous = getParagraphElement(pos.paragraph() - 1);
			result = new TextPosition(pos.paragraph() - 1,
					previous.getOffsetInLine(previous.getLineCount() - 1, x - previous.getBounds().getMinX()));
		} else {
			result = TextPosition.START;
		}
		notifyGeometryChanges();
		return result;
	}

	/// The start of the displayed line containing pos.
	public TextPosition getLineStart(TextPosition pos) {
		ParagraphElement e = getParagraphElement(pos.paragraph());
		TextLineElement line = e.getLine(e.getLineIndexFor(pos.offset()));
		notifyGeometryChanges();
		return new TextPosition(pos.paragraph(), line.getStart());
	}

	/// The end of the displayed line containing pos (before the space the line
	/// was broken on, if any).
	public TextPosition getLineEnd(TextPosition pos) {
		ParagraphElement e = getParagraphElement(pos.paragraph());
		int index = e.getLineIndexFor(pos.offset());
		TextLineElement line = e.getLine(index);
		int end = line.getEnd();
		if (index < e.getLineCount() - 1 && line.endsWithSpace(e.getParagraph()))
			end--;
		notifyGeometryChanges();
		return new TextPosition(pos.paragraph(), end);
	}

	// ------------------------------------------------------------------
	// Caret and selection.

	public TextPosition getCaret() {
		return caret;
	}

	/// Moves the caret, and removes the selection.
	public void setCaret(TextPosition pos) {
		moveCaret(pos, false);
	}

	/// Moves the caret.
	///
	/// @param pos the new position.
	/// @param extendSelection if true, the selection goes from its current
	///  anchor (or from the old caret position) to pos. If false, the selection
	///  is removed.
	public void moveCaret(TextPosition pos, boolean extendSelection) {
		if (extendSelection) {
			if (anchor == null)
				anchor = caret;
		} else {
			anchor = null;
		}
		caret = document.clamp(pos);
		fireDrawingEvent(new CursorChangedEvent());
	}

	/// Selects the text between two positions; the caret goes to `to`.
	public void select(TextPosition from, TextPosition to) {
		anchor = document.clamp(from);
		caret = document.clamp(to);
		fireDrawingEvent(new CursorChangedEvent());
	}

	public void selectAll() {
		select(TextPosition.START, document.getEnd());
	}

	public boolean hasSelection() {
		return anchor != null && !anchor.equals(caret);
	}

	/// The start of the selection (the caret if there is no selection).
	public TextPosition getSelectionStart() {
		return anchor == null ? caret : TextPosition.min(anchor, caret);
	}

	/// The end of the selection (the caret if there is no selection).
	public TextPosition getSelectionEnd() {
		return anchor == null ? caret : TextPosition.max(anchor, caret);
	}

	public void setCaretVisible(boolean caretVisible) {
		this.caretVisible = caretVisible;
		fireDrawingEvent(new DrawingChangedEvent());
	}

	public boolean isCaretVisible() {
		return caretVisible;
	}

	// ------------------------------------------------------------------
	// Configuration.

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
		configurationChanged();
	}

	public double getPageWidth() {
		return pageWidth;
	}

	/// Sets the width available for the text lines (margins excluded).
	public void setPageWidth(double pageWidth) {
		if (pageWidth != this.pageWidth) {
			this.pageWidth = pageWidth;
			configurationChanged();
		}
	}

	public Margin getPageMargin() {
		return pageMargin;
	}

	/// Sets the space around the text column.
	public void setPageMargin(Margin pageMargin) {
		this.pageMargin = pageMargin;
		configurationChanged();
	}

	public double getParagraphSpacing() {
		return paragraphSpacing;
	}

	public void setParagraphSpacing(double paragraphSpacing) {
		this.paragraphSpacing = paragraphSpacing;
		configurationChanged();
	}

	public Color getTextColor() {
		return textColor;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
		configurationChanged();
	}

	public void setSelectionColor(Color selectionColor) {
		this.selectionColor = selectionColor;
		fireDrawingEvent(new DrawingChangedEvent());
	}

	public void setCaretColor(Color caretColor) {
		this.caretColor = caretColor;
		fireDrawingEvent(new DrawingChangedEvent());
	}

	private void configurationChanged() {
		invalidateLayout();
		fireDrawingEvent(new DrawingChangedEvent());
	}
}
