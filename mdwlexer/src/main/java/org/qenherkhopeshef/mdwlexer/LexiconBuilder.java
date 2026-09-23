package org.qenherkhopeshef.mdwlexer;

import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.oneOf;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.oneOrMore;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.qenherkhopeshef.mdwlexer.automata.AutomataHelper;
import org.qenherkhopeshef.mdwlexer.automata.DeterministicFiniteAutomaton;

/// Builder for a complete lexicon, based on characters.
///
/// To accomodate the full Unicode character sets, the lexicon will use codepoints.
///
/// By default, runs of the usual whitespace characters (space, tab, newline, carriage
/// return, form feed, vertical tab) are skipped automatically between tokens; use
/// [#skipWhitespace(Expression)] to recognize something else as whitespace, or
/// [#noWhitespaceSkipping()] to turn the feature off entirely.
public final class LexiconBuilder<T extends Enum<T>> {
    private static final Expression DEFAULT_WHITESPACE = oneOrMore(oneOf(" \t\n\r\f\u000B"));

    private final Map<T, Expression> rules = new LinkedHashMap<>();
    private Expression whitespace = DEFAULT_WHITESPACE;

    private LexiconBuilder() {
    }

    public static <T extends Enum<T>> LexiconBuilder<T> newBuilder() {
        return new LexiconBuilder<>();
    }

    /// Adds a rule: `expression` is recognized as a token of type `tokenType`.
    public LexiconBuilder<T> rule(T tokenType, Expression expression) {
        Objects.requireNonNull(tokenType, "tokenType");
        Objects.requireNonNull(expression, "expression");
        rules.put(tokenType, expression);
        return this;
    }

    /// Replaces the default definition of whitespace with `expression`. Whatever it
    /// matches is skipped automatically between tokens, unless disabled at the [Lexer]
    /// level with [Lexer#setSkipWhitespace(boolean)].
    public LexiconBuilder<T> skipWhitespace(Expression expression) {
        this.whitespace = Objects.requireNonNull(expression, "expression");
        return this;
    }

    /// Disables automatic whitespace skipping entirely: every character must be matched by a rule.
    public LexiconBuilder<T> noWhitespaceSkipping() {
        this.whitespace = null;
        return this;
    }

    /// Compiles every rule into a shared, minimized DFA.
    public Lexicon<T> build() {
        if (rules.isEmpty()) {
            throw new IllegalStateException("At least one rule is required");
        }
        ExpressionCompiler.Result<T> compiled = ExpressionCompiler.compile(rules);
        DeterministicFiniteAutomaton<Integer, T> automaton =
                AutomataHelper.minimize(AutomataHelper.determinize(compiled.automaton()));
        Set<T> tokenTypes = Collections.unmodifiableSet(new LinkedHashSet<>(rules.keySet()));

        if (whitespace == null) {
            return new Lexicon<>(automaton, compiled.classifier(), tokenTypes, null, null);
        }
        ExpressionCompiler.Result<Lexicon.WhitespaceMarker> compiledWhitespace =
                ExpressionCompiler.compile(whitespace, Lexicon.WhitespaceMarker.WHITESPACE);
        DeterministicFiniteAutomaton<Integer, Lexicon.WhitespaceMarker> whitespaceAutomaton =
                AutomataHelper.minimize(AutomataHelper.determinize(compiledWhitespace.automaton()));
        return new Lexicon<>(
                automaton, compiled.classifier(), tokenTypes, whitespaceAutomaton, compiledWhitespace.classifier());
    }
}
