/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document.embeddedsigns;

import java.util.Objects;

import jsesh.glyphs.signdata.PossibilitiesList;
import jsesh.glyphs.signdata.Possibility;
import jsesh.glyphs.signdata.PossibilityRepository;
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
        PossibilitiesList result = baseList == null ? new PossibilitiesList(code) : new PossibilitiesList(baseList);
        if (list.contains(code)) {
            result.addSign(code);
        }
        ManuelDeCodage mdc = ManuelDeCodage.getInstance();
        for (EmbeddedSign sign : list.asList()) {
            if (sign.hasVariantOf() && proposes(result, code, sign.variantOf(), mdc)) {
                result.addSign(sign.code());
            }
        }
        return result;
    }

    private static boolean proposes(PossibilitiesList list, String typed, String variantOf, ManuelDeCodage mdc) {
        if (typed.equalsIgnoreCase(variantOf) || mdc.equalsCanonical(typed, variantOf)) {
            return true;
        }
        for (Possibility p : list.asList()) {
            if (p.isSingleSign() && mdc.equalsCanonical(p.getCode(), variantOf)) {
                return true;
            }
        }
        return false;
    }
}
