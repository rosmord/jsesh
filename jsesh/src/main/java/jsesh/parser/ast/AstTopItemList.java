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
 * A list of top-level items, as they appear directly in a text line: cadrats,
 * cartouches, rules, breaks, toggles, zone markers, tabbing, and so on.
 * <p>Built incrementally, via left-recursive grammar rules of unbounded
 * arity, hence the {@link #of} factory rather than a fixed-arity constructor
 * call at each parse site.
 *
 * @author rosmord
 */
public record AstTopItemList(List<AstNode> items) implements AstNode {

    public AstTopItemList {
        items = List.copyOf(items);
    }

    /**
     * Hand-builds a top item list from its items, for tests.
     */
    public static AstTopItemList of(AstNode... items) {
        return new AstTopItemList(List.of(items));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitTopItemList(this);
    }
}
