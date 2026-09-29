/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.lexer.symbols;

/// The start of a "MacScribe"-style old cartouche, e.g. `<Sb`: `code` is the
/// cartouche kind (`'c'` plain, or one of `S F H G`), `part` is which piece of
/// it this is (`'a'` the whole thing, or one of `b m e` for begin/middle/end).
public record OldCartoucheStart(char code, char part) {
}
