/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.io.Serializable;
import java.util.Arrays;

/// A position in a MDC model.
///
/// A position is a plain, immutable value: it does **not** hold a reference to
/// the text it refers to. It is only meaningful relative to a
/// [HieroglyphicText] supplied by the caller. Operations which need the text
/// (element access, bounded navigation, line navigation...) live in
/// [HieroglyphicText], and take the position as argument; for instance
/// [HieroglyphicText#getElementAfter(MDCPosition)] or
/// [HieroglyphicText#getPositionAt(int)], which builds a position clamped to the
/// text's bounds.
///
/// A position is a **path** of integers in the model tree. All values but the
/// last are child indices: they select the element in which the position lies.
/// The last value is a *gap* index in the element thus reached: 0 is before its
/// first child, and n is after its n-th child. A position never designates a
/// model element; it's a cursor **between** elements.
///
/// For instance, in the text `m-p*t:pt`, position `(0)` is before the `m`,
/// `(1)` is after the `m` and `(2)` after the second quadrat. Position `(1,0,1)`
/// is in child 1 of the text (the quadrat `p*t`), in its hbox 0, after its first
/// element: that is, just after the `p`.
///
/// Positions are ordered in document order, which is the lexicographic order of
/// their paths: `(1) < (1,0,1) < (2)`.
///
/// **Current limitation:** only *top-level* positions (one-element paths, see
/// [#isTop()]) are accepted by the text operations (copy, removal, insertion,
/// export of a range...). Those operations use [#getIndex()], which throws an
/// [UnsupportedOperationException] on nested positions.
///
/// A position is not a "long term" object. It's quite likely to become invalid
/// or outdated if the text is modified. For long term marking of mutable text
/// position, one should use a [MDCMark].
///
/// @author rosmord
@SuppressWarnings("serial")
public final class MDCPosition implements Serializable, Comparable<MDCPosition> {

	private final int[] path;

	/// Builds a top-level position.
	///
	/// A negative index is clamped to 0. The position is *not* checked against
	/// the upper bound of any text; use [HieroglyphicText#getPositionAt(int)] to get
	/// a position guaranteed to fall inside a given text.
	///
	/// @param index the index of the position.
	public MDCPosition(int index) {
		this.path = new int[] { Math.max(0, index) };
	}

	private MDCPosition(int[] path, boolean copied) {
		this.path = path;
	}

	/// Builds a position from a path. Negative values are clamped to 0.
	///
	/// @param path the child indices, followed by the final gap index. Must not
	///             be empty.
	/// @return the position.
	/// @throws IllegalArgumentException if the path is empty.
	public static MDCPosition of(int... path) {
		if (path.length == 0) {
			throw new IllegalArgumentException("A position needs at least one index");
		}
		int[] copy = new int[path.length];
		for (int i = 0; i < path.length; i++) {
			copy[i] = Math.max(0, path[i]);
		}
		return new MDCPosition(copy, true);
	}

	/// @return true if this position is a gap between top items of the text.
	public boolean isTop() {
		return path.length == 1;
	}

	/// @return the length of the path. 1 for a top-level position.
	public int depth() {
		return path.length;
	}

	/// @return a copy of the path.
	public int[] getPath() {
		return path.clone();
	}

	/// @return the child indices leading to the element containing the position
	///         (the path without its last value). Empty for a top-level position.
	public int[] getParentPath() {
		return Arrays.copyOf(path, path.length - 1);
	}

	/// @return the gap index in the element which contains this position.
	public int getLastIndex() {
		return path[path.length - 1];
	}

	/// Index of a top-level position.
	///
	/// @return the index.
	/// @throws UnsupportedOperationException if the position is not top-level.
	public int getIndex() {
		if (!isTop()) {
			throw new UnsupportedOperationException("Only top-level positions are supported here: " + this);
		}
		return path[0];
	}

	/// The top-level position which stands just before the top item containing
	/// this position. For a top-level position, the position itself.
	///
	/// @return a top-level position.
	public MDCPosition getTopLevelPosition() {
		return isTop() ? this : new MDCPosition(path[0]);
	}

	/// Returns the ith position before this one, in the same element. If the
	/// position would fall before the start of the element, it will be the
	/// first position in it.
	///
	/// @param delta the delta between the two positions. Must be positive.
	/// @return ith position before this one.
	public MDCPosition getPreviousPosition(int delta) {
		if (delta < 0) throw new IllegalArgumentException("delta must be positive "+ delta);
		int[] copy = path.clone();
		copy[copy.length - 1] = Math.max(0, copy[copy.length - 1] - delta);
		return new MDCPosition(copy, true);
	}

	public String toString() {
		if (isTop()) {
			return "pos " + path[0];
		}
		StringBuilder b = new StringBuilder("pos (");
		for (int i = 0; i < path.length; i++) {
			if (i > 0) b.append(',');
			b.append(path[i]);
		}
		return b.append(')').toString();
	}

	/// @return true if there is a previous position.
	public boolean hasPrevious() {
		return getLastIndex() > 0;
	}

	public boolean equals(Object obj) {
		return obj instanceof MDCPosition p && Arrays.equals(path, p.path);
	}

	public int hashCode() {
		return Arrays.hashCode(path);
	}

	/// Implementation of the Comparable interface: document order.
	///
	/// Comparing positions which refer to two different texts doesn't mean much.
	/// @see Comparable#compareTo(java.lang.Object)
	public int compareTo(MDCPosition other) {
		return Arrays.compare(path, other.path);
	}
}
