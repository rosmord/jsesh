/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

/// A container inside a quadrat, which can be edited at its inner positions
/// (see [InnerEdit]).
///
/// Each kind of container knows how a new sign is combined with its
/// neighbours, and how it cleans up after a removal. This way, no separator
/// is needed to tell where a sign goes.
///
/// Implemented by [Cadrat], [HBox] and [Ligature]. Other containers
/// ([ComplexLigature], [AbsoluteGroup]) are not editable this way yet.
public interface InnerEditable {

	/// @return the number of children of the container.
	int getNumberOfChildren();

	/// Inserts a sign at a gap of this container. The container decides how
	/// the sign is combined with its neighbours: in a [Cadrat], it becomes a
	/// new row; in a [HBox] or a [Ligature], a new element.
	///
	/// @param gap the gap, between 0 and [#getNumberOfChildren()].
	/// @param sign the sign, used as is.
	void insertSignAt(int gap, Hieroglyph sign);

	/// Should this container be removed from its parent when its last child
	/// is removed?
	///
	/// @return true if an empty container has no reason to exist.
	boolean disappearsWhenEmpty();

	/// Replaces one of the elements of this container by its simplified form
	/// (see [HorizontalListElement#simplified()]), if it has one.
	///
	/// The default does nothing: only containers of [HorizontalListElement]s
	/// have something to simplify.
	///
	/// @param index the index of the element.
	default void simplifyElementAt(int index) {
	}
}
