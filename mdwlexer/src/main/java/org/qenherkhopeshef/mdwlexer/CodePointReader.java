package org.qenherkhopeshef.mdwlexer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;

/// Decodes a [Reader]'s UTF-16 chars into Unicode code points, with support for pushing
/// code points back so a lexer can backtrack after overshooting during maximal munch.
final class CodePointReader {
    private final Reader reader;
    private final Deque<Integer> pushedBack = new ArrayDeque<>();

    CodePointReader(Reader reader) {
        this.reader = reader;
    }

    /// The next code point, or -1 at end of input.
    int read() throws IOException {
        if (!pushedBack.isEmpty()) {
            return pushedBack.pop();
        }
        int first = reader.read();
        if (first == -1) {
            return -1;
        }
        if (Character.isHighSurrogate((char) first)) {
            int second = reader.read();
            if (second != -1 && Character.isLowSurrogate((char) second)) {
                return Character.toCodePoint((char) first, (char) second);
            }
            if (second != -1) {
                pushedBack.push(second);
            }
        }
        return first;
    }

    /// Pushes `codePoint` back, so the next [#read()] returns it again.
    void pushBack(int codePoint) {
        pushedBack.push(codePoint);
    }
}
