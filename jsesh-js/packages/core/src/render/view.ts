/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The view tree built by the layout: port of `jsesh.render.view.MDCView`.
///
/// A view's position is expressed in its parent's system, with the y axis
/// pointing down. Its scale is its *inner* scale, used for its subviews. The
/// layout always places subviews from left to right; a right-to-left view is
/// mirrored when drawn ([MDCView.layoutToDrawing]).
///
/// Java stores sizes and scales as `float`: they are rounded with
/// `Math.fround` here, so that the results match Java's.

import type { TextDirection } from "../model/constants.js";
import { SubCadrat, type ModelElement } from "../model/model.js";

const f = Math.fround;

export interface Point {
  x: number;
  y: number;
}

/// A rectangle, as `java.awt.geom.Rectangle2D`.
export class Rect {
  constructor(
    public x = 0,
    public y = 0,
    public width = 0,
    public height = 0,
  ) {}

  get minX(): number {
    return this.x;
  }

  get minY(): number {
    return this.y;
  }

  get maxX(): number {
    return this.x + this.width;
  }

  get maxY(): number {
    return this.y + this.height;
  }

  get centerX(): number {
    return this.x + this.width / 2;
  }

  get centerY(): number {
    return this.y + this.height / 2;
  }

  isEmpty(): boolean {
    return this.width <= 0 || this.height <= 0;
  }

  /// The smallest rectangle containing this one and `r` (`Rectangle2D.createUnion`).
  union(r: Rect): Rect {
    const x1 = Math.min(this.minX, r.minX);
    const y1 = Math.min(this.minY, r.minY);
    return new Rect(x1, y1, Math.max(this.maxX, r.maxX) - x1, Math.max(this.maxY, r.maxY) - y1);
  }

  clone(): Rect {
    return new Rect(this.x, this.y, this.width, this.height);
  }

  /// Grows this rectangle to include `r` (`Rectangle2D.add`, which, unlike
  /// a union, also works from an empty rectangle).
  add(r: Rect): void {
    this.setFromDiagonal(
      Math.min(this.minX, r.minX),
      Math.min(this.minY, r.minY),
      Math.max(this.maxX, r.maxX),
      Math.max(this.maxY, r.maxY),
    );
  }

  addPoint(x: number, y: number): void {
    this.setFromDiagonal(Math.min(this.minX, x), Math.min(this.minY, y), Math.max(this.maxX, x), Math.max(this.maxY, y));
  }

  private setFromDiagonal(x1: number, y1: number, x2: number, y2: number): void {
    this.x = x1;
    this.y = y1;
    this.width = x2 - x1;
    this.height = y2 - y1;
  }
}

export type ViewSide = "TOP" | "BOTTOM" | "START" | "END";

export class MDCView {
  private _angle = 0;
  deltaBaseX = 0;
  deltaBaseY = 0;
  xStretchable = false;
  yStretchable = false;
  direction: TextDirection = "LEFT_TO_RIGHT";
  private _parent: MDCView | null = null;
  private _next: MDCView | null = null;
  private _previous: MDCView | null = null;
  position: Point = { x: 0, y: 0 };
  private readonly subViews: MDCView[] = [];
  private _width = 0;
  private _height = 0;
  private _xScale = 1;
  private _yScale = 1;

  constructor(public model: ModelElement) {}

  get width(): number {
    return this._width;
  }

  set width(w: number) {
    this._width = f(w);
  }

  get height(): number {
    return this._height;
  }

  set height(h: number) {
    this._height = f(h);
  }

  get xScale(): number {
    return this._xScale;
  }

  set xScale(s: number) {
    this._xScale = f(s);
  }

  get yScale(): number {
    return this._yScale;
  }

  set yScale(s: number) {
    this._yScale = f(s);
  }

  get angle(): number {
    return this._angle;
  }

  set angle(a: number) {
    this._angle = f(a);
  }

  get parent(): MDCView | null {
    return this._parent;
  }

  get next(): MDCView | null {
    return this._next;
  }

  get previous(): MDCView | null {
    return this._previous;
  }

  get numberOfSubviews(): number {
    return this.subViews.length;
  }

  get subviews(): readonly MDCView[] {
    return this.subViews;
  }

  getSubView(i: number): MDCView {
    const v = this.subViews[i];
    if (v === undefined) throw new RangeError(`no subview ${i}`);
    return v;
  }

