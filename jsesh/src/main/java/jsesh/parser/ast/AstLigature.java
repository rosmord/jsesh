/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

import java.util.List;


/**
 * A ligature: hieroglyphs joined with "&amp;" (e.g. {@code p&t&n}).
 * <p>Built incrementally, via a left-recursive grammar rule of unbounded
 * arity, hence the {@link #of} factory rather than a fixed-arity constructor
 * call at each parse site.
 *
 * @author rosmord
 */
public record AstLigature(List<AstHieroglyph> hieroglyphs) implements AstInnerGroup {

    public AstLigature {
        hieroglyphs = List.copyOf(hieroglyphs);
    }

    /**
     * Hand-builds a ligature from its hieroglyphs, for tests.
     */
    public static AstLigature of(AstHieroglyph... hieroglyphs) {
        return new AstLigature(List.of(hieroglyphs));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitLigature(this);
    }
}
