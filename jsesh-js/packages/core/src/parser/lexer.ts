/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Hand-written scanner for the Manuel de Codage.
///
/// Port of `jsesh.parser.lexer.MdcLexicon` (the rules) and
/// `jsesh.parser.lexer.MdcLexer` (their interpretation as symbols).
///
/// As in the Java `mdwlexer`, the **longest match wins**, and on a tie the
/// token type declared first in the [TokenType] enum wins (its order is the
/// one of `MdcTokenType`; the order of the rule lists does not matter).

import {
  MdcSymbolCode,
  type MdcSymbol,
  type PhilologyKind,
  type PlainSignSubType,
  type ToggleType,
} from "./symbols.js";

enum TokenType {
  // INITIAL state
  PAGE_END,
  LINE_END,
  TAB_STOP,
  HRULE_THIN,
  HRULE_WIDE,
  ALPHABETIC_TEXT,
  ZONE,
  QUADRAT,
  INSERT,
  HKERN,
  VKERN,
  START_HIEROGLYPHS,
  TEXT_SUPER,
  WORD_END_CANDIDATE,
  SENTENCE_END_CANDIDATE,
  SEPARATOR,
  SHADING_TOGGLE_LITERAL,
  HASH_DASH_OR_SPACES,
  HASH_ALONE,
  SHADING_ON,
  SHADING_OFF,
  TOGGLE_RED,
  TOGGLE_BLACK,
  TOGGLE_LACUNA,
  TOGGLE_LINE_LACUNA,
  TOGGLE_OMIT,
  TOGGLE_BLACK_RED,
  SHADING,
  COLON,
  STAR,
  OPEN_BRACE,
  OLD_CARTOUCHE_PLAIN,
  OLD_CARTOUCHE_TYPED_PART,
  OLD_CARTOUCHE_TYPED,
  OLD_CARTOUCHE_PART,
  CARTOUCHE_START_TYPED_PART,
  CARTOUCHE_START_TYPED,
  CARTOUCHE_START_PART,
  CARTOUCHE_END_DEFAULT,
  CARTOUCHE_END_TYPED_PART,
  CARTOUCHE_END_TYPED,
  CARTOUCHE_END_PART,
  PHIL_ERASED_SIGNS_BEGIN,
  PHIL_EDITOR_SUPERFLUOUS_BEGIN,
  PHIL_PREVIOUSLY_READABLE_BEGIN,
  PHIL_SCRIBE_ADDITION_BEGIN,
  PHIL_EDITOR_ADDITION_BEGIN,
  PHIL_ERASED_SIGNS_END,
  PHIL_EDITOR_SUPERFLUOUS_END,
  PHIL_PREVIOUSLY_READABLE_END,
  PHIL_SCRIBE_ADDITION_END,
  PHIL_EDITOR_ADDITION_END,
  PHIL_MINOR_ADDITION_END,
  PHIL_MINOR_ADDITION_BEGIN,
  PHIL_DUBIOUS_END,
  PHIL_DUBIOUS_BEGIN,
  LPAREN,
  RPAREN,
  OVERWRITE_DOUBLE_HASH,
  AMP,
  GRAMMAR_EQUALS,
  MODIFIER_QUESTION,
  MODIFIER_R,
  MODIFIER_GENERIC,
  SIGN_BACKTICK,
  SIGN_RED_POINT,
  SIGN_BLACK_POINT,
  SIGN_SMALL_TEXT,
  SIGN_CODE,
  SIGN_HALF_SPACE,
  SIGN_FULL_SPACE,
  SIGN_FULL_SHADE,
  SIGN_VERTICAL_SHADE,
  SIGN_QUARTER_SHADE,
  SIGN_HORIZONTAL_SHADE,
  DOUBLE_AMP,
  LIG_AFTER,
  LIG_BEFORE,
  DOUBLE_LEFT_CURLY,
  // PROPERTIES state
  DOUBLE_RIGHT_CURLY,
  CLOSE_BRACE,
  COMMA,
  EQUAL,
  PROPERTY_INTEGER,
  PROPERTY_IDENTIFIER,
  // Both states
  UNKNOWN,
}

/// A matcher returns the length of the match at `pos`, or -1.
type Matcher = (src: string, pos: number) => number;

