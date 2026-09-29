/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;


/**
 * A cartouche, serekh, hout-sign or castle.
 *
 * @param type the kind of cartouche.
 * @param startPart how the first extremity of the cartouche should be drawn.
 * @param endPart how the last extremity of the cartouche should be drawn.
 * @author rosmord
 */
public record AstCartouche(CartoucheType type, CartouchePart startPart, CartouchePart endPart, AstBasicItemList content)
        implements AstInnerGroup {

    /**
     * A plain, fully-drawn cartouche ({@code <...>}: both extremities drawn,
     * as opposed to a partial cartouche). For tests.
     */
    public static AstCartouche of(AstNode... content) {
        return new AstCartouche(CartoucheType.CARTOUCHE, CartouchePart.FIRST, CartouchePart.SECOND, AstBasicItemList.of(content));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitCartouche(this);
    }
}
