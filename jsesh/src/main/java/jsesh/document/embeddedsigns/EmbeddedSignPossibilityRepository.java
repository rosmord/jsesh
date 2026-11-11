/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document.embeddedsigns;

import java.util.Objects;

import jsesh.glyphs.possibilities.PossibilitiesList;
import jsesh.glyphs.possibilities.Possibility;
import jsesh.glyphs.possibilities.PossibilityRepository;
import jsesh.signcodes.ManuelDeCodage;

/// Adds a document's embedded signs to the possibility lists of another
/// repository.
///
/// An embedded sign whose "variant of" code is `X` is proposed:
///
/// - when the user types `X` itself;
/// - when the list for what the user typed already proposes `X` (so a sign
///   declared a variant of `Y5` also appears for `mn`).
///
/// When the user types an embedded sign code (`sign1`), it is proposed too.
/// The lists are recomputed at each call (documents have few such signs).
///
/// This is a decorator rather than an independent source, as its proposals
/// depend on what the base repository proposes.
public final class EmbeddedSignPossibilityRepository implements PossibilityRepository {

    private final PossibilityRepository base;
    private final EmbeddedSignShapeRepository signs;

    /// @param base  the repository giving the usual possibilities.
    /// @param signs the repository of the document signs; the possibilities
    ///              follow its current binding.
    public EmbeddedSignPossibilityRepository(PossibilityRepository base, EmbeddedSignShapeRepository signs) {
        this.base = Objects.requireNonNull(base);
        this.signs = Objects.requireNonNull(signs);
    }

    @Override
    public PossibilitiesList getPossibilityListFor(String code) {
        PossibilitiesList baseList = base.getPossibilityListFor(code);
        EmbeddedSignList list = signs.getSigns();
        if (list.isEmpty()) {
            return baseList;
        }
        PossibilitiesList result = baseList;
        if (list.contains(code)) {
            result = withSign(result, code);
        }
        ManuelDeCodage mdc = ManuelDeCodage.getInstance();
        // Checked against the list being built: a variant of an embedded sign
        // already proposed is proposed too.
        for (EmbeddedSign sign : list.asList()) {
            if (sign.hasVariantOf() && proposes(result, code, sign.variantOf(), mdc)) {
                result = withSign(result, sign.code());
            }
        }
        return result;
    }

    private static PossibilitiesList withSign(PossibilitiesList list, String signCode) {
        return PossibilitiesList.builder(list.code()).addAll(list).addSign(signCode).build();
    }

    private static boolean proposes(PossibilitiesList list, String typed, String variantOf, ManuelDeCodage mdc) {
        if (typed.equalsIgnoreCase(variantOf) || mdc.equalsCanonical(typed, variantOf)) {
            return true;
        }
        for (Possibility p : list.asList()) {
            if (p instanceof Possibility.Sign sign && mdc.equalsCanonical(sign.code(), variantOf)) {
                return true;
            }
        }
        return false;
    }
}
