/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/// A single `name` or `name=value` entry inside a `{...}` option list.
/// A bare name carries an [AstOptionValue.Flag].
///
/// @author rosmord
public record AstOption(String name, AstOptionValue value) {

    /// A flag option with no value, e.g. `bold` in `{bold}`.
    public static AstOption flag(String name) {
        return new AstOption(name, new AstOptionValue.Flag());
    }

    public static AstOption of(String name, String value) {
        return new AstOption(name, new AstOptionValue.Text(value));
    }

    public static AstOption of(String name, int value) {
        return new AstOption(name, new AstOptionValue.Int(value));
    }
}
