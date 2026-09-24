/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer;

import java.util.Objects;

/// A scanned token: its type, the exact text matched, and where it starts.
///
/// Lines are separated by `\n`, `\r\n` or a lone `\r`, each counting as a single line break.
/// @param type the token type recognized
/// @param text the matched text, verbatim
/// @param position the number of code points consumed before this token, i.e. where it starts
/// @param line the line where the token starts, counted from 1
/// @param column the number of code points between the start of that line and the token, counted from 0
public record Token<T extends Enum<T>>(T type, String text, int position, int line, int column) {
    public Token {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(text, "text");
        if (position < 0) {
            throw new IllegalArgumentException("position must be non-negative: " + position);
        }
        if (line < 1) {
            throw new IllegalArgumentException("line must be positive: " + line);
        }
        if (column < 0) {
            throw new IllegalArgumentException("column must be non-negative: " + column);
        }
    }
}
