/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Places the sub views of a view one after another along an axis, honouring the kerning
/// operators found among them: port of `jsesh.render.layout.KerningPacker`.
///
/// Kerning operators take no room and add no space. An operator of the packing axis kerns the
/// elements around it; they then form a rigid *block*, which is moved as a whole when the free
/// space of the parent is distributed. An operator of the other axis is ignored: the elements are
/// placed as if it was not there.
///
/// When there is no kerning operator, each element is a block of its own, and the result is the
/// same as the usual layout.

import type { KerningAxis } from "../model/constants.js";
import { Cadrat, HBox, Kerning } from "../model/model.js";
import type { Kerner } from "./insert/kerner.js";
import type { MDCView } from "./view.js";

/// The kerning operator a view stands for, if any: a kerning element, or a box or quadrat
/// containing only kerning.
export function kerningOf(view: MDCView): KerningAxis | undefined {
  const m = view.model;
  if (m instanceof Kerning) return m.axis;
  if (m instanceof HBox || m instanceof Cadrat) return m.getKerningAxis();
  return undefined;
}

/// Does one of the sub views of a view stand for a kerning operator?
export function hasKerning(view: MDCView): boolean {
  return view.subviews.some((sub) => kerningOf(sub) !== undefined);
}

export class KerningPacker {
  /// @param kerner computes the kerning.
  /// @param axis the axis along which the views are placed.
  /// @param skip the space between two elements, and between kerned signs.
  constructor(
    private readonly kerner: Kerner,
    private readonly axis: KerningAxis,
    private readonly skip: number,
  ) {}

  /// The blocks formed by the sub views of a view: lists of indices of sub views. Each kerning
  /// operator belongs to the block of the element before it (or of the first element, for leading
  /// operators).
  blocks(parent: MDCView): number[][] {
    const blocks: number[][] = [];
    let current: number[] | null = null;
    const leading: number[] = [];
    let kern = false;
    for (let i = 0; i < parent.numberOfSubviews; i++) {
      const k = kerningOf(parent.getSubView(i));
      if (k !== undefined) {
        if (current === null) {
          leading.push(i);
        } else {
          current.push(i);
          kern ||= k === this.axis;
        }
      } else {
        if (current === null || !kern) {
          current = [];
          blocks.push(current);
        }
        current.push(i);
        kern = false;
        if (leading.length > 0) {
          current.unshift(...leading);
          leading.length = 0;
        }
      }
    }
    if (leading.length > 0) blocks.push(leading);
    return blocks;
  }

  /// Sets the positions of the sub views along the axis, starting from 0.
  ///
  /// Positions on the other axis must already be set, as the kerning depends on them. Kerning
  /// operators are put in the middle of their gap.
  ///
  /// @returns the extent of the sub views along the axis.
  pack(parent: MDCView): number {
    const block: MDCView[] = [];
    const pendingKernings: MDCView[] = [];
    let previous: MDCView | null = null;
    let end = 0;
    let kern = false;
    for (const v of parent.subviews) {
      const k = kerningOf(v);
      if (k !== undefined) {
        kern ||= previous !== null && k === this.axis;
        pendingKernings.push(v);
        continue;
      }
      let start = 0;
      if (previous !== null) {
        start = end + this.skip;
        this.setMain(v, start);
        if (kern) start -= this.kerner.shift(block, v, this.axis, this.skip);
        else block.length = 0;
      }
      this.setMain(v, start);
      for (const p of pendingKernings) this.setMain(p, previous === null ? start : (end + start) / 2);
      pendingKernings.length = 0;
      block.push(v);
      end = Math.max(end, start + this.length(v));
      previous = v;
      kern = false;
    }
    for (const p of pendingKernings) this.setMain(p, end);
    return end;
  }

  /// Distributes blocks over a length, as [MDCView.distributeHorizontally] and
  /// [MDCView.distributeFromTopToBottom] do for views: the free space is shared equally between
  /// the blocks, which keep their inner layout.
  ///
  /// @param blocks the blocks of `parent`, from [blocks]; there should be more than one.
  /// @param length the length to fill, in the parent's internal coordinates.
  distribute(parent: MDCView, blocks: number[][], length: number): void {
    let sum = 0;
    for (const b of blocks) sum += this.blockLength(parent, b);
    const interSpace = (length - sum) / (blocks.length - 1);
    let pos = 0;
    for (const b of blocks) {
      this.moveBlockTo(parent, b, pos);
      pos += this.blockLength(parent, b) + interSpace;
    }
  }

  /// Moves a block rigidly along the axis, so that it starts at `pos`.
  moveBlockTo(parent: MDCView, block: number[], pos: number): void {
    const delta = pos - this.blockStart(parent, block);
    for (const i of block) {
      const v = parent.getSubView(i);
      this.setMain(v, this.main(v) + delta);
    }
  }

  /// The length of a block along the axis (that of its elements, not of its kerning operators).
  blockLength(parent: MDCView, block: number[]): number {
    let start = Infinity;
    let end = -Infinity;
    for (const i of block) {
      const v = parent.getSubView(i);
      if (kerningOf(v) === undefined) {
        start = Math.min(start, this.main(v));
        end = Math.max(end, this.main(v) + this.length(v));
      }
    }
    return start > end ? 0 : end - start;
  }

  private blockStart(parent: MDCView, block: number[]): number {
    let start = Infinity;
    for (const i of block) {
      const v = parent.getSubView(i);
      if (kerningOf(v) === undefined) start = Math.min(start, this.main(v));
    }
    return start === Infinity ? this.main(parent.getSubView(block[0]!)) : start;
  }

  private main(v: MDCView): number {
    return this.axis === "HORIZONTAL" ? v.position.x : v.position.y;
  }

  private setMain(v: MDCView, pos: number): void {
    if (this.axis === "HORIZONTAL") v.position.x = pos;
    else v.position.y = pos;
  }

  private length(v: MDCView): number {
    return this.axis === "HORIZONTAL" ? v.width : v.height;
  }
}
