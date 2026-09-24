/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer;

/// Thrown by [Lexer#nextToken()] when no rule of the [Lexicon] matches the input at
/// the current position.
public class LexicalException extends RuntimeException {
    private final int position;
    private final int line;
    private final int column;

    public LexicalException(String message, int position, int line, int column) {
        super(message + " (line " + line + ", column " + column + ")");
        this.position = position;
        this.line = line;
        this.column = column;
    }

    /// The number of code points consumed before the position where scanning failed.
    public int position() {
        return position;
    }

    /// The line where scanning failed, counted from 1.
    public int line() {
        return line;
    }

    /// The column where scanning failed, in code points from the start of the line, counted from 0.
    public int column() {
        return column;
    }
}
