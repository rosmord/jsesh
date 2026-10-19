/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.util.ArrayList;
import java.util.List;

/// A sequence of quadrat-level items: a text, or a text in miniature.
///
/// The whole text ([HieroglyphicText]) is one, and so is the content of a
/// [Cartouche] or of a [SubCadrat] (a [BasicItemList]). Such a sequence is an
/// editing *scope*: the editor can work in it as it works at the top level of
/// the text (see [TextScope]).
///
/// Indices are gap indices, as in a top-level [MDCPosition]: 0 is before the
/// first item, and `getNumberOfChildren()` after the last one.
public interface ItemSequence {

	/// @return the number of items in the sequence.
	int getNumberOfChildren();

	/// @param index the index of an item.
	/// @return the item (not a copy).
	TopItem getItemAt(int index);

	/// Can an item be placed in this sequence?
	///
	/// For instance, a cartouche only accepts [BasicItem]s: it can't contain a
	/// line break.
	///
	/// @param item an item.
	/// @return true if the item may be inserted here.
	boolean accepts(TopItem item);

	/// Inserts items.
	///
	/// @param index where to insert them.
	/// @param items the items, used as they are. All must be accepted by this
	///        sequence (see [#accepts(TopItem)]).
	/// @throws IllegalArgumentException if one of the items is not accepted.
	void insertItemsAt(int index, List<? extends TopItem> items);

	/// Removes the items between two gaps.
	///
	/// @param start the first gap.
	/// @param end the last gap (`start <= end`).
	/// @return the removed items.
	List<TopItem> removeItems(int start, int end);

	/// @param start the first gap.
	/// @param end the last gap (`start <= end`).
	/// @return a **copy** of the items between the two gaps.
	default List<TopItem> getItemsBetween(int start, int end) {
		List<TopItem> result = new ArrayList<>(end - start);
		for (int i = start; i < end; i++) {
			result.add((TopItem) getItemAt(i).deepCopy());
		}
		return result;
	}

	/// @param items some items.
	/// @return true if all of them are accepted by this sequence.
	default boolean acceptsAll(List<? extends TopItem> items) {
		return items.stream().allMatch(this::accepts);
	}
}
