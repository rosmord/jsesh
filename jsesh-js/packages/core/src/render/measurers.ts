/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// [TextMeasurer] implementations: with a canvas (browsers), or
/// approximate (when no canvas is available, for instance on a server).

import type { ScriptCode } from "../model/constants.js";
import { cssFont, fontForScript, type FontDescription, type FontSpecification } from "./style.js";
import type { CharacterMetrics, RunMeasure, SmallTextDimensions, TextMeasurer } from "./text.js";

/// Measures a whole run from the widths of its prefixes, so that kerning is
/// taken into account.
function measureRunWith(
  pieces: readonly string[],
  rightToLeft: boolean,
  width: (s: string) => number,
): RunMeasure {
  const prefixes = [0];
  let s = "";
  for (const p of pieces) {
    s += p;
    prefixes.push(width(s));
  }
  const total = prefixes[pieces.length]!;
  const widths = pieces.map((_, k) => Math.max(0, prefixes[k + 1]! - prefixes[k]!));
  const offsets = pieces.map((_, k) => (rightToLeft ? total - prefixes[k + 1]! : prefixes[k]!));
  return { offsets, widths, width: total };
}

/// Measures text with a canvas (`OffscreenCanvas`, or a `canvas` element).
///
/// The fonts must be loaded before measuring (see `document.fonts.load`).
export class CanvasTextMeasurer implements TextMeasurer {
  private readonly context: CanvasRenderingContext2D | OffscreenCanvasRenderingContext2D;

  constructor() {
    let context: CanvasRenderingContext2D | OffscreenCanvasRenderingContext2D | null = null;
    if (typeof OffscreenCanvas !== "undefined") {
      context = new OffscreenCanvas(1, 1).getContext("2d");
    } else if (typeof document !== "undefined") {
      context = document.createElement("canvas").getContext("2d");
    }
    if (context === null) throw new Error("No canvas available to measure text");
    this.context = context;
  }

  private metrics(font: FontDescription, text: string): TextMetrics {
    this.context.font = cssFont(font);
    return this.context.measureText(text);
  }

  measureRun(fonts: FontSpecification, script: ScriptCode, pieces: readonly string[], rightToLeft: boolean): RunMeasure {
    const font = fontForScript(fonts, script);
    return measureRunWith(pieces, rightToLeft, (s) => this.metrics(font, s).width);
  }

  characterMetrics(fonts: FontSpecification, script: ScriptCode, text: string): CharacterMetrics {
    const font = fontForScript(fonts, script);
    const m = this.metrics(font, text);
    return {
      advance: m.width,
      ascent: m.fontBoundingBoxAscent ?? font.size * 0.8,
      descent: m.fontBoundingBoxDescent ?? font.size * 0.2,
    };
  }

  smallTextDimensions(fonts: FontSpecification, text: string): SmallTextDimensions {
    const m = this.metrics(fonts.superScriptFont, text);
    const ascent = m.fontBoundingBoxAscent ?? fonts.superScriptFont.size * 0.8;
    const descent = m.fontBoundingBoxDescent ?? fonts.superScriptFont.size * 0.2;
    return { width: m.width, height: ascent + descent, ascent };
  }
}

/// A rough measurer, for when no canvas is available: characters are half
/// as wide as the font size (a bit more for capitals).
export class ApproximateTextMeasurer implements TextMeasurer {
  private width(font: FontDescription, text: string): number {
    let w = 0;
    for (const c of text) w += font.size * (c >= "A" && c <= "Z" ? 0.65 : c === " " ? 0.25 : 0.5);
    return w;
  }

  measureRun(fonts: FontSpecification, script: ScriptCode, pieces: readonly string[], rightToLeft: boolean): RunMeasure {
    const font = fontForScript(fonts, script);
    return measureRunWith(pieces, rightToLeft, (s) => this.width(font, s));
  }

  characterMetrics(fonts: FontSpecification, script: ScriptCode, text: string): CharacterMetrics {
    const font = fontForScript(fonts, script);
    return { advance: this.width(font, text), ascent: font.size * 0.9, descent: font.size * 0.22 };
  }

  smallTextDimensions(fonts: FontSpecification, text: string): SmallTextDimensions {
    const font = fonts.superScriptFont;
    return { width: this.width(font, text), height: font.size * 1.15, ascent: font.size * 0.9 };
  }
}
