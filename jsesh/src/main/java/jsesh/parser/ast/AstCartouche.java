/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;
import jsesh.sharedvocabulary.CartouchePart;
import jsesh.sharedvocabulary.CartoucheType;


/**
 * A cartouche, serekh, hout-sign or castle.
 *
 * @param type the kind of cartouche.
 * @param startPart how the first extremity of the cartouche should be drawn.
 * @param endPart how the last extremity of the cartouche should be drawn.
 * @param options the raw options given between brackets after the cartouche,
 * e.g. {@code <-ra->[fill]} (possibly none).
 * @author rosmord
 */
public record AstCartouche(CartoucheType type, CartouchePart startPart, CartouchePart endPart, AstBasicItemList content,
        AstOptionList options)
        implements AstInnerGroup {

    /// A cartouche without options.
    public AstCartouche(CartoucheType type, CartouchePart startPart, CartouchePart endPart, AstBasicItemList content) {
        this(type, startPart, endPart, content, AstOptionList.of());
    }

    /**
     * A plain, fully-drawn cartouche ({@code <...>}: both extremities drawn,
     * as opposed to a partial cartouche). For tests.
     */
    public static AstCartouche of(AstBasicItem... content) {
        return new AstCartouche(CartoucheType.CARTOUCHE, CartouchePart.FIRST, CartouchePart.SECOND, AstBasicItemList.of(content));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitCartouche(this);
    }
}
