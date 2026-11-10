/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.glossary;

import java.util.Objects;

import jsesh.glyphs.possibilities.PossibilitiesList;
import jsesh.glyphs.possibilities.PossibilityRepository;

/// Proposes the glossary entries whose key is the typed code, as Manuel de
/// Codage texts.
///
/// The glossary is read at each call, so its changes are taken into account
/// at once.
public final class GlossaryPossibilities implements PossibilityRepository {

	private final Glossary glossary;

	public GlossaryPossibilities(Glossary glossary) {
		this.glossary = Objects.requireNonNull(glossary);
	}

	@Override
	public PossibilitiesList getPossibilityListFor(String code) {
		PossibilitiesList.Builder builder = PossibilitiesList.builder(code);
		for (GlossaryEntry entry : glossary.get(code)) {
			builder.addMdCText(entry.getMdc());
		}
		return builder.build();
	}
}
