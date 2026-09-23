package jsesh.parser.mdwlexer;

/// The value of a `HIEROGLYPH` symbol: its subtype, and the raw text it was read from.
public record MdcSign(SignSubType subtype, String text) {
}
