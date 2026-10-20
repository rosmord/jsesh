package org.qenherkhopeshef.viewToolKit.drawing.textOriented;

/// A change in a [TextDocument].
///
/// @param type the kind of change.
/// @param firstParagraph the first paragraph concerned (for
///  [Type#INSERTED], the index of the first new paragraph; for
///  [Type#REMOVED], the index the first removed paragraph used to have).
/// @param count the number of paragraphs concerned.
public record TextDocumentEvent(Type type, int firstParagraph, int count) {

	public enum Type {
		/// The content of existing paragraphs changed.
		CHANGED,
		/// New paragraphs were inserted.
		INSERTED,
		/// Paragraphs were removed.
		REMOVED,
		/// The whole content was replaced.
		RESET
	}
}
