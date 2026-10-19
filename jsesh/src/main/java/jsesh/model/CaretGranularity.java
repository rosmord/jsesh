/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

/// The "grain" of cursor movements in a text.
///
/// @see PositionNavigator
public enum CaretGranularity {
	/// The cursor stands between top-level items (quadrats, breaks...) only.
	QUADRAT,
	/// The cursor may also stand between the elements *inside* a quadrat:
	/// between its rows, between the elements of a row, between the signs of a
	/// ligature...
	INNER
}
