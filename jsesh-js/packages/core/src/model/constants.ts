/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Model constants: port of `jsesh.model.constants` and `jsesh.model.ShadingCode`.

/// Sign types (`SymbolCodes` in Java).
export const SymbolCodes = {
  HALFSPACE: 1,
  FULLSPACE: 2,
  REDPOINT: 3,
  BLACKPOINT: 4,
  FULLSHADE: 5,
  VERTICALSHADE: 6,
  HORIZONTALSHADE: 7,
  QUATERSHADE: 8,
  MDCCODE: 9,
  SMALLTEXT: 10,
  // Philological marks, when they are read as parenthesis (tksesh).
  ERASEDSIGNS: 50,
  EDITORADDITION: 51,
  EDITORSUPERFLUOUS: 52,
  PREVIOUSLYREADABLE: 53,
  SCRIBEADDITION: 54,
  MINORADDITION: 55,
  DUBIOUS: 56,
  // Philological marks as plain signs: x * 2 for the start, x * 2 + 1 for the end.
  BEGINERASE: 100,
  ENDERASE: 101,
  BEGINEDITORADDITION: 102,
  ENDEDITORADDITION: 103,
  BEGINEDITORSUPERFLUOUS: 104,
  ENDEDITORSUPERFLUOUS: 105,
  BEGINPREVIOUSLYREADABLE: 106,
  ENDPREVIOUSLYREADABLE: 107,
  BEGINSCRIBEADDITION: 108,
  ENDSCRIBEADDITION: 109,
  BEGINMINORADDITION: 110,
  ENDMINORADDITION: 111,
  BEGINDUBIOUS: 112,
  ENDDUBIOUS: 113,
} as const;

/// Shading bits of a cadrat (`ShadingCode` in Java).
export const ShadingCode = {
  NONE: 0,
  TOP_START: 1,
  TOP_END: 2,
  BOTTOM_START: 4,
  BOTTOM_END: 8,
  FULL: 15,
} as const;

/// `ShadingCode.toString`: `marker` followed by the digits of the shaded
/// quarters, or the empty string if nothing is shaded.
export function shadingToString(marker: string, sh: number): string {
  if (sh === ShadingCode.NONE) return "";
  let result = marker;
  if (sh & ShadingCode.TOP_START) result += "1";
  if (sh & ShadingCode.TOP_END) result += "2";
  if (sh & ShadingCode.BOTTOM_START) result += "3";
  if (sh & ShadingCode.BOTTOM_END) result += "4";
  return result;
}

export type CartoucheType = "CARTOUCHE" | "SEREKH" | "HWT" | "CASTLE" | "CIRCULAR_ENCLOSURE";

const CARTOUCHE_CODES: Record<CartoucheType, string> = {
  CARTOUCHE: "c",
  SEREKH: "s",
  HWT: "h",
  CASTLE: "f",
  CIRCULAR_ENCLOSURE: "g",
};

export function cartoucheTypeCode(type: CartoucheType): string {
  return CARTOUCHE_CODES[type];
}

export function cartoucheTypeForCode(code: string): CartoucheType {
  const lower = code.toLowerCase();
  for (const [type, c] of Object.entries(CARTOUCHE_CODES)) {
    if (c === lower) return type as CartoucheType;
  }
  throw new Error("Not a valid cartouche type code: " + code);
}

/// A cartouche part, as its digit: 0 (none), 1 (first), 2 (second), 3 (third).
export type CartouchePart = 0 | 1 | 2 | 3;

export type WordEndingCode = "NONE" | "WORD_END" | "SENTENCE_END";

export type ToggleType =
  | "SHADINGTOGGLE"
  | "SHADINGON"
  | "SHADINGOFF"
  | "RED"
  | "BLACK"
  | "LACUNA"
  | "LINELACUNA"
  | "OMMIT"
  | "BLACKRED";

export type TabbingJustification = "FILL" | "LEFT" | "RIGHT";
export type TextDirection = "LEFT_TO_RIGHT" | "RIGHT_TO_LEFT";
export type TextOrientation = "HORIZONTAL" | "VERTICAL";

/// Alphabetic scripts, keyed by the MdC letter following `+`.
export type ScriptCode =
  | "LATIN"
  | "BOLD"
  | "ITALIC"
  | "TRANSLITERATION"
  | "COPTIC"
  | "GREEK"
  | "HEBREW"
  | "CYRILLIC"
  | "OTHER";

const SCRIPT_LETTERS: Record<string, ScriptCode> = {
  l: "LATIN",
  b: "BOLD",
  i: "ITALIC",
  t: "TRANSLITERATION",
  c: "COPTIC",
  g: "GREEK",
  h: "HEBREW",
  r: "CYRILLIC",
};

/// The script for a MdC letter; `OTHER` if the letter is not a known script code.
export function scriptForMdcCode(code: string): ScriptCode {
  return SCRIPT_LETTERS[code] ?? "OTHER";
}

