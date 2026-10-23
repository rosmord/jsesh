/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Sign shapes and sizes, as the layout and the drawing need them: the
/// [GlyphProvider] interface (the data source, see `@jsesh/glyphs`) and
/// [HieroglyphDrawer], port of `jsesh.render.elements.HieroglyphDrawer`.

import { parsePath, pathBounds, rotation, transformPath } from "./path.js";
import type { JSeshStyle } from "./style.js";
import { Rect } from "./view.js";

const f = Math.fround;

export type HorizontalGravity = "START" | "CENTER" | "END";
export type VerticalGravity = "TOP" | "CENTER" | "BOTTOM";

/// Where a sign can receive another one in a ligature (port of
/// `jsesh.glyphs.shape.LigatureZone`), in the sign's own coordinates.
export interface LigatureZone {
  x: number;
  y: number;
  width: number;
  height: number;
  horizontalGravity: HorizontalGravity;
  verticalGravity: VerticalGravity;
}

export function scaleZone(z: LigatureZone, scale: number): LigatureZone {
  return { ...z, x: z.x * scale, y: z.y * scale, width: z.width * scale, height: z.height * scale };
}

/// A sign's place in a predefined ligature (`ExplicitPosition`): position in
/// group units (thousandths of the height of A1) and scale in percent.
export interface ExplicitPosition {
  x: number;
  y: number;
  scale: number;
}

/// The outline of a sign, as an SVG path whose bounding box starts at 0,0.
export interface SignShape {
  d: string;
  evenOdd?: boolean;
}

/// A source of sign shapes ("font"). Sizes are in font units; all methods
/// but [GlyphProvider.canonicalCode] take canonical codes.
///
/// Sizes and zones are available at once; shapes may need to be loaded
/// first with [GlyphProvider.loadShapes].
export interface GlyphProvider {
  /// The canonical code of a sign (Gardiner code for a phonetic code).
  canonicalCode(code: string): string;
  /// The size of a sign, if the sign is known.
  size(code: string): { width: number; height: number } | undefined;
  /// Ligature zone `i` (0: before, 1: after, 2: below) of a sign, if any.
  ligatureZone(code: string, i: number): LigatureZone | undefined;
  /// The predefined ligature for a list of signs, if any.
  ligaturePositions(codes: readonly string[]): readonly ExplicitPosition[] | undefined;
  /// The shape of a sign, if it is known and loaded.
  shape(code: string): SignShape | undefined;
  /// Loads the shapes of signs (unknown codes are ignored).
  loadShapes(codes: Iterable<string>): Promise<void>;
}

const EDITOR_MARKUP_SMALL_HEIGHT = 2;
export const EDITOR_MARKUP_HEIGHT = 18;
export const PARENTHESIS_STROKE_WIDTH = 0.75;

/// Codes drawn by `SpecialSymbolDrawer` (philological brackets as signs).
const SPECIAL_SYMBOLS = new Set(["[[", "]]", "[?", "?]", "[&", "&]", "[{", "}]", '["', '"]', "['", "']", "[(", ")]"]);

/// Width of the special symbols (`AbstractSymbolDrawer.getBaseWidth`).
const SPECIAL_SYMBOL_WIDTH = 3;

/// Port of `HieroglyphDrawer` (and of its delegates `SVGFontHieroglyphDrawer`,
/// `SpecialSymbolDrawer` and `ShadingCodeCatalogue`).
export class HieroglyphDrawer {
  private readonly heightOfA1: number;
  private readonly shadingSizes: Map<string, Rect>;

  constructor(readonly provider: GlyphProvider) {
    const a1 = provider.size(provider.canonicalCode("A1"));
    if (a1 === undefined) throw new Error("The glyph provider has no A1 sign");
    this.heightOfA1 = f(a1.height);
    const w = f(a1.width);
    const h = this.heightOfA1;
    this.shadingSizes = new Map([
      ["/", new Rect(0, 0, f(w / 2), f(h / 2))],
      ["//", new Rect(0, 0, w, h)],
      ["h/", new Rect(0, 0, w, f(h / 2))],
      ["v/", new Rect(0, 0, f(w / 2), h)],
    ]);
  }

  static isSpecial(code: string): boolean {
    return SPECIAL_SYMBOLS.has(code);
  }

  /// The bounding box of a sign. For a rotated sign, the box of the rotated
  /// shape (which must be loaded; else the rotated box is used).
  ///
  /// `fixed` is for special symbols: if false, they are small, and will be
  /// stretched by the layout.
  getBBox(code: string, angle: number, fixed: boolean): Rect | undefined {
    if (HieroglyphDrawer.isSpecial(code)) {
      if (angle === 0 && !fixed) return new Rect(0, 0, SPECIAL_SYMBOL_WIDTH, EDITOR_MARKUP_SMALL_HEIGHT);
      const r = new Rect(0, 0, SPECIAL_SYMBOL_WIDTH, EDITOR_MARKUP_HEIGHT);
      return angle === 0 ? r : rotatedRect(r, angle);
    }
    const canonical = this.provider.canonicalCode(code);
    if (angle === 0) {
      const size = this.provider.size(canonical);
      if (size !== undefined) return new Rect(0, 0, size.width, size.height);
      return this.shadingSizes.get(code)?.clone();
    }
    const shape = this.provider.shape(canonical);
    if (shape !== undefined) {
      return pathBounds(transformPath(parsePath(shape.d), rotation(angle)));
    }
    const box = this.getBBox(code, 0, fixed);
    return box === undefined ? undefined : rotatedRect(box, angle);
  }

  /// One thousandth of the height of A1: the unit of explicit positions.
  getGroupUnitLength(): number {
    return this.heightOfA1 / 1000;
  }

  getHeightOfA1(): number {
    return this.heightOfA1;
  }

  getLigatureZone(i: number, code: string): LigatureZone | undefined {
    if (HieroglyphDrawer.isSpecial(code)) return undefined;
    return this.provider.ligatureZone(this.provider.canonicalCode(code), i);
  }

  isKnown(code: string): boolean {
    if (HieroglyphDrawer.isSpecial(code)) return true;
    return this.provider.size(this.provider.canonicalCode(code)) !== undefined || this.shadingSizes.has(code);
  }

  getPositions(codes: readonly string[]): readonly ExplicitPosition[] | undefined {
    return this.provider.ligaturePositions(codes.map((c) => this.provider.canonicalCode(c)));
  }

  getShape(code: string): SignShape | undefined {
    return this.provider.shape(this.provider.canonicalCode(code));
  }

  /// The scale from font units to points, for a style.
  scaleFromFontToStyle(style: JSeshStyle): number {
    return f(style.geometry.standardSignHeight / this.heightOfA1);
  }
}

function rotatedRect(r: Rect, angle: number): Rect {
  return pathBounds(
    transformPath(
      [
        { type: "M", points: [r.minX, r.minY] },
        { type: "L", points: [r.maxX, r.minY] },
        { type: "L", points: [r.maxX, r.maxY] },
        { type: "L", points: [r.minX, r.maxY] },
        { type: "Z" },
      ],
      rotation(angle),
    ),
  );
}
