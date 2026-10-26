/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// A coarse black-and-white raster of a shape, over a fixed rectangular frame: port of
/// `jsesh.render.layout.insert.ShapeMask`.
///
/// Masks are used by [InsertionFitter] to test whether two shapes collide. They are built by an
/// own scanline filler, so that the result is deterministic, needs no canvas, and is the same as
/// Java's.
///
/// Filling is slightly **conservative**: each row is sampled on four horizontal lines, and any
/// cell touched horizontally by a span found on one of them is set. A shape covering more than a
/// quarter of a row's height is therefore never lost.

import { IDENTITY, type Matrix, type PathSegment } from "../path.js";
import type { Rect } from "../view.js";

/// A filled outline: a path, and the rule used to fill it.
///
/// Outlines must **not** be merged into a single area: that would fill the holes of signs, which
/// are precisely where an element can be inserted.
export interface Outline {
  path: PathSegment[];
  evenOdd: boolean;
}

/// An outline for a rectangle.
export function rectOutline(r: Rect, transform: Matrix = IDENTITY): Outline {
  const point = (x: number, y: number): [number, number] => [
    transform[0] * x + transform[2] * y + transform[4],
    transform[1] * x + transform[3] * y + transform[5],
  ];
  return {
    path: [
      { type: "M", points: point(r.minX, r.minY) },
      { type: "L", points: point(r.maxX, r.minY) },
      { type: "L", points: point(r.maxX, r.maxY) },
      { type: "L", points: point(r.minX, r.maxY) },
      { type: "Z" },
    ],
    evenOdd: false,
  };
}

/// Maximum number of columns (and rows) along the longest side of the frame.
export const MAX_RESOLUTION = 64;

/// Number of sample lines per row.
const SAMPLES_PER_ROW = 4;

/// `FlatteningPathIterator`'s recursion limit.
const FLATTENING_LIMIT = 8;

/// Squared distance from a point to a segment (`Line2D.ptSegDistSq`).
function ptSegDistSq(x1: number, y1: number, x2: number, y2: number, px: number, py: number): number {
  x2 -= x1;
  y2 -= y1;
  px -= x1;
  py -= y1;
  let dotprod = px * x2 + py * y2;
  let projlenSq: number;
  if (dotprod <= 0) {
    projlenSq = 0;
  } else {
    px = x2 - px;
    py = y2 - py;
    dotprod = px * x2 + py * y2;
    projlenSq = dotprod <= 0 ? 0 : (dotprod * dotprod) / (x2 * x2 + y2 * y2);
  }
  const lenSq = px * px + py * py - projlenSq;
  return lenSq < 0 ? 0 : lenSq;
}

export class ShapeMask {
  readonly cellSize: number;
  readonly columns: number;
  readonly rowCount: number;
  private readonly originX: number;
  private readonly originY: number;
  /// One byte per cell, row by row.
  private cells: Uint8Array;

  /// Creates an empty mask.
  ///
  /// @param frame the part of the plane covered by the mask. Must not be empty.
  /// @param resolution number of cells along the longest side of the frame (at most [MAX_RESOLUTION]).
  constructor(frame: Rect, resolution: number);
  /// Creates an empty mask over the same grid as another one.
  constructor(model: ShapeMask);
  constructor(a: Rect | ShapeMask, resolution?: number) {
    if (a instanceof ShapeMask) {
      this.originX = a.originX;
      this.originY = a.originY;
      this.cellSize = a.cellSize;
      this.columns = a.columns;
      this.rowCount = a.rowCount;
    } else {
      if (resolution === undefined || resolution < 1 || resolution > MAX_RESOLUTION) {
        throw new RangeError(`resolution must be in 1..${MAX_RESOLUTION}`);
      }
      if (a.width <= 0 || a.height <= 0) throw new RangeError("empty frame");
      this.originX = a.minX;
      this.originY = a.minY;
      this.cellSize = Math.max(a.width, a.height) / resolution;
      this.columns = Math.max(1, Math.min(resolution, Math.ceil(a.width / this.cellSize - 1e-9)));
      this.rowCount = Math.max(1, Math.min(resolution, Math.ceil(a.height / this.cellSize - 1e-9)));
    }
    this.cells = new Uint8Array(this.columns * this.rowCount);
  }

  /// Removes every cell of the mask.
  clear(): void {
    this.cells.fill(0);
  }

