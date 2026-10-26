/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/// A kerning operator, `@hkern` or `@vkern`, with an optional option list.
///
/// It stands between two groups, which should be kerned together along its axis.
/// It is written where a group could be: `p*@hkern*q`, `p:@vkern:q`, `p-@hkern-q`.
///
/// @param axis the axis along which the groups are kerned.
/// @param options the options, as written (not used yet; empty if none).
public record AstKerning(KerningAxis axis, AstOptionList options) implements AstInnerGroup {

    /// The axis of a kerning operator.
    public static enum KerningAxis {
        /// `@hkern`
        HORIZONTAL,
        /// `@vkern`
        VERTICAL
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitKerning(this);
    }
}
