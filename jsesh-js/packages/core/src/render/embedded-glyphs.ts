/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Drawing the signs embedded in a document: a [GlyphProvider] which answers
/// for `sign1`, `sign2`... from the drawing JSesh stored next to their SVG,
/// and asks another provider for the other signs. Mirrors the Java
/// `EmbeddedSignShapeRepository`, put in front of the global fonts.

import {
  embeddedSignRendering,
  type EmbeddedSign,
  type EmbeddedSignRendering,
  type EmbeddedSigns,
} from "../document/embedded-signs.js";
import type {
  ExplicitPosition,
  GlyphProvider,
  HorizontalGravity,
  LigatureZone,
  SignShape,
  VerticalGravity,
} from "./glyphs.js";

/// An embedded sign, ready to draw.
interface EmbeddedGlyph {
  width: number;
  height: number;
  shape: SignShape;
  zones: (LigatureZone | undefined)[];
}

const HORIZONTAL_GRAVITY: Record<string, HorizontalGravity> = { s: "START", c: "CENTER", e: "END" };
const VERTICAL_GRAVITY: Record<string, VerticalGravity> = { t: "TOP", c: "CENTER", b: "BOTTOM" };

/// Converts the drawing data of a sign for the layout.
function toGlyph(r: EmbeddedSignRendering): EmbeddedGlyph {
  return {
    width: r.w,
    height: r.h,
    shape: { d: r.d },
    zones: [0, 1, 2].map((i) => {
      const z = r.zones[i];
      if (z === undefined || z === null) return undefined;
      const [x, y, width, height, gravity] = z;
      return {
        x,
        y,
        width,
        height,
        horizontalGravity: HORIZONTAL_GRAVITY[gravity[0]!] ?? "CENTER",
        verticalGravity: VERTICAL_GRAVITY[gravity[1]!] ?? "CENTER",
      };
    }),
  };
}

/// A provider for the embedded signs of a document, over `base` for the
/// others.
///
/// The data of a sign is compressed: like the shapes of `base`, it is only
/// available after [loadShapes] (its size and zones too). Until then, the
/// sign is unknown.
export class EmbeddedSignGlyphProvider implements GlyphProvider {
  private readonly signs = new Map<string, EmbeddedSign>();
  private readonly glyphs = new Map<string, EmbeddedGlyph>();
  private readonly pending = new Map<string, Promise<void>>();

  constructor(
    private readonly base: GlyphProvider,
    signs: readonly EmbeddedSign[],
  ) {
    for (const s of signs) this.signs.set(s.code, s);
  }

  canonicalCode(code: string): string {
    return this.signs.has(code) ? code : this.base.canonicalCode(code);
  }

  size(code: string): { width: number; height: number } | undefined {
    if (!this.signs.has(code)) return this.base.size(code);
    const g = this.glyphs.get(code);
    return g === undefined ? undefined : { width: g.width, height: g.height };
  }

  ligatureZone(code: string, i: number): LigatureZone | undefined {
    return this.signs.has(code) ? this.glyphs.get(code)?.zones[i] : this.base.ligatureZone(code, i);
  }

  ligaturePositions(codes: readonly string[]): readonly ExplicitPosition[] | undefined {
    return this.base.ligaturePositions(codes);
  }

  shape(code: string): SignShape | undefined {
    return this.signs.has(code) ? this.glyphs.get(code)?.shape : this.base.shape(code);
  }

  async loadShapes(codes: Iterable<string>): Promise<void> {
    const others: string[] = [];
    const waits: Promise<void>[] = [];
    for (const code of codes) {
      const sign = this.signs.get(code);
      if (sign === undefined) {
        others.push(code);
        continue;
      }
      if (this.glyphs.has(code)) continue;
      let p = this.pending.get(code);
      if (p === undefined) {
        p = embeddedSignRendering(sign).then(
          (r) => {
            this.glyphs.set(code, toGlyph(r));
            this.pending.delete(code);
          },
          (error: unknown) => {
            this.pending.delete(code);
            console.warn(`Unreadable embedded sign ${code}`, error);
          },
        );
        this.pending.set(code, p);
      }
      waits.push(p);
    }
    waits.push(this.base.loadShapes(others));
    await Promise.all(waits);
  }
}

/// `base`, or `base` with the embedded signs of a document in front of it.
export function withEmbeddedSigns(base: GlyphProvider, signs: EmbeddedSigns | undefined): GlyphProvider {
  return signs === undefined || signs.signs.length === 0 ? base : new EmbeddedSignGlyphProvider(base, signs.signs);
}
