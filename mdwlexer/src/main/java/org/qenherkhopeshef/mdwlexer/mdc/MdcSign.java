package org.qenherkhopeshef.mdwlexer.mdc;

/// The value of a `HIEROGLYPH` symbol: its subtype, and the raw text it was read from.
public record MdcSign(SignSubType subtype, String text) {
}
