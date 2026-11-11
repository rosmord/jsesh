/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.glyphs.possibilities;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import jsesh.glyphs.signdata.HieroglyphDatabase;
import jsesh.glyphs.signdata.SignDescriptionConstants;
import jsesh.signcodes.GardinerCode;

/// Proposes the signs of a [HieroglyphDatabase] for a code.
///
/// - For a Gardiner code (`a1`, `A1`...), the sign and its variants
///   ([HieroglyphDatabase#getSuitableSignsForCode(String)]). These are
///   computed at each call: they are read from the sign codes of the fonts,
///   which change when the user adds signs to their font directory.
/// - Otherwise, the signs having this value at the keyboard
///   ([HieroglyphDatabase#getSignsForValue(String, String)] with
///   [SignDescriptionConstants#KEYBOARD]). These come from the sign
///   descriptions, which don't change once the database is built, so they are
///   cached.
public final class SignDatabasePossibilities implements PossibilityRepository {

    private final HieroglyphDatabase database;
    /// The lists for transliterations only.
    private final Map<String, PossibilitiesList> transliterationCache = new HashMap<>();

    public SignDatabasePossibilities(HieroglyphDatabase database) {
        this.database = Objects.requireNonNull(database);
    }

    @Override
    public PossibilitiesList getPossibilityListFor(String code) {
        if (GardinerCode.isWellFormedCodeIgnoreCase(code.toUpperCase(Locale.ENGLISH))) {
            return PossibilitiesList.ofSigns(code, database.getSuitableSignsForCode(code));
        }
        synchronized (transliterationCache) {
            return transliterationCache.computeIfAbsent(code, c -> PossibilitiesList.ofSigns(c,
                    database.getSignsForValue(c, SignDescriptionConstants.KEYBOARD)));
        }
    }
}
