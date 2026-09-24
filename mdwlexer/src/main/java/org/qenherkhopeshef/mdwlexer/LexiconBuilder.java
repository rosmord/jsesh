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
///
/// ## Lexical states
///
/// A lexicon may have several *lexical states* (JFlex's `%state`), each one a separate
/// sub-lexicon with its own rules and its own whitespace definition. At any time, a [Lexer]
/// scans with the rules of its current state only.
///
/// Every builder starts with [Lexicon#INITIAL_STATE], which is also the state a new [Lexer]
/// starts in. [#state(String)] selects (creating it if needed) the state that the following
/// calls to [#rule], [#skipWhitespace(Expression)] and [#noWhitespaceSkipping()] apply to:
///
/// ```java
/// LexiconBuilder.<Tok>newBuilder()
///         .rule(Tok.WORD, word)
///         .rule(Tok.OPEN, character('['), "PROPERTIES")   // switches to PROPERTIES after '['
///         .state("PROPERTIES")
///         .rule(Tok.NUMBER, integer)
///         .rule(Tok.CLOSE, character(']'), Lexicon.INITIAL_STATE)
///         .build();
/// ```
///
/// A state change is either:
///  - **declarative**: [#rule(Enum, Expression, String)] makes a token, when matched in the
///    state being defined, switch the lexer to another state before the next token is scanned;
///  - **explicit**: the caller (typically the parser) calls [Lexer#beginState(String)].
public final class LexiconBuilder<T extends Enum<T>> {
    private static final Expression DEFAULT_WHITESPACE = oneOrMore(oneOf(" \t\n\r\f\u000B"));

    private final Map<String, StateSpec<T>> states = new LinkedHashMap<>();
    private StateSpec<T> current;

    private LexiconBuilder() {
        state(Lexicon.INITIAL_STATE);
    }

    public static <T extends Enum<T>> LexiconBuilder<T> newBuilder() {
        return new LexiconBuilder<>();
    }

    /// Selects the lexical state that the following rule and whitespace definitions apply to,
    /// creating it if it doesn't exist yet. The builder starts in [Lexicon#INITIAL_STATE].
    public LexiconBuilder<T> state(String name) {
        Objects.requireNonNull(name, "name");
        current = states.computeIfAbsent(name, StateSpec::new);
        return this;
    }

    /// Adds a rule to the current state: `expression` is recognized as a token of type `tokenType`.
    public LexiconBuilder<T> rule(T tokenType, Expression expression) {
        Objects.requireNonNull(tokenType, "tokenType");
        Objects.requireNonNull(expression, "expression");
        current.rules.put(tokenType, expression);
        current.transitions.remove(tokenType);
        return this;
    }

    /// Adds a rule to the current state, which, once matched, switches the lexer to `nextState`.
    ///
    /// The switch applies to the scanning of the *next* token; `nextState` must be defined
    /// (have rules) by the time [#build()] is called.
    public LexiconBuilder<T> rule(T tokenType, Expression expression, String nextState) {
        rule(tokenType, expression);
        current.transitions.put(tokenType, Objects.requireNonNull(nextState, "nextState"));
        return this;
    }

    /// Replaces, for the current state, the default definition of whitespace with `expression`.
    /// Whatever it matches is skipped automatically between tokens, unless disabled at the
    /// [Lexer] level with [Lexer#setSkipWhitespace(boolean)].
    public LexiconBuilder<T> skipWhitespace(Expression expression) {
        current.whitespace = Objects.requireNonNull(expression, "expression");
        return this;
    }

    /// Disables automatic whitespace skipping entirely in the current state: every character
    /// must be matched by a rule.
    public LexiconBuilder<T> noWhitespaceSkipping() {
        current.whitespace = null;
        return this;
    }

    /// Compiles every state's rules into its own shared, minimized DFA.
    /// @throws IllegalStateException if a state has no rules, or a rule switches to an undefined state
    public Lexicon<T> build() {
        Map<String, Lexicon.CompiledState<T>> compiled = new LinkedHashMap<>();
        for (StateSpec<T> spec : states.values()) {
            if (spec.rules.isEmpty()) {
                throw new IllegalStateException("At least one rule is required in state " + spec.name);
            }
            spec.transitions.forEach((tokenType, target) -> {
                if (!states.containsKey(target)) {
                    throw new IllegalStateException(
                            "Rule for " + tokenType + " in state " + spec.name + " switches to undefined state " + target);
                }
            });
            compiled.put(spec.name, compile(spec));
        }
        return new Lexicon<>(Collections.unmodifiableMap(compiled));
    }

    private static <T extends Enum<T>> Lexicon.CompiledState<T> compile(StateSpec<T> spec) {
        ExpressionCompiler.Result<T> compiled = ExpressionCompiler.compile(spec.rules);
        DeterministicFiniteAutomaton<Integer, T> automaton =
                AutomataHelper.minimize(AutomataHelper.determinize(compiled.automaton()));
        Set<T> tokenTypes = Collections.unmodifiableSet(new LinkedHashSet<>(spec.rules.keySet()));
        Map<T, String> transitions = Collections.unmodifiableMap(new LinkedHashMap<>(spec.transitions));

        if (spec.whitespace == null) {
            return new Lexicon.CompiledState<>(automaton, compiled.classifier(), tokenTypes, transitions, null, null);
        }
        ExpressionCompiler.Result<Lexicon.WhitespaceMarker> compiledWhitespace =
                ExpressionCompiler.compile(spec.whitespace, Lexicon.WhitespaceMarker.WHITESPACE);
        DeterministicFiniteAutomaton<Integer, Lexicon.WhitespaceMarker> whitespaceAutomaton =
                AutomataHelper.minimize(AutomataHelper.determinize(compiledWhitespace.automaton()));
        return new Lexicon.CompiledState<>(automaton, compiled.classifier(), tokenTypes, transitions,
                whitespaceAutomaton, compiledWhitespace.classifier());
    }

    /// The not-yet-compiled definition of one lexical state.
    private static final class StateSpec<T extends Enum<T>> {
        private final String name;
        private final Map<T, Expression> rules = new LinkedHashMap<>();
        private final Map<T, String> transitions = new LinkedHashMap<>();
        private Expression whitespace = DEFAULT_WHITESPACE;

        StateSpec(String name) {
            this.name = name;
        }
    }
}