/// `LexicalSymbolsUtils` (codes of the special signs).
const CODES_FOR_SYMBOLS: Record<number, string> = {
  [SymbolCodes.HALFSPACE]: ".",
  [SymbolCodes.FULLSPACE]: "..",
  [SymbolCodes.REDPOINT]: "o",
  [SymbolCodes.BLACKPOINT]: "O",
  [SymbolCodes.FULLSHADE]: "//",
  [SymbolCodes.VERTICALSHADE]: "v/",
  [SymbolCodes.HORIZONTALSHADE]: "h/",
  [SymbolCodes.QUATERSHADE]: "/",
  [SymbolCodes.BEGINERASE]: "[[",
  [SymbolCodes.ENDERASE]: "]]",
  [SymbolCodes.BEGINEDITORADDITION]: "[&",
  [SymbolCodes.ENDEDITORADDITION]: "&]",
  [SymbolCodes.BEGINMINORADDITION]: "[(",
  [SymbolCodes.ENDMINORADDITION]: ")]",
  [SymbolCodes.BEGINDUBIOUS]: "[?",
  [SymbolCodes.ENDDUBIOUS]: "?]",
  [SymbolCodes.BEGINEDITORSUPERFLUOUS]: "[{",
  [SymbolCodes.ENDEDITORSUPERFLUOUS]: "}]",
  [SymbolCodes.BEGINPREVIOUSLYREADABLE]: '["',
  [SymbolCodes.ENDPREVIOUSLYREADABLE]: '"]',
  [SymbolCodes.BEGINSCRIBEADDITION]: "['",
  [SymbolCodes.ENDSCRIBEADDITION]: "']",
  [SymbolCodes.SMALLTEXT]: '""',
};

const TYPES_FOR_CODES = new Map<string, number>(
  Object.entries(CODES_FOR_SYMBOLS).map(([k, v]) => [v, Number(k)]),
);

export function codeForLexicalItem(type: number): string | undefined {
  return CODES_FOR_SYMBOLS[type];
}

/// The sign type of a special code, or -1 for an ordinary code.
export function typeForCode(code: string): number {
  return TYPES_FOR_CODES.get(code) ?? -1;
}

/// `LexicalSymbolsUtils.getOpenCodeForPhilology`.
export function openCodeForPhilology(type: number): string | undefined {
  return {
    [SymbolCodes.ERASEDSIGNS]: "[[",
    [SymbolCodes.EDITORADDITION]: "[&",
    [SymbolCodes.EDITORSUPERFLUOUS]: "[{",
    [SymbolCodes.PREVIOUSLYREADABLE]: '["',
    [SymbolCodes.SCRIBEADDITION]: "['",
  }[type];
}

/// `LexicalSymbolsUtils.getCloseCodeForPhilology`.
export function closeCodeForPhilology(type: number): string | undefined {
  return {
    [SymbolCodes.ERASEDSIGNS]: "]]",
    [SymbolCodes.EDITORADDITION]: "&]",
    [SymbolCodes.EDITORSUPERFLUOUS]: "}]",
    [SymbolCodes.PREVIOUSLYREADABLE]: '"]',
    [SymbolCodes.SCRIBEADDITION]: "']",
  }[type];
}

/// Where an inserted element is placed relative to the main element of an insertion
/// (`jsesh.model.constants.InsertionDirection`).
///
/// Directions are given relative to the writing direction: "start" and "end" are the left and
/// right sides in left-to-right text, and the opposite in right-to-left text.
export type InsertionDirection =
  | "START"
  | "END"
  | "TOP"
  | "BOTTOM"
  | "TOP_START"
  | "BOTTOM_START"
  | "TOP_END"
  | "BOTTOM_END"
  | "CENTER";

/// The short code of each direction, as in the `dir` option of `@insert`.
export const INSERTION_DIRECTION_CODES: Record<InsertionDirection, string> = {
  START: "s",
  END: "e",
  TOP: "t",
  BOTTOM: "b",
  TOP_START: "ts",
  BOTTOM_START: "bs",
  TOP_END: "te",
  BOTTOM_END: "be",
  CENTER: "c",
};

/// Does this direction have a "start" or "end" component?
export function isHorizontalInsertion(d: InsertionDirection): boolean {
  return d !== "TOP" && d !== "BOTTOM" && d !== "CENTER";
}

/// Does this direction have a "top" or "bottom" component?
export function isVerticalInsertion(d: InsertionDirection): boolean {
  return d !== "START" && d !== "END" && d !== "CENTER";
}

/// Is the inserted element on the start side? Only meaningful if [isHorizontalInsertion].
export function isStartInsertion(d: InsertionDirection): boolean {
  return d === "START" || d === "TOP_START" || d === "BOTTOM_START";
}

/// Is the inserted element on the top side? Only meaningful if [isVerticalInsertion].
export function isTopInsertion(d: InsertionDirection): boolean {
  return d === "TOP" || d === "TOP_START" || d === "TOP_END";
}