enum LexState {
  INITIAL,
  PROPERTIES,
}

interface Rule {
  type: TokenType;
  match: Matcher;
  /// State to switch to after this rule matched.
  next?: LexState;
}

function re(source: string): Matcher {
  const regexp = new RegExp(source, "suy");
  return (src, pos) => {
    regexp.lastIndex = pos;
    const m = regexp.exec(src);
    return m && m[0].length > 0 ? m[0].length : -1;
  };
}

function lit(s: string): Matcher {
  return (src, pos) => (src.startsWith(s, pos) ? s.length : -1);
}

function longestOf(...matchers: Matcher[]): Matcher {
  return (src, pos) => Math.max(...matchers.map((m) => m(src, pos)));
}

/// Any single code point.
const anyCharacter: Matcher = (src, pos) => {
  if (pos >= src.length) return -1;
  return (src.codePointAt(pos) ?? 0) > 0xffff ? 2 : 1;
};

/// `"+"[a-rt-z+](\+|[^+]|"+"[^a-z+])*`.
///
/// Hand-written rather than a regexp, so that the match is the longest one,
/// as with the Java DFA.
const alphabeticText: Matcher = (src, pos) => {
  if (src[pos] !== "+" || pos + 1 >= src.length) return -1;
  const start = src[pos + 1]!;
  if (!(/[a-rt-z+]/.test(start))) return -1;
  let i = pos + 2;
  while (i < src.length) {
    const c = src[i]!;
    if (c === "\\" && src[i + 1] === "+") {
      i += 2;
    } else if (c !== "+") {
      i += 1;
    } else if (i + 1 < src.length && !/[a-z+]/.test(src[i + 1]!)) {
      i += 2;
    } else {
      break;
    }
  }
  return i - pos;
};

const TRUESPACE = "[ \\t\\n\\r_]";
const ESPSO = "[ \\t\\n\\r_-]*";

