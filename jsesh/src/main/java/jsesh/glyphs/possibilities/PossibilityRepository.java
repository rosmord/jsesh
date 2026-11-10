/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.glyphs.possibilities;

/// Proposes the possible texts for a code typed by the user.
///
/// There are two ways to extend the proposals:
///
/// - write an independent source, and put it in a [CompositePossibilityRepository];
/// - write a decorator, which transforms the list of a base repository (when the
///   new proposals depend on what the base proposes, as for
///   [jsesh.document.embeddedsigns.EmbeddedSignPossibilityRepository]).
@FunctionalInterface
public interface PossibilityRepository {

    /// The possibilities for a code.
    ///
    /// @param code the code typed by the user.
    /// @return an immutable list, the preferred possibility first; never null
    ///         (empty if there is nothing to propose).
    PossibilitiesList getPossibilityListFor(String code);

}
