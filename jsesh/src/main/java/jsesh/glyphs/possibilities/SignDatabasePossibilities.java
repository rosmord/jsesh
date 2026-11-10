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

import jsesh.glyphs.fonts.HieroglyphShapeRepository;
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
/// The lists are cached. The variants of a Gardiner code are read from the
/// shape repository the database is built on, whose signs can change (the
/// user font directory is replaced, for instance): the cache is emptied each
/// time this repository signals a change.
public final class SignDatabasePossibilities implements PossibilityRepository {

    private final HieroglyphDatabase database;
    private final Map<String, PossibilitiesList> cache = new HashMap<>();

    /// @param database the sign database.
    /// @param shapes   the shape repository `database` takes its sign codes
    ///                 from; its changes empty the cache. This object listens
    ///                 to it as long as it lives.
    public SignDatabasePossibilities(HieroglyphDatabase database, HieroglyphShapeRepository shapes) {
        this.database = Objects.requireNonNull(database);
        shapes.addListener(e -> clearCache());
    }

    /// Forgets the cached lists: they will be computed again from the database.
    public synchronized void clearCache() {
        cache.clear();
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
