package jsesh.parser.lexer.symbols;


/// The lexicon as seen by the parser.
/// 
/// Symbols at this level are not atomic entries, but structured values.

public enum MdcSymbolCode {
    PAGE_END,
    LINE_END,
    TAB_STOP,
    TABBING,
    TABBING_CLEAR,
    HRULE,
    ZONE,
    QUADRAT,
    UNKNOWN_OPERATOR,
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