  get firstSubView(): MDCView {
    return this.getSubView(0);
  }

  get lastSubView(): MDCView | null {
    return this.subViews[this.subViews.length - 1] ?? null;
  }

  add(subview: MDCView): void {
    this.addAt(this.subViews.length, subview);
  }

  addAt(i: number, subView: MDCView): void {
    this.subViews.splice(i, 0, subView);
    this.fixSiblingsAt(i - 1);
    this.fixSiblingsAt(i);
    this.fixSiblingsAt(i + 1);
    subView._parent = this;
  }

  remove(a: number, b = a + 1): void {
    for (let i = b - 1; i >= a; i--) {
      const v = this.getSubView(i);
      v._parent = null;
      v._next = null;
      v._previous = null;
      this.subViews.splice(i, 1);
    }
    this.fixSiblingsAt(a - 1);
    this.fixSiblingsAt(a);
  }

  replaceSubView(i: number, v: MDCView): void {
    this.subViews[i] = v;
    this.fixSiblingsAt(i - 1);
    this.fixSiblingsAt(i);
    this.fixSiblingsAt(i + 1);
    v._parent = this;
  }

  clear(): void {
    for (const v of this.subViews) {
      v._parent = null;
      v._next = null;
      v._previous = null;
    }
    this.subViews.length = 0;
  }

  private fixSiblingsAt(index: number): void {
    if (index < 0 || index >= this.subViews.length) return;
    const v = this.subViews[index]!;
    v._previous = this.subViews[index - 1] ?? null;
    v._next = this.subViews[index + 1] ?? null;
  }

  /// The width of this view, from the point of view of its contents.
  get internalWidth(): number {
    return f(this._width / this._xScale);
  }

  /// The height of this view, from the point of view of its contents.
  get internalHeight(): number {
    return f(this._height / this._yScale);
  }

  centerSubViewHorizontally(): void {
    for (const sub of this.subViews) {
      sub.position.x = f((this.internalWidth - sub.width) / 2);
    }
  }

  centerSubViewsVertically(): void {
    for (const sub of this.subViews) {
      sub.position.y = f((this.internalHeight - sub.height) / 2);
    }
  }

  /// Distributes the subviews vertically, so that they fill the space.
  distributeFromTopToBottom(): void {
    if (this.subViews.length > 1) {
      const minH = this.sumOfSubViewsHeights;
      const interSpace = f((this.internalHeight - minH) / (this.subViews.length - 1));
      let y = 0;
      for (const v of this.subViews) {
        v.position.y = y;
        y += f(v.height + interSpace);
      }
    }
  }

  /// Distributes the subviews horizontally, so that they fill the space.
  distributeHorizontally(): void {
    const n = this.subViews.length;
    if (n > 1) {
      const minW = this.sumOfSubViewsWidths;
      const interSpace = f((this.internalWidth - minW) / (n - 1));
      let x = 0;
      for (const v of this.subViews) {
        v.resetPos();
        v.position.x = x;
        x += f(v.width + interSpace);
      }
    }
  }

  /// Sets the size of this view so that it contains its subviews; if
  /// `doTranslate`, the subviews are moved so that the area starts at 0,0.
  fitToSubViews(doTranslate: boolean): void {
    this.width = 0;
    this.height = 0;
    if (this.subViews.length === 0) return;
    let area: Rect | null = null;
    for (const sub of this.subViews) {
      const r = new Rect(sub.position.x, sub.position.y, sub.width, sub.height);
      if (area === null) area = r;
      else area.add(r);
    }
    if (doTranslate) {
      for (const sub of this.subViews) {
        sub.position.x -= area!.minX;
        sub.position.y -= area!.minY;
      }
      this.width = f(area!.width) * this._xScale;
      this.height = f(area!.height) * this._yScale;
    } else {
      this.width = f(area!.maxX) * this._xScale;
      this.height = f(area!.maxY) * this._yScale;
    }
  }

  get maximalHeightOfSubView(): number {
    let result = 0;
    for (const v of this.subViews) if (result < v.height) result = v.height;
    return result;
  }

  get maximalWidthOfSubView(): number {
    let result = 0;
    for (const v of this.subViews) if (result < v.width) result = v.width;
    return result;
  }

