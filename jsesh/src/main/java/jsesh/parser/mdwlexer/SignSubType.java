package jsesh.parser.mdwlexer;

/// What kind of thing a `HIEROGLYPH` symbol's [MdcSign] actually denotes.
///
/// The original scanner packed every sign subtype into one shared `int` space
/// (ordinary sign subtypes and, when philology-as-signs mode is on, a doubled philology code).
/// Here the two cases are kept as a proper sealed type instead: [Plain] for the ordinary
/// subtypes, [Philology] for a philological bracket reinterpreted as a sign.
public sealed interface SignSubType {

    /// Ordinary sign subtypes, in the order their rules appear in `MDCLexAux.l`.
    enum Plain implements SignSubType {
        /// A backtick, or the generic Gardiner-style code rule.
        MDC_CODE,
        /// `o`
        RED_POINT,
        /// `O`
        BLACK_POINT,
        /// A double-quoted `"..."` run.
        SMALL_TEXT,
        /// `.`
        HALF_SPACE,
        /// `..`
        FULL_SPACE,
        /// `//`
        FULL_SHADE,
        /// `v/`
        VERTICAL_SHADE,
        /// `/`
        QUARTER_SHADE,
        /// `h/`
        HORIZONTAL_SHADE
    }

    /// A philological bracket (`kind`, begin or end) treated as a plain sign.
    record Philology(PhilologyKind kind, boolean begin) implements SignSubType {
    }
}
