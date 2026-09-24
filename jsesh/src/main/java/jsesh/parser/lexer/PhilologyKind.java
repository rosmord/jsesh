package jsesh.parser.lexer;

/// The seven kinds of philological parenthesis pair (e.g. `[[...]]` for erased signs)
/// recognized by the `BEGIN_PHIL`/`END_PHIL` rules.
public enum PhilologyKind {
    ERASED_SIGNS,
    EDITOR_SUPERFLUOUS,
    PREVIOUSLY_READABLE,
    SCRIBE_ADDITION,
    EDITOR_ADDITION,
    MINOR_ADDITION,
    DUBIOUS
}
