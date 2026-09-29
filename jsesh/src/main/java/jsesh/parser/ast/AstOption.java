/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/**
 * A single {@code name} or {@code name=value} entry inside a {@code {...}}
 * option list. {@code value} is an {@code Integer}, a {@code String}, or
 * {@code Boolean.TRUE} for a flag with no value.
 *
 * @author rosmord
 */
public record AstOption(String name, Object value) {

    /**
     * A flag option with no value, e.g. {@code bold} in {@code {bold}}.
     */
    public static AstOption flag(String name) {
        return new AstOption(name, Boolean.TRUE);
    }

    public static AstOption of(String name, String value) {
        return new AstOption(name, value);
    }

    public static AstOption of(String name, int value) {
        return new AstOption(name, value);
    }
}
