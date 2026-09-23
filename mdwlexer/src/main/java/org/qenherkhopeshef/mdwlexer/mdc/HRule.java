package org.qenherkhopeshef.mdwlexer.mdc;

/// A horizontal rule, e.g. `{l12,34}`: `type` is `'l'` (thin) or `'L'`
/// (wide), `start`/`end` are absolute positions in glyph units.
public record HRule(char type, int start, int end) {
}
