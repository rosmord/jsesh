/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Minimal path geometry: parsing the paths of sign shapes, transforming
/// them and computing their exact bounds (as `Path2D.getBounds2D`).

import { Rect } from "./view.js";

export type PathSegment =
  | { type: "M" | "L"; points: [number, number] }
  | { type: "Q"; points: [number, number, number, number] }
  | { type: "C"; points: [number, number, number, number, number, number] }
  | { type: "Z" };

/// Parses a path with absolute `M L Q C Z` commands (the format of the
/// exported sign shapes).
export function parsePath(d: string): PathSegment[] {
  const result: PathSegment[] = [];
  const re = /([MLQCZ])([^MLQCZ]*)/g;
  for (const m of d.matchAll(re)) {
    const type = m[1] as PathSegment["type"];
    const n = m[2]!.trim() === "" ? [] : m[2]!.trim().split(/[\s,]+/).map(Number);
    result.push(type === "Z" ? { type } : ({ type, points: n } as PathSegment));
  }
  return result;
}

/// A 2D affine transform `[a, b, c, d, e, f]`: `x' = a x + c y + e`,
/// `y' = b x + d y + f` (as in SVG).
export type Matrix = [number, number, number, number, number, number];

export function rotation(degrees: number): Matrix {
  const r = (degrees * Math.PI) / 180;
  const cos = Math.cos(r);
  const sin = Math.sin(r);
  return [cos, sin, -sin, cos, 0, 0];
}

export function transformPath(path: PathSegment[], m: Matrix): PathSegment[] {
  return path.map((s) => {
    if (s.type === "Z") return s;
    const p = [...s.points];
    for (let i = 0; i < p.length; i += 2) {
      const x = p[i]!;
      const y = p[i + 1]!;
      p[i] = m[0] * x + m[2] * y + m[4];
      p[i + 1] = m[1] * x + m[3] * y + m[5];
    }
    return { type: s.type, points: p } as PathSegment;
  });
}

export function pathToString(path: PathSegment[]): string {
  return path.map((s) => (s.type === "Z" ? "Z" : s.type + s.points.map(round).join(" "))).join("");
}

function round(n: number): number {
  return Math.round(n * 1000) / 1000;
}

/// The exact bounds of a path, curves included.
export function pathBounds(path: PathSegment[]): Rect {
  let minX = Infinity;
  let minY = Infinity;
  let maxX = -Infinity;
  let maxY = -Infinity;
  const add = (x: number, y: number) => {
    minX = Math.min(minX, x);
    minY = Math.min(minY, y);
    maxX = Math.max(maxX, x);
    maxY = Math.max(maxY, y);
  };
  let cx = 0;
  let cy = 0;
  for (const s of path) {
    switch (s.type) {
      case "M":
      case "L":
        [cx, cy] = s.points;
        add(cx, cy);
        break;
      case "Q": {
        const [x1, y1, x2, y2] = s.points;
        for (const t of quadExtrema(cx, x1, x2).concat(quadExtrema(cy, y1, y2))) {
          const u = 1 - t;
          add(u * u * cx + 2 * u * t * x1 + t * t * x2, u * u * cy + 2 * u * t * y1 + t * t * y2);
        }
        add(x2, y2);
        cx = x2;
        cy = y2;
        break;
      }
      case "C": {
        const [x1, y1, x2, y2, x3, y3] = s.points;
        for (const t of cubicExtrema(cx, x1, x2, x3).concat(cubicExtrema(cy, y1, y2, y3))) {
          add(cubicAt(cx, x1, x2, x3, t), cubicAt(cy, y1, y2, y3, t));
        }
        add(x3, y3);
        cx = x3;
        cy = y3;
        break;
      }
      case "Z":
        break;
    }
  }
  if (minX === Infinity) return new Rect();
  return new Rect(minX, minY, maxX - minX, maxY - minY);
}

function quadExtrema(p0: number, p1: number, p2: number): number[] {
  const denominator = p0 - 2 * p1 + p2;
  if (denominator === 0) return [];
  const t = (p0 - p1) / denominator;
  return t > 0 && t < 1 ? [t] : [];
}

function cubicAt(p0: number, p1: number, p2: number, p3: number, t: number): number {
  const u = 1 - t;
  return u * u * u * p0 + 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t * p3;
}

function cubicExtrema(p0: number, p1: number, p2: number, p3: number): number[] {
  // Roots of the derivative a t² + b t + c.
  const a = 3 * (-p0 + 3 * p1 - 3 * p2 + p3);
  const b = 6 * (p0 - 2 * p1 + p2);
  const c = 3 * (p1 - p0);
  const roots: number[] = [];
  if (Math.abs(a) < 1e-12) {
    if (b !== 0) roots.push(-c / b);
  } else {
    const delta = b * b - 4 * a * c;
    if (delta >= 0) {
      const sq = Math.sqrt(delta);
      roots.push((-b + sq) / (2 * a), (-b - sq) / (2 * a));
    }
  }
  return roots.filter((t) => t > 0 && t < 1);
}
