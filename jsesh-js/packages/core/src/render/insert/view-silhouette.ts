/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The silhouette of a laid-out view: the outlines of the signs it contains, as they will be
/// drawn. Port of `jsesh.render.layout.insert.ViewSilhouette`.
///
/// It follows the way the SVG drawing places views and signs (sub-view positions, view scales,
/// mirroring, base sign scale). The silhouette is given in the view's own coordinate system, in
/// the parent's scale: that of its position and size. The view is supposed to be laid out.
///
/// Approximations:
///
/// - the standard body of signs is always used;
/// - views without a known outline (text, unknown signs, shading, signs whose shape is not
///   loaded) count as their box;
/// - spaces count as nothing.

import type { TextDirection } from "../../model/constants.js";
import { SymbolCodes } from "../../model/constants.js";
import { Hieroglyph } from "../../model/model.js";
import type { HieroglyphDrawer } from "../glyphs.js";
import { concatenate, IDENTITY, parsePath, pathBounds, rotation, scaling, transformPath, translation, type Matrix } from "../path.js";
import { Rect, type MDCView } from "../view.js";
import { rectOutline, type Outline } from "./shape-mask.js";

/// The transformation from glyph coordinates (the shape as it is drawn, rotation included) to the
/// coordinates of the sign's view (`SignPlacement.glyphToView`).
export function glyphToView(reversed: boolean, viewWidth: number, baseSignScale: number, xScale: number, yScale: number): Matrix {
  const mirror: Matrix = [-1, 0, 0, 1, viewWidth, 0];
  return concatenate(concatenate(reversed ? mirror : IDENTITY, scaling(baseSignScale, baseSignScale)), scaling(xScale, yScale));
}

export class ViewSilhouette {
  /// @param drawer used to find the shape of signs.
  /// @param baseSignScale the scale from the font to the style.
  /// @param textDirection the direction of the text: signs are mirrored in right-to-left text.
  constructor(
    private readonly drawer: HieroglyphDrawer,
    private readonly baseSignScale: number,
    private readonly textDirection: TextDirection,
  ) {}

  /// The outlines of a view, which should be filled separately (each one has its own winding rule).
  silhouette(view: MDCView): Outline[] {
    const result: Outline[] = [];
    this.collect(view, IDENTITY, result);
    return result;
  }

  private collect(view: MDCView, toRoot: Matrix, result: Outline[]): void {
    if (view.numberOfSubviews === 0) {
      this.collectLeaf(view, toRoot, result);
      return;
    }
    // As in the drawing: first the view's scale, then each sub view's origin.
    const inner = concatenate(toRoot, scaling(view.xScale, view.yScale));
    for (let i = 0; i < view.numberOfSubviews; i++) {
      const bounds = view.getSubViewBounds(i);
      this.collect(view.getSubView(i), concatenate(inner, translation(bounds.minX, bounds.minY)), result);
    }
  }

  private collectLeaf(view: MDCView, toRoot: Matrix, result: Outline[]): void {
    const h = view.model;
    if (h instanceof Hieroglyph) {
      if (h.type === SymbolCodes.HALFSPACE || h.type === SymbolCodes.FULLSPACE) return;
      if (h.type === SymbolCodes.MDCCODE && this.drawer.isKnown(h.code) && this.collectSign(h, view, toRoot, result)) return;
    }
    result.push(rectOutline(new Rect(0, 0, view.width, view.height), toRoot));
  }

  /// Adds the outline of a sign. Returns false if its shape is not available.
  private collectSign(h: Hieroglyph, view: MDCView, toRoot: Matrix, result: Outline[]): boolean {
    const shape = this.drawer.getShape(h.code);
    if (shape === undefined) return false;
    // The same rule as the drawing of signs.
    const reversed = h.isReversed !== (this.textDirection === "RIGHT_TO_LEFT");
    let path = parsePath(shape.d);
    if (h.angle !== 0) {
      // Rotated, then moved so that its top-left corner is at 0,0.
      path = transformPath(path, rotation(h.angle));
      const b = pathBounds(path);
      path = transformPath(path, translation(-b.minX, -b.minY));
    }
    const transform = concatenate(toRoot, glyphToView(reversed, view.width, this.baseSignScale, view.xScale, view.yScale));
    result.push({ path: transformPath(path, transform), evenOdd: shape.evenOdd === true });
    return true;
  }
}