  isEmpty(): boolean {
    return this.cells.every((c) => c === 0);
  }

  /// Is the cell set?
  get(column: number, row: number): boolean {
    return this.cells[row * this.columns + column] !== 0;
  }

  /// Sets the cells touched by a shape (cells outside the grid are ignored).
  ///
  /// @param outline the shape to add.
  /// @param transform applied to the shape before filling.
  fill(outline: Outline, transform: Matrix = IDENTITY): void {
    const edges = this.flatten(outline.path, transform);
    const edgeCount = edges.length / 4;
    if (edgeCount === 0) return;
    const xs = new Float64Array(edgeCount);
    const dirs = new Int8Array(edgeCount);
    for (let row = 0; row < this.rowCount; row++) {
      for (let sample = 0; sample < SAMPLES_PER_ROW; sample++) {
        const y = this.originY + (row + (sample + 0.5) / SAMPLES_PER_ROW) * this.cellSize;
        const n = crossings(edges, y, xs, dirs);
        this.fillSpans(row, xs, dirs, n, outline.evenOdd);
      }
    }
  }

  /// Grows the mask by a number of cells in the four directions.
  dilate(cells: number): ShapeMask {
    const result = new ShapeMask(this);
    result.cells.set(this.cells);
    const w = this.columns;
    const h = this.rowCount;
    for (let k = 0; k < cells; k++) {
      const src = result.cells.slice();
      for (let r = 0; r < h; r++) {
        for (let c = 0; c < w; c++) {
          const i = r * w + c;
          if (
            src[i] !== 0 ||
            (c > 0 && src[i - 1] !== 0) ||
            (c < w - 1 && src[i + 1] !== 0) ||
            (r > 0 && src[i - w] !== 0) ||
            (r < h - 1 && src[i + w] !== 0)
          ) {
            result.cells[i] = 1;
          }
        }
      }
    }
    return result;
  }

  /// Do the two masks have a common cell? They must share the same grid.
  intersects(other: ShapeMask): boolean {
    for (let i = 0; i < this.cells.length; i++) if (this.cells[i] !== 0 && other.cells[i] !== 0) return true;
    return false;
  }

  /// The number of cells set in both masks.
  overlap(other: ShapeMask): number {
    let count = 0;
    for (let i = 0; i < this.cells.length; i++) if (this.cells[i] !== 0 && other.cells[i] !== 0) count++;
    return count;
  }

  /// The edges of the flattened outline, as x0, y0, x1, y1 quadruples. Curves are flattened as
  /// `java.awt.geom.FlatteningPathIterator` does, with a flatness of a quarter of a cell.
  private flatten(path: PathSegment[], m: Matrix): number[] {
    const squareFlat = (this.cellSize / 4) ** 2;
    const edges: number[] = [];
    let startX = 0;
    let startY = 0;
    let curX = 0;
    let curY = 0;
    const lineTo = (x: number, y: number) => {
      edges.push(curX, curY, x, y);
      curX = x;
      curY = y;
    };
    const tx = (x: number, y: number): [number, number] => [m[0] * x + m[2] * y + m[4], m[1] * x + m[3] * y + m[5]];
    for (const s of path) {
      switch (s.type) {
        case "M": {
          // Closes the previous subpath implicitly.
          if (curX !== startX || curY !== startY) edges.push(curX, curY, startX, startY);
          [startX, startY] = tx(s.points[0], s.points[1]);
          curX = startX;
          curY = startY;
          break;
        }
        case "L":
          lineTo(...tx(s.points[0], s.points[1]));
          break;
        case "Q": {
          const [cx, cy] = tx(s.points[0], s.points[1]);
          const [ex, ey] = tx(s.points[2], s.points[3]);
          flattenQuad(curX, curY, cx, cy, ex, ey, 0, squareFlat, lineTo);
          break;
        }
        case "C": {
          const [c1x, c1y] = tx(s.points[0], s.points[1]);
          const [c2x, c2y] = tx(s.points[2], s.points[3]);
          const [ex, ey] = tx(s.points[4], s.points[5]);
          flattenCubic(curX, curY, c1x, c1y, c2x, c2y, ex, ey, 0, squareFlat, lineTo);
          break;
        }
        case "Z":
          lineTo(startX, startY);
          break;
      }
    }
    if (curX !== startX || curY !== startY) edges.push(curX, curY, startX, startY);
    return edges;
  }

