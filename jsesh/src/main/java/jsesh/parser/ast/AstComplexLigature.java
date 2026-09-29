/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;


/**
 * A ligature (inspired from MacScribe) combining a glyph with one or two
 * "insert zones": {@code t^^w&&t}, {@code ns&&(mSa*Z3)}.
 *
 * @param beforeGroup the group inserted before the main sign, or {@code null} if none.
 * @param hieroglyph the main sign.
 * @param afterGroup the group inserted after the main sign, or {@code null} if none.
 * @author rosmord
 */
public record AstComplexLigature(AstInnerGroup beforeGroup, AstHieroglyph hieroglyph, AstInnerGroup afterGroup)
        implements AstHorizontalListElement {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitComplexLigature(this);
    }
}