const INITIAL_RULES: Rule[] = [
  { type: TokenType.PAGE_END, match: re(`!!${ESPSO}`) },
  { type: TokenType.LINE_END, match: re(`!(?:=[0-9]+%)?${ESPSO}`) },
  { type: TokenType.TAB_STOP, match: re("\\?[0-9]+") },
  { type: TokenType.HRULE_THIN, match: re("\\{l[0-9]+,[0-9]+\\}") },
  { type: TokenType.HRULE_WIDE, match: re("\\{L[0-9]+,[0-9]+\\}") },
  { type: TokenType.ALPHABETIC_TEXT, match: alphabeticText },
  { type: TokenType.ZONE, match: lit("%zone") },
  { type: TokenType.QUADRAT, match: lit("%quadrat") },
  { type: TokenType.INSERT, match: lit("%insert") },
  { type: TokenType.HKERN, match: lit("%hkern") },
  { type: TokenType.VKERN, match: lit("%vkern") },
  { type: TokenType.START_HIEROGLYPHS, match: lit("+s") },
  // "\-" is tried first, so that the greedy regexp gives the longest match.
  { type: TokenType.TEXT_SUPER, match: re("\\|(?:\\\\-|[^-])*") },
  { type: TokenType.WORD_END_CANDIDATE, match: re(TRUESPACE) },
  { type: TokenType.SENTENCE_END_CANDIDATE, match: re(`(?:${TRUESPACE}${TRUESPACE})+`) },
  { type: TokenType.SEPARATOR, match: re(`-${ESPSO}`) },
  { type: TokenType.SHADING_TOGGLE_LITERAL, match: re("-#-?") },
  { type: TokenType.HASH_DASH_OR_SPACES, match: re("#(?:-| +)") },
  { type: TokenType.HASH_ALONE, match: lit("#") },
  { type: TokenType.SHADING_ON, match: re("-?#b") },
  { type: TokenType.SHADING_OFF, match: re("-?#e") },
  { type: TokenType.TOGGLE_RED, match: lit("$r") },
  { type: TokenType.TOGGLE_BLACK, match: lit("$b") },
  { type: TokenType.TOGGLE_LACUNA, match: lit("?") },
  { type: TokenType.TOGGLE_LINE_LACUNA, match: lit("??") },
  { type: TokenType.TOGGLE_OMIT, match: lit("^") },
  { type: TokenType.TOGGLE_BLACK_RED, match: lit("$") },
  { type: TokenType.SHADING, match: re("#1?2?3?4?") },
  { type: TokenType.COLON, match: lit(":") },
  { type: TokenType.STAR, match: lit("*") },
  { type: TokenType.OPEN_BRACE, match: lit("["), next: LexState.PROPERTIES },
  { type: TokenType.OLD_CARTOUCHE_PLAIN, match: lit("<") },
  { type: TokenType.OLD_CARTOUCHE_TYPED_PART, match: re("<[SFH][bme]") },
  { type: TokenType.OLD_CARTOUCHE_TYPED, match: re("<[SFHG]") },
  { type: TokenType.OLD_CARTOUCHE_PART, match: re("<[bme]") },
  { type: TokenType.CARTOUCHE_START_TYPED_PART, match: re("<[sfhg][0123]") },
  { type: TokenType.CARTOUCHE_START_TYPED, match: re("<[sfhg]") },
  { type: TokenType.CARTOUCHE_START_PART, match: re("<[012]") },
  { type: TokenType.CARTOUCHE_END_DEFAULT, match: lit(">") },
  { type: TokenType.CARTOUCHE_END_TYPED_PART, match: re("[sfhg][0123]>") },
  { type: TokenType.CARTOUCHE_END_TYPED, match: re("[sfhg]>") },
  { type: TokenType.CARTOUCHE_END_PART, match: re("[012]>") },
  { type: TokenType.PHIL_ERASED_SIGNS_BEGIN, match: lit("[[") },
  { type: TokenType.PHIL_EDITOR_SUPERFLUOUS_BEGIN, match: lit("[{") },
  { type: TokenType.PHIL_PREVIOUSLY_READABLE_BEGIN, match: lit('["') },
  { type: TokenType.PHIL_SCRIBE_ADDITION_BEGIN, match: lit("['") },
  { type: TokenType.PHIL_EDITOR_ADDITION_BEGIN, match: lit("[&") },
  { type: TokenType.PHIL_ERASED_SIGNS_END, match: lit("]]") },
  { type: TokenType.PHIL_EDITOR_SUPERFLUOUS_END, match: lit("}]") },
  { type: TokenType.PHIL_PREVIOUSLY_READABLE_END, match: lit('"]') },
  { type: TokenType.PHIL_SCRIBE_ADDITION_END, match: lit("']") },
  { type: TokenType.PHIL_EDITOR_ADDITION_END, match: lit("&]") },
  { type: TokenType.PHIL_MINOR_ADDITION_END, match: lit(")]") },
  { type: TokenType.PHIL_MINOR_ADDITION_BEGIN, match: lit("[(") },
  { type: TokenType.PHIL_DUBIOUS_END, match: lit("?]") },
  { type: TokenType.PHIL_DUBIOUS_BEGIN, match: lit("[?") },
  { type: TokenType.LPAREN, match: lit("(") },
  { type: TokenType.RPAREN, match: lit(")") },
  { type: TokenType.OVERWRITE_DOUBLE_HASH, match: lit("##") },
  { type: TokenType.AMP, match: lit("&") },
  { type: TokenType.GRAMMAR_EQUALS, match: lit("=") },
  { type: TokenType.MODIFIER_QUESTION, match: lit("\\?") },
  { type: TokenType.MODIFIER_R, match: re("\\\\R-?[0-9]*") },
  { type: TokenType.MODIFIER_GENERIC, match: re("\\\\[A-Za-z]*[0-9]*") },
  { type: TokenType.SIGN_BACKTICK, match: lit("`") },
  { type: TokenType.SIGN_RED_POINT, match: lit("o") },
  { type: TokenType.SIGN_BLACK_POINT, match: lit("O") },
  { type: TokenType.SIGN_SMALL_TEXT, match: re('"(?:[^"\\\\\\]]|\\\\.)*"') },
  {
    type: TokenType.SIGN_CODE,
    match: longestOf(re("(?:[A-Z]|Aa)[0-9]+A*"), re("[@a-zA-Z0-9]+"), re("@.")),
  },
  { type: TokenType.SIGN_HALF_SPACE, match: lit(".") },
  { type: TokenType.SIGN_FULL_SPACE, match: lit("..") },
  { type: TokenType.SIGN_FULL_SHADE, match: lit("//") },
  { type: TokenType.SIGN_VERTICAL_SHADE, match: lit("v/") },
  { type: TokenType.SIGN_QUARTER_SHADE, match: lit("/") },
  { type: TokenType.SIGN_HORIZONTAL_SHADE, match: lit("h/") },
  { type: TokenType.DOUBLE_AMP, match: longestOf(lit("&&"), lit("**")) },
  { type: TokenType.LIG_AFTER, match: lit("&&&") },
  { type: TokenType.LIG_BEFORE, match: longestOf(lit("^^^"), lit("^^")) },
  { type: TokenType.DOUBLE_LEFT_CURLY, match: lit("{{"), next: LexState.PROPERTIES },
  { type: TokenType.UNKNOWN, match: anyCharacter },
  { type: TokenType.COMMA, match: lit(",") },
];

