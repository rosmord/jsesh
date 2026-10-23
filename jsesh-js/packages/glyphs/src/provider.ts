/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// A [GlyphProvider] reading the data exported from JSesh by
/// `./gradlew :jseshJsExport:exportGlyphData`.

import type { ExplicitPosition, GlyphProvider, LigatureZone, SignShape } from "@jsesh/core";
import { fileNameForCode } from "./glyph-files.js";

/// A zone: x, y, width, height and gravity (`s`, `c` or `e`, then `t`, `c` or `b`).
export type ZoneEntry = [number, number, number, number, string];

/// A sign: width, height, and, if it has any, its three ligature zones.
export type GlyphEntry = [number, number] | [number, number, (ZoneEntry | null)[]];

/// The content of `glyph-index.json`.
export interface GlyphIndexData {
  format: number;
  /// Canonical code of the codes which have one (mostly phonetic codes).
  canonical: Record<string, string>;
  /// Predefined ligatures: canonical codes, and `[x, y, scale]` for each sign.
  ligatures: [string[], [number, number, number][]][];
  /// The signs, by canonical code.
  glyphs: Record<string, GlyphEntry>;
}

/// Loads the shape stored in a file of the `glyphs` folder.
export type ShapeLoader = (fileName: string) => Promise<SignShape>;

const HORIZONTAL_GRAVITY = { s: "START", c: "CENTER", e: "END" } as const;
const VERTICAL_GRAVITY = { t: "TOP", c: "CENTER", b: "BOTTOM" } as const;

export class GlyphIndexProvider implements GlyphProvider {
  private readonly ligatures = new Map<string, readonly ExplicitPosition[]>();
  private readonly shapes = new Map<string, SignShape>();
  private readonly pending = new Map<string, Promise<void>>();

  constructor(
    private readonly index: GlyphIndexData,
    private readonly loadShape: ShapeLoader,
  ) {
    for (const [codes, positions] of index.ligatures) {
      this.ligatures.set(codes.join("\0"), positions.map(([x, y, scale]) => ({ x, y, scale })));
    }
  }

  /// All the canonical codes of the signs.
  get codes(): string[] {
    return Object.keys(this.index.glyphs);
  }

  /// `ManuelDeCodage.getCanonicalCode`: the Gardiner code for a phonetic
  /// code; a one-letter `H` or `V` variant after a digit is lowercased.
  canonicalCode(code: string): string {
    const result = this.index.canonical[code];
    if (result !== undefined) return result;
    let fixedCode = code;
    if ((code.endsWith("H") || code.endsWith("V")) && code.length > 1 && /\d/.test(code[code.length - 2]!)) {
      fixedCode = code.substring(0, code.length - 1) + code.substring(code.length - 1).toLowerCase();
    }
    return this.index.canonical[fixedCode] ?? fixedCode;
  }

  size(code: string): { width: number; height: number } | undefined {
    const entry = this.index.glyphs[code];
    return entry === undefined ? undefined : { width: entry[0], height: entry[1] };
  }

  ligatureZone(code: string, i: number): LigatureZone | undefined {
    const z = this.index.glyphs[code]?.[2]?.[i];
    if (z === undefined || z === null) return undefined;
    const [x, y, width, height, gravity] = z;
    return {
      x,
      y,
      width,
      height,
      horizontalGravity: HORIZONTAL_GRAVITY[gravity[0] as keyof typeof HORIZONTAL_GRAVITY],
      verticalGravity: VERTICAL_GRAVITY[gravity[1] as keyof typeof VERTICAL_GRAVITY],
    };
  }

  ligaturePositions(codes: readonly string[]): readonly ExplicitPosition[] | undefined {
    return this.ligatures.get(codes.join("\0"));
  }

  shape(code: string): SignShape | undefined {
    return this.shapes.get(code);
  }

  async loadShapes(codes: Iterable<string>): Promise<void> {
    const waits: Promise<void>[] = [];
    for (const code of codes) {
      if (this.shapes.has(code) || this.index.glyphs[code] === undefined) continue;
      let p = this.pending.get(code);
      if (p === undefined) {
        p = this.loadShape(fileNameForCode(code)).then(
          (shape) => {
            this.shapes.set(code, shape);
            this.pending.delete(code);
          },
          (error: unknown) => {
            this.pending.delete(code);
            console.warn(`Could not load the shape of ${code}`, error);
          },
        );
        this.pending.set(code, p);
      }
      waits.push(p);
    }
    await Promise.all(waits);
  }
}
