/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/// Two groups drawn one over the other (`%stack(g1,g2)`).
///
/// This is the group counterpart of [AstOverwrite] (`a##b`), which only accepts
/// two hieroglyphs. Both arguments can be any group of items, including
/// other stacks.
///
/// @param firstItems the first argument (drawn first, so below the second).
/// @param secondItems the second argument.
public record AstStack(AstBasicItemList firstItems, AstBasicItemList secondItems) implements AstInnerGroup {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitStack(this);
    }
}