  get sumOfSubViewsHeights(): number {
    let result = 0;
    for (const v of this.subViews) result = f(result + v.height);
    return result;
  }

  get sumOfSubViewsWidths(): number {
    let result = 0;
    for (const v of this.subViews) result = f(result + v.width);
    return result;
  }

  /// The area of a subview, in the system used to draw the subviews of this
  /// view (see [layoutToDrawing]).
  getSubViewBounds(i: number): Rect {
    const sub = this.getSubView(i);
    return this.layoutToDrawing(new Rect(sub.position.x, sub.position.y, sub.width, sub.height));
  }

  /// Converts an area from the layout system to the drawing system of this
  /// view's subviews: a right-to-left view is mirrored across its internal
  /// width.
  layoutToDrawing(area: Rect): Rect {
    if (this.direction !== "RIGHT_TO_LEFT") return area;
    return new Rect(this.internalWidth - area.x - area.width, area.y, area.width, area.height);
  }

  /// Multiplies the inner scale by `scale`; the view's size changes too.
  reScale(scale: number): void {
    scale = f(scale);
    this.xScale = scale * this._xScale;
    this.yScale = scale * this._yScale;
    this.width = this._width * scale;
    this.height = this._height * scale;
  }

  /// Resets size, scale and angle, but not the position.
  reset(): void {
    this.width = 0;
    this.height = 0;
    this._xScale = 1;
    this._yScale = 1;
    this._angle = 0;
    this.xStretchable = false;
    this.yStretchable = false;
  }

  resetPos(): void {
    this.position.x = 0;
    this.position.y = 0;
  }

  /// Scales the view down if it's higher than `maxHeight` (0: no limit).
  scaleForMaxHeight(maxHeight: number): void {
    if (maxHeight === 0) return;
    if (this.height > maxHeight) this.reScale(f(maxHeight / this.height));
  }

  /// Scales the view down if it's wider than `maxWidth` (0: no limit).
  scaleForMaxWidth(maxWidth: number): void {
    if (maxWidth === 0) return;
    if (this.width > maxWidth) this.reScale(f(maxWidth / this.width));
  }

  /// Uniformly scales the view so that it fits the dimensions. Does nothing
  /// if the view is already small enough.
  scaleToFitDimensions(wantedWidth: number, wantedHeight: number): void {
    if (this.width < wantedWidth && this.height < wantedHeight) return;
    let zoneScaleW = 1;
    let zoneScaleH = 1;
    if (this.width !== 0) zoneScaleW = wantedWidth / this.width;
    if (this.height !== 0) zoneScaleH = wantedHeight / this.height;
    const s = f(Math.min(zoneScaleH, zoneScaleW));
    this._xScale = s;
    this._yScale = s;
    this.width = this._width * s;
    this.height = this._height * s;
  }

  /// Scales the view horizontally, so that its content fills `w`.
  scaleToWidth(w: number): void {
    if (this.internalWidth !== 0) {
      this.xScale = w / this.internalWidth;
      this.width = w;
    } else {
      this._xScale = 1;
      this.width = w;
    }
  }

  /// Sets the height of the sub-cadrat subviews to this view's height, and
  /// sticks all subviews to the bottom.
  setSubViewsToHeight(): void {
    const h = this.internalHeight;
    for (const v of this.subViews) {
      if (v.model instanceof SubCadrat) {
        v.height = h;
        v.distributeFromTopToBottom();
      }
    }
    this.stickSubViewsTo("BOTTOM");
  }

  /// Stacks the subviews from the top, separated by `skip`, and fits.
  stackTop(skip: number): void {
    let y = 0;
    for (const v of this.subViews) {
      v.resetPos();
      v.position.y = y;
      y += f(v.height + skip);
    }
    this.fitToSubViews(true);
  }

  stickSubViewsTo(side: ViewSide): void {
    for (const v of this.subViews) {
      switch (side) {
        case "BOTTOM":
          v.position.y = f(this.internalHeight - v.height);
          break;
        case "TOP":
          v.position.y = 0;
          break;
        case "END":
          v.position.x = f(this.width - v.width);
          break;
        default:
          v.position.x = 0;
      }
    }
  }

  /// Is the next sibling, as placed, horizontally adjacent to this one?
  nextIsHorizontallyAdjacent(): boolean {
    return this._next !== null && this._next.position.y < this.position.y + this.height;
  }
}
