/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;


/**
 * A parenthesized group, embedding a {@link AstBasicItemList} (which may
 * itself contain cadrats) as a single element of an {@link AstHBox}.
 *
 * @author rosmord
 */
public record AstSubCadrat(AstBasicItemList content) implements AstInnerGroup {

    /**
     * Hand-builds a sub-cadrat from its content, for tests.
     */
    public static AstSubCadrat of(AstNode... content) {
        return new AstSubCadrat(AstBasicItemList.of(content));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitSubCadrat(this);
    }
}
