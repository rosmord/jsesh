package org.qenherkhopeshef.viewToolKit.drawing.textOriented;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;

/// A paragraph of a [TextDocument]: a string of characters, in which graphical
/// elements (pictures...) can be embedded.
///
/// An embedded element occupies one character position, filled with
/// [#OBJECT_REPLACEMENT_CHARACTER] in the text. The k-th such character
/// stands for the k-th embedded element.
///
/// Paragraphs are only modified through their document, so that the
/// document listeners are notified.
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public final class Paragraph {

	/// The Unicode character which stands for an embedded element.
	public static final char OBJECT_REPLACEMENT_CHARACTER = '￼';

	private final StringBuilder text;

	private final ArrayList<GraphicalElement> elements;

	Paragraph() {
		this("");
	}

	/// @param plainText a text without any object replacement character.
	Paragraph(String plainText) {
		this.text = new StringBuilder(plainText);
		this.elements = new ArrayList<>(0);
	}

	public int length() {
		return text.length();
	}

	public boolean isEmpty() {
		return text.length() == 0;
	}

	public char charAt(int offset) {
		return text.charAt(offset);
	}

	/// The paragraph text, embedded elements being represented by
	/// [#OBJECT_REPLACEMENT_CHARACTER].
	public String getText() {
		return text.toString();
	}

	/// Part of the paragraph text, embedded elements being represented by
	/// [#OBJECT_REPLACEMENT_CHARACTER].
	public String getText(int start, int end) {
		return text.substring(start, end);
	}

	/// Returns the element embedded at a given offset, or null if the
	/// character at this offset is a normal character.
	public GraphicalElement getElementAt(int offset) {
		if (text.charAt(offset) != OBJECT_REPLACEMENT_CHARACTER)
			return null;
		return elements.get(countElementsBefore(offset));
	}

	/// The embedded elements, in text order.
	public List<GraphicalElement> getElements() {
		return Collections.unmodifiableList(elements);
	}

	/// Number of embedded elements before a given offset.
	private int countElementsBefore(int offset) {
		if (elements.isEmpty())
			return 0;
		int count = 0;
		for (int i = 0; i < offset; i++) {
			if (text.charAt(i) == OBJECT_REPLACEMENT_CHARACTER)
				count++;
		}
		return count;
	}

	void insertText(int offset, String plainText) {
		text.insert(offset, plainText);
	}

	void insertElement(int offset, GraphicalElement element) {
		elements.add(countElementsBefore(offset), element);
		text.insert(offset, OBJECT_REPLACEMENT_CHARACTER);
	}

	void appendText(String plainText) {
		text.append(plainText);
	}

	void appendElement(GraphicalElement element) {
		elements.add(element);
		text.append(OBJECT_REPLACEMENT_CHARACTER);
	}

	void delete(int start, int end) {
		int first = countElementsBefore(start);
		int last = countElementsBefore(end);
		elements.subList(first, last).clear();
		text.delete(start, end);
	}

	/// Cuts this paragraph at offset, and returns its tail as a new paragraph.
	Paragraph split(int offset) {
		Paragraph tail = new Paragraph(text.substring(offset));
		int k = countElementsBefore(offset);
		tail.elements.addAll(elements.subList(k, elements.size()));
		elements.subList(k, elements.size()).clear();
		text.setLength(offset);
		return tail;
	}

	/// Appends the content of another paragraph to this one.
	void append(Paragraph other) {
		text.append(other.text);
		elements.addAll(other.elements);
	}

	@Override
	public String toString() {
		return "[paragraph " + text + "]";
	}
}