const PROPERTIES_RULES: Rule[] = [
  { type: TokenType.DOUBLE_RIGHT_CURLY, match: lit("}}"), next: LexState.INITIAL },
  { type: TokenType.CLOSE_BRACE, match: lit("]"), next: LexState.INITIAL },
  { type: TokenType.COMMA, match: lit(",") },
  { type: TokenType.EQUAL, match: lit("=") },
  { type: TokenType.PROPERTY_INTEGER, match: re("[0-9]+") },
  { type: TokenType.PROPERTY_IDENTIFIER, match: re("[A-Za-z_][A-Za-z0-9_]*") },
  { type: TokenType.UNKNOWN, match: anyCharacter },
];

const PROPERTIES_WHITESPACE = /[ \t\n\r]*/y;

/// A stateful scanner for MdC source text.
///
/// Port of `jsesh.parser.lexer.MdcLexer`.
export class MdcLexer {
  private pos = 0;
  private state = LexState.INITIAL;
  /// Are philological brackets (`[[...]]` and friends) treated as plain signs?
  philologyAsSigns = false;
  /// Is a run of whitespace right now significant (word/sentence end) or just filler?
  private expectSpace = false;
  /// Did the last emitted symbol denote a sign, making a following `#` an overwrite?
  private justAfterSign = false;

  constructor(private readonly source: string) {}

  /// The offset where the next symbol starts.
  position(): number {
    return this.pos;
  }

  /// Reads the next symbol, or `undefined` at end of input.
  nextSymbol(): MdcSymbol | undefined {
    for (;;) {
      const token = this.nextToken();
      if (token === undefined) return undefined;
      const symbol = this.interpret(token.type, token.text, token.start);
      if (symbol !== undefined) {
        this.fixExpect(symbol);
        return symbol;
      }
    }
  }

  private nextToken(): { type: TokenType; text: string; start: number } | undefined {
    if (this.state === LexState.PROPERTIES) {
      PROPERTIES_WHITESPACE.lastIndex = this.pos;
      PROPERTIES_WHITESPACE.exec(this.source);
      this.pos = PROPERTIES_WHITESPACE.lastIndex;
    }
    if (this.pos >= this.source.length) return undefined;
    const rules = this.state === LexState.INITIAL ? INITIAL_RULES : PROPERTIES_RULES;
    let best: Rule | undefined;
    let bestLength = 0;
    for (const rule of rules) {
      const length = rule.match(this.source, this.pos);
      if (length > bestLength || (length === bestLength && length > 0 && rule.type < best!.type)) {
        best = rule;
        bestLength = length;
      }
    }
    // UNKNOWN matches any character, so best is always defined here.
    const start = this.pos;
    this.pos += bestLength;
    if (best!.next !== undefined) this.state = best!.next;
    return { type: best!.type, text: this.source.substring(start, this.pos), start };
  }

  /// Handle spaces, which can be both significant as word ending, or simple
  /// separators, depending on the previous symbol.
  private fixExpect(symbol: MdcSymbol): void {
    this.expectSpace =
      symbol.code === MdcSymbolCode.HIEROGLYPH ||
      symbol.code === MdcSymbolCode.MODIFIER ||
      symbol.code === MdcSymbolCode.DOUBLE_RIGHT_CURLY;
    this.justAfterSign = symbol.code === MdcSymbolCode.HIEROGLYPH;
  }

