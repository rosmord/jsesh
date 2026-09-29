/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.lexer.symbols;

/// A horizontal rule, e.g. `{l12,34}`: `type` is `'l'` (thin) or `'L'`
/// (wide), `start`/`end` are absolute positions in glyph units.
public record HRule(char type, int start, int end) {
}
