/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer;

import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.qenherkhopeshef.mdwlexer.automata.DeterministicFiniteAutomaton;

/// An immutable, compiled lexicon, made of one or more named *lexical states* (see
/// [LexiconBuilder]). Each state has its own DFA recognizing its rules' token types, the
/// classifier mapping code points to that DFA's input alphabet, the state changes its tokens
/// trigger, and (unless disabled) a second, independent DFA/classifier pair recognizing runs
/// of whitespace to skip automatically.
///
/// A lexicon built without calling [LexiconBuilder#state(String)] has a single state,
/// [#INITIAL_STATE]; the state-less accessors ([#automaton()], [#classifier()]) refer to it.
///
/// Built with [LexiconBuilder]. Use [#newLexer(Reader)] to scan actual input.
public final class Lexicon<T extends Enum<T>> {
    /// The state every [LexiconBuilder] starts defining, and every [Lexer] starts scanning in.
    public static final String INITIAL_STATE = "INITIAL";

    /// Marker token type for the standalone whitespace DFA; never seen outside this package.
    enum WhitespaceMarker {
        WHITESPACE
    }

    /// Everything compiled for one lexical state.
    record CompiledState<T extends Enum<T>>(
            DeterministicFiniteAutomaton<Integer, T> automaton,
            CharacterClassifier classifier,
            Set<T> tokenTypes,
            Map<T, String> transitions,
            DeterministicFiniteAutomaton<Integer, WhitespaceMarker> whitespaceAutomaton,
            CharacterClassifier whitespaceClassifier) {

        Optional<String> nextState(T tokenType) {
            return Optional.ofNullable(transitions.get(tokenType));
        }

        boolean skipsWhitespace() {
            return whitespaceAutomaton != null;
        }
    }

    private final Map<String, CompiledState<T>> states;
    private final Set<T> tokenTypes;

    Lexicon(Map<String, CompiledState<T>> states) {
        this.states = states;
        Set<T> all = new LinkedHashSet<>();
        states.values().forEach(state -> all.addAll(state.tokenTypes()));
        this.tokenTypes = Collections.unmodifiableSet(all);
    }

    /// The names of every lexical state, starting with [#INITIAL_STATE].
    public Set<String> states() {
        return states.keySet();
    }

    /// Every token type this lexicon was built to recognize, in any state.
    public Set<T> tokenTypes() {
        return tokenTypes;
    }

    /// The token types recognized in `state`.
    public Set<T> tokenTypes(String state) {
        return compiledState(state).tokenTypes();
    }

    /// The DFA of the initial state, for introspection (e.g. `toMermaid()`).
    public DeterministicFiniteAutomaton<Integer, T> automaton() {
        return automaton(INITIAL_STATE);
    }

    /// The DFA recognizing `state`'s rules, for introspection (e.g. `toMermaid()`).
    public DeterministicFiniteAutomaton<Integer, T> automaton(String state) {
        return compiledState(state).automaton();
    }

    /// The classifier mapping code points to [#automaton()]'s input alphabet.
    public CharacterClassifier classifier() {
        return classifier(INITIAL_STATE);
    }

    /// The classifier mapping code points to [#automaton(String)]'s input alphabet.
    public CharacterClassifier classifier(String state) {
        return compiledState(state).classifier();
    }

    /// The state that matching `tokenType` in `state` switches to, if the rule declared one.
    public Optional<String> nextState(String state, T tokenType) {
        return compiledState(state).nextState(tokenType);
    }

    /// The compiled data for `state`.
    /// @throws IllegalArgumentException if this lexicon has no such state
    CompiledState<T> compiledState(String state) {
        CompiledState<T> compiled = states.get(state);
        if (compiled == null) {
            throw new IllegalArgumentException("Unknown lexical state: " + state);
        }
        return compiled;
    }

    /// Creates a new stateful lexer, scanning `reader` against this lexicon, starting in [#INITIAL_STATE].
    public Lexer<T> newLexer(Reader reader) {
        return new Lexer<>(this, reader);
    }
}
