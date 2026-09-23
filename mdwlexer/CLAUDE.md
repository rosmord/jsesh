# mdwlexer

The final system will build a shared DFA from a set of rules expressed in code.

The first step is to write the code for the DFA.

The first step will be to build a NDFA, to determinize and minimize it.

- Final states of the DFA will be marked (they will be generic classes) with an entry from an enum which will define the token types.

- possible the labels will be a small set of integers. In practice, the front end will map the real alphabet to this set (for instance, if we meet [a-z], 'i', and 'f' in the original language, the automaton will have a class for [a-z] - {`i`, `f`}, a class for `i`, and a class for `f`), and a mapper from characters to classes.

## Project structure

Gradle project (Java 21 toolchain), source under `src/main/java`, tests under `src/test/java` (JUnit 5 / Jupiter). Run tests with `gradle test`.

Two packages:

- `org.qenherkhopeshef.mdwlexer.automata` — low-level, alphabet-agnostic automaton machinery. Doesn't know about `Expression`, code points, or lexing.
- `org.qenherkhopeshef.mdwlexer` — the lexer-facing layer: `Expression`s, and the compiler that turns them into an `automata` NFA.

Current classes, `org.qenherkhopeshef.mdwlexer.automata`:

- `State` — immutable automaton state, identified by a builder-assigned int id.
- `FiniteAutomaton<I, T>` — read-only interface shared by both automaton classes (`initialState`, `states`, `inputs`, `transitions`, `acceptingTokenType`, `epsilonTransitions`, `toMermaid`), for algorithms that don't care about determinism. `epsilonTransitions` and `toMermaid` are default methods here (empty set / shared rendering); `NondeterministicFiniteAutomaton` overrides `epsilonTransitions` with its real ones. DFA-specific operations (e.g. a single-target `transition`) stay on `DeterministicFiniteAutomaton` itself.
- `NondeterministicFiniteAutomaton<I, T>` — immutable epsilon-NFA; built via its nested `Builder`. `I` is the input symbol type, `T` is the token type attached to accepting states. Implements `FiniteAutomaton<I, T>`.
- `DeterministicFiniteAutomaton<I, T>` — immutable DFA, same shape/roles for `I`/`T`, no epsilon transitions; built via its nested `Builder`. Implements `FiniteAutomaton<I, T>`.
- `AutomataHelper` — the actual algorithms: `determinize` (subset construction, NFA → DFA), `minimize` (Moore's algorithm on a DFA), `complement` (completes a DFA over a given alphabet — adding a dead state for missing transitions — then flips which states accept), and `intersect` (product construction of two DFAs; no completion needed, since a missing transition on either side already means "reject"). All are static, generic methods.
- `MermaidDiagram` — package-private renderer used by the default `toMermaid()` on `FiniteAutomaton`, to dump an automaton as a Mermaid flowchart for debugging/visualization.

Current classes, `org.qenherkhopeshef.mdwlexer`:

- `Expression` — sealed interface: immutable regular-expression AST over Unicode code points (`Epsilon`, `CharacterSet`, `Sequence`, `Union`, `Intersection`, `Complement`, `Repeat`). Describes a language only; token type/priority are decided elsewhere.
- `ExpressionBuilder` — static factory DSL (meant for static import) to build `Expression` trees (`sequence`, `union`, `repeat`, `oneOf`, `literal`, character ranges, etc.).
- `CharacterClassifier` — partitions the code point space into the smallest set of disjoint integer classes such that every `CharacterSet` reachable from a group of expressions is exactly a union of classes (e.g. `[a-z]`, `i`, `f` → one class each for `i`, `f`, and what's left of `[a-z]`). `classOf(codepoint)` is the mapper from real characters to classes; `classesOf(CharacterSet)` is used by the compiler.
- `ExpressionCompiler` — compiles `Expression`s into an `automata.NondeterministicFiniteAutomaton<Integer, T>` via Thompson construction, using `CharacterClassifier` so the NFA's alphabet is class ids, not raw code points. `compile(Expression, T)` for one rule, `compile(Map<T, Expression>)` for several rules sharing one automaton (each branching from a common initial state). Returns a `Result<T>` pairing the automaton with the classifier used to build it. `Complement` and `Intersection` can't be built by gluing NFA fragments, so both are handled out of band: each operand is compiled and determinized on its own (with a private throwaway marker token type), combined at the DFA level (`AutomataHelper.complement`, over every class the shared `CharacterClassifier` knows about; `AutomataHelper.intersect`, folded pairwise over an `Intersection`'s operands), and the result is spliced back into the surrounding NFA as a fragment (its accepting states epsilon-join a single fragment exit).
- `Lexer<T extends Enum<T>>` — stub, not yet implemented.
- `LexiconBuilder` — stub, not yet implemented; will build a complete codepoint-based lexicon (rules → shared DFA), presumably on top of `ExpressionCompiler`'s multi-rule `compile`.

Not yet built: the `Lexer`/`LexiconBuilder` implementations that tie rules, the DFA, and the token-type enum together into something that actually scans text.

Test fixtures worth knowing about: `DemoSmallAutomaton`, `DemoSmallAutomatonHelper` (package `automata`) are a small hand-built NFA shared across `AutomataTest`, `DeterministicFiniteAutomatonTest`, and `NondeterministicFiniteAutomatonTest`. `DemoToken` (root package) is a token-type enum with regex-shaped doc comments (`IDENTIFIER`, `INTEGER`, ...), used by `ExpressionCompilerTest`.

