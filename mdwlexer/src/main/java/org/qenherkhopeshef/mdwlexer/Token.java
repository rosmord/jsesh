package org.qenherkhopeshef.mdwlexer;

import java.util.Objects;

/// A scanned token: its type, the exact text matched, and where it starts.
/// @param type the token type recognized
/// @param text the matched text, verbatim
/// @param position the number of code points consumed before this token, i.e. where it starts
public record Token<T extends Enum<T>>(T type, String text, int position) {
    public Token {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(text, "text");
        if (position < 0) {
            throw new IllegalArgumentException("position must be non-negative: " + position);
        }
    }
}
