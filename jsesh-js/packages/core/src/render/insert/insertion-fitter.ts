/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Computes where, and how large, an element can be inserted in the free space of another one,
/// using the actual shapes of both: port of `jsesh.render.layout.insert.InsertionFitter`.
///
/// This is the geometric core of the `%insert` operator. It is independent from the model and
/// from the view classes: it works on outlines and boxes, in the geometric (y axis downwards)
/// coordinates of the layout. "Left" and "right" are geometric: the caller translates "start" and
/// "end" according to the text direction.
///
/// ## Algorithm
///
/// The *frame* is the target's box, possibly enlarged on the side of the insertion (see
/// [enlargedFrame]). The target is rasterised in the frame (see [ShapeMask]) and dilated by a
/// small margin. Then:
///
/// - scales are tried from largest to smallest: `1, 2^(-1/4), 2^(-2/4), ... 1/4` (1 being the
///   natural size of the inserted element);
/// - for each scale, candidate positions are tried, nearest to the insertion corner or edge first:
///   - oblique insertion: a regular 5x5 grid in the square at the corner, whose side is 1/4 of the
///     frame's;
///   - insertion from a side: 10 points on the central line, from the edge to the center of the frame;
///   - center insertion: the center of the frame;
/// - the inserted element is anchored on the candidate position by the corner (or side midpoint)
///   of its box which faces the insertion corner (or side);
/// - the first placement where the inserted element is inside the frame and does not touch the
///   target wins;
/// - for an insertion from a side, once the inserted element touches the target, the positions
///   further from the edge are not tried at this scale: the element must not pass through the
///   target to reach some free space behind it.
///
/// If nothing fits, the smallest scale is used at the position with the least overlap.

import { concatenate, scaling, translation, type Matrix } from "../path.js";
import { Rect } from "../view.js";
import { MAX_RESOLUTION, ShapeMask, type Outline } from "./shape-mask.js";

/// A horizontal side of the frame.
export type HorizontalSide = "LEFT" | "CENTER" | "RIGHT";

/// A vertical side of the frame. As layout coordinates are y-down, `TOP` is the side with the
/// smallest y.
export type VerticalSide = "TOP" | "CENTER" | "BOTTOM";

/// What to fit.
export interface FitRequest {
  /// The outlines of the main element.
  target: readonly Outline[];
  /// The area where the inserted element must go. Usually, the target's box, see [enlargedFrame].
  frame: Rect;
  /// The outlines of the inserted element, at its natural size.
  inserted: readonly Outline[];
  /// The box of the inserted element, at its natural size.
  insertedBox: Rect;
  horizontal: HorizontalSide;
  vertical: VerticalSide;
}

/// How to transform the inserted element.
export interface Placement {
  /// The (uniform) scaling factor, relative to the natural size.
  scale: number;
  /// x translation, applied after scaling.
  translateX: number;
  /// y translation, applied after scaling.
  translateY: number;
  /// False if nothing fitted, and the placement is a best effort.
  fits: boolean;
}

/// The transformation to apply to the inserted element's coordinates.
export function placementTransform(p: Placement): Matrix {
  return concatenate(translation(p.translateX, p.translateY), scaling(p.scale, p.scale));
}

/// The box of the inserted element once placed.
export function placedBox(p: Placement, box: Rect): Rect {
  return new Rect(p.translateX + p.scale * box.x, p.translateY + p.scale * box.y, p.scale * box.width, p.scale * box.height);
}

/// Number of scales tried: 1, 2^(-1/4), ..., 2^(-8/4) = 1/4.
export const SCALE_STEPS = 9;

/// Number of points on the center line for insertions from a side.
export const SIDE_POINTS = 10;

/// Number of grid points per side for oblique insertions.
export const GRID_POINTS = 5;

/// Side of the corner square used for oblique insertions, as a fraction of the frame.
export const CORNER_SQUARE = 0.25;

const EPSILON = 1e-9;

/// The scale tried at step `k`.
export function scaleAt(k: number): number {
  return Math.pow(2, -k / 4);
}

/// Enlarges the target's box on the side of the insertion.
///
/// The frame grows by `dw` percent of the inserted element's natural width on the horizontal side
/// of the insertion, and by `dh` percent of its natural height on the vertical side. On the center
/// line (no horizontal side, or no vertical side), the corresponding enlargement only applies if
/// there is no side at all (center insertion), in which case it is split evenly between both sides.
/// The target itself is not moved in the frame.
export function enlargedFrame(
  targetBox: Rect,
  insertedBox: Rect,
  horizontal: HorizontalSide,
  vertical: VerticalSide,
  dw: number,
  dh: number,
): Rect {
  const center = horizontal === "CENTER" && vertical === "CENTER";
  const w = (insertedBox.width * dw) / 100;
  const h = (insertedBox.height * dh) / 100;
  let minX = targetBox.minX;
  let maxX = targetBox.maxX;
  let minY = targetBox.minY;
  let maxY = targetBox.maxY;
  if (center) {
    minX -= w / 2;
    maxX += w / 2;
    minY -= h / 2;
    maxY += h / 2;
  } else {
    if (horizontal === "LEFT") minX -= w;
    else if (horizontal === "RIGHT") maxX += w;
    if (vertical === "TOP") minY -= h;
    else if (vertical === "BOTTOM") maxY += h;
  }
  return new Rect(minX, minY, maxX - minX, maxY - minY);
}

