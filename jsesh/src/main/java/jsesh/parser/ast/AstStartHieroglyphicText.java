/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/**
 * The "+s" marker, toggling between latin and hieroglyphic text. Kept purely
 * so the AST is a faithful record of the source; {@link jsesh.mdcreader.AstModelBuilder}
 * discards this construct entirely when building the model.
 *
 * @author rosmord
 */
public record AstStartHieroglyphicText() implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitStartHieroglyphicText(this);
    }
}