  private interpret(type: TokenType, text: string, start: number): MdcSymbol | undefined {
    const emit = (code: MdcSymbolCode, value: unknown = null): MdcSymbol => ({
      code,
      value,
      text,
      position: start,
    });
    const sign = (subtype: PlainSignSubType) =>
      emit(MdcSymbolCode.HIEROGLYPH, { subtype, text });
    const toggle = (t: ToggleType) => emit(MdcSymbolCode.TOGGLE, t);
    const philology = (kind: PhilologyKind, begin: boolean) =>
      this.philologyAsSigns
        ? emit(MdcSymbolCode.HIEROGLYPH, { subtype: { nodeType: "Philology", kind, begin }, text })
        : emit(begin ? MdcSymbolCode.BEGIN_PHIL : MdcSymbolCode.END_PHIL, kind);

    switch (type) {
      case TokenType.PAGE_END:
        return emit(MdcSymbolCode.PAGE_END);
      case TokenType.LINE_END:
        return emit(MdcSymbolCode.LINE_END, extractLineSkip(text));
      case TokenType.TAB_STOP:
        return emit(MdcSymbolCode.TAB_STOP, parseInt(text.substring(1), 10));
      case TokenType.HRULE_THIN:
        return emit(MdcSymbolCode.HRULE, parseHRule("l", text));
      case TokenType.HRULE_WIDE:
        return emit(MdcSymbolCode.HRULE, parseHRule("L", text));
      case TokenType.ALPHABETIC_TEXT:
        return emit(MdcSymbolCode.TEXT, parseAlphabeticText(text));
      case TokenType.ZONE:
        return emit(MdcSymbolCode.ZONE);
      case TokenType.QUADRAT:
        return emit(MdcSymbolCode.QUADRAT);
      case TokenType.INSERT:
        return emit(MdcSymbolCode.INSERT);
      case TokenType.HKERN:
        return emit(MdcSymbolCode.HKERN);
      case TokenType.VKERN:
        return emit(MdcSymbolCode.VKERN);
      case TokenType.START_HIEROGLYPHS:
        return emit(MdcSymbolCode.START_HIEROGLYPHS);
      case TokenType.TEXT_SUPER:
        return emit(MdcSymbolCode.TEXT_SUPER, text.substring(1));
      case TokenType.WORD_END_CANDIDATE:
        return this.expectSpace ? emit(MdcSymbolCode.WORD_END) : undefined;
      case TokenType.SENTENCE_END_CANDIDATE:
        return this.expectSpace ? emit(MdcSymbolCode.SENTENCE_END) : undefined;
      case TokenType.SEPARATOR:
        return emit(MdcSymbolCode.SEPARATOR);
      case TokenType.SHADING_TOGGLE_LITERAL:
        return toggle("SHADING_TOGGLE");
      case TokenType.HASH_DASH_OR_SPACES:
        return this.justAfterSign ? emit(MdcSymbolCode.SHADING, "#1234") : toggle("SHADING_TOGGLE");
      case TokenType.HASH_ALONE:
        return this.justAfterSign ? emit(MdcSymbolCode.OVERWRITE) : toggle("SHADING_TOGGLE");
      case TokenType.SHADING_ON:
        return toggle("SHADING_ON");
      case TokenType.SHADING_OFF:
        return toggle("SHADING_OFF");
      case TokenType.TOGGLE_RED:
        return toggle("RED");
      case TokenType.TOGGLE_BLACK:
        return toggle("BLACK");
      case TokenType.TOGGLE_LACUNA:
        return toggle("LACUNA");
      case TokenType.TOGGLE_LINE_LACUNA:
        return toggle("LINE_LACUNA");
      case TokenType.TOGGLE_OMIT:
        return toggle("OMIT");
      case TokenType.TOGGLE_BLACK_RED:
        return toggle("BLACK_RED");
      case TokenType.SHADING:
        return emit(MdcSymbolCode.SHADING, text);
      case TokenType.COLON:
        return emit(MdcSymbolCode.COLON);
      case TokenType.STAR:
        return emit(MdcSymbolCode.STAR);
      case TokenType.OPEN_BRACE:
        return emit(MdcSymbolCode.OPEN_BRACE);
      case TokenType.OLD_CARTOUCHE_PLAIN:
        return emit(MdcSymbolCode.BEGIN_OLD_CARTOUCHE, { code: "c", part: "a" });
      case TokenType.OLD_CARTOUCHE_TYPED_PART:
        return emit(MdcSymbolCode.BEGIN_OLD_CARTOUCHE, { code: text[1], part: text[2] });
      case TokenType.OLD_CARTOUCHE_TYPED:
        return emit(MdcSymbolCode.BEGIN_OLD_CARTOUCHE, { code: text[1], part: "a" });
      case TokenType.OLD_CARTOUCHE_PART:
        return emit(MdcSymbolCode.BEGIN_OLD_CARTOUCHE, { code: "c", part: text[1] });
      case TokenType.CARTOUCHE_START_TYPED_PART:
        return emit(MdcSymbolCode.BEGIN_CARTOUCHE, { type: text[1], part: digit(text[2]!) });
      case TokenType.CARTOUCHE_START_TYPED:
        return emit(MdcSymbolCode.BEGIN_CARTOUCHE, { type: text[1], part: 1 });
      case TokenType.CARTOUCHE_START_PART:
        return emit(MdcSymbolCode.BEGIN_CARTOUCHE, { type: "c", part: digit(text[1]!) });
      case TokenType.CARTOUCHE_END_DEFAULT:
        return emit(MdcSymbolCode.END_CARTOUCHE, { type: "c", part: 2 });
      case TokenType.CARTOUCHE_END_TYPED_PART:
        return emit(MdcSymbolCode.END_CARTOUCHE, { type: text[0], part: digit(text[1]!) });
      case TokenType.CARTOUCHE_END_TYPED:
        return emit(MdcSymbolCode.END_CARTOUCHE, { type: text[0], part: 2 });
      case TokenType.CARTOUCHE_END_PART:
        return emit(MdcSymbolCode.END_CARTOUCHE, { type: "c", part: digit(text[0]!) });
      case TokenType.PHIL_ERASED_SIGNS_BEGIN:
        return philology("ERASED_SIGNS", true);
      case TokenType.PHIL_ERASED_SIGNS_END:
        return philology("ERASED_SIGNS", false);
      case TokenType.PHIL_EDITOR_SUPERFLUOUS_BEGIN:
        return philology("EDITOR_SUPERFLUOUS", true);
      case TokenType.PHIL_EDITOR_SUPERFLUOUS_END:
        return philology("EDITOR_SUPERFLUOUS", false);
      case TokenType.PHIL_PREVIOUSLY_READABLE_BEGIN:
        return philology("PREVIOUSLY_READABLE", true);
      case TokenType.PHIL_PREVIOUSLY_READABLE_END:
        return philology("PREVIOUSLY_READABLE", false);
      case TokenType.PHIL_SCRIBE_ADDITION_BEGIN:
        return philology("SCRIBE_ADDITION", true);
      case TokenType.PHIL_SCRIBE_ADDITION_END:
        return philology("SCRIBE_ADDITION", false);
      case TokenType.PHIL_EDITOR_ADDITION_BEGIN:
        return philology("EDITOR_ADDITION", true);
      case TokenType.PHIL_EDITOR_ADDITION_END:
        return philology("EDITOR_ADDITION", false);
      case TokenType.PHIL_MINOR_ADDITION_END:
        return philology("MINOR_ADDITION", false);
      case TokenType.PHIL_MINOR_ADDITION_BEGIN:
        return philology("MINOR_ADDITION", true);
      case TokenType.PHIL_DUBIOUS_END:
        return philology("DUBIOUS", false);
      case TokenType.PHIL_DUBIOUS_BEGIN:
        return philology("DUBIOUS", true);
      case TokenType.LPAREN:
        return emit(MdcSymbolCode.LPAREN);
      case TokenType.RPAREN:
        return emit(MdcSymbolCode.RPAREN);
      case TokenType.OVERWRITE_DOUBLE_HASH:
        return emit(MdcSymbolCode.OVERWRITE);
      case TokenType.AMP:
        return emit(MdcSymbolCode.AMP);
      case TokenType.GRAMMAR_EQUALS:
        return emit(MdcSymbolCode.GRAMMAR);
      case TokenType.MODIFIER_QUESTION:
      case TokenType.MODIFIER_R:
      case TokenType.MODIFIER_GENERIC:
        return emit(MdcSymbolCode.MODIFIER, parseModifier(text));
      case TokenType.SIGN_BACKTICK:
      case TokenType.SIGN_CODE:
        return sign("MDC_CODE");
      case TokenType.SIGN_RED_POINT:
        return sign("RED_POINT");
      case TokenType.SIGN_BLACK_POINT:
        return sign("BLACK_POINT");
      case TokenType.SIGN_SMALL_TEXT:
        return sign("SMALL_TEXT");
      case TokenType.SIGN_HALF_SPACE:
        return sign("HALF_SPACE");
      case TokenType.SIGN_FULL_SPACE:
        return sign("FULL_SPACE");
      case TokenType.SIGN_FULL_SHADE:
        return sign("FULL_SHADE");
      case TokenType.SIGN_VERTICAL_SHADE:
        return sign("VERTICAL_SHADE");
      case TokenType.SIGN_QUARTER_SHADE:
        return sign("QUARTER_SHADE");
      case TokenType.SIGN_HORIZONTAL_SHADE:
        return sign("HORIZONTAL_SHADE");
      case TokenType.DOUBLE_AMP:
        return emit(MdcSymbolCode.DOUBLE_AMP);
      case TokenType.LIG_AFTER:
        return emit(MdcSymbolCode.LIG_AFTER);
      case TokenType.LIG_BEFORE:
        return emit(MdcSymbolCode.LIG_BEFORE);
      case TokenType.DOUBLE_LEFT_CURLY:
        return emit(MdcSymbolCode.DOUBLE_LEFT_CURLY);
      case TokenType.DOUBLE_RIGHT_CURLY:
        return emit(MdcSymbolCode.DOUBLE_RIGHT_CURLY);
      case TokenType.CLOSE_BRACE:
        return emit(MdcSymbolCode.CLOSE_BRACE);
      case TokenType.COMMA:
        return emit(MdcSymbolCode.COMMA);
      case TokenType.EQUAL:
        return emit(MdcSymbolCode.EQUAL);
      case TokenType.PROPERTY_INTEGER:
        return emit(MdcSymbolCode.INTEGER, parseInt(text, 10));
      case TokenType.PROPERTY_IDENTIFIER:
        return emit(MdcSymbolCode.IDENTIFIER, text);
      case TokenType.UNKNOWN:
        return emit(MdcSymbolCode.UNKNOWN, text);
    }
  }
}