/// Finds the placement of the inserted element.
///
/// @param resolution number of cells along the longest side of the frame.
/// @param marginCells minimum distance between the shapes, in cells.
export function fitInsertion(request: FitRequest, resolution = MAX_RESOLUTION, marginCells = 1): Placement {
  const { frame, insertedBox: box } = request;
  const anchor = anchorOf(box, request.horizontal, request.vertical);
  const candidates = candidatePositions(frame, request.horizontal, request.vertical);
  const fromSide = (request.horizontal === "CENTER") !== (request.vertical === "CENTER");

  if (frame.width <= 0 || frame.height <= 0 || box.width <= 0 || box.height <= 0) {
    return placementAt(1, candidates[0]!, anchor, false);
  }

  const target = new ShapeMask(frame, resolution);
  for (const s of request.target) target.fill(s);
  const forbidden = target.dilate(marginCells);
  const trial = new ShapeMask(forbidden);

  let bestScale = scaleAt(SCALE_STEPS - 1);
  let bestPosition = candidates[0]!;
  let bestOverlap = Number.MAX_SAFE_INTEGER;

  for (let k = 0; k < SCALE_STEPS; k++) {
    const scale = scaleAt(k);
    const lastScale = k === SCALE_STEPS - 1;
    for (const position of candidates) {
      const p = placementAt(scale, position, anchor, true);
      const inside = isInside(frame, box, p);
      if (inside || lastScale) {
        trial.clear();
        const t = placementTransform(p);
        for (const s of request.inserted) trial.fill(s, t);
        const overlap = forbidden.overlap(trial);
        if (overlap === 0 && inside) return p;
        if (lastScale && overlap < bestOverlap) {
          bestOverlap = overlap;
          bestScale = scale;
          bestPosition = position;
        }
        if (fromSide && overlap > 0) break;
      }
    }
  }
  return placementAt(bestScale, bestPosition, anchor, false);
}

function placementAt(scale: number, position: Point, anchor: Point, fits: boolean): Placement {
  return { scale, translateX: position.x - scale * anchor.x, translateY: position.y - scale * anchor.y, fits };
}

function isInside(frame: Rect, box: Rect, p: Placement): boolean {
  const moved = placedBox(p, box);
  const eps = EPSILON * Math.max(frame.width, frame.height);
  return (
    moved.minX >= frame.minX - eps &&
    moved.maxX <= frame.maxX + eps &&
    moved.minY >= frame.minY - eps &&
    moved.maxY <= frame.maxY + eps
  );
}

interface Point {
  x: number;
  y: number;
}

/// The point of a box used to attach it to the candidate position.
function anchorOf(box: Rect, h: HorizontalSide, v: VerticalSide): Point {
  return {
    x: h === "LEFT" ? box.minX : h === "RIGHT" ? box.maxX : box.centerX,
    y: v === "TOP" ? box.minY : v === "BOTTOM" ? box.maxY : box.centerY,
  };
}

/// Candidate positions in the frame, best first.
export function candidatePositions(frame: Rect, h: HorizontalSide, v: VerticalSide): Point[] {
  const result: Point[] = [];
  const w = frame.width;
  const ht = frame.height;
  const sx = h === "LEFT" ? 1 : -1;
  const sy = v === "TOP" ? 1 : -1;
  const cornerX = h === "LEFT" ? frame.minX : frame.maxX;
  const cornerY = v === "TOP" ? frame.minY : frame.maxY;

  if (h === "CENTER" && v === "CENTER") {
    result.push({ x: frame.centerX, y: frame.centerY });
  } else if (h !== "CENTER" && v !== "CENTER") {
    // Oblique: grid points (i,j), nearest to the corner first.
    const grid: [number, number][] = [];
    for (let j = 0; j < GRID_POINTS; j++) for (let i = 0; i < GRID_POINTS; i++) grid.push([i, j]);
    // Distance to the corner, then smaller horizontal offset first.
    grid.sort((a, b) => a[0] * a[0] + a[1] * a[1] - (b[0] * b[0] + b[1] * b[1]) || a[0] - b[0]);
    const stepX = (CORNER_SQUARE * w) / (GRID_POINTS - 1);
    const stepY = (CORNER_SQUARE * ht) / (GRID_POINTS - 1);
    for (const [i, j] of grid) result.push({ x: cornerX + sx * i * stepX, y: cornerY + sy * j * stepY });
  } else if (v === "CENTER") {
    // From the left or right edge to the center.
    for (let k = 0; k < SIDE_POINTS; k++) {
      const t = (0.5 * k) / (SIDE_POINTS - 1);
      result.push({ x: cornerX + sx * t * w, y: frame.centerY });
    }
  } else {
    // From the top or bottom edge to the center.
    for (let k = 0; k < SIDE_POINTS; k++) {
      const t = (0.5 * k) / (SIDE_POINTS - 1);
      result.push({ x: frame.centerX, y: cornerY + sy * t * ht });
    }
  }
  return result;
}
