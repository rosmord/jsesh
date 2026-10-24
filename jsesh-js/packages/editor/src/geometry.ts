/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Where things are in a laid-out text, in pixels. This is what a view needs
/// to place the cursor, draw a selection and find which item a click hit. No
/// DOM here: a React or Vue component can use these functions with its own
/// markup.
///
/// The model is linked to the picture like this: given a `HieroglyphicText`
/// (not a string), `layOutInPage` returns views which refer to the model's
/// elements. The subviews of the root view are the views of the top items
/// (quadrats, line breaks...), in the same order.

import type { LaidOutText } from "@jsesh/core";

/// A rectangle, in pixels, from the top left corner of the picture.
export interface Area {
  x: number;
  y: number;
  width: number;
  height: number;
}

/// How the text flows.
export interface TextFlow {
  vertical: boolean;
  rightToLeft: boolean;
}

/// Thickness of the cursor, in pixels.
export const CARET_THICKNESS = 2;

/// The area of each top item of a laid-out text, in pixels, for quadrats of
/// `cadratHeight` pixels.
export function itemAreas(laidOut: LaidOutText, cadratHeight: number): Area[] {
  const scale = cadratHeight / laidOut.style.geometry.standardSignHeight;
  const root = laidOut.view;
  return root.subviews.map((_, i) => {
    const b = root.getSubViewBounds(i);
    return {
      x: b.x * root.xScale * scale,
      y: b.y * root.yScale * scale,
      width: b.width * root.xScale * scale,
      height: b.height * root.yScale * scale,
    };
  });
}

/// The cursor position (a number of top items) closest to a point of the
/// picture: before or after the nearest quadrat.
export function positionAt(areas: readonly Area[], x: number, y: number, flow: TextFlow): number {
  if (areas.length === 0) return 0;
  let best = 0;
  let bestDistance = Infinity;
  areas.forEach((a, i) => {
    const dx = Math.max(a.x - x, 0, x - (a.x + a.width));
    const dy = Math.max(a.y - y, 0, y - (a.y + a.height));
    // Being on the right line matters more than being near the quadrat.
    const distance = flow.vertical ? dx * 4 + dy : dy * 4 + dx;
    if (distance < bestDistance) {
      bestDistance = distance;
      best = i;
    }
  });
  const a = areas[best];
  let after: boolean;
  if (flow.vertical) after = y > a.y + a.height / 2;
  else if (flow.rightToLeft) after = x < a.x + a.width / 2;
  else after = x > a.x + a.width / 2;
  return after ? best + 1 : best;
}

/// The area of the cursor when it stands after `caret` top items.
///
/// @param cadratHeight height of a quadrat, in pixels.
/// @param origin where the text starts (the margin), in pixels; used when the text is empty.
export function caretArea(
  areas: readonly Area[],
  caret: number,
  flow: TextFlow,
  cadratHeight: number,
  origin: number,
): Area {
  const size = cadratHeight;
  const thickness = CARET_THICKNESS;
  // The cursor is drawn before the item which follows it, or after the last one.
  const next = areas[caret];
  const last = areas[caret - 1];
  if (next === undefined && last === undefined) {
    return flow.vertical
      ? { x: origin, y: origin, width: size, height: thickness }
      : { x: origin, y: origin, width: thickness, height: size };
  }
  const a = (next ?? last)!;
  if (flow.vertical) {
    return { x: a.x, y: next !== undefined ? a.y - thickness : a.y + a.height, width: Math.max(a.width, size / 2), height: thickness };
  }
  const startSide = next !== undefined;
  // In right-to-left lines, the start of an item is its right side.
  const x = startSide !== flow.rightToLeft ? a.x - thickness : a.x + a.width;
  return { x, y: a.y, width: thickness, height: Math.max(a.height, size / 2) };
}
