package org.qenherkhopeshef.viewtoolkit1.drawing.textOriented;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.qenherkhopeshef.viewtoolkit1.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewtoolkit1.drawing.textOriented.TextDocumentEvent.Type;

/// A text, made of paragraphs, with embedded graphical elements.
///
/// This is the model edited through a [TextDrawing]. It knows nothing about
/// layout. A document always contains at least one (possibly empty) paragraph.
///
/// Embedded elements are any [GraphicalElement]. They are treated as
/// immutable leaves: the document never changes them, and they should not be
/// modified once embedded.
///
/// For large texts, build the content with a [Builder] and install it with
/// [#setContent(Builder)]: listeners then receive a single event.
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public class TextDocument {

	private ArrayList<Paragraph> paragraphs = new ArrayList<>();

	private final List<TextDocumentListener> listeners = new CopyOnWriteArrayList<>();

	/// Creates an empty document.
	public TextDocument() {
		paragraphs.add(new Paragraph());
	}

	public void addTextDocumentListener(TextDocumentListener listener) {
		listeners.add(listener);
	}

	public void removeTextDocumentListener(TextDocumentListener listener) {
		listeners.remove(listener);
	}

	private void fire(Type type, int first, int count) {
		TextDocumentEvent event = new TextDocumentEvent(type, first, count);
		for (TextDocumentListener l : listeners)
			l.documentChanged(event);
	}

	public int getParagraphCount() {
		return paragraphs.size();
	}

	public Paragraph getParagraph(int index) {
		return paragraphs.get(index);
	}

	/// The position after the last character of the document.
	public TextPosition getEnd() {
		int last = paragraphs.size() - 1;
		return new TextPosition(last, paragraphs.get(last).length());
	}

	/// Returns the nearest valid position.
	public TextPosition clamp(TextPosition pos) {
		int p = Math.min(pos.paragraph(), paragraphs.size() - 1);
		int offset = Math.min(pos.offset(), paragraphs.get(p).length());
		if (p == pos.paragraph() && offset == pos.offset())
			return pos;
		return new TextPosition(p, offset);
	}

	/// The position one character after pos (or null at the end of the text).
	/// The end of a paragraph is followed by the start of the next one.
	public TextPosition next(TextPosition pos) {
		Paragraph p = paragraphs.get(pos.paragraph());
		if (pos.offset() < p.length())
			return new TextPosition(pos.paragraph(), pos.offset() + 1);
		if (pos.paragraph() + 1 < paragraphs.size())
			return new TextPosition(pos.paragraph() + 1, 0);
		return null;
	}

	/// The position one character before pos (or null at the start of the text).
	public TextPosition previous(TextPosition pos) {
		if (pos.offset() > 0)
			return new TextPosition(pos.paragraph(), pos.offset() - 1);
		if (pos.paragraph() > 0)
			return new TextPosition(pos.paragraph() - 1, paragraphs.get(pos.paragraph() - 1).length());
		return null;
	}

	/// Inserts text. Line feeds (`\n`, `\r\n` or `\r`) split paragraphs.
	///
	/// Tabulations are replaced by spaces, other control characters and object
	/// replacement characters are dropped.
	///
	/// @return the position just after the inserted text.
	public TextPosition insertText(TextPosition pos, String text) {
		checkPosition(pos);
		String[] lines = splitLines(text);
		Paragraph first = paragraphs.get(pos.paragraph());
		if (lines.length == 1) {
			first.insertText(pos.offset(), lines[0]);
			fire(Type.CHANGED, pos.paragraph(), 1);
			return new TextPosition(pos.paragraph(), pos.offset() + lines[0].length());
		}
		Paragraph tail = first.split(pos.offset());
		first.appendText(lines[0]);
		List<Paragraph> newParagraphs = new ArrayList<>(lines.length - 1);
		for (int i = 1; i < lines.length - 1; i++)
			newParagraphs.add(new Paragraph(lines[i]));
		Paragraph last = new Paragraph(lines[lines.length - 1]);
		last.append(tail);
		newParagraphs.add(last);
		paragraphs.addAll(pos.paragraph() + 1, newParagraphs);
		fire(Type.CHANGED, pos.paragraph(), 1);
		fire(Type.INSERTED, pos.paragraph() + 1, newParagraphs.size());
		return new TextPosition(pos.paragraph() + lines.length - 1, lines[lines.length - 1].length());
	}

	/// Embeds a graphical element in the text.
	///
	/// @return the position just after the element.
	public TextPosition insertElement(TextPosition pos, GraphicalElement element) {
		checkPosition(pos);
		paragraphs.get(pos.paragraph()).insertElement(pos.offset(), element);
		fire(Type.CHANGED, pos.paragraph(), 1);
		return new TextPosition(pos.paragraph(), pos.offset() + 1);
	}

	/// Deletes the text between two positions (in any order).
	public void delete(TextPosition a, TextPosition b) {
		checkPosition(a);
		checkPosition(b);
		TextPosition start = TextPosition.min(a, b);
		TextPosition end = TextPosition.max(a, b);
		if (start.equals(end))
			return;
		Paragraph first = paragraphs.get(start.paragraph());
		if (start.paragraph() == end.paragraph()) {
			first.delete(start.offset(), end.offset());
			fire(Type.CHANGED, start.paragraph(), 1);
		} else {
			Paragraph last = paragraphs.get(end.paragraph());
			first.delete(start.offset(), first.length());
			first.append(last.split(end.offset()));
			paragraphs.subList(start.paragraph() + 1, end.paragraph() + 1).clear();
			fire(Type.CHANGED, start.paragraph(), 1);
			fire(Type.REMOVED, start.paragraph() + 1, end.paragraph() - start.paragraph());
		}
	}

	/// The text between two positions (in any order). Paragraphs are separated
	/// by `\n`; embedded elements are skipped.
	public String getText(TextPosition a, TextPosition b) {
		TextPosition start = TextPosition.min(a, b);
		TextPosition end = TextPosition.max(a, b);
		StringBuilder result = new StringBuilder();
		for (int i = start.paragraph(); i <= end.paragraph(); i++) {
			Paragraph p = paragraphs.get(i);
			int from = i == start.paragraph() ? start.offset() : 0;
			int to = i == end.paragraph() ? end.offset() : p.length();
			for (int k = from; k < to; k++) {
				char c = p.charAt(k);
				if (c != Paragraph.OBJECT_REPLACEMENT_CHARACTER)
					result.append(c);
			}
			if (i < end.paragraph())
				result.append('\n');
		}
		return result.toString();
	}

	/// Replaces the whole content of the document.
	public void setContent(Builder builder) {
		paragraphs = builder.build();
		fire(Type.RESET, 0, paragraphs.size());
	}

	/// Replaces the whole content of the document with a plain text.
	public void setText(String text) {
		Builder builder = new Builder();
		builder.appendText(text);
		setContent(builder);
	}

	private void checkPosition(TextPosition pos) {
		if (pos.paragraph() >= paragraphs.size() || pos.offset() > paragraphs.get(pos.paragraph()).length())
			throw new IndexOutOfBoundsException("Invalid position " + pos);
	}

	/// Splits a text into lines, cleaning each line.
	private static String[] splitLines(String text) {
		String[] lines = text.split("\r\n|\r|\n", -1);
		for (int i = 0; i < lines.length; i++)
			lines[i] = clean(lines[i]);
		return lines;
	}

	private static String clean(String line) {
		StringBuilder result = null;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			boolean ok = c >= ' ' && c != Paragraph.OBJECT_REPLACEMENT_CHARACTER && c != '\u007f';
			if (!ok && result == null) {
				result = new StringBuilder(line.length());
				result.append(line, 0, i);
			}
			if (result != null) {
				if (ok)
					result.append(c);
				else if (c == '\t')
					result.append("    ");
			}
		}
		return result == null ? line : result.toString();
	}

	/// Builds the content of a document, without firing any event.
	///
	/// ```java
	/// TextDocument.Builder b = new TextDocument.Builder();
	/// b.appendText("A first paragraph.\nA second one, with a picture: ");
	/// b.appendElement(new ImageElement(image));
	/// document.setContent(b);
	/// ```
	public static class Builder {
		private ArrayList<Paragraph> paragraphs = new ArrayList<>();

		public Builder() {
			paragraphs.add(new Paragraph());
		}

		/// Appends text to the current paragraph. Line feeds start new paragraphs.
		public Builder appendText(String text) {
			String[] lines = splitLines(text);
			paragraphs.get(paragraphs.size() - 1).appendText(lines[0]);
			for (int i = 1; i < lines.length; i++)
				paragraphs.add(new Paragraph(lines[i]));
			return this;
		}

		/// Appends an embedded element to the current paragraph.
		public Builder appendElement(GraphicalElement element) {
			paragraphs.get(paragraphs.size() - 1).appendElement(element);
			return this;
		}

		/// Starts a new paragraph.
		public Builder newParagraph() {
			paragraphs.add(new Paragraph());
			return this;
		}

		private ArrayList<Paragraph> build() {
			ArrayList<Paragraph> result = paragraphs;
			paragraphs = new ArrayList<>();
			paragraphs.add(new Paragraph());
			return result;
		}
	}
}
