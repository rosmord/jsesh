package org.qenherkhopeshef.mdwlexer.mdc;

/// The start or end of a cartouche, e.g. `<s2` / `s2>`: `type` is the
/// cartouche kind (`'c'` plain, or one of `s f h g`), `part` (0-3) is which
/// piece of it this is.
public record Cartouche(char type, int part) {
}
