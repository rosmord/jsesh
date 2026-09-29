/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.lexer.symbols;

/// The seven kinds of philological parenthesis pair (e.g. `[[...]]` for erased signs)
/// recognized by the `BEGIN_PHIL`/`END_PHIL` rules.
public enum PhilologyKind {
    ERASED_SIGNS,
    EDITOR_SUPERFLUOUS,
    PREVIOUSLY_READABLE,
    SCRIBE_ADDITION,
    EDITOR_ADDITION,
    MINOR_ADDITION,
    DUBIOUS
}
