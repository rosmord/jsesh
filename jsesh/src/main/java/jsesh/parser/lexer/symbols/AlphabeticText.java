/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.lexer.symbols;

/// A run of free-form alphabetic text, e.g. `+ftranslation`: `code` says which kind
/// of text this is (the letter right after `+`), `text` is the content with its
/// `\+` escapes resolved back into plain `+`.
public record AlphabeticText(char code, String text) {
}
