/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
/// Possibility lists: what is proposed when the user types a code
/// (`m` → G17, U1, Aa15...; `a1` → A1 and its variants; glossary words).
///
/// - [jsesh.glyphs.possibilities.PossibilitiesList] is an immutable list of
///   [jsesh.glyphs.possibilities.Possibility] (single signs or Manuel de
///   Codage texts). The currently selected possibility is not stored in it: the
///   editor keeps it.
/// - [jsesh.glyphs.possibilities.PossibilityRepository] gives the list for a
///   code. Independent sources are combined, by order of preference, with
///   [jsesh.glyphs.possibilities.CompositePossibilityRepository]:
///   [jsesh.glyphs.possibilities.SignDatabasePossibilities] (the sign
///   database) and [jsesh.glossary.GlossaryPossibilities] (the user glossary).
///   A source whose proposals depend on what the others propose is written as a
///   decorator instead ([jsesh.document.embeddedsigns.EmbeddedSignPossibilityRepository]).
///
/// To add a source: implement `PossibilityRepository` and add it to the
/// composite built by [jsesh.defaults.HieroglyphResourcesBuilder].
package jsesh.glyphs.possibilities;
