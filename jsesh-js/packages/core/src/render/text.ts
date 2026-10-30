/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Alphabetic text: measuring (the [TextMeasurer] interface), runs of text
/// (port of `jsesh.render.layout.TextRun`) and display strings
/// (`TransliterationUtilities.getActualTransliterationString`).

import type { ScriptCode } from "../model/constants.js";
import { AlphabeticCharacter } from "../model/model.js";
import type { FontSpecification } from "./style.js";
import type { MDCView } from "./view.js";

/// The measure of a run of text: the visual offset of each piece from the
/// left of the run, its width, and the total width.
export interface RunMeasure {
  offsets: readonly number[];
  widths: readonly number[];
  width: number;
}

export interface SmallTextDimensions {
  width: number;
  height: number;
  ascent?: number;
}

export interface CharacterMetrics {
  advance: number;
  ascent: number;
  descent: number;
}

/// Measures text. The layout needs it for alphabetic text, superscripts and
/// small texts. Implementations: `CanvasTextMeasurer` in browsers; tests
/// replay the measures made by Java.
export interface TextMeasurer {
  /// Measures a run of characters of one script (with kerning). Each piece
  /// extends to the next piece in visual order; the widths add up to the
  /// width of the run.
  measureRun(fonts: FontSpecification, script: ScriptCode, pieces: readonly string[], rightToLeft: boolean): RunMeasure;
  /// The advance of a character, and the ascent and descent of its font.
  characterMetrics(fonts: FontSpecification, script: ScriptCode, text: string): CharacterMetrics;
  /// The size of a text in the superscript font (logical bounds), and, if
  /// known, its ascent.
  smallTextDimensions(fonts: FontSpecification, text: string): SmallTextDimensions;
}

/// Is this script written from right to left?
export function isRightToLeft(script: ScriptCode): boolean {
  return script === "HEBREW";
}

/// The string displayed for a character
/// (`AlphabeticCharacter.getDisplayString`).
export function displayString(c: AlphabeticCharacter, fonts: FontSpecification): string {
  if (c.script === "TRANSLITERATION" && c.codePoint < 0x10000) {
    return transliterationString(String.fromCharCode(c.codePoint), c.uppercase, fonts);
  }
  return String.fromCodePoint(c.codePoint);
}

/// One MdC transliteration letter, as displayed.
export function transliterationString(c: string, upper: boolean, fonts: FontSpecification): string {
  if (!fonts.translitUnicode) return c;
  let up: string;
  let down: string;
  switch (c) {
    case "A":
      [up, down] = ["Ꜣ", "ꜣ"];
      break;
    case "i":
      [up, down] = {
        U0313: ["I̓", "i̓"],
        UA7BD: ["Ꞽ", "ꞽ"],
        U0486: ["I҆", "i҆"],
        PLAIN_J: ["J", "j"],
      }[fonts.yodChoice] as [string, string];
      break;
    case "a":
      [up, down] = ["Ꜥ", "ꜥ"];
      break;
    case "H":
      [up, down] = ["Ḥ", "ḥ"];
      break;
    case "x":
      [up, down] = ["Ḫ", "ḫ"];
      break;
    case "X":
      [up, down] = ["H̱", "ẖ"];
      break;
    case "S":
      [up, down] = ["Š", "š"];
      break;
    case "q":
      [up, down] = fonts.gardinerQofUsed ? ["Ḳ", "ḳ"] : ["Q", "q"];
      break;
    case "T":
      [up, down] = ["Ṯ", "ṯ"];
      break;
    case "D":
      [up, down] = ["Ḏ", "ḏ"];
      break;
    default:
      [up, down] = [c.toUpperCase(), c];
  }
  return upper ? up : down;
}

/// Dash, closing and final-quote, and other punctuation (`Character.getType` in Java).
const BREAK_AFTER = /^[\p{Pd}\p{Pe}\p{Pf}\p{Po}]$/u;

/// A run of alphabetic text among the subviews of a view: consecutive
/// [AlphabeticCharacter] views of the same script, measured as a whole and
/// placed as one block.
export class TextRun {
  constructor(
    readonly start: number,
    readonly end: number,
    readonly offsets: readonly number[],
    readonly widths: readonly number[],
    readonly width: number,
  ) {}

  /// The x position of the `k`-th character in the parent's layout system:
  /// mirrored beforehand if the parent is drawn right to left, so that the
  /// text still reads in its own direction.
  layoutOffset(k: number, mirrored: boolean): number {
    return mirrored ? this.width - this.offsets[k]! - this.widths[k]! : this.offsets[k]!;
  }

  /// The visual left side of the characters `from` to `to - 1` (logical order),
  /// relative to the run.
  private minX(from: number, to: number): number {
    let min = Infinity;
    for (let k = from; k < to; k++) min = Math.min(min, this.offsets[k]!);
    return min;
  }

  /// The width of a part of the run, i.e. of the characters `from` to `to - 1`
  /// (logical order). The width of the whole run is the one of the part `0` to `n`.
  partWidth(from: number, to: number): number {
    let max = -Infinity;
    for (let k = from; k < to; k++) max = Math.max(max, this.offsets[k]! + this.widths[k]!);
    return max - this.minX(from, to);
  }

  /// The layout offsets of a part of the run (the whole run by default), when
  /// it is placed on its own: the x position of each character from `from` to
  /// `to - 1`, relative to the part.
  layoutOffsets(mirrored: boolean, from = 0, to = this.offsets.length): number[] {
    const min = this.minX(from, to);
    const width = this.partWidth(from, to);
    const result: number[] = [];
    for (let k = from; k < to; k++) {
      const x = this.offsets[k]! - min;
      result.push(mirrored ? width - x - this.widths[k]! : x);
    }
    return result;
  }

  /// Can a line end after this character? True for white space and
  /// punctuation (but not opening brackets and quotes).
  static canBreakAfter(c: AlphabeticCharacter): boolean {
    return c.isSpace() || BREAK_AFTER.test(String.fromCodePoint(c.codePoint));
  }

  views(parent: MDCView): MDCView[] {
    const result: MDCView[] = [];
    for (let i = this.start; i < this.end; i++) result.push(parent.getSubView(i));
    return result;
  }

  /// Finds the runs of text among the subviews of `parent`, measures them,
  /// and sets the width of each character view to its share of its run.
  static layoutRuns(parent: MDCView, fonts: FontSpecification, measurer: TextMeasurer): TextRun[] {
    const runs: TextRun[] = [];
    const n = parent.numberOfSubviews;
    let i = 0;
    while (i < n) {
      const first = parent.getSubView(i).model;
      if (!(first instanceof AlphabeticCharacter)) {
        i++;
        continue;
      }
      let end = i + 1;
      while (end < n) {
        const c = parent.getSubView(end).model;
        if (!(c instanceof AlphabeticCharacter) || !c.isSameScriptAs(first)) break;
        end++;
      }
      const pieces: string[] = [];
      for (let k = i; k < end; k++) pieces.push(displayString(parent.getSubView(k).model as AlphabeticCharacter, fonts));
      const m = measurer.measureRun(fonts, first.script, pieces, isRightToLeft(first.script));
      const run = new TextRun(i, end, m.offsets, m.widths, m.width);
      for (let k = i; k < end; k++) parent.getSubView(k).width = run.widths[k - i]!;
      runs.push(run);
      i = end;
    }
    return runs;
  }
}
