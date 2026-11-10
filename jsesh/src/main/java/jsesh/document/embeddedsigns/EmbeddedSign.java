/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document.embeddedsigns;

import java.util.Objects;

/// A sign drawn by the user and stored in the document itself.
///
/// An embedded sign belongs to one text: its drawing travels in the document
/// header, not in the user's sign folder. See [EmbeddedSignList].
///
/// @param code        the code used in the MdC text, `SIGN1`, `SIGN2`...
/// @param svg         the drawing, as the normalized SVG written by
///                    [jsesh.glyphs.shape.ShapeChar#exportToSVG(java.io.OutputStream, String)]
///                    (one path at JSesh scale, plus ligature zones).
/// @param variantOf   the Gardiner code this sign is a variant of, or `null`.
///                    When set, the sign is proposed in the possibility list of
///                    that code.
/// @param description a free description; never `null`, possibly empty.
public record EmbeddedSign(String code, String svg, String variantOf, String description) {

    public EmbeddedSign {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(svg, "svg");
        if (!EmbeddedSignList.isEmbeddedSignCode(code)) {
            throw new IllegalArgumentException("Not an embedded sign code: " + code);
        }
        variantOf = normalizeVariantOf(variantOf);
        description = description == null ? "" : description;
    }

    /// A copy of this sign with another "variant of" code.
    public EmbeddedSign withVariantOf(String newVariantOf) {
        return new EmbeddedSign(code, svg, newVariantOf, description);
    }

    /// A copy of this sign with another description.
    public EmbeddedSign withDescription(String newDescription) {
        return new EmbeddedSign(code, svg, variantOf, newDescription);
    }

    /// Does this sign declare a "variant of" code.
    public boolean hasVariantOf() {
        return variantOf != null;
    }

    /// Blank and space-containing values make no sense as a sign code: blank
    /// gives `null`, and surrounding spaces are removed.
    private static String normalizeVariantOf(String variantOf) {
        if (variantOf == null) {
            return null;
        }
        String trimmed = variantOf.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("A sign code can't contain spaces: " + variantOf);
        }
        return trimmed;
    }
}
