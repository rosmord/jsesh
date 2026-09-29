/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.io.Serializable;

/// A position in a MDC model.
///
/// A position is a plain, immutable value: it does **not** hold a reference to
/// the text it refers to. It is only meaningful relative to a
/// [TopItemList] supplied by the caller. Operations which need the text
/// (element access, bounded navigation, line navigation...) live in
/// [TopItemList], and take the position as argument; for instance
/// [TopItemList#getElementAfter(MDCPosition)] or
/// [TopItemList#getPositionAt(int)], which builds a position clamped to the
/// text's bounds.
/// 
/// The usual way to create a position is to use [TopItemList#getPositionAt(int)], which checks that 
/// the position is within bounds for the text.
///
/// AT THE TIME BEING, THE POSITION IS JUST AN INTEGER, AT TOP LEVEL. THE TEXT BELOW DESCRIBES
/// WHAT THE TEXT POSITION WILL PROBABLY BE.
///
/// A Text position represents a position in a Manuel de Codage model tree. A
/// position does not designate a *model element;* it's  a
/// cursor **between** elements. This clarifies the semantics
/// of element insertion and the like. Positions are short lived, we have
/// decided to make them **read-only**.
///
/// More precisely, a position is **always** a position
/// **in** an element. it designates an element, and a
/// index in this element. For instance, inserting element e1 at position 0 in
/// element e0 would make e1 the first child of e0. The first child of an element
/// stands between positions 0 and 1.
///
/// A position can be used to insert new data, as well as to implement cursors,
/// text selection, etc.
///
/// A position is not a "long term" object. It's quite likely to become invalid
/// or outdated if the text is modified. For long term marking of mutable text
/// position, one should use a [MDCMark].
///
/// @author rosmord
@SuppressWarnings("serial")
public final class MDCPosition implements Serializable, Comparable<MDCPosition> {

	private final int index;

	/// Builds a position.
	///
	/// A negative index is clamped to 0. The position is *not* checked against
	/// the upper bound of any text; use [TopItemList#getPositionAt(int)] to get
	/// a position guaranteed to fall inside a given text.
	///
	/// @param index the index of the position.
	public MDCPosition(int index) {
		this.index = Math.max(0, index);
	}

	/// @return the index.
	public int getIndex() {
		return index;
	}

	/// Returns the ith position before this one, at top level. if the position
	/// would fall before the start of the text, it will be the first position.
	///
	/// @param delta the delta between the two positions. Must be positive.
	/// @return ith position before this one.
	public MDCPosition getPreviousPosition(int delta) {
		if (delta < 0) throw new IllegalArgumentException("delta must be positive "+ delta);
		return new MDCPosition(index - delta);
	}

	public String toString() {
		return "pos " + getIndex();
	}

	/// @return true if there is a previous position.
	public boolean hasPrevious() {
		return index > 0;
	}

	public boolean equals(Object obj) {
		return obj instanceof MDCPosition p && index == p.index;
	}

	public int hashCode() {
		return Integer.hashCode(index);
	}

	/// Implementation of the Comparable interface.
	///
	/// Comparing positions which refer to two different texts doesn't mean much.
	/// @see Comparable#compareTo(java.lang.Object)
	public int compareTo(MDCPosition other) {
		return Integer.compare(index, other.index);
	}

	/**
	 * Returns a couple of <em>orderered</em> text positions.
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static MDCPosition [] getOrdereredPositions(MDCPosition p1, MDCPosition p2) {
		MDCPosition result[];
		if (p1.compareTo(p2) < 0) {
			result= new MDCPosition[]{p1, p2};
		} else {
			result= new MDCPosition[]{p2, p1};
		}
		return result;
	}
}
