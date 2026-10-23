/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Lexer-level symbols, port of `jsesh.parser.lexer.symbols`.

export enum MdcSymbolCode {
  PAGE_END = "PAGE_END",
  LINE_END = "LINE_END",
  TAB_STOP = "TAB_STOP",
  TABBING = "TABBING",
  TABBING_CLEAR = "TABBING_CLEAR",
  HRULE = "HRULE",
  ZONE = "ZONE",
  QUADRAT = "QUADRAT",
  INSERT = "INSERT",
  UNKNOWN_OPERATOR = "UNKNOWN_OPERATOR",
  START_HIEROGLYPHS = "START_HIEROGLYPHS",
  TEXT_SUPER = "TEXT_SUPER",
  WORD_END = "WORD_END",
  SENTENCE_END = "SENTENCE_END",
  SEPARATOR = "SEPARATOR",
  TOGGLE = "TOGGLE",
  SHADING = "SHADING",
  COLON = "COLON",
  STAR = "STAR",
  OPEN_BRACE = "OPEN_BRACE",
  BEGIN_OLD_CARTOUCHE = "BEGIN_OLD_CARTOUCHE",
  BEGIN_CARTOUCHE = "BEGIN_CARTOUCHE",
  END_CARTOUCHE = "END_CARTOUCHE",
  BEGIN_PHIL = "BEGIN_PHIL",
  END_PHIL = "END_PHIL",
  LPAREN = "LPAREN",
  RPAREN = "RPAREN",
  OVERWRITE = "OVERWRITE",
  AMP = "AMP",
  GRAMMAR = "GRAMMAR",
  MODIFIER = "MODIFIER",
  HIEROGLYPH = "HIEROGLYPH",
  DOUBLE_AMP = "DOUBLE_AMP",
  LIG_AFTER = "LIG_AFTER",
  LIG_BEFORE = "LIG_BEFORE",
  DOUBLE_LEFT_CURLY = "DOUBLE_LEFT_CURLY",
  DOUBLE_RIGHT_CURLY = "DOUBLE_RIGHT_CURLY",
  CLOSE_BRACE = "CLOSE_BRACE",
  COMMA = "COMMA",
  EQUAL = "EQUAL",
  INTEGER = "INTEGER",
  IDENTIFIER = "IDENTIFIER",
  TEXT = "TEXT",
  UNKNOWN = "UNKNOWN",
  EOF = "EOF",
}

export type PhilologyKind =
  | "ERASED_SIGNS"
  | "EDITOR_SUPERFLUOUS"
  | "PREVIOUSLY_READABLE"
  | "SCRIBE_ADDITION"
  | "EDITOR_ADDITION"
  | "MINOR_ADDITION"
  | "DUBIOUS";

export type ToggleType =
  | "SHADING_TOGGLE"
  | "SHADING_ON"
  | "SHADING_OFF"
  | "RED"
  | "BLACK"
  | "BLACK_RED"
  | "LACUNA"
  | "LINE_LACUNA"
  | "OMIT";

/// The plain sign subtypes (`SignSubType.Plain` in Java).
export type PlainSignSubType =
  | "MDC_CODE"
  | "RED_POINT"
  | "BLACK_POINT"
  | "SMALL_TEXT"
  | "HALF_SPACE"
  | "FULL_SPACE"
  | "FULL_SHADE"
  | "VERTICAL_SHADE"
  | "QUARTER_SHADE"
  | "HORIZONTAL_SHADE";

/// A philological bracket read as a sign (`SignSubType.Philology` in Java).
export interface PhilologySignSubType {
  nodeType: "Philology";
  kind: PhilologyKind;
  begin: boolean;
}

export type SignSubType = PlainSignSubType | PhilologySignSubType;

export interface AlphabeticTextValue {
  code: string;
  text: string;
}

export interface CartoucheValue {
  type: string;
  part: number;
}

export interface OldCartoucheStartValue {
  code: string;
  part: string;
}

export interface HRuleValue {
  type: string;
  start: number;
  end: number;
}

export interface MdcSignValue {
  subtype: SignSubType;
  text: string;
}

export interface ModifierValue {
  name: string;
  value: number | null;
}

/// One symbol produced by the lexer.
///
/// `position` is the UTF-16 offset of the symbol in the source (the Java
/// version counts code points; they only differ after astral characters).
export interface MdcSymbol {
  code: MdcSymbolCode;
  value: unknown;
  text: string;
  position: number;
}
