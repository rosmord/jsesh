package org.qenherkhopeshef.mdwlexer;

/// Thrown by [Lexer#nextToken()] when no rule of the [Lexicon] matches the input at
/// the current position.
public class LexicalException extends RuntimeException {
    private final int position;

    public LexicalException(String message, int position) {
        super(message);
        this.position = position;
    }

    /// The number of code points consumed before the position where scanning failed.
    public int position() {
        return position;
    }
}
