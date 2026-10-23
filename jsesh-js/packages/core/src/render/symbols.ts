/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Drawing of the philological brackets read as signs (`[[`, `[&`...):
/// port of `jsesh.render.elements.SpecialSymbolDrawer` and of its delegates
/// in `internal/symboldrawers`.

import { EDITOR_MARKUP_HEIGHT, PARENTHESIS_STROKE_WIDTH } from "./glyphs.js";
import { pathBounds, rotation, transformPath, type PathSegment } from "./path.js";
import { num } from "./svg-writer.js";

/// A path, and the path used to compute its bounds when it is rotated.
interface CombinedPath {
  actual: PathSegment[];
  bounding: PathSegment[] | null;
}

type ShapeBuilder = (width: number, height: number, stroke: number) => CombinedPath;

const M = (x: number, y: number): PathSegment => ({ type: "M", points: [x, y] });
const L = (x: number, y: number): PathSegment => ({ type: "L", points: [x, y] });
const C = (x1: number, y1: number, x2: number, y2: number, x3: number, y3: number): PathSegment => ({
  type: "C",
  points: [x1, y1, x2, y2, x3, y3],
});

const own = (actual: PathSegment[]): CombinedPath => ({ actual, bounding: null });

function rectangle(width: number, height: number): PathSegment[] {
  return [M(0, 0), L(width, 0), L(width, height), L(0, height), L(0, 0)];
}

const openErased: ShapeBuilder = (w, h, s) =>
  own([M(w - s / 2, s / 2), L(s / 2, s / 2), L(s / 2, h - s / 2), L(w - s / 2, h - s / 2)]);
const closeErased: ShapeBuilder = (w, h, s) =>
  own([M(s / 2, s / 2), L(w - s / 2, s / 2), L(w - s / 2, h - s / 2), L(s / 2, h - s / 2)]);
const openDubious: ShapeBuilder = (w, h, s) => own([M(w, s / 2), L(s / 2, s / 2), L(s / 2, h / 2)]);
const closeDubious: ShapeBuilder = (w, h, s) => own([M(s / 2, s / 2), L(w - s / 2, s / 2), L(w - s / 2, h / 2)]);
const openEditorAddition: ShapeBuilder = (w, h, s) => own([M(w - s / 2, s / 2), L(s / 2, h / 2), L(w - s / 2, h - s / 2)]);
const closeEditorAddition: ShapeBuilder = (w, h, s) => own([M(s / 2, s / 2), L(w - s / 2, h / 2), L(s / 2, h - s / 2)]);
const openMinorAddition: ShapeBuilder = (w, h, s) => own([M(w - s / 2, s / 2), C(0, 0, 0, h - s / 2, w - s / 2, h - s / 2)]);
const closeMinorAddition: ShapeBuilder = (w, h, s) => own([M(s / 2, s / 2), C(w, s / 2, w, h - s / 2, s / 2, h - s / 2)]);
const openScribeAddition: ShapeBuilder = (w, h, s) => ({ actual: [M(w / 2, s / 2), L(w / 2, h / 3)], bounding: rectangle(w, h) });
const closeScribeAddition = openScribeAddition;

function superfluousPath(w: number, h: number, s: number, open: boolean): PathSegment[] {
  const hs = s / 2;
  const cy = h / 2;
  const pw = w - hs;
  return open
    ? [M(pw, hs), C(hs, hs, pw, cy, hs, cy), M(hs, cy), C(pw, cy, hs, h - hs, pw, h - hs)]
    : [M(hs, hs), C(w - hs, hs, hs, cy, w - hs, cy), M(w - hs, cy), C(hs, cy, w - hs, h - hs, hs, h - hs)];
}

const openSuperfluousTop: ShapeBuilder = (w, h, s) => ({
  actual: superfluousPath(w, h, s, true).slice(0, 2),
  bounding: superfluousPath(w, h, s, true),
});
const openSuperfluousBottom: ShapeBuilder = (w, h, s) => ({
  actual: superfluousPath(w, h, s, true).slice(2),
  bounding: superfluousPath(w, h, s, true),
});
const closeSuperfluousTop: ShapeBuilder = (w, h, s) => ({
  actual: superfluousPath(w, h, s, false).slice(0, 2),
  bounding: superfluousPath(w, h, s, false),
});
const closeSuperfluousBottom: ShapeBuilder = (w, h, s) => ({
  actual: superfluousPath(w, h, s, false).slice(2),
  bounding: superfluousPath(w, h, s, false),
});

/// The small vertical line of the "previously readable" brackets.
function previouslyReadableLine(open: boolean, parentStroke: number): ShapeBuilder {
  return (w, h) => {
    const hStart = w / 2 + parentStroke;
    const vStart = 3 * parentStroke;
    const x = open ? hStart : w - hStart;
    return { actual: [M(x, vStart), L(x, h - vStart)], bounding: rectangle(w, h) };
  };
}

/// Each symbol is drawn by one or more builders, with a stroke width factor.
const SYMBOLS: Record<string, [ShapeBuilder, number][]> = {
  "[[": [[openErased, 1]],
  "]]": [[closeErased, 1]],
  "[?": [[openDubious, 1]],
  "?]": [[closeDubious, 1]],
  "[&": [[openEditorAddition, 1]],
  "&]": [[closeEditorAddition, 1]],
  "[{": [
    [openSuperfluousTop, 1],
    [openSuperfluousBottom, 1],
  ],
  "}]": [
    [closeSuperfluousTop, 1],
    [closeSuperfluousBottom, 1],
  ],
  '["': [
    [openErased, 1],
    [previouslyReadableLine(true, PARENTHESIS_STROKE_WIDTH), 0.75],
  ],
  '"]': [
    [closeErased, 1],
    [previouslyReadableLine(false, PARENTHESIS_STROKE_WIDTH), 0.75],
  ],
  "['": [[openScribeAddition, 1]],
  "']": [[closeScribeAddition, 1]],
  "[(": [[openMinorAddition, 1]],
  ")]": [[closeMinorAddition, 1]],
};

const BASE_WIDTH = 3;

/// Draws a special symbol in a view of size `width` × `height` and scale
/// `xScale`, `yScale` (`AbstractSymbolDrawer.draw`).
export function drawSpecialSymbol(
  code: string,
  angle: number,
  width: number,
  height: number,
  xScale: number,
  yScale: number,
  color: string,
): string {
  let result = "";
  for (const [builder, factor] of SYMBOLS[code] ?? []) {
    const stroke = PARENTHESIS_STROKE_WIDTH * factor;
    let path: PathSegment[];
    if (angle === 0) {
      path = builder(width, height, stroke).actual;
    } else {
      // The shape at its normal size, rotated, and moved back to 0,0.
      const combined = builder(BASE_WIDTH * xScale, EDITOR_MARKUP_HEIGHT * yScale, stroke);
      const m = rotation(angle);
      const actual = transformPath(combined.actual, m);
      const bounds = pathBounds(combined.bounding === null ? actual : transformPath(combined.bounding, m));
      path = transformPath(actual, [1, 0, 0, 1, -bounds.minX + stroke / 2, -bounds.minY + stroke / 2]);
    }
    const d = path
      .map((s) => (s.type === "Z" ? "Z" : s.type + s.points.map(num).join(" ")))
      .join("");
    result += `<path d="${d}" fill="none" stroke="${color}" stroke-width="${num(stroke)}" stroke-linecap="square" stroke-miterlimit="10"/>`;
  }
  return result;
}
