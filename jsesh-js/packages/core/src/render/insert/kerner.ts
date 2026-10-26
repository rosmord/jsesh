/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Computes how much two groups can be kerned: port of `jsesh.render.layout.insert.Kerner`.
///
/// It tells how far the second group can be moved back towards the first, along the kerning axis,
/// so that their signs end up about `skip` apart where they are closest.
///
/// The computation uses the actual outlines of the signs (see [ViewSilhouette]). The cross axis is
/// cut into [BANDS] bands. In each band, the *profile* of the first group is its furthest extent
/// along the axis, and the profile of the second group is its nearest one. Both are exact for the
/// flattened outlines. The first profile is spread over the bands closer than `skip`, so that the
/// remaining space is close to an actual distance between the shapes.
///
/// Views are given with their positions in the layout coordinates of a common parent. In
/// right-to-left text, layout coordinates are the mirror of drawing coordinates; silhouettes are
/// mirrored back accordingly.

import type { KerningAxis, TextDirection } from "../../model/constants.js";
import type { HieroglyphDrawer } from "../glyphs.js";
import { concatenate, translation, type Matrix } from "../path.js";
import type { MDCView } from "../view.js";
import { flattenPath } from "./shape-mask.js";
import { ViewSilhouette } from "./view-silhouette.js";

/// Number of bands along the cross axis.
export const BANDS = 64;

export class Kerner {
  private readonly silhouette: ViewSilhouette;
  private readonly rightToLeft: boolean;

  /// @param drawer used to find the shape of signs.
  /// @param baseSignScale the scale from the font to the style.
  /// @param textDirection the direction of the text.
  constructor(drawer: HieroglyphDrawer, baseSignScale: number, textDirection: TextDirection) {
    this.silhouette = new ViewSilhouette(drawer, baseSignScale, textDirection);
    this.rightToLeft = textDirection === "RIGHT_TO_LEFT";
  }

  /// How far `after` can be moved back towards `before`.
  ///
  /// @param before the views of the first group, already placed.
  /// @param after the view of the second group, placed where it would be without kerning.
  /// @param axis the kerning axis.
  /// @param skip the space to leave between the signs.
  /// @returns the distance to move `after` back, along the axis. It is never negative (kerning
  ///          never pushes groups apart), and never moves `after` before the start of the first
  ///          group.
  shift(before: readonly MDCView[], after: MDCView, axis: KerningAxis, skip: number): number {
    const horizontal = axis === "HORIZONTAL";
    let beforeStart = Infinity;
    let crossMin = cross(after, horizontal);
    let crossMax = crossMin + crossLength(after, horizontal);
    for (const v of before) {
      beforeStart = Math.min(beforeStart, main(v, horizontal));
      crossMin = Math.min(crossMin, cross(v, horizontal));
      crossMax = Math.max(crossMax, cross(v, horizontal) + crossLength(v, horizontal));
    }
    const maxShift = main(after, horizontal) - beforeStart;
    if (before.length === 0 || maxShift <= 0 || crossMax <= crossMin) return 0;

    const first = new Profile(crossMin, crossMax, horizontal);
    for (const v of before) this.addView(first, v);
    const second = new Profile(crossMin, crossMax, horizontal);
    this.addView(second, after);

    const firstMax = first.dilatedMax(skip);
    let gap = Infinity;
    for (let b = 0; b < BANDS; b++) {
      if (second.min[b] !== Infinity && firstMax[b] !== -Infinity) {
        gap = Math.min(gap, second.min[b]! - firstMax[b]!);
      }
    }
    const shift = gap === Infinity ? maxShift : gap - skip;
    return Math.max(0, Math.min(shift, maxShift));
  }

  /// Adds the outlines of a view, at its position.
  private addView(profile: Profile, view: MDCView): void {
    let transform: Matrix = translation(view.position.x, view.position.y);
    if (this.rightToLeft) {
      // Back from drawing to layout coordinates, inside the view's box.
      transform = concatenate(transform, [-1, 0, 0, 1, view.width, 0]);
    }
    for (const outline of this.silhouette.silhouette(view)) {
      const edges = flattenPath(outline.path, transform, profile.bandSize / 4);
      for (let i = 0; i < edges.length; i += 4) profile.addEdge(edges[i]!, edges[i + 1]!, edges[i + 2]!, edges[i + 3]!);
    }
  }
}

function main(v: MDCView, horizontal: boolean): number {
  return horizontal ? v.position.x : v.position.y;
}

function cross(v: MDCView, horizontal: boolean): number {
  return horizontal ? v.position.y : v.position.x;
}

function crossLength(v: MDCView, horizontal: boolean): number {
  return horizontal ? v.height : v.width;
}

/// The extent of some outlines along the main axis, band by band.
class Profile {
  readonly bandSize: number;
  /// Smallest and largest coordinate along the main axis, for each band.
  readonly min = new Float64Array(BANDS).fill(Infinity);
  readonly max = new Float64Array(BANDS).fill(-Infinity);

  constructor(
    private readonly origin: number,
    crossMax: number,
    private readonly horizontal: boolean,
  ) {
    this.bandSize = (crossMax - origin) / BANDS;
  }

  /// Updates the bands crossed by an edge. The extremes of an edge clipped to a band are at the
  /// ends of the clipped part.
  addEdge(x0: number, y0: number, x1: number, y1: number): void {
    const u0 = this.horizontal ? x0 : y0;
    const v0 = this.horizontal ? y0 : x0;
    const u1 = this.horizontal ? x1 : y1;
    const v1 = this.horizontal ? y1 : x1;
    const low = Math.min(v0, v1);
    const high = Math.max(v0, v1);
    const first = Math.max(0, Math.floor((low - this.origin) / this.bandSize));
    const last = Math.min(BANDS - 1, Math.floor((high - this.origin) / this.bandSize));
    for (let b = first; b <= last; b++) {
      const bandLow = Math.max(low, this.origin + b * this.bandSize);
      const bandHigh = Math.min(high, this.origin + (b + 1) * this.bandSize);
      if (bandLow > bandHigh) continue;
      let a: number;
      let c: number;
      if (v0 === v1) {
        a = u0;
        c = u1;
      } else {
        a = u0 + ((bandLow - v0) * (u1 - u0)) / (v1 - v0);
        c = u0 + ((bandHigh - v0) * (u1 - u0)) / (v1 - v0);
      }
      this.min[b] = Math.min(this.min[b]!, a, c);
      this.max[b] = Math.max(this.max[b]!, a, c);
    }
  }

  /// The largest extent, spread over the bands closer than `distance`.
  dilatedMax(distance: number): Float64Array {
    const k = Math.ceil(distance / this.bandSize - 1e-9);
    const result = new Float64Array(BANDS);
    for (let b = 0; b < BANDS; b++) {
      let m = -Infinity;
      for (let j = Math.max(0, b - k); j <= Math.min(BANDS - 1, b + k); j++) m = Math.max(m, this.max[j]!);
      result[b] = m;
    }
    return result;
  }
}
