/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/**
 * A single modifier attached to a hieroglyph (e.g. {@code \det}, {@code \col50}).
 *
 * @param name the modifier's name.
 * @param value the modifier's integer value, or {@code null} if it has none.
 * @author rosmord
 * @see jsesh.model.Modifier
 */
public record AstModifier(String name, Integer value) implements AstNode {

    /**
     * A modifier with no value (e.g. {@code \det}), for tests.
     */
    public static AstModifier flag(String name) {
        return new AstModifier(name, null);
    }

    /**
     * A modifier with an integer value (e.g. {@code \col50}), for tests.
     */
    public static AstModifier of(String name, int value) {
        return new AstModifier(name, value);
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitModifier(this);
    }
}