function digit(c: string): number {
  return c.charCodeAt(0) - 48;
}

/// `buildHRule`: parses `"{" type start "," end "}"`.
function parseHRule(type: string, text: string) {
  const comma = text.indexOf(",");
  return {
    type,
    start: parseInt(text.substring(2, comma), 10),
    end: parseInt(text.substring(comma + 1, text.indexOf("}")), 10),
  };
}

/// `name` is the letters (or `?`) right after the leading `\`, `value` the
/// optional trailing integer.
function parseModifier(text: string) {
  let endPos = 1;
  while (endPos < text.length && /[\p{L}?]/u.test(text[endPos]!)) endPos++;
  const name = text.substring(1, endPos);
  const value = endPos < text.length ? parseInt(text.substring(endPos), 10) : null;
  return { name, value };
}

/// Resolves the `\+` and `\\` escapes of an alphabetic text block.
function parseAlphabeticText(text: string) {
  let body = text.length > 2 ? text.substring(2) : "";
  body = body.replaceAll("\\+", "+").replaceAll("\\\\", "\\");
  return { code: text[1]!, text: body };
}

/// Percentage of normal line skip, defaulting to 100.
function extractLineSkip(text: string): number {
  const eq = text.indexOf("=");
  if (eq === -1) return 100;
  return parseInt(text.substring(eq + 1, text.indexOf("%")), 10);
}
