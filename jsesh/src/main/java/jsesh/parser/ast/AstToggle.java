/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

import jsesh.parser.lexer.symbols.ToggleType;

/**
 * A toggle marker (e.g. {@code $r}, {@code $b}, {@code #}), either at top
 * level or inside a basic item list.
 * <p>Unlike {@link jsesh.mdcreader.AstModelBuilder}, which folds toggles into
 * hidden red/shaded state on the items that follow, this AST keeps each
 * toggle as its own node, in the position where it was parsed.
 *
 * @author rosmord
 */
public record AstToggle(ToggleType toggleType) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitToggle(this);
    }
}
