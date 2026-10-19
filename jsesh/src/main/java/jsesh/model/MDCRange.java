/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.util.Objects;

/// A span of text, delimited by two [MDCPosition]s.
///
/// The range is always *ordered*: `start` is never after `end` in document
/// order. Use [#ofUnordered(MDCPosition, MDCPosition)] when the two positions
/// come from a user selection (where the anchor may be after the caret).
///
/// Like [MDCPosition], a range is a plain value, only meaningful relative to
/// the [HieroglyphicText] supplied by the caller.
///
/// @param start the beginning of the range
/// @param end   the end of the range; not before `start`
public record MDCRange(MDCPosition start, MDCPosition end) {

	public MDCRange {
		Objects.requireNonNull(start, "start");
		Objects.requireNonNull(end, "end");
		if (start.compareTo(end) > 0) {
			throw new IllegalArgumentException("range start " + start + " is after its end " + end);
		}
	}

	/// Builds a range from two positions, given in any order.
	///
	/// @param p1 a position
	/// @param p2 another position
	/// @return the range between the two positions, smallest first.
	public static MDCRange ofUnordered(MDCPosition p1, MDCPosition p2) {
		return p1.compareTo(p2) <= 0 ? new MDCRange(p1, p2) : new MDCRange(p2, p1);
	}

	/// @return true if the range contains no text.
	public boolean isEmpty() {
		return start.equals(end);
	}
}
