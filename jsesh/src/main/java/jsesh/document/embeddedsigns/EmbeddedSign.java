/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document.embeddedsigns;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/// A sign drawn by the user and stored in the document itself.
///
/// An embedded sign belongs to one text: its drawing travels in the document
/// header, not in the user's sign folder. See [EmbeddedSignList].
///
/// @param code        the code used in the MdC text, `sign1`, `sign2`...
/// @param svg         the drawing: the user's SVG file, byte for byte. It is
///                    read as a sign of the user's sign folder would be (see
///                    [EmbeddedSignShapes]). The array is copied: the record
///                    is immutable.
/// @param variantOf   the Gardiner code this sign is a variant of, or `null`.
///                    When set, the sign is proposed in the possibility list of
///                    that code.
/// @param description a free description; never `null`, possibly empty.
///
/// Serializable, to travel on the clipboard with copied text.
public record EmbeddedSign(String code, byte[] svg, String variantOf, String description) implements Serializable {

    public EmbeddedSign {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(svg, "svg");
        if (!EmbeddedSignList.isEmbeddedSignCode(code)) {
            throw new IllegalArgumentException("Not an embedded sign code: " + code);
        }
        svg = svg.clone();
        variantOf = normalizeVariantOf(variantOf);
        description = description == null ? "" : description;
    }

    /// A copy of the SVG data.
    @Override
    public byte[] svg() {
        return svg.clone();
    }

    /// Is the drawing of this sign the same as `data`, byte for byte.
    public boolean hasSameSvg(byte[] data) {
        return Arrays.equals(svg, data);
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

    @Override
    public boolean equals(Object o) {
        return o instanceof EmbeddedSign s && code.equals(s.code) && Arrays.equals(svg, s.svg)
                && Objects.equals(variantOf, s.variantOf) && description.equals(s.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, Arrays.hashCode(svg), variantOf, description);
    }

    @Override
    public String toString() {
        return "EmbeddedSign[" + code + ", " + svg.length + " bytes of SVG, variantOf=" + variantOf
                + ", description=" + description + "]";
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
