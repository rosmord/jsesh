/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/**
 * A horizontal rule.
 *
 * @param lineType 'l' for a single line, 'L' for a double line.
 * @param startPos start of the rule, in tab units from the left edge of the page.
 * @param endPos end of the rule, in tab units.
 * @author rosmord
 */
public record AstHRule(char lineType, int startPos, int endPos) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitHRule(this);
    }
}
