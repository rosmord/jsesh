package org.qenherkhopeshef.mdwlexer;

import java.io.Reader;
import java.util.Optional;
import java.util.Set;

import org.qenherkhopeshef.mdwlexer.automata.DeterministicFiniteAutomaton;

/// An immutable, compiled lexicon: the shared DFA recognizing every rule's token type, the
/// classifier mapping code points to that DFA's input alphabet, and (unless disabled) a second,
/// independent DFA/classifier pair recognizing runs of whitespace to skip automatically.
///
/// Built with [LexiconBuilder]. Use [#newLexer(Reader)] to scan actual input.
public final class Lexicon<T extends Enum<T>> {
    /// Marker token type for the standalone whitespace DFA; never seen outside this package.
    enum WhitespaceMarker {
        WHITESPACE
    }

    private final DeterministicFiniteAutomaton<Integer, T> automaton;
    private final CharacterClassifier classifier;
    private final Set<T> tokenTypes;
    private final DeterministicFiniteAutomaton<Integer, WhitespaceMarker> whitespaceAutomaton;
    private final CharacterClassifier whitespaceClassifier;

    Lexicon(
            DeterministicFiniteAutomaton<Integer, T> automaton,
            CharacterClassifier classifier,
            Set<T> tokenTypes,
            DeterministicFiniteAutomaton<Integer, WhitespaceMarker> whitespaceAutomaton,
            CharacterClassifier whitespaceClassifier) {
        this.automaton = automaton;
        this.classifier = classifier;
        this.tokenTypes = tokenTypes;
        this.whitespaceAutomaton = whitespaceAutomaton;
        this.whitespaceClassifier = whitespaceClassifier;
    }

    /// Every token type this lexicon was built to recognize.
    public Set<T> tokenTypes() {
        return tokenTypes;
    }

    /// The shared DFA recognizing every rule, for introspection (e.g. `toMermaid()`).
    public DeterministicFiniteAutomaton<Integer, T> automaton() {
        return automaton;
    }

    /// The classifier mapping code points to [#automaton()]'s input alphabet.
    public CharacterClassifier classifier() {
        return classifier;
    }

    Optional<DeterministicFiniteAutomaton<Integer, WhitespaceMarker>> whitespaceAutomaton() {
        return Optional.ofNullable(whitespaceAutomaton);
    }

    Optional<CharacterClassifier> whitespaceClassifier() {
        return Optional.ofNullable(whitespaceClassifier);
    }

    /// Creates a new stateful lexer, scanning `reader` against this lexicon.
    public Lexer<T> newLexer(Reader reader) {
        return new Lexer<>(this, reader);
    }
}
