/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.glyphs.possibilities;

import java.util.List;

/// Combines the possibilities of several repositories.
///
/// The possibilities of the first repository come first, then those of the
/// second which are not already proposed, and so on.
public final class CompositePossibilityRepository implements PossibilityRepository {

    private final List<PossibilityRepository> sources;

    /// @param sources the repositories, by order of preference.
    public CompositePossibilityRepository(PossibilityRepository... sources) {
        this(List.of(sources));
    }

    /// @param sources the repositories, by order of preference.
    public CompositePossibilityRepository(List<PossibilityRepository> sources) {
        this.sources = List.copyOf(sources);
    }

    @Override
    public PossibilitiesList getPossibilityListFor(String code) {
        PossibilitiesList result = PossibilitiesList.empty(code);
        for (PossibilityRepository source : sources) {
            result = result.combine(source.getPossibilityListFor(code));
        }
        return result;
    }
}