  private fillSpans(row: number, xs: Float64Array, dirs: Int8Array, n: number, evenOdd: boolean): void {
    let winding = 0;
    for (let i = 0; i < n - 1; i++) {
      winding = evenOdd ? (winding === 0 ? 1 : 0) : winding + dirs[i]!;
      if (winding !== 0) this.setSpan(row, xs[i]!, xs[i + 1]!);
    }
  }

  private setSpan(row: number, x0: number, x1: number): void {
    const u0 = (x0 - this.originX) / this.cellSize;
    const u1 = (x1 - this.originX) / this.cellSize;
    let c0 = Math.floor(u0);
    // A span ending exactly on a cell boundary doesn't touch the next cell.
    let c1 = Math.max(c0, Math.ceil(u1) - 1);
    if (c1 < 0 || c0 >= this.columns) return;
    c0 = Math.max(c0, 0);
    c1 = Math.min(c1, this.columns - 1);
    this.cells.fill(1, row * this.columns + c0, row * this.columns + c1 + 1);
  }
}

function flattenQuad(
  x1: number,
  y1: number,
  cx: number,
  cy: number,
  x2: number,
  y2: number,
  level: number,
  squareFlat: number,
  lineTo: (x: number, y: number) => void,
): void {
  if (level < FLATTENING_LIMIT && ptSegDistSq(x1, y1, x2, y2, cx, cy) >= squareFlat) {
    const lx = (x1 + cx) / 2;
    const ly = (y1 + cy) / 2;
    const rx = (cx + x2) / 2;
    const ry = (cy + y2) / 2;
    const mx = (lx + rx) / 2;
    const my = (ly + ry) / 2;
    flattenQuad(x1, y1, lx, ly, mx, my, level + 1, squareFlat, lineTo);
    flattenQuad(mx, my, rx, ry, x2, y2, level + 1, squareFlat, lineTo);
  } else {
    lineTo(x2, y2);
  }
}

function flattenCubic(
  x1: number,
  y1: number,
  c1x: number,
  c1y: number,
  c2x: number,
  c2y: number,
  x2: number,
  y2: number,
  level: number,
  squareFlat: number,
  lineTo: (x: number, y: number) => void,
): void {
  const flatness = Math.max(ptSegDistSq(x1, y1, x2, y2, c1x, c1y), ptSegDistSq(x1, y1, x2, y2, c2x, c2y));
  if (level < FLATTENING_LIMIT && flatness >= squareFlat) {
    const abx = (x1 + c1x) / 2;
    const aby = (y1 + c1y) / 2;
    const bcx = (c1x + c2x) / 2;
    const bcy = (c1y + c2y) / 2;
    const cdx = (c2x + x2) / 2;
    const cdy = (c2y + y2) / 2;
    const abcx = (abx + bcx) / 2;
    const abcy = (aby + bcy) / 2;
    const bcdx = (bcx + cdx) / 2;
    const bcdy = (bcy + cdy) / 2;
    const mx = (abcx + bcdx) / 2;
    const my = (abcy + bcdy) / 2;
    flattenCubic(x1, y1, abx, aby, abcx, abcy, mx, my, level + 1, squareFlat, lineTo);
    flattenCubic(mx, my, bcdx, bcdy, cdx, cdy, x2, y2, level + 1, squareFlat, lineTo);
  } else {
    lineTo(x2, y2);
  }
}

/// Computes the sorted crossings of the outline with the horizontal line at `y`. Edges are
/// half-open on their upper end, so that a vertex is counted once.
function crossings(edges: number[], y: number, xs: Float64Array, dirs: Int8Array): number {
  let n = 0;
  for (let e = 0; e < edges.length; e += 4) {
    const x0 = edges[e]!;
    const y0 = edges[e + 1]!;
    const x1 = edges[e + 2]!;
    const y1 = edges[e + 3]!;
    if (y0 === y1) continue;
    const dir = y1 > y0 ? 1 : -1;
    const low = Math.min(y0, y1);
    const high = Math.max(y0, y1);
    if (y < low || y >= high) continue;
    const x = x0 + ((y - y0) * (x1 - x0)) / (y1 - y0);
    // Insertion sort by x.
    let i = n++;
    while (i > 0 && xs[i - 1]! > x) {
      xs[i] = xs[i - 1]!;
      dirs[i] = dirs[i - 1]!;
      i--;
    }
    xs[i] = x;
    dirs[i] = dir;
  }
  return n;
}
