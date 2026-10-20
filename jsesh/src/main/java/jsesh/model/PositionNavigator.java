/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// Computes the successive places where the cursor may stand in a [HieroglyphicText], depending on a [CaretGranularity].
///
/// With [CaretGranularity#QUADRAT], the cursor stands between quadrats:
/// on top-level positions `(0)`, `(1)`, ..., and on the gaps of the editing
/// scopes nested in them (the content of cartouches and sub-quadrats, see
/// below and [TextScope]).
///
/// With [CaretGranularity#INNER], it also stands on the *interior* gaps of
/// the quadrats, taken in document order:
///
/// - between the rows of a [Cadrat]: `(q, h)`;
/// - between the elements of a [HBox]: `(q, h, e)`;
/// - between the signs of a [Ligature]: `(q, h, e, g)`;
/// - between the parts of a [ComplexLigature] (the group before the main sign,
///   the main sign, the group after it);
/// - between the signs of an [AbsoluteGroup], in their order in the group;
///
/// and so on, recursively. The first and last gaps of such a container (0 and n)
/// are not stops: they coincide visually with the gap which surrounds the
/// container. Hence, a quadrat which contains one sign only has no inner stop.
///
/// A [Cartouche] or a [SubCadrat] is entered too: its content (a
/// [BasicItemList] of quadrats) is a text in miniature. Here, *all* the gaps of
/// the list are stops, including the first and the last one, as they are
/// distinct from the gaps outside the cartouche. For instance, in `<-A1-B1->`,
/// the stops are `(0)`, `(0,0,0,0,0)` (after the opening of the cartouche),
/// `(0,0,0,0,1)`, `(0,0,0,0,2)` (before its closing) and `(1)`.
///
/// Other kinds of elements (hieroglyphs, breaks, alphabetic
/// characters...) are not entered.
///
/// The kinds of elements which are entered, and whether their edge gaps are
/// stops, are decided by the elements themselves
/// ([ModelElement#isCursorStop(int, CaretGranularity)]).
///
/// Stops are ordered like [MDCPosition#compareTo(MDCPosition)]: `(1) < (1,0,1) <
/// (1,1) < (2)`.
public final class PositionNavigator {

	private final HieroglyphicText text;

	/// Creates a navigator for a text.
	///
	/// @param text the text.
	public PositionNavigator(HieroglyphicText text) {
		this.text = text;
	}

	/// Lists the interior stops of a top-level item, in document order, for
	/// [CaretGranularity#INNER].
	///
	/// @param topIndex the index of a top-level item of the text.
	/// @return the stops inside the item (empty if there is none, or if the
	///         index is invalid).
	public List<MDCPosition> innerStops(int topIndex) {
		return stops(topIndex, CaretGranularity.INNER);
	}

	/// Lists the stops inside a top-level item, in document order.
	///
	/// @param topIndex the index of a top-level item of the text.
	/// @param granularity the kinds of stops: with [CaretGranularity#QUADRAT],
	///        only the gaps of the nested scopes (cartouches...).
	/// @return the stops inside the item (empty if there is none, or if the
	///         index is invalid).
	public List<MDCPosition> stops(int topIndex, CaretGranularity granularity) {
		if (topIndex < 0 || topIndex >= text.getNumberOfChildren()) {
			return Collections.emptyList();
		}
		List<MDCPosition> result = new ArrayList<>();
		collectStops(text.getTopItemAt(topIndex), MDCPosition.of(topIndex), granularity, result);
		return result;
	}

	/// @param element an element.
	/// @param elementPath the path of `element`, from the text.
	/// @param granularity the kinds of stops.
	private void collectStops(ModelElement element, MDCPosition elementPath, CaretGranularity granularity,
			List<MDCPosition> result) {
		if (!element.isNavigable()) {
			return;
		}
		int n = element.getNumberOfChildren();
		for (int i = 0; i <= n; i++) {
			// The gap before child i, which is also the path of child i.
			MDCPosition gap = elementPath.child(i);
			if (element.isCursorStop(i, granularity)) {
				result.add(gap);
			}
			if (i < n) {
				collectStops(element.getChildAt(i), gap, granularity, result);
			}
		}
	}

	/// Brings a position to the nearest place where the cursor may stand
	/// (see [TopItem#canonicalize(MDCPosition)]).
	///
	/// @param position a position, possibly nested. A path which no longer
	///        matches the structure of the text is handled as far as possible.
	/// @return the equivalent stop.
	public MDCPosition canonicalize(MDCPosition position) {
		if (position.isTop() || position.getPath()[0] >= text.getNumberOfChildren()) {
			return position;
		}
		return text.getTopItemAt(position.getPath()[0]).canonicalize(position);
	}

	/// The position which follows or precedes another one, according to a
	/// granularity.
	///
	/// @param position a valid position in the text. It may be nested.
	/// @param delta the number of stops to move by (may be negative). The result
	///        is clamped to the text bounds.
	/// @param granularity the kinds of stops allowed.
	/// @return the new position.
	public MDCPosition move(MDCPosition position, int delta, CaretGranularity granularity) {
		MDCPosition current = position;
		for (int i = 0; i < Math.abs(delta); i++) {
			MDCPosition next = delta > 0 ? next(current, granularity) : previous(current, granularity);
			if (next.equals(current)) {
				break;
			}
			current = next;
		}
		return current;
	}

	/// @param position a valid position in the text. It may be nested.
	/// @param granularity the kinds of stops allowed.
	/// @return the stop after `position` (`position` itself at the end of the
	///         text).
	public MDCPosition next(MDCPosition position, CaretGranularity granularity) {
		int top = position.getPath()[0];
		int n = text.getNumberOfChildren();
		if (position.isTop() && top >= n) {
			return text.getLastPosition();
		}
		for (MDCPosition stop : stops(top, granularity)) {
			if (stop.compareTo(position) > 0) {
				return stop;
			}
		}
		return text.getPositionAt(top + 1);
	}

	/// @param position a valid position in the text. It may be nested.
	/// @param granularity the kinds of stops allowed.
	/// @return the stop before `position` (`position` itself at the start of the
	///         text).
	public MDCPosition previous(MDCPosition position, CaretGranularity granularity) {
		int top = position.getPath()[0];
		if (position.isTop()) {
			if (top <= 0) {
				return text.getPositionAt(0);
			}
			List<MDCPosition> before = stops(top - 1, granularity);
			return before.isEmpty() ? text.getPositionAt(top - 1) : before.get(before.size() - 1);
		}
		List<MDCPosition> stops = stops(top, granularity);
		for (int i = stops.size() - 1; i >= 0; i--) {
			if (stops.get(i).compareTo(position) < 0) {
				return stops.get(i);
			}
		}
		return text.getPositionAt(top);
	}
}
