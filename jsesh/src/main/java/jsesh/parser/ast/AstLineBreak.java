/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/**
 * A line break.
 *
 * @param skip the vertical skip, as a percentage of line height (100 is a
 * normal skip).
 * @author rosmord
 */
public record AstLineBreak(int skip) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitLineBreak(this);
    }
}
