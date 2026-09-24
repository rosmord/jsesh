package org.qenherkhopeshef.mdwlexer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.qenherkhopeshef.mdwlexer.automata.DeterministicFiniteAutomaton;
import org.qenherkhopeshef.mdwlexer.automata.State;

/// A stateful scanner, based on an immutable [Lexicon] DFA.
/// 
/// Runs of whitespace (as defined by the [Lexicon] that created this lexer) are
/// skipped automatically before each token; disable this with [#setSkipWhitespace(boolean)].
///
/// Not thread-safe: a [Lexer] holds the scanning position in its [Reader] and is
/// meant to be driven by a single thread.
public final class Lexer<T extends Enum<T>> {
    private final Lexicon<T> lexicon;
    private final CodePointReader input;
    private boolean skipWhitespace;
    private int position;
    private int line = 1;
    private int column;
    /// Whether the last code point consumed was `\r`, so that a following `\n` doesn't start another line.
    private boolean afterCarriageReturn;

    Lexer(Lexicon<T> lexicon, Reader reader) {
        this.lexicon = Objects.requireNonNull(lexicon, "lexicon");
        this.input = new CodePointReader(Objects.requireNonNull(reader, "reader"));
        this.skipWhitespace = lexicon.whitespaceAutomaton().isPresent();
    }

    /// Whether runs of whitespace are currently skipped automatically between tokens.
    /// @return true if whitespace is skipped, false if every character must be matched by a rule
    public boolean isSkippingWhitespace() {
        return skipWhitespace;
    }

    /// Enables or disables automatic whitespace skipping. Has no effect if the [Lexicon]
    /// was built with [LexiconBuilder#noWhitespaceSkipping()]: there is then nothing to
    /// skip, and every character must be matched by a rule.
    public void setSkipWhitespace(boolean skipWhitespace) {
        this.skipWhitespace = skipWhitespace && lexicon.whitespaceAutomaton().isPresent();
    }

    /// The number of code points consumed so far, i.e. where the next token starts.
    public int position() {
        return position;
    }

    /// The line where the next token starts, counted from 1.
    ///
    /// Lines are separated by `\n`, `\r\n` or a lone `\r`, each counting as a single line break.
    public int line() {
        return line;
    }

    /// The column where the next token starts, in code points from the start of its line, counted from 0.
    public int column() {
        return column;
    }

    /// Reads the next token, skipping leading whitespace first unless disabled.
    /// @return the next token, or [Optional#empty()] at end of input
    /// @throws LexicalException if no rule matches the input at the current position
    /// @throws IOException if the underlying reader fails
    public Optional<Token<T>> nextToken() throws IOException {
        if (skipWhitespace) {
            skipWhitespace();
        }
        int start = position;
        int startLine = line;
        int startColumn = column;
        Optional<Match<T>> match = scan(lexicon.automaton(), lexicon.classifier());
        if (match.isEmpty()) {
            int lookahead = input.read();
            if (lookahead == -1) {
                return Optional.empty();
            }
            input.pushBack(lookahead);
            throw new LexicalException("No rule matches the input", position, line, column);
        }
        Match<T> found = match.get();
        if (found.length() == 0) {
            throw new LexicalException("Rule for " + found.tokenType() + " matches the empty string", position, line, column);
        }
        advance(found.text());
        return Optional.of(new Token<>(found.tokenType(), found.text(), start, startLine, startColumn));
    }

    /// Moves [#position()], [#line()] and [#column()] past `consumed`.
    private void advance(String consumed) {
        consumed.codePoints().forEach(codePoint -> {
            position++;
            if (codePoint == '\r' || (codePoint == '\n' && !afterCarriageReturn)) {
                line++;
                column = 0;
            } else if (codePoint != '\n') { // a '\n' here ends a \r\n pair, already counted
                column++;
            }
            afterCarriageReturn = codePoint == '\r';
        });
    }

    private void skipWhitespace() throws IOException {
        Optional<DeterministicFiniteAutomaton<Integer, Lexicon.WhitespaceMarker>> whitespaceAutomaton =
                lexicon.whitespaceAutomaton();
        if (whitespaceAutomaton.isEmpty()) {
            return;
        }
        CharacterClassifier whitespaceClassifier = lexicon.whitespaceClassifier().orElseThrow();
        Optional<Match<Lexicon.WhitespaceMarker>> match;
        do {
            match = scan(whitespaceAutomaton.get(), whitespaceClassifier);
            match.ifPresent(found -> advance(found.text()));
        } while (match.isPresent() && match.get().length() > 0);
    }

    /// Consumes the longest prefix of the input matched by `automaton`, if any.
    private <X> Optional<Match<X>> scan(
            DeterministicFiniteAutomaton<Integer, X> automaton, CharacterClassifier classifier) throws IOException {
        List<Integer> codePoints = new ArrayList<>();
        State state = automaton.initialState();
        int acceptedCount = -1;
        X acceptedType = null;

        while (true) {
            int codePoint = input.read();
            if (codePoint == -1) {
                break;
            }
            Optional<State> next = automaton.transition(state, classifier.classOf(codePoint));
            if (next.isEmpty()) {
                input.pushBack(codePoint);
                break;
            }
            state = next.get();
            codePoints.add(codePoint);
            Optional<X> tokenType = automaton.acceptingTokenType(state);
            if (tokenType.isPresent()) {
                acceptedCount = codePoints.size();
                acceptedType = tokenType.get();
            }
        }

        int keep = Math.max(acceptedCount, 0);
        for (int i = codePoints.size() - 1; i >= keep; i--) {
            input.pushBack(codePoints.get(i));
        }
        if (acceptedCount < 0) {
            return Optional.empty();
        }

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < acceptedCount; i++) {
            text.appendCodePoint(codePoints.get(i));
        }
        return Optional.of(new Match<>(acceptedType, text.toString(), acceptedCount));
    }

    /// A match found by [#scan]: the token type, the matched text, and its length in code points.
    private record Match<X>(X tokenType, String text, int length) {
    }
}
