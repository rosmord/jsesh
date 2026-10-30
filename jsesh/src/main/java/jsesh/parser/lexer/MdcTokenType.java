/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.lexer;

import jsesh.parser.lexer.symbols.MdcSymbol;

/// Low-level tokens identified by the lexical analyser.
///
/// Order matters: the lexical analyser uses it to solve ties between rules.
///
/// Rules are grouped by which start-state they belonged to.
/// 
/// Tokens are not analysed directly: the client of the lexer must interpret them. In our case, [MdcLexer] transforms those plain tokens into [MdcSymbol]s.
/// 
enum MdcTokenType {

    // ---- INITIAL state ----

    /// `"!!"{ESPSO}`
    PAGE_END,
    /// `"!"("="{INTEGER}"%")?{ESPSO}`
    LINE_END,
    /// `"?"{INTEGER}`
    TAB_STOP,
    /// `"{l"{INTEGER}","{INTEGER}"}"`
    HRULE_THIN,
    /// `"{L"{INTEGER}","{INTEGER}"}"`
    HRULE_WIDE,
    /// `"+"[a-rt-z+](\+|[^+]|"+"[^a-z+])*`
    ALPHABETIC_TEXT,
    /// `"%zone"`
    ZONE,
    /// `"%quadrat"`
    QUADRAT,
    /// `"%insert"` 
    INSERT,
    /// `"%hkern"`
    HKERN,
    /// `"%vkern"`
    VKERN,
    /// `"+s"`
    START_HIEROGLYPHS,
    /// `"|"([^-]|"\-")*` (the redundant `'\\'` alternative is dropped, see [MdcLexicon])
    TEXT_SUPER,
    /// `{TRUESPACE}` (the redundant `|"_"` alternative is dropped, `_` already being a `TRUESPACE` char)
    WORD_END_CANDIDATE,
    /// `({TRUESPACE}{TRUESPACE})+` (the redundant `"__"` alternative is dropped)
    SENTENCE_END_CANDIDATE,
    /// `"-"({TRUESPACE}|"-")*`, merging lines 288 and 289 (both plain `SEPARATOR`)
    SEPARATOR,
    /// `"-#-"|"-#"`, merging lines 292 and 304 (both an unconditional shading toggle)
    SHADING_TOGGLE_LITERAL,
    /// `"#-"|("#"" "+)`
    HASH_DASH_OR_SPACES,
    /// `"#"`
    HASH_ALONE,
    /// `-?"#b"`
    SHADING_ON,
    /// `-?"#e"`
    SHADING_OFF,
    /// `"$r"`
    TOGGLE_RED,
    /// `"$b"`
    TOGGLE_BLACK,
    /// `"?"`
    TOGGLE_LACUNA,
    /// `"??"`
    TOGGLE_LINE_LACUNA,
    /// `"^"`
    TOGGLE_OMIT,
    /// `"$"`
    TOGGLE_BLACK_RED,
    /// `"#""1"?"2"?"3"?"4"?`
    SHADING,
    /// `":"`
    COLON,
    /// `"*"`
    STAR,
    /// `"["` - switches to `PROPERTIES`
    OPEN_BRACE,
    /// `"<"`
    OLD_CARTOUCHE_PLAIN,
    /// `"<"[SFH]([bme])`
    OLD_CARTOUCHE_TYPED_PART,
    /// `"<"[SFHG]`
    OLD_CARTOUCHE_TYPED,
    /// `"<"[bme]`
    OLD_CARTOUCHE_PART,
    /// `"<"[sfhg][0123]`
    CARTOUCHE_START_TYPED_PART,
    /// `"<"[sfhg]`
    CARTOUCHE_START_TYPED,
    /// `"<"[012]`
    CARTOUCHE_START_PART,
    /// `">"`
    CARTOUCHE_END_DEFAULT,
    /// `[sfhg][0123]">"`
    CARTOUCHE_END_TYPED_PART,
    /// `[sfhg]">"`
    CARTOUCHE_END_TYPED,
    /// `[012]">"`
    CARTOUCHE_END_PART,
    /// `"[["`
    PHIL_ERASED_SIGNS_BEGIN,
    /// `"[{"`
    PHIL_EDITOR_SUPERFLUOUS_BEGIN,
    /// `"[\""`
    PHIL_PREVIOUSLY_READABLE_BEGIN,
    /// `"['"`
    PHIL_SCRIBE_ADDITION_BEGIN,
    /// `"[&"`
    PHIL_EDITOR_ADDITION_BEGIN,
    /// `"]]"`
    PHIL_ERASED_SIGNS_END,
    /// `"}]"`
    PHIL_EDITOR_SUPERFLUOUS_END,
    /// `"\"]"`
    PHIL_PREVIOUSLY_READABLE_END,
    /// `"']"`
    PHIL_SCRIBE_ADDITION_END,
    /// `"&]"`
    PHIL_EDITOR_ADDITION_END,
    /// `")]"`
    PHIL_MINOR_ADDITION_END,
    /// `"[("`
    PHIL_MINOR_ADDITION_BEGIN,
    /// `"?]"`
    PHIL_DUBIOUS_END,
    /// `"[?"`
    PHIL_DUBIOUS_BEGIN,
    /// `"("`
    LPAREN,
    /// `")"`
    RPAREN,
    /// `"##"`
    OVERWRITE_DOUBLE_HASH,
    /// `"&"`
    AMP,
    /// `"="`
    GRAMMAR_EQUALS,
    /// `\"?"`
    MODIFIER_QUESTION,
    /// `\R"-"?[0-9]*` (see [MdcLexicon])
    MODIFIER_R,
    /// `\([a-zA-Z]*[0-9]*)`
    MODIFIER_GENERIC,
    /// ``"`"``
    SIGN_BACKTICK,
    /// `"o"`
    SIGN_RED_POINT,
    /// `"O"`
    SIGN_BLACK_POINT,
    /// `\"([^\"\\\]]|\\.)*\"`
    SIGN_SMALL_TEXT,
    /// `(([A-Z]|"Aa")[0-9]+A*|[@a-zA-Z0-9]+|"@".)`
    SIGN_CODE,
    /// `"."`
    SIGN_HALF_SPACE,
    /// `".."`
    SIGN_FULL_SPACE,
    /// `"//"`
    SIGN_FULL_SHADE,
    /// `"v/"`
    SIGN_VERTICAL_SHADE,
    /// `"/"`
    SIGN_QUARTER_SHADE,
    /// `"h/"`
    SIGN_HORIZONTAL_SHADE,
    /// `"&&"|"**"`, merging lines 404 and 405 (both `DOUBLEAMP`)
    DOUBLE_AMP,
    /// `"&&&"`
    LIG_AFTER,
    /// `"^^^"|"^^"`, merging lines 407 and 408 (both `LIGBEFORE`)
    LIG_BEFORE,
    /// `"{{"` - switches to `PROPERTIES`
    DOUBLE_LEFT_CURLY,

    // ---- <PROPERTIES> ----

    /// `"}}"` - switches back to the initial state
    DOUBLE_RIGHT_CURLY,
    /// `"]"` - switches back to the initial state
    CLOSE_BRACE,
    /// `","`
    COMMA,
    /// `"="`
    EQUAL,
    /// `{INTEGER}`
    PROPERTY_INTEGER,
    /// `[a-zA-Z_][a-zA-Z0-9_]*`
    PROPERTY_IDENTIFIER,
    // `[ \t\n\015]`, always discarded, is the PROPERTIES state's skipped whitespace (see MdcLexicon).

    // ---- INITIAL and PROPERTIES states (no explicit state prefix) ----

    /// `.` - matches any single character not otherwise matched
    UNKNOWN
}
