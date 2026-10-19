/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.util.Arrays;
import java.util.Optional;

/// Editing at *inner* positions of a [HieroglyphicText]: the gaps inside a
/// quadrat, as listed by [PositionNavigator].
///
/// The text itself is never modified. Each operation computes the new version
/// of the top item which contains the position, as a copy, and the position
/// where the cursor should stand afterwards. The caller then replaces the top
/// item by its new version, using an ordinary (undoable) top-level
/// replacement.
///
/// The kind of container which holds the position decides what is done. In
/// particular, no separator is needed to say how a new sign is combined with
/// its neighbours:
///
/// | container | insertion | deletion |
/// |---|---|---|
/// | [Cadrat] (between rows) | a new row (as with `:`) | a row |
/// | [HBox] (between elements of a row) | a new element (as with `*`) | an element |
/// | [Ligature] | a new sign (as with `&`) | a sign |
/// | [BasicItemList] (content of a cartouche...) | a new quadrat | a quadrat |
///
/// Other containers ([ComplexLigature], [AbsoluteGroup]) are not supported yet.
public final class InnerEdit {

	/// The outcome of an edit.
	///
	/// @param replacement the new version of the top item which contained the
	///        position, or empty if this top item has disappeared.
	/// @param caret the position where the cursor should stand once the top item
	///        is replaced. It is always a valid stop.
	public record Result(Optional<TopItem> replacement, MDCPosition caret) {
	}

	private InnerEdit() {
	}

	/// Inserts a sign at an inner position.
	///
	/// @param text the text.
	/// @param position a nested position of `text`.
	/// @param sign the sign to insert. It is used as is.
	/// @return the result, with the cursor just after the new sign; empty if
	///         the position is not valid, or in an unsupported container.
	public static Optional<Result> insertSign(HieroglyphicText text, MDCPosition position, Hieroglyph sign) {
		Optional<TopItem> copy = copyTopItem(text, position);
		if (copy.isEmpty()) {
			return Optional.empty();
		}
		TopItem top = copy.get();
		int[] path = position.getPath();
		ModelElement container = PositionNavigator.descend(top, path, path.length - 1);
		int gap = path[path.length - 1];
		if (container == null || gap > container.getNumberOfChildren()) {
			return Optional.empty();
		}
		if (container instanceof Cadrat) {
			container.addChildAt(gap, new HBox(sign));
		} else if (container instanceof HBox || container instanceof Ligature) {
			container.addChildAt(gap, sign);
		} else if (container instanceof BasicItemList) {
			container.addChildAt(gap, new Cadrat(sign));
		} else {
			return Optional.empty();
		}
		path[path.length - 1] = gap + 1;
		return Optional.of(new Result(Optional.of(top), PositionNavigator.canonicalize(top, path)));
	}

	/// Deletes the element which precedes an inner position.
	///
	/// @param text the text.
	/// @param position a nested position of `text`.
	/// @return the result, with the cursor in the gap which has been closed;
	///         empty if there is nothing to delete, or in an unsupported container.
	public static Optional<Result> deleteBefore(HieroglyphicText text, MDCPosition position) {
		return delete(text, position, true);
	}

	/// Deletes the element which follows an inner position.
	///
	/// @param text the text.
	/// @param position a nested position of `text`.
	/// @return the result, with the cursor still in the same place; empty if
	///         there is nothing to delete, or in an unsupported container.
	public static Optional<Result> deleteAfter(HieroglyphicText text, MDCPosition position) {
		return delete(text, position, false);
	}

	private static Optional<Result> delete(HieroglyphicText text, MDCPosition position, boolean before) {
		Optional<TopItem> copy = copyTopItem(text, position);
		if (copy.isEmpty()) {
			return Optional.empty();
		}
		TopItem top = copy.get();
		int[] path = position.getPath();
		ModelElement container = PositionNavigator.descend(top, path, path.length - 1);
		int gap = path[path.length - 1];
		if (!isSupportedContainer(container)) {
			return Optional.empty();
		}
		int index = before ? gap - 1 : gap;
		if (index < 0 || index >= container.getNumberOfChildren()) {
			return Optional.empty();
		}
		container.removeChildAt(index);
		path[path.length - 1] = index;
		return removeEmptyContainers(top, path);
	}

	/// Cleans up after a removal: containers which have become empty are removed
	/// too, and a ligature reduced to one sign becomes this sign.
	private static Optional<Result> removeEmptyContainers(TopItem top, int[] path) {
		int[] p = path;
		while (true) {
			ModelElement container = PositionNavigator.descend(top, p, p.length - 1);
			if (container instanceof BasicItemList) {
				break;
			}
			if (container.getNumberOfChildren() == 0) {
				if (p.length == 2) {
					// The top item itself is empty.
					return top instanceof Cadrat
							? Optional.of(new Result(Optional.empty(), MDCPosition.of(p[0])))
							: Optional.empty();
				}
				ModelElement parent = PositionNavigator.descend(top, p, p.length - 2);
				boolean removable = (container instanceof HBox && parent instanceof Cadrat)
						|| (container instanceof Cadrat && parent instanceof BasicItemList);
				if (!removable) {
					return Optional.empty();
				}
				int index = p[p.length - 2];
				parent.removeChildAt(index);
				p = Arrays.copyOf(p, p.length - 1);
				p[p.length - 1] = index;
			} else {
				if (container instanceof Ligature && container.getNumberOfChildren() == 1 && p.length > 2) {
					ModelElement parent = PositionNavigator.descend(top, p, p.length - 2);
					if (parent instanceof HBox) {
						Hieroglyph remaining = (Hieroglyph) container.getChildAt(0);
						parent.setChildAt(p[p.length - 2], (Hieroglyph) remaining.deepCopy());
					}
				}
				break;
			}
		}
		return Optional.of(new Result(Optional.of(top), PositionNavigator.canonicalize(top, p)));
	}

	private static boolean isSupportedContainer(ModelElement container) {
		return container instanceof Cadrat || container instanceof HBox || container instanceof Ligature
				|| container instanceof BasicItemList;
	}

	/// A copy of the top item which holds a nested position.
	private static Optional<TopItem> copyTopItem(HieroglyphicText text, MDCPosition position) {
		if (position.isTop() || !text.isValidPosition(position)) {
			return Optional.empty();
		}
		return Optional.of(text.getTopItemAt(position.getPath()[0]).deepCopy());
	}
}
