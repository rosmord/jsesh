package jsesh.parser.mdwlexer;

/// The high-level symbol kind carried by an [MdcSymbol], mirroring the CUP symbol
/// constants (`SymbolCodes`/`MDCSymbols`) that the original `MDCLexAux.l`
/// scanner used. Several [MdcTokenType] lexical rules can map to the same code (e.g. every
/// sign-recognizing rule produces [#HIEROGLYPH]); the code is what a parser built on top
/// of this scanner would actually switch on.
public enum MdcSymbolCode {
    PAGE_END,
    LINE_END,
    TAB_STOP,
    TABBING,
    TABBING_CLEAR,
    HRULE,
    ZONE,
    QUADRAT,
    START_HIEROGLYPHS,
    TEXT_SUPER,
    WORD_END,
    SENTENCE_END,
    SEPARATOR,
    TOGGLE,
    SHADING,
    COLON,
    STAR,
    OPEN_BRACE,
    BEGIN_OLD_CARTOUCHE,
    BEGIN_CARTOUCHE,
    END_CARTOUCHE,
    BEGIN_PHIL,
    END_PHIL,
    LPAREN,
    RPAREN,
    OVERWRITE,
    AMP,
    GRAMMAR,
    MODIFIER,
    HIEROGLYPH,
    DOUBLE_AMP,
    LIG_AFTER,
    LIG_BEFORE,
    DOUBLE_LEFT_CURLY,
    DOUBLE_RIGHT_CURLY,
    CLOSE_BRACE,
    COMMA,
    EQUAL,
    INTEGER,
    IDENTIFIER,
    TEXT,
    UNKNOWN,
    EOF
}
