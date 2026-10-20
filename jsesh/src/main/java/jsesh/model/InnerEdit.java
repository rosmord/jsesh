/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.util.Optional;

/// Editing at *inner* positions of a [HieroglyphicText]: the gaps inside a
/// quadrat, as listed by [PositionNavigator].
///
/// The gaps between the quadrats of a cartouche or of a sub-quadrat are not
/// inner positions: they belong to an editing scope ([TextScope]), which is
/// edited like the top level of the text (see
/// `jsesh.document.EditingScope`). The operations below refuse them. They
/// work, however, inside a quadrat of such a scope.
///
/// The text itself is never modified. Each operation computes the new version
/// of the top item which contains the position, as a copy, and the position
/// where the cursor should stand afterwards. The caller then replaces the top
/// item by its new version, using an ordinary (undoable) top-level
/// replacement.
///
/// The kind of container which holds the position decides what is done (see
/// [InnerEditable]). In particular, no separator is needed to say how a new
/// sign is combined with its neighbours:
///
/// | container                                   | insertion                   | deletion   |
/// |---                                          |---                          |---         |
/// | [Cadrat] (between rows)                     | a new row (as with `:`)     | a row      |
/// | [HBox] (between elements of a row)          | a new element (as with `*`) | an element |
/// | [Ligature]                                  | a new sign (as with `&`)    | a sign     |
///
/// Other containers ([ComplexLigature], [AbsoluteGroup]) are not supported yet.
///
/// An `InnerEdit` stands for one place in one text; each of its operations
/// starts afresh from the text, which it doesn't modify:
///
/// ```java
/// new InnerEdit(text, position).insertSign(new Hieroglyph("A1"))
/// ```
public final class InnerEdit {

	/// The outcome of an edit.
	///
	/// @param replacement the new version of the top item which contained the
	///        position, or empty if this top item has disappeared.
	/// @param caret the position where the cursor should stand once the top item
	///        is replaced. It is always a valid stop.
	public record Result(Optional<TopItem> replacement, MDCPosition caret) {
	}

	private final HieroglyphicText text;

	private final MDCPosition position;

	/// Prepares edits at a place of a text.
	///
	/// @param text the text. It is never modified.
	/// @param position the place of the edits; operations refuse anything but a
	///        valid nested position of `text`.
	public InnerEdit(HieroglyphicText text, MDCPosition position) {
		this.text = text;
		this.position = position;
	}

	/// Inserts a sign at the position.
	///
	/// @param sign the sign to insert. It is used as is.
	/// @return the result, with the cursor just after the new sign; empty if
	///         the position is not valid, or in an unsupported container.
	public Optional<Result> insertSign(Hieroglyph sign) {
		if (!isApplicable()) {
			return Optional.empty();
		}
		Draft draft = new Draft();
		int gap = position.getLastIndex();
		Optional<InnerEditable> container = draft.editableContainerOf(position);
		if (container.isEmpty() || gap > container.get().getNumberOfChildren()) {
			return Optional.empty();
		}
		container.get().insertSignAt(gap, sign);
		return Optional.of(draft.result(position.withLastIndex(gap + 1)));
	}

	/// Deletes the element which precedes the position.
	///
	/// @return the result, with the cursor in the gap which has been closed;
	///         empty if there is nothing to delete, or in an unsupported container.
	public Optional<Result> deleteBefore() {
		return delete(position.getLastIndex() - 1);
	}

	/// Deletes the element which follows the position.
	///
	/// @return the result, with the cursor still in the same place; empty if
	///         there is nothing to delete, or in an unsupported container.
	public Optional<Result> deleteAfter() {
		return delete(position.getLastIndex());
	}

	/// @param index the index of the element to delete, in the container of the position.
	private Optional<Result> delete(int index) {
		if (!isApplicable()) {
			return Optional.empty();
		}
		Draft draft = new Draft();
		Optional<ModelElement> container = draft.containerOf(position);
		if (container.isEmpty() || !(container.get() instanceof InnerEditable)
				|| index < 0 || index >= container.get().getNumberOfChildren()) {
			return Optional.empty();
		}
		container.get().removeChildAt(index);
		return draft.cleanUpAfterRemoval(position.withLastIndex(index));
	}

	/// Only nested positions are edited here; the gaps of the text itself are
	/// edited at the top level.
	private boolean isApplicable() {
		return !position.isTop() && text.isValidPosition(position);
	}

	/// A copy of the top item which contains the position, being edited.
	private final class Draft {

		private final TopItem top = text.getTopItemAt(position.getPath()[0]).deepCopy();

		/// The element which contains a nested gap, in the copy.
		Optional<ModelElement> containerOf(MDCPosition gap) {
			return top.getGapContainer(gap);
		}

		Optional<InnerEditable> editableContainerOf(MDCPosition gap) {
			return containerOf(gap).filter(InnerEditable.class::isInstance).map(InnerEditable.class::cast);
		}

		/// Cleans up after a removal: containers which have become empty
		/// disappear too, and the container which lost an element may be
		/// simplified by its own container (a ligature reduced to one sign
		/// becomes this sign).
		///
		/// @param gap the gap left by the removal.
		Optional<Result> cleanUpAfterRemoval(MDCPosition gap) {
			MDCPosition p = gap;
			Optional<InnerEditable> container;
			while ((container = editableContainerOf(p)).isPresent()) {
				// The place of the container, in its own container.
				MDCPosition containerGap = p.parent();
				if (container.get().getNumberOfChildren() == 0) {
					if (!container.get().disappearsWhenEmpty()) {
						return Optional.empty();
					}
					if (containerGap.isTop()) {
						// The top item itself is empty.
						return Optional.of(new Result(Optional.empty(), containerGap));
					}
					containerOf(containerGap).orElseThrow().removeChildAt(containerGap.getLastIndex());
					p = containerGap;
				} else {
					if (!containerGap.isTop()) {
						editableContainerOf(containerGap)
								.ifPresent(parent -> parent.simplifyElementAt(containerGap.getLastIndex()));
					}
					break;
				}
			}
			return Optional.of(result(p));
		}

		/// The result, with the cursor at the stop nearest to `caret`.
		Result result(MDCPosition caret) {
			return new Result(Optional.of(top), top.canonicalize(caret));
		}
	}
}
