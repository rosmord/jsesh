/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Drawing the signs embedded in a document: a [GlyphProvider] which answers
/// for `SIGN1`, `SIGN2`... from their SVG, and asks another provider for the
/// other signs. Mirrors the Java `EmbeddedSignShapeRepository`, put in front
/// of the global fonts.

import type { EmbeddedSign, EmbeddedSigns } from "../document/embedded-signs.js";
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

const HORIZONTAL_GRAVITY: Record<string, HorizontalGravity> = { s: "START", e: "END" };
const VERTICAL_GRAVITY: Record<string, VerticalGravity> = { t: "TOP", b: "BOTTOM" };

/// The attributes of an SVG start tag (`<path d='...'/>`).
function attributes(element: string): Record<string, string> {
  const result: Record<string, string> = {};
  for (const m of element.matchAll(/([\w:-]+)\s*=\s*(?:'([^']*)'|"([^"]*)")/g)) {
    result[m[1]!] = m[2] ?? m[3] ?? "";
  }
  return result;
}

/// Reads the normalized SVG of an embedded sign (as written by
/// `ShapeChar.exportToSVG`): its size (the `width` and `height` of the
/// `svg` element, which are its bounding box), its path (already at 0,0)
/// and its ligature zones (`rect` elements `zone1` to `zone3`).
export function parseEmbeddedSignSvg(svg: string): EmbeddedGlyph | undefined {
  const root = /<svg\b[^>]*>/.exec(svg);
  const path = /<path\b[^>]*>/.exec(svg);
  if (root === null || path === null) return undefined;
  const size = attributes(root[0]);
  const d = attributes(path[0]).d;
  const width = Number(size.width);
  const height = Number(size.height);
  if (d === undefined || !Number.isFinite(width) || !Number.isFinite(height)) return undefined;
  const zones: (LigatureZone | undefined)[] = [undefined, undefined, undefined];
  for (const m of svg.matchAll(/<rect\b[^>]*>/g)) {
    const a = attributes(m[0]);
    const index = /^zone([123])$/.exec(a.id ?? "");
    if (index === null) continue;
    const gravity = /^gravity:(.*)$/.exec(a["inkscape:label"] ?? "")?.[1] ?? "";
    let horizontalGravity: HorizontalGravity = "CENTER";
    let verticalGravity: VerticalGravity = "CENTER";
    for (const c of gravity) {
      horizontalGravity = HORIZONTAL_GRAVITY[c] ?? horizontalGravity;
      verticalGravity = VERTICAL_GRAVITY[c] ?? verticalGravity;
    }
    zones[Number(index[1]) - 1] = {
      x: Number(a.x),
      y: Number(a.y),
      width: Number(a.width),
      height: Number(a.height),
      horizontalGravity,
      verticalGravity,
    };
  }
  return { width, height, shape: { d }, zones };
}

/// A provider for the embedded signs of a document, over `base` for the
/// others.
export class EmbeddedSignGlyphProvider implements GlyphProvider {
  private readonly glyphs = new Map<string, EmbeddedGlyph>();

  constructor(
    private readonly base: GlyphProvider,
    signs: readonly EmbeddedSign[],
  ) {
    for (const s of signs) {
      const glyph = parseEmbeddedSignSvg(s.svg);
      if (glyph !== undefined) this.glyphs.set(s.code, glyph);
      else console.warn(`Unreadable embedded sign ${s.code}`);
    }
  }

  canonicalCode(code: string): string {
    return this.glyphs.has(code) ? code : this.base.canonicalCode(code);
  }

  size(code: string): { width: number; height: number } | undefined {
    const g = this.glyphs.get(code);
    return g !== undefined ? { width: g.width, height: g.height } : this.base.size(code);
  }

  ligatureZone(code: string, i: number): LigatureZone | undefined {
    const g = this.glyphs.get(code);
    return g !== undefined ? g.zones[i] : this.base.ligatureZone(code, i);
  }

  ligaturePositions(codes: readonly string[]): readonly ExplicitPosition[] | undefined {
    return this.base.ligaturePositions(codes);
  }

  shape(code: string): SignShape | undefined {
    return this.glyphs.get(code)?.shape ?? this.base.shape(code);
  }

  async loadShapes(codes: Iterable<string>): Promise<void> {
    await this.base.loadShapes([...codes].filter((c) => !this.glyphs.has(c)));
  }
}

/// `base`, or `base` with the embedded signs of a document in front of it.
export function withEmbeddedSigns(base: GlyphProvider, signs: EmbeddedSigns | undefined): GlyphProvider {
  return signs === undefined || signs.signs.length === 0 ? base : new EmbeddedSignGlyphProvider(base, signs.signs);
}
