package jsesh.parser.mdwlexer;

/// The value of a `MODIFIER` symbol, e.g. `\R-3`, `\col50`, `\?`: `name` is the
/// letters right after the `\` (or `?`), `value` is the optional trailing
/// integer (possibly negative), or `null` if there is none.
public record Modifier(String name, Integer value) {
}
