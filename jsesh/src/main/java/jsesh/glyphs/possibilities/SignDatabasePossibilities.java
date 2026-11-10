/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.glyphs.possibilities;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import jsesh.glyphs.signdata.HieroglyphDatabase;
import jsesh.glyphs.signdata.SignDescriptionConstants;
import jsesh.signcodes.GardinerCode;

/// Proposes the signs of a [HieroglyphDatabase] for a code.
///
/// - For a Gardiner code (`a1`, `A1`...), the sign and its variants
///   ([HieroglyphDatabase#getSuitableSignsForCode(String)]).
/// - Otherwise, the signs having this value at the keyboard
///   ([HieroglyphDatabase#getPossibilityFor(String, String)] with
///   [SignDescriptionConstants#KEYBOARD]).
///
/// The lists are cached: the database doesn't change once built.
public final class SignDatabasePossibilities implements PossibilityRepository {

    private final HieroglyphDatabase database;
    private final Map<String, PossibilitiesList> cache = new HashMap<>();

    public SignDatabasePossibilities(HieroglyphDatabase database) {
        this.database = Objects.requireNonNull(database);
    }

    @Override
    public synchronized PossibilitiesList getPossibilityListFor(String code) {
        return cache.computeIfAbsent(code, this::compute);
    }

    private PossibilitiesList compute(String code) {
        List<String> signs;
        if (GardinerCode.isWellFormedCodeIgnoreCase(code.toUpperCase(Locale.ENGLISH))) {
            signs = database.getSuitableSignsForCode(code);
        } else {
            signs = database.getPossibilityFor(code, SignDescriptionConstants.KEYBOARD);
        }
        return PossibilitiesList.ofSigns(code, signs);
    }
}
