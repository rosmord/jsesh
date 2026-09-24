package jsesh.parser.lexer;

import java.util.Objects;

/// A scanned MDC symbol: analogous to the `java_cup.runtime.Symbol` the original
/// `MDCLexAux.l` scanner produced, but self-contained (no CUP/jsesh dependency).
///
/// `value`'s type depends on `code`; see [MdcSymbolCode] for which rules
/// produce which code, and the per-code javadoc below for the value's type:
///  - [MdcSymbolCode#LINE_END], [MdcSymbolCode#TAB_STOP], [MdcSymbolCode#INTEGER] - [Integer]
///  - [MdcSymbolCode#HRULE] - [HRule]
///  - [MdcSymbolCode#BEGIN_OLD_CARTOUCHE] - [OldCartoucheStart]
///  - [MdcSymbolCode#BEGIN_CARTOUCHE], [MdcSymbolCode#END_CARTOUCHE] - [Cartouche]
///  - [MdcSymbolCode#BEGIN_PHIL], [MdcSymbolCode#END_PHIL] - [PhilologyKind]
///  - [MdcSymbolCode#TOGGLE] - [ToggleType]
///  - [MdcSymbolCode#HIEROGLYPH] - [MdcSign]
///  - [MdcSymbolCode#TEXT] - [AlphabeticText]
///  - [MdcSymbolCode#MODIFIER] - [Modifier]
///  - [MdcSymbolCode#SHADING], [MdcSymbolCode#TEXT_SUPER],
///    [MdcSymbolCode#IDENTIFIER], [MdcSymbolCode#UNKNOWN] - [String]
///  - every other code - `null`
/// @param position the number of code points consumed before this symbol, i.e. where it starts
/// @param text the raw text this symbol was scanned from
public record MdcSymbol(MdcSymbolCode code, Object value, String text, int position) {
    public MdcSymbol {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(text, "text");
    }
}
