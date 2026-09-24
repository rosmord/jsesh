package jsesh.parser.lexer;

/// One constant per lexical rule of `MDCLexAux.l` (the original JLex specification), in
/// exactly the order those rules appear in the file.
///
/// Order matters: [org.qenherkhopeshef.mdwlexer.automata.AutomataHelper#determinize]
/// resolves a same-length tie between two rules by picking whichever token type has the lower
/// [Enum#ordinal()], which is exactly how JLex itself resolves the same tie ("the rule
/// listed first in the specification wins"). Declaring these constants in file order is what
/// makes the compiled DFA agree with the original scanner on every such tie; see
/// [MdcLexicon] for the handful of rules that turned out to be genuinely ambiguous with
/// each other and how they're ordered here.
///
/// A few rules whose action was identical (same symbol, no value) were merged into one
/// constant covering the union of their patterns - this changes nothing observable, since JLex
/// would have picked either rule and produced the same symbol either way. Each constant's
/// comment quotes the original pattern(s) and line number(s) it corresponds to.
///
/// Rules are grouped by which JLex start-state they belonged to; [#UNKNOWN] is the
/// catch-all `.` rule, which (having no state prefix in the original) applies in both.
public enum MdcTokenType {

    // ---- <YYINITIAL> ----

    /// `"!!"{ESPSO}` (line 228)
    PAGE_END,
    /// `"!"("="{INTEGER}"%")?{ESPSO}` (line 230)
    LINE_END,
    /// `"?"{INTEGER}` (line 236)
    TAB_STOP,
    /// `"%"` (line 241)
    TABBING,
    /// `"%clear"` (line 243)
    TABBING_CLEAR,
    /// `"{l"{INTEGER}","{INTEGER}"}"` (line 245)
    HRULE_THIN,
    /// `"{L"{INTEGER}","{INTEGER}"}"` (line 249)
    HRULE_WIDE,
    /// `"+"[a-rt-z+](\+|[^+]|"+"[^a-z+])*` (line 252)
    ALPHABETIC_TEXT,
    /// `"zone"` (line 258)
    ZONE,
    /// `"quadrat"` (line 262)
    QUADRAT,
    /// `"+s"` (line 267)
    START_HIEROGLYPHS,
    /// `"|"([^-]|"\-")*` (line 271; the redundant `'\\'` alternative is dropped, see [MdcLexicon])
    TEXT_SUPER,
    /// `{TRUESPACE}` (line 276; the redundant `|"_"` alternative is dropped, `_` already being a `TRUESPACE` char)
    WORD_END_CANDIDATE,
    /// `({TRUESPACE}{TRUESPACE})+` (line 282; the redundant `"__"` alternative is dropped)
    SENTENCE_END_CANDIDATE,
    /// `"-"({TRUESPACE}|"-")*`, merging lines 288 and 289 (both plain `SEPARATOR`)
    SEPARATOR,
    /// `"-#-"|"-#"`, merging lines 292 and 304 (both an unconditional shading toggle)
    SHADING_TOGGLE_LITERAL,
    /// `"#-"|("#"" "+)` (line 293)
    HASH_DASH_OR_SPACES,
    /// `"#"` (line 306)
    HASH_ALONE,
    /// `-?"#b"` (line 313)
    SHADING_ON,
    /// `-?"#e"` (line 314)
    SHADING_OFF,
    /// `"$r"` (line 315)
    TOGGLE_RED,
    /// `"$b"` (line 316)
    TOGGLE_BLACK,
    /// `"?"` (line 317)
    TOGGLE_LACUNA,
    /// `"??"` (line 318)
    TOGGLE_LINE_LACUNA,
    /// `"^"` (line 319)
    TOGGLE_OMIT,
    /// `"$"` (line 320)
    TOGGLE_BLACK_RED,
    /// `"#""1"?"2"?"3"?"4"?` (line 321)
    SHADING,
    /// `":"` (line 322)
    COLON,
    /// `"*"` (line 323)
    STAR,
    /// `"["` (line 324) - switches to `PROPERTIES`
    OPEN_BRACE,
    /// `"<"` (line 325)
    OLD_CARTOUCHE_PLAIN,
    /// `"<"[SFH]([bme])` (line 326)
    OLD_CARTOUCHE_TYPED_PART,
    /// `"<"[SFHG]` (line 329)
    OLD_CARTOUCHE_TYPED,
    /// `"<"[bme]` (line 330)
    OLD_CARTOUCHE_PART,
    /// `"<"[sfhg][0123]` (line 331)
    CARTOUCHE_START_TYPED_PART,
    /// `"<"[sfhg]` (line 334)
    CARTOUCHE_START_TYPED,
    /// `"<"[012]` (line 335)
    CARTOUCHE_START_PART,
    /// `">"` (line 336)
    CARTOUCHE_END_DEFAULT,
    /// `[sfhg][0123]">"` (line 337)
    CARTOUCHE_END_TYPED_PART,
    /// `[sfhg]">"` (line 339)
    CARTOUCHE_END_TYPED,
    /// `[012]">"` (line 341)
    CARTOUCHE_END_PART,
    /// `"[["` (line 344)
    PHIL_ERASED_SIGNS_BEGIN,
    /// `"[{"` (line 345)
    PHIL_EDITOR_SUPERFLUOUS_BEGIN,
    /// `"[\""` (line 347)
    PHIL_PREVIOUSLY_READABLE_BEGIN,
    /// `"['"` (line 348)
    PHIL_SCRIBE_ADDITION_BEGIN,
    /// `"[&"` (line 350)
    PHIL_EDITOR_ADDITION_BEGIN,
    /// `"]]"` (line 351)
    PHIL_ERASED_SIGNS_END,
    /// `"}]"` (line 353)
    PHIL_EDITOR_SUPERFLUOUS_END,
    /// `"\"]"` (line 354)
    PHIL_PREVIOUSLY_READABLE_END,
    /// `"']"` (line 356)
    PHIL_SCRIBE_ADDITION_END,
    /// `"&]"` (line 357)
    PHIL_EDITOR_ADDITION_END,
    /// `")]"` (line 358)
    PHIL_MINOR_ADDITION_END,
    /// `"[("` (line 359)
    PHIL_MINOR_ADDITION_BEGIN,
    /// `"?]"` (line 360)
    PHIL_DUBIOUS_END,
    /// `"[?"` (line 361)
    PHIL_DUBIOUS_BEGIN,
    /// `"("` (line 364)
    LPAREN,
    /// `")"` (line 365)
    RPAREN,
    /// `"##"` (line 366)
    OVERWRITE_DOUBLE_HASH,
    /// `"&"` (line 367)
    AMP,
    /// `"="` (line 368)
    GRAMMAR_EQUALS,
    /// `\"?"` (line 369)
    MODIFIER_QUESTION,
    /// `\R"-"?[0-9]*` (line 379; the stray trailing `)` in the original is dropped, see [MdcLexicon])
    MODIFIER_R,
    /// `\([a-zA-Z]*[0-9]*)` (line 380)
    MODIFIER_GENERIC,
    /// ``"`"`` (line 381)
    SIGN_BACKTICK,
    /// `"o"` (line 384)
    SIGN_RED_POINT,
    /// `"O"` (line 385)
    SIGN_BLACK_POINT,
    /// `\"([^\"\\\]]|\\.)*\"` (line 386)
    SIGN_SMALL_TEXT,
    /// `(([A-Z]|"Aa")[0-9]+A*|[@a-zA-Z0-9]+|"@".)` (line 393)
    SIGN_CODE,
    /// `"."` (line 398)
    SIGN_HALF_SPACE,
    /// `".."` (line 399)
    SIGN_FULL_SPACE,
    /// `"//"` (line 400)
    SIGN_FULL_SHADE,
    /// `"v/"` (line 401)
    SIGN_VERTICAL_SHADE,
    /// `"/"` (line 402)
    SIGN_QUARTER_SHADE,
    /// `"h/"` (line 403)
    SIGN_HORIZONTAL_SHADE,
    /// `"&&"|"**"`, merging lines 404 and 405 (both `DOUBLEAMP`)
    DOUBLE_AMP,
    /// `"&&&"` (line 406)
    LIG_AFTER,
    /// `"^^^"|"^^"`, merging lines 407 and 408 (both `LIGBEFORE`)
    LIG_BEFORE,
    /// `"{{"` (line 410) - switches to `PROPERTIES`
    DOUBLE_LEFT_CURLY,

    // ---- <PROPERTIES> ----

    /// `"}}"` (line 412) - switches back to `YYINITIAL`
    DOUBLE_RIGHT_CURLY,
    /// `"]"` (line 413) - switches back to `YYINITIAL`
    CLOSE_BRACE,
    /// `","` (line 415)
    COMMA,
    /// `"="` (line 416)
    EQUAL,
    /// `{INTEGER}` (line 417)
    PROPERTY_INTEGER,
    /// `[a-zA-Z_][a-zA-Z0-9_]*` (line 418)
    PROPERTY_IDENTIFIER,
    /// `[ \t\n\015]` (line 419) - always discarded
    PROPERTY_WHITESPACE,

    // ---- <YYINITIAL,PROPERTIES> (no explicit state prefix in the original) ----

    /// `.` (line 421) - matches any single character not otherwise matched
    UNKNOWN
}
