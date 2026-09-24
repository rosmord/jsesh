# mdwlexer

A small Java library for building lexers (tokenizers) in memory, with no code-generation step: you describe your tokens as regular expressions in Java, and the library provides an efficient parsing mechanism.


## Contents

- [Lexical Analysis](#lexical-analysis)
- [Architecture](#architecture)
- [Quick example](#quick-example)
- [Defining a language](#defining-a-language)
- [The automata layer](#the-automata-layer-advanced)
- [Class reference](#class-reference)

## Lexical Analysis

When you need to analyse a complex text file, for instance, the code of a program, the lexer helps you to cut a stream of characters into “words”, more properly *tokens*. Token may have very different forms, depending on the language you need to analyse. For Java, you would have keywords like `if`, identifiers like `myVariable`, numbers like `42` or `3.14`, operators such as `+` or `++`, and so on.

Usually, a lexer is built by defining those tokens with small **regular expressions**. In many cases, a lexer is generated once, before compilation, from a text file describing the existing tokens. Software such as JFlex will take a `.l` file and produce a Java class that implements the lexer. It's efficient, but requires this preliminary step, which makes the build process a bit more complex.

`mdwlexer`is a library which allows you to build a lexer **at runtime**. The cost is minimal, as the lexer needs to be built once, and can be reused throughout the lifetime of the application.

`mdwlexer`:

- works in Unicode (full code points);
- support conflicts by using:
  - longest match wins;
  - in case of tie, the first defined token wins;
- uses finite automata, and provides an interesting library to manipulate them ; it is even possible to export the automata to a mermaid diagram, which is useful for debugging or understanding the algorithms at hand;
- handle the full range of regular languages, including intersection and complement, which are not supported by most regex libraries;
- implements both determinisation and minimization of finite automata.

In some cases,  for instance if you separate each token by spaces, this library is overkill. It's meant for actual little languages: configuration formats, markup dialects, expression languages, and the like.

## Architecture

The first step is to list the types of the tokens you expect to meet and name them. This will result in a java `enum` type.

Turning a set of rules into a running tokenizer goes through several immutable stages:

```text
Expression (AST)                                 you build this, via ExpressionBuilder
      │
      ▼  CharacterClassifier.forExpressions(...)
character classes                                partitions the Unicode code point space
      │                                           into the few classes your rules actually
      │                                           distinguish
      ▼  ExpressionCompiler.compile(...)          (Thompson construction, one branch per rule,
NondeterministicFiniteAutomaton<Integer, T>         merged from a shared initial state)
      │
      ▼  AutomataHelper.determinize(...)
DeterministicFiniteAutomaton<Integer, T>
      │
      ▼  AutomataHelper.minimize(...)
DeterministicFiniteAutomaton<Integer, T>         (fewest possible states)
      │
      ▼  LexiconBuilder.build() wraps the above
Lexicon<T>                                       immutable, thread-safe; build it once
      │
      ▼  Lexicon.newLexer(Reader)
Lexer<T>                                         stateful: one Reader, one thread
      │
      ▼  Lexer.nextToken()
Token<T>
```

`T` is your own token-type enum. Because it's an `Enum`, its declaration order doubles as
rule priority (see below) — that's the only reason `LexiconBuilder` and friends require
`T extends Enum<T>` instead of accepting any type.

### Two packages, one boundary

- **`org.qenherkhopeshef.mdwlexer`** — the lexer-facing layer: `Expression`, the
  `ExpressionBuilder` DSL, the compiler, `Lexicon`/`Lexer`/`Token`. This is almost
  everything you'll touch directly.
- **`org.qenherkhopeshef.mdwlexer.automata`** — generic, alphabet-agnostic automaton
  machinery (`State`, `FiniteAutomaton`, `NondeterministicFiniteAutomaton`,
  `DeterministicFiniteAutomaton`, `AutomataHelper`). It knows nothing about `Expression`,
  code points, or lexing — it works over any input-symbol type `I` and token type `T`. It's
  what the rest of the library is built on, and it's public mainly so you can inspect a
  compiled `Lexicon`'s automaton (e.g. render it, see [Debugging](#debugging-rendering-the-automaton))
  or reuse the algorithms (`determinize`, `minimize`, `complement`, `intersect`)
  independently. It is *not* a way to hand a hand-built automaton to a `Lexer`: the only way
  to obtain a `Lexicon` is `LexiconBuilder`, and the only way to obtain a `Lexer` is
  `Lexicon.newLexer(Reader)`.

## Quick example

```java
import org.qenherkhopeshef.mdwlexer.Expression;
import org.qenherkhopeshef.mdwlexer.Lexer;
import org.qenherkhopeshef.mdwlexer.Lexicon;
import org.qenherkhopeshef.mdwlexer.LexiconBuilder;
import org.qenherkhopeshef.mdwlexer.Token;

import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

public class Demo {

    enum TokenType {
        IF, ELSE, IDENTIFIER, INTEGER
    }

    public static void main(String[] args) throws IOException {
        Expression letter = union(characterRange('a', 'z'), characterRange('A', 'Z'), character('_'));
        Expression digit = characterRange('0', '9');

        Lexicon<TokenType> lexicon = LexiconBuilder.<>newBuilder()
                .rule(TokenType.IF, literal("if"))
                .rule(TokenType.ELSE, literal("else"))
                .rule(TokenType.IDENTIFIER, sequence(letter, repeat(union(letter, digit))))
                .rule(TokenType.INTEGER, oneOrMore(digit))
                .build();

        Lexer<TokenType> lexer = lexicon.newLexer(new StringReader("if x1 else 42"));
        Optional<Token<TokenType>> next;
        while ((next = lexer.nextToken()).isPresent()) {
            Token<TokenType> token = next.get();
            System.out.println(token.type() + " " + token.text() + " @" + token.position());
        }
    }
}
```

Output:

```text
IF if @0
IDENTIFIER x1 @3
ELSE else @6
INTEGER 42 @11
```

Runs of whitespace between tokens are skipped automatically; see [Whitespace](#whitespace).

## Defining a language

### Add the dependency

`mdwlexer` is a module of this multi-project Gradle build. Depend on it like `jsesh`
already does ([jsesh/build.gradle.kts](../jsesh/build.gradle.kts)):

```kotlin
dependencies {
    implementation(project(":mdwlexer"))
}
```

### 1. Choose your token-type enum

Every rule is tagged with a value of your own `enum`, which must implement
`Enum<T>` (any plain Java enum does):

```java
enum TokenType {
    IF, ELSE, FUN, IDENTIFIER, INTEGER, REAL, STRING
}
```

#### Rule priority

At a given position, the `Lexicon`'s DFA finds the **longest** prefix of the remaining
input that *some* rule matches (maximal munch). If more than one rule matches that same
longest prefix — the classic case being a keyword like `if` also matching the identifier
rule — the winner is whichever token type has the **lower `ordinal()`**, i.e. whichever was
declared **first** in the enum. This is why keywords are listed before `IDENTIFIER` above:
`if` is recognized as `IF`, not as a two-letter identifier, while `iffy` still matches
`IDENTIFIER` (it's not tied with `IF`, since `IF` doesn't match past `"if"`).

Declaration order is the *only* thing that decides ties — the order you call
`LexiconBuilder.rule(...)` in does not matter.

### 2. Build expressions

`Expression` is an immutable, sealed AST over Unicode code points. You almost never
construct it directly; instead, static-import `ExpressionBuilder` and use it as a small
DSL:

```java
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.*;
```

| Function | Matches |
|---|---|
| `empty()` | the empty string, and nothing else |
| `character(cp)` | exactly one code point |
| `negatedCharacter(cp)` | any one code point *except* `cp` |
| `characterRange(from, to)` | one code point in the inclusive range `[from, to]` |
| `negatedCharacterRange(from, to)` | one code point outside `[from, to]` |
| `anyCharacter()` | any single code point |
| `oneOf("abc")` | one of the given characters, like `[abc]` |
| `noneOf("abc")` | any character except the given ones, like `[^abc]` |
| `literal("text")` | that exact text, character by character |
| `sequence(a, b, ...)` | `a` then `b`... in order |
| `union(a, b, ...)` | whatever any one of `a`, `b`, ... matches |
| `optional(a)` | `a?` — zero or one occurrence |
| `repeat(a)` | `a*` — zero or more |
| `oneOrMore(a)` | `a+` — one or more |
| `repeat(a, min, max)` | between `min` and `max` occurrences (`Expression.Repeat.UNBOUNDED` for no upper limit) |
| `complement(a)` | every string that `a` does **not** match |
| `intersection(a, b, ...)` | strings matched by *every* one of `a`, `b`, ... |
| `difference(a, b)` | what `a` matches, minus what `b` matches |

Characters, `codepoint`s, are plain `int`s, so you can pass a Java `char` literal
(`'a'`) directly, or any Unicode code point, including ones outside the BMP.

Note the difference between two easily-confused "empty" expressions: `empty()` matches the
empty string; `oneOf("")` (equivalently, an empty character class) matches *nothing at
all* — no string, not even the empty one.

A few building blocks worth knowing:

```java
Expression letter = union(characterRange('a', 'z'), characterRange('A', 'Z'), character('_'));
Expression digit = characterRange('0', '9');
Expression identifier = sequence(letter, repeat(union(letter, digit)));
Expression integer = oneOrMore(digit);
Expression real = sequence(integer, literal("."), integer);
Expression stringLiteral = sequence(literal("\""), repeat(noneOf("\"")), literal("\""));
```

When every argument to `union`, `intersection` or `difference` is itself a single
character/character-range expression, the result collapses into one character class rather
than staying a tree of alternatives — `union(characterRange('a','z'), characterRange('A','Z'), character('_'))`
above really does become one `[a-zA-Z_]` class. This keeps the compiled automaton smaller
and its diagrams (see [Debugging](#debugging-rendering-the-automaton)) readable; you don't
need to do anything to get it.

### 3. Assemble the lexicon

`LexiconBuilder` collects your rules and compiles them:

```java
Lexicon<TokenType> lexicon = LexiconBuilder.<TokenType>newBuilder()
        .rule(TokenType.IF, literal("if"))
        .rule(TokenType.ELSE, literal("else"))
        .rule(TokenType.FUN, literal("fun"))
        .rule(TokenType.IDENTIFIER, identifier)
        .rule(TokenType.INTEGER, integer)
        .rule(TokenType.REAL, real)
        .rule(TokenType.STRING, stringLiteral)
        .build();
```

`build()` does the actual work described in [Architecture](#architecture): compile every
rule into a shared NFA, determinize it, and minimize it. It's the expensive step, so build
a `Lexicon` once and reuse it — it's immutable and thread-safe, and cheap to hand out new
`Lexer`s from (one per input, since a `Lexer` itself is stateful).

`LexiconBuilder.rule(tokenType, expression)` requires at least one rule before `build()`;
calling `rule` twice for the same `tokenType` replaces its expression rather than adding an
alternative (use `union(...)` if you want several patterns for one token type).

### 4. Scan

```java
Lexer<TokenType> lexer = lexicon.newLexer(new StringReader(sourceText));
Optional<Token<TokenType>> token;
while ((token = lexer.nextToken()).isPresent()) {
    // token.get().type(), .text(), .position()
}
```

- `nextToken()` returns `Optional.empty()` at end of input, or throws `LexicalException`
  if no rule (and no whitespace) matches at the current position.
- `Token<T>` is an immutable record: `type()` (your enum value), `text()` (the exact
  matched text), and `position()` (how many code points were consumed *before* this token
  started).
- `Lexer.position()` reports how many code points have been consumed so far — code
  points, not UTF-16 `char`s, so it stays correct across surrogate pairs.
- `LexicalException.position()` reports the code-point position scanning failed at; the
  offending character itself is left unconsumed in the `Reader`.

### Matching semantics, precisely

- **Maximal munch, then priority.** Described in [Rule priority](#rule-priority) above.
- **A rule that matches the empty string is dangerous.** If the *winning* match (after
  maximal munch) has zero length, `nextToken()` throws `LexicalException` rather than
  looping forever. This happens if a rule can accept an empty match at the current
  position — typically a top-level `repeat(x)` or `optional(x)` used directly as a whole
  token's expression, with `x` not present in the input. Prefer `oneOrMore(x)` (which
  requires at least one occurrence) for "zero or more, but as a whole token" rules, or make
  sure such a rule is never the longest/highest-priority match on its own.
- **Unmatched input is an error, not a skip.** If no rule (and, unless disabled, no
  whitespace rule) matches at the current position, `nextToken()` throws
  `LexicalException` immediately; nothing is silently dropped.

### Whitespace

By default, runs of the usual ASCII whitespace characters (space, tab, line feed, carriage
return, form feed, vertical tab) are skipped automatically before each token. You can:

- **Redefine** what counts as whitespace: `LexiconBuilder.skipWhitespace(Expression)`.
  Whatever it matches is skipped the same way, still automatically, between tokens.
- **Disable it entirely**: `LexiconBuilder.noWhitespaceSkipping()` — then every character
  in the input must be matched by one of your own rules, whitespace included.
- **Toggle it per `Lexer`**, at runtime: `Lexer.setSkipWhitespace(boolean)` /
  `Lexer.isSkippingWhitespace()`. This only has an effect if the `Lexicon` wasn't built
  with `noWhitespaceSkipping()` — with that option, there is nothing to skip, and
  `setSkipWhitespace(true)` is a no-op.

Whitespace is compiled into its own separate DFA (with its own `CharacterClassifier`),
independent from your token rules — it does not affect rule priority or interact with your
token DFA in any way.

Whitespace is defined per lexical state (see below): `skipWhitespace(...)` and
`noWhitespaceSkipping()` apply to the state being defined, and each new state starts with
the default definition.

### Lexical states

Some languages need different rules in different contexts — the same `12` might be part of a
name in running text, but a number inside `[...]`. Like JFlex's `%state`, a `Lexicon` can hold
several named *lexical states*, each with its own rules (compiled to its own DFA) and its own
whitespace definition. A `Lexer` only uses the rules of its current state.

Every builder starts defining `Lexicon.INITIAL_STATE`, which is also where every `Lexer`
starts. `state(name)` selects (or creates) the state that the following calls apply to:

```java
Lexicon<Tok> lexicon = LexiconBuilder.<Tok>newBuilder()
        .rule(Tok.WORD, oneOrMore(union(letter, digit)))
        .rule(Tok.OPEN, character('['), "PROPERTIES")          // declarative switch
        .state("PROPERTIES")
        .rule(Tok.NUMBER, oneOrMore(digit))
        .rule(Tok.CLOSE, character(']'), Lexicon.INITIAL_STATE)
        .build();
```

The state changes in one of two ways, both taking effect from the next `nextToken()` on:

- **declaratively**: `rule(tokenType, expression, nextState)` switches the lexer to
  `nextState` whenever that rule matches in the state it was declared in;
- **explicitly**: `Lexer.beginState(name)`, typically called by the parser when it knows the
  context better than the lexer. `Lexer.state()` returns the current state.

`build()` rejects a state with no rules and a switch to an undefined state; `beginState` rejects
an unknown name. Switching is safe between any two tokens: the look-ahead the previous scan read
and pushed back stays in the shared input buffer and is seen by the new state's DFA.

### Boolean operators: complement, intersection, difference

Most of a language's tokens are plain sequences/unions/repetitions, and for single
characters, `union`/`intersection`/`difference` already read naturally as character-class
operations (`difference(characterRange('a','z'), oneOf("if"))` is `[a-z]` minus `i` and
`f`). `complement` and `intersection` become genuinely useful, though, once you apply them
to whole sub-*languages*, not just single characters — something a plain regex (no
lookaround) can't express directly. A recurring building block for that is "matches any
string that contains `needle` somewhere as a substring":

```java
static Expression containing(Expression needle) {
    return sequence(repeat(anyCharacter()), needle, repeat(anyCharacter()));
}
```

**`complement`** — the body of a `/* ... */` block comment is "any run of characters that
does not contain the closing `*/`":

```java
Expression closer = literal("*/");
Expression commentBody = complement(containing(closer));
Expression blockComment = sequence(literal("/*"), commentBody, closer);
```

**`intersection`** — a token that must contain both a digit and a letter somewhere, in
either order, with anything else around them:

```java
Expression digit = characterRange('0', '9');
Expression letter = union(characterRange('a', 'z'), characterRange('A', 'Z'));
Expression mixedToken = intersection(containing(digit), containing(letter));
```

Under the hood, each operand of `complement`/`intersection` is compiled and determinized
*on its own* before being combined and spliced back into the surrounding expression (see
[`ExpressionCompiler`](src/main/java/org/qenherkhopeshef/mdwlexer/ExpressionCompiler.java)).
That's unavoidable — these operators can't be built by simply gluing NFA fragments the way
sequence/union/repeat can — so keep them scoped to the sub-expression that actually needs
them rather than wrapping your whole grammar in one.

### Debugging: rendering the automaton

A compiled `Lexicon` exposes its DFA and classifier for introspection:

```java
String mermaid = lexicon.automaton().toMermaid(lexicon.classifier()::describe);
```

Paste the result into a ```` ```mermaid ```` fenced block (GitHub, most Markdown viewers,
mermaid.live...) to see the DFA as a flowchart. `CharacterClassifier.describe(int)` turns
the automaton's raw character-class ids back into readable labels (`'a'-'z'`, `U+0041`...);
without it, `toMermaid()` falls back to the class ids' own `toString()`, which is
technically correct but unreadable. This is the fastest way to see why two rules are
clashing, or why a rule isn't matching what you expect.

## The automata layer (advanced)

`org.qenherkhopeshef.mdwlexer.automata` is the generic engine `ExpressionCompiler` and
`LexiconBuilder` are built on. You don't need it to define a language, but it's worth
knowing about if you want to inspect a compiled automaton in depth, or reuse its algorithms
for something unrelated to this library's own `Expression`/`Lexicon` pipeline.

- **`State`** — an opaque state identity, a builder-assigned non-negative `int`
  (`State#toString()` renders it as `q0`, `q1`, ...).
- **`FiniteAutomaton<I, T>`** — the read-only view shared by both automaton kinds:
  `initialState()`, `states()`, `inputs()`, `transitions(state, input)`,
  `acceptingTokenType(state)`, `epsilonTransitions(state)` (always empty for a DFA), and the
  default `toMermaid()` / `toMermaid(Function<I, String> inputLabel)` renderer. `I` is the
  input-symbol type (an arbitrary character class id, in this library's own use); `T` is
  the token type attached to accepting states.
- **`NondeterministicFiniteAutomaton<I, T>`** — an immutable epsilon-NFA, built via its
  nested `Builder` (`addState()`, `initialState(state)`, `transition(from, input, to)`,
  `epsilon(from, to)`, `accepting(state, tokenType)`, `build()`).
- **`DeterministicFiniteAutomaton<I, T>`** — an immutable DFA (no epsilon transitions),
  same builder shape minus `epsilon(...)`, plus a single-target
  `transition(state, input) : Optional<State>` (a DFA state has at most one target per
  input, unlike the NFA's `transitions(...) : Set<State>`).
- **`AutomataHelper`** — the static algorithms: `determinize` (subset construction, NFA
  → DFA, not minimized), `minimize` (Moore's algorithm), `complement(automaton, alphabet,
  tokenType)` (completes the DFA over `alphabet` with a dead state, then flips acceptance —
  pass the *whole* alphabet you care about, not just the symbols the automaton happens to
  transition on), and `intersect(first, second, tokenType)` (product construction; a
  missing transition on either side is already an implicit reject, so no completion is
  needed first).

These are all generic in `I` and `T`; nothing here knows about `Expression`, code points,
or `CharacterClassifier`. `ExpressionCompiler` is the piece that bridges the two layers, by
compiling `Expression` trees into `NondeterministicFiniteAutomaton<Integer, T>` (using
`CharacterClassifier` to turn Unicode code points into the small `Integer` alphabet the
automaton actually runs on).

## Class reference

| Class | Role |
|---|---|
| `Expression` | Immutable, sealed AST for a regular expression over code points |
| `ExpressionBuilder` | Static factory DSL for building `Expression` trees |
| `CharacterClassifier` | Partitions the code point space into the classes a set of expressions distinguish |
| `ExpressionCompiler` | Compiles `Expression`(s) into a `NondeterministicFiniteAutomaton<Integer, T>` (Thompson construction) |
| `LexiconBuilder<T>` | Collects rules (and an optional whitespace rule), per lexical state, and compiles a `Lexicon` |
| `Lexicon<T>` | Immutable, compiled, thread-safe: the shared DFA plus classifier, and (unless disabled) the whitespace DFA/classifier |
| `Lexer<T>` | Stateful scanner over one `Reader`, produced by `Lexicon.newLexer(Reader)`; tracks the current lexical state |
| `Token<T>` | One scanned token: type, matched text, starting position |
| `LexicalException` | Thrown by `Lexer.nextToken()` when nothing matches at the current position |
| `automata.State` | Opaque automaton state identity |
| `automata.FiniteAutomaton<I,T>` | Read-only view shared by both automaton kinds, incl. Mermaid rendering |
| `automata.NondeterministicFiniteAutomaton<I,T>` | Immutable epsilon-NFA, built via a nested `Builder` |
| `automata.DeterministicFiniteAutomaton<I,T>` | Immutable DFA, built via a nested `Builder` |
| `automata.AutomataHelper` | `determinize`, `minimize`, `complement`, `intersect` |
