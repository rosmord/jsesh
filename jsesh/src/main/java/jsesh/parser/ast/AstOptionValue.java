/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/// The value of an [AstOption]: what follows the `=` in `name=value`, or
/// nothing at all for a bare flag.
///
/// Sealed, so that a `switch` over an option value is checked for
/// exhaustiveness by the compiler.
public sealed interface AstOptionValue {

    /// No value: `bold` in `{bold}`.
    record Flag() implements AstOptionValue {
    }

    /// A textual value: `justification=left`.
    record Text(String value) implements AstOptionValue {
    }

    /// An integer value: `id=3`.
    record Int(int value) implements AstOptionValue {
    }
}
