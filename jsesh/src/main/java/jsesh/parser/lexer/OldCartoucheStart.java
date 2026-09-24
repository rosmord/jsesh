package jsesh.parser.lexer;

/// The start of a "MacScribe"-style old cartouche, e.g. `<Sb`: `code` is the
/// cartouche kind (`'c'` plain, or one of `S F H G`), `part` is which piece of
/// it this is (`'a'` the whole thing, or one of `b m e` for begin/middle/end).
public record OldCartoucheStart(char code, char part) {
}
