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

/// Computes the successive places where the cursor may stand in a
/// [HieroglyphicText], depending on a [CaretGranularity].
///
/// With [CaretGranularity#QUADRAT], the cursor stands on top-level positions
/// only: `(0)`, `(1)`, ...
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
/// Stops are ordered like [MDCPosition#compareTo(MDCPosition)]: `(1) < (1,0,1) <
/// (1,1) < (2)`.
public final class PositionNavigator {

	private PositionNavigator() {
	}

	/// Lists the interior stops of a top-level item, in document order.
	///
	/// @param text the text.
	/// @param topIndex the index of a top-level item of `text`.
	/// @return the stops inside the item (empty if there is none, or if the
	///         index is invalid).
	public static List<MDCPosition> innerStops(HieroglyphicText text, int topIndex) {
		if (topIndex < 0 || topIndex >= text.getNumberOfChildren()) {
			return Collections.emptyList();
		}
		List<MDCPosition> result = new ArrayList<>();
		collectStops(text.getChildAt(topIndex), new int[] { topIndex }, result);
		return result;
	}

	private static void collectStops(ModelElement element, int[] path, List<MDCPosition> result) {
		if (!isNavigable(element)) {
			return;
		}
		int n = element.getNumberOfChildren();
		// Lists of quadrats (in cartouches...) have their edges as stops. For other
		// containers, the edges coincide with the gaps around the container.
		boolean edgesAreStops = element instanceof BasicItemList;
		for (int i = 0; i <= n; i++) {
			// The gap before child i.
			if (edgesAreStops || (i > 0 && i < n)) {
				result.add(MDCPosition.of(append(path, i)));
			}
			if (i < n) {
				collectStops(element.getChildAt(i), append(path, i), result);
			}
		}
	}

	private static boolean isNavigable(ModelElement element) {
		return element instanceof Cadrat || element instanceof HBox || element instanceof Ligature
				|| element instanceof ComplexLigature || element instanceof AbsoluteGroup || element instanceof Cartouche
				|| element instanceof SubCadrat || element instanceof BasicItemList;
	}

	private static int[] append(int[] path, int value) {
		int[] result = java.util.Arrays.copyOf(path, path.length + 1);
		result[path.length] = value;
		return result;
	}

	/// The position which follows or precedes another one, according to a
	/// granularity.
	///
	/// @param text the text.
	/// @param position a valid position in `text`. It may be nested.
	/// @param delta the number of stops to move by (may be negative). The result
	///        is clamped to the text bounds.
	/// @param granularity the kinds of stops allowed.
	/// @return the new position.
	public static MDCPosition move(HieroglyphicText text, MDCPosition position, int delta,
			CaretGranularity granularity) {
		MDCPosition current = position;
		for (int i = 0; i < Math.abs(delta); i++) {
			MDCPosition next = delta > 0 ? next(text, current, granularity) : previous(text, current, granularity);
			if (next.equals(current)) {
				break;
			}
			current = next;
		}
		return current;
	}

	/// @param text the text.
	/// @param position a valid position in `text`. It may be nested.
	/// @param granularity the kinds of stops allowed.
	/// @return the stop after `position` (`position` itself at the end of the
	///         text). With [CaretGranularity#QUADRAT], a nested position is
	///         first snapped to the top-level position before its item.
	public static MDCPosition next(HieroglyphicText text, MDCPosition position, CaretGranularity granularity) {
		int top = position.getPath()[0];
		int n = text.getNumberOfChildren();
		if (granularity == CaretGranularity.QUADRAT) {
			// Nested positions are first snapped to the position before their top item.
			return text.getPositionAt(top + 1);
		}
		List<MDCPosition> stops = innerStops(text, top);
		if (position.isTop()) {
			if (top >= n) {
				return text.getLastPosition();
			}
			return stops.isEmpty() ? text.getPositionAt(top + 1) : stops.get(0);
		}
		for (MDCPosition stop : stops) {
			if (stop.compareTo(position) > 0) {
				return stop;
			}
		}
		return text.getPositionAt(top + 1);
	}

	/// @param text the text.
	/// @param position a valid position in `text`. It may be nested.
	/// @param granularity the kinds of stops allowed.
	/// @return the stop before `position` (`position` itself at the start of the
	///         text). With [CaretGranularity#QUADRAT], a nested position is
	///         first snapped to the top-level position before its item.
	public static MDCPosition previous(HieroglyphicText text, MDCPosition position, CaretGranularity granularity) {
		int top = position.getPath()[0];
		if (granularity == CaretGranularity.QUADRAT) {
			// Nested positions are first snapped to the position before their top item.
			return text.getPositionAt(top - 1);
		}
		if (position.isTop()) {
			if (top <= 0) {
				return text.getPositionAt(0);
			}
			List<MDCPosition> before = innerStops(text, top - 1);
			return before.isEmpty() ? text.getPositionAt(top - 1) : before.get(before.size() - 1);
		}
		List<MDCPosition> stops = innerStops(text, top);
		for (int i = stops.size() - 1; i >= 0; i--) {
			if (stops.get(i).compareTo(position) < 0) {
				return stops.get(i);
			}
		}
		return text.getPositionAt(top);
	}
}
