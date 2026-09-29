/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;


/**
 * The root of a parsed Manuel de Codage text.
 *
 * @author rosmord
 */
public record AstDocument(AstTopItemList topItems) implements AstNode {

    /**
     * Hand-builds a document from its top-level items, for tests.
     */
    public static AstDocument of(AstNode... topItems) {
        return new AstDocument(AstTopItemList.of(topItems));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitDocument(this);
    }
}
