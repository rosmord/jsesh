/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Drawing of cartouches and other enclosures, as SVG: port of
/// `jsesh.render.elements.cartouche` (`NormalCartoucheDrawer`, `SerekhDrawer`,
/// `HwtDrawer`, `EnclosureDrawer`, `CircularEnclosureDrawer`).
///
/// The frame fills the cartouche's view, whose size is `width` × `height`.

import type { CartouchePart, TextDirection, TextOrientation } from "../model/constants.js";
import type { Cartouche } from "../model/model.js";
import { computeCartouchePartLength, type JSeshStyle } from "./style.js";
import { filledPath, PathBuilder, strokedPath } from "./svg-writer.js";

interface Point {
  x: number;
  y: number;
}

const pt = (x: number, y: number): Point => ({ x, y });

/// Returns the SVG elements of a cartouche's frame.
export function drawCartouche(
  c: Cartouche,
  style: JSeshStyle,
  width: number,
  height: number,
  color: string,
): string {
  const drawer = new CartoucheDrawer(style, style.options.textDirection, style.options.textOrientation, width, height, color);
  const horizontal = style.options.textOrientation === "HORIZONTAL";
  switch (c.type) {
    case "CASTLE":
      return horizontal ? drawer.enclosureHorizontal(c) : drawer.enclosureVertical(c);
    case "SEREKH":
      return horizontal ? drawer.serekhHorizontal(c) : drawer.serekhVertical(c);
    case "HWT":
      return horizontal ? drawer.hwtHorizontal(c) : drawer.hwtVertical(c);
    case "CIRCULAR_ENCLOSURE":
      return horizontal ? drawer.circularHorizontal(c) : drawer.circularVertical(c);
    default:
      return horizontal ? drawer.normalHorizontal(c) : drawer.normalVertical(c);
  }
}

class CartoucheDrawer {
  /// Lines drawn with the cartouche stroke.
  private readonly lines = new PathBuilder();
  /// Lines drawn with the fine stroke.
  private readonly fineLines = new PathBuilder();
  /// Filled areas.
  private readonly fills = new PathBuilder();

  constructor(
    private readonly style: JSeshStyle,
    private readonly direction: TextDirection,
    private readonly orientation: TextOrientation,
    private readonly width: number,
    private readonly height: number,
    private readonly color: string,
  ) {}

  private result(): string {
    const g = this.style.geometry;
    return (
      strokedPath(this.lines, this.color, g.cartoucheLineWidth) +
      strokedPath(this.fineLines, this.color, g.fineLineWidth) +
      filledPath(this.fills, this.color)
    );
  }

  private line(a: Point, b: Point, fine = false): void {
    (fine ? this.fineLines : this.lines).line(a.x, a.y, b.x, b.y);
  }

  /// The parts on the left and on the right of a horizontal cartouche.
  private sides(c: Cartouche): [CartouchePart, CartouchePart] {
    return this.direction === "LEFT_TO_RIGHT" ? [c.startPart, c.endPart] : [c.endPart, c.startPart];
  }

  private partLength(c: Cartouche, part: CartouchePart): number {
    return computeCartouchePartLength(this.style, c.type, part);
  }

  /// The corners of the body of a horizontal cartouche: p1 and p2 on the
  /// left, p3 and p4 on the right.
  private horizontalCorners(w1: number, w2: number): [Point, Point, Point, Point] {
    const d = this.style.geometry.cartoucheLineWidth / 2;
    return [pt(w1, d), pt(w1, this.height - d), pt(this.width - w2, d), pt(this.width - w2, this.height - d)];
  }

  /// The corners of the body of a vertical cartouche: p1 and p2 at the top,
  /// p3 and p4 at the bottom.
  private verticalCorners(w1: number, w2: number): [Point, Point, Point, Point] {
    const d = this.style.geometry.cartoucheLineWidth / 2;
    return [
      pt(this.width - d, w1),
      pt(d, w1),
      pt(this.width - d, this.height - d - w2),
      pt(d, this.height - d - w2),
    ];
  }

  // ---- Cartouches (NormalCartoucheDrawer)

  normalHorizontal(c: Cartouche): string {
    const g = this.style.geometry;
    const [leftElement, rightElement] = this.sides(c);
    const [p1, p2, p3, p4] = this.horizontalCorners(this.partLength(c, leftElement), this.partLength(c, rightElement));
    const dx = g.cartoucheLineWidth / 2;
    const loopSkip = g.cartoucheLoopLength / 3;
    if (leftElement !== 0) {
      let p0x = dx;
      if (leftElement === 2) {
        p0x += g.cartoucheLineWidth;
        this.lines.line(dx * 1.5, p1.y, dx * 1.5, p2.y);
      }
      this.lines.moveTo(p1.x, p1.y).curveTo(p0x - loopSkip, p1.y, p0x - loopSkip, p2.y, p2.x, p2.y);
    }
    this.line(p1, p3);
    this.line(p2, p4);
    if (rightElement !== 0) {
      let p0x = -dx;
      if (rightElement === 2) {
        p0x += -g.cartoucheLineWidth;
        this.lines.line(this.width - dx * 1.5, p3.y, this.width - dx * 1.5, p4.y);
      }
      this.lines
        .moveTo(p3.x, p3.y)
        .curveTo(this.width + loopSkip + p0x, p3.y, this.width + loopSkip + p0x, p4.y, p4.x, p4.y);
    }
    return this.result();
  }

  normalVertical(c: Cartouche): string {
    const g = this.style.geometry;
    const [p1, p2, p3, p4] = this.verticalCorners(this.partLength(c, c.startPart), this.partLength(c, c.endPart));
    const dy = g.cartoucheLineWidth / 2;
    // Java quirk: the loop skip uses the line width, not the loop length.
    const loopSkip = g.cartoucheLineWidth / 3;
    if (c.startPart !== 0) {
      let p0x = dy;
      if (c.startPart === 2) {
        p0x += g.cartoucheLineWidth;
        this.lines.line(p2.x, dy, p1.x, dy);
      }
      this.lines.moveTo(p1.x, p1.y).curveTo(p1.x, p0x - loopSkip, p2.x, p0x - loopSkip, p2.x, p2.y);
    }
    this.line(p1, p3);
    this.line(p2, p4);
    if (c.endPart !== 0) {
      let p0x = -dy;
      if (c.endPart === 2) {
        p0x += -g.cartoucheLineWidth;
        this.lines.line(p4.x, this.height - dy * 1.5, p3.x, this.height - dy * 1.5);
      }
      this.lines
        .moveTo(p3.x, p3.y)
        .curveTo(p3.x, this.height + loopSkip + p0x, p4.x, this.height + loopSkip + p0x, p4.x, p4.y);
    }
    return this.result();
  }

  // ---- Hwt (HwtDrawer, and AbstractCartoucheDrawer.drawHutEnd)

  hwtHorizontal(c: Cartouche): string {
    const d = this.style.geometry.cartoucheLineWidth / 2;
    const [leftElement, rightElement] = this.sides(c);
    const [p1, p2, p3, p4] = this.horizontalCorners(this.partLength(c, leftElement), this.partLength(c, rightElement));
    if (leftElement !== 0) this.drawHutEnd(leftElement, pt(d, d), pt(d, this.height - d), p1, p2);
    this.line(p1, p3);
    this.line(p2, p4);
    if (rightElement !== 0) {
      this.drawHutEnd(rightElement, pt(this.width - d, d), pt(this.width - d, this.height - d), p3, p4);
    }
    return this.result();
  }

  hwtVertical(c: Cartouche): string {
    const d = this.style.geometry.cartoucheLineWidth / 2;
    const [p1, p2, p3, p4] = this.verticalCorners(this.partLength(c, c.startPart), this.partLength(c, c.endPart));
    if (c.startPart !== 0) this.drawHutEnd(c.startPart, pt(this.width - d, d), pt(d, d), p1, p2);
    this.line(p1, p3);
    this.line(p2, p4);
    if (c.endPart !== 0) {
      this.drawHutEnd(c.endPart, pt(this.width - d, this.height - d), pt(d, this.height - d), p3, p4);
    }
    return this.result();
  }

  /// A square end (hwt, or the closed end of a serekh): the side pa-pb,
  /// joined to the body p1-p2, with a small square for parts 2 and 3.
  private drawHutEnd(part: CartouchePart, pa: Point, pb: Point, p1: Point, p2: Point): void {
    if (part === 0) return;
    const g = this.style.geometry;
    let xSign = 1;
    let ySign = 1;
    this.line(pa, pb);
    this.line(pa, p1);
    this.line(pb, p2);
    const squareSize = g.hwtSquareSize - g.cartoucheLineWidth / 2;
    const horizontal = this.orientation === "HORIZONTAL";
    if (horizontal) {
      if (p1.x < pa.x) xSign = -1;
    } else {
      if (pa.x < pb.x) xSign = -1;
      if (p1.y > pa.y) ySign = -1;
      // In columns, the sides have no relation with the text direction.
      if (this.direction !== "LEFT_TO_RIGHT") {
        if (part === 2) part = 3;
        else if (part === 3) part = 2;
      }
    }
    if (part === 2) {
      // Square on the pb-p2 side.
      const pca = horizontal ? pt(pb.x, pb.y - squareSize) : pt(pb.x, pb.y - squareSize * ySign);
      const pcb = pt(pb.x + xSign * squareSize, pb.y);
      const pc = horizontal
        ? pt(pb.x + xSign * squareSize, pb.y - squareSize)
        : pt(pb.x + xSign * squareSize, pb.y - squareSize * ySign);
      this.line(pca, pc);
      this.line(pc, pcb);
    } else if (part === 3) {
      // Square on the pa-p1 side.
      let pca: Point, pcb: Point, pc: Point;
      if (horizontal) {
        pca = pt(pa.x, pa.y + squareSize);
        pcb = pt(pa.x + xSign * squareSize, pa.y);
        pc = pt(pa.x + xSign * squareSize, p1.y + squareSize);
      } else {
        pca = pt(pa.x, pa.y - squareSize * ySign);
        pcb = pt(pa.x - xSign * squareSize, pa.y);
        pc = pt(pa.x - xSign * squareSize, pa.y - squareSize * ySign);
      }
      this.line(pca, pc);
      this.line(pc, pcb);
    }
  }

  // ---- Serekh (SerekhDrawer)

  serekhHorizontal(c: Cartouche): string {
    const d = this.style.geometry.cartoucheLineWidth / 2;
    const [leftElement, rightElement] = this.sides(c);
    const [p1, p2, p3, p4] = this.horizontalCorners(this.partLength(c, leftElement), this.partLength(c, rightElement));
    if (leftElement !== 0) {
      const pa = pt(d, d);
      const pb = pt(d, this.height - d);
      if (leftElement === 1) this.drawHutEnd(leftElement, pa, pb, p1, p2);
      else this.drawSerekhEnd(p1, p2, pa, pb);
    }
    this.line(p1, p3);
    this.line(p2, p4);
    if (rightElement !== 0) {
      const pa = pt(this.width - d, d);
      const pb = pt(this.width - d, this.height - d);
      if (rightElement === 1) this.drawHutEnd(rightElement, pa, pb, p3, p4);
      else this.drawSerekhEnd(p3, p4, pa, pb);
    }
    return this.result();
  }

  serekhVertical(c: Cartouche): string {
    const d = this.style.geometry.cartoucheLineWidth / 2;
    const [p1, p2, p3, p4] = this.verticalCorners(this.partLength(c, c.startPart), this.partLength(c, c.endPart));
    if (c.startPart !== 0) {
      const pb = pt(d, d);
      const pa = pt(this.width - d, d);
      if (c.startPart === 1) this.drawHutEnd(1, pa, pb, p1, p2);
      else this.drawSerekhEnd(p1, p2, pa, pb);
    }
    this.line(p1, p3);
    this.line(p2, p4);
    if (c.endPart !== 0) {
      const pb = pt(d, this.height - d);
      const pa = pt(this.width - d, this.height - d);
      if (c.endPart === 1) this.drawHutEnd(1, pa, pb, p3, p4);
      else this.drawSerekhEnd(p3, p4, pa, pb);
    }
    return this.result();
  }

  /// The "palace facade" of a serekh: p1-p2 is the side of the text, pa-pb
  /// the end of the facade.
  private drawSerekhEnd(p1: Point, p2: Point, pa: Point, pb: Point): void {
    this.line(p1, pa);
    this.line(p2, pb);
    if (this.orientation === "HORIZONTAL") {
      const dx = pa.x - p1.x;
      const dy = p2.y - p1.y;
      const line1x = p1.x + dx * 0.1;
      const line2x = p1.x + dx * 0.3;
      const line3x = p1.x + dx * 0.4;
      this.lines.line(line1x, p1.y, line1x, p2.y);
      this.fineLines.line(line2x, p1.y, line2x, p2.y);
      this.fineLines.line(line3x, p1.y, line3x, p2.y);
      // Recesses.
      const recessX = p1.x + dx * 0.5;
      for (let i = 1; i < 10; i += 3) {
        const posy1 = p1.y + 0.1 * dy * i;
        const posy2 = p1.y + 0.1 * dy * (i + 1);
        const posy3 = p1.y + 0.1 * dy * (i + 2);
        const endX = pa.x;
        this.fineLines.line(recessX, posy1, endX, posy1);
        this.fineLines.line(recessX, posy3, endX, posy3);
        this.fineLines.line(recessX, posy1, recessX, posy3);
        const innerX = recessX + 0.2 * (endX - recessX);
        this.fineLines.line(innerX, posy2, endX, posy2);
      }
    } else {
      const dx = p2.x - p1.x;
      const dy = pa.y - p1.y;
      const line1y = p1.y + dy * 0.1;
      const line2y = p1.y + dy * 0.3;
      const line3y = p1.y + dy * 0.4;
      this.lines.line(p1.x, line1y, p2.x, line1y);
      this.fineLines.line(p1.x, line2y, p2.x, line2y);
      this.fineLines.line(p1.x, line3y, p2.x, line3y);
      const recessY = p1.y + dy * 0.5;
      const endY = pa.y;
      for (let i = 1; i < 10; i += 3) {
        const posx1 = p1.x + 0.1 * dx * i;
        const posx2 = p1.x + 0.1 * dx * (i + 1);
        const posx3 = p1.x + 0.1 * dx * (i + 2);
        this.fineLines.line(posx1, recessY, posx1, endY);
        this.fineLines.line(posx3, recessY, posx3, endY);
        this.fineLines.line(posx1, recessY, posx3, recessY);
        const innerY = recessY + 0.2 * (endY - recessY);
        this.fineLines.line(posx2, innerY, posx2, endY);
      }
    }
  }

  // ---- Fortified enclosures (EnclosureDrawer)

  enclosureHorizontal(c: Cartouche): string {
    const [leftPart, rightPart] = this.sides(c);
    this.drawEnclosure(this.width, this.height, true, true, leftPart !== 0, rightPart !== 0);
    return this.result();
  }

  enclosureVertical(c: Cartouche): string {
    this.drawEnclosure(this.width, this.height, c.startPart !== 0, c.endPart !== 0, true, true);
    return this.result();
  }

  private drawEnclosure(
    outerWidth: number,
    outerHeight: number,
    drawTop: boolean,
    drawBottom: boolean,
    drawLeft: boolean,
    drawRight: boolean,
  ): void {
    const g = this.style.geometry;
    const bLength = g.bastionLength;
    const bWidth = g.bastionDepth;
    let topX = 0;
    let topY = 0;
    let innerWidth = outerWidth;
    let innerHeight = outerHeight;
    if (drawLeft) {
      topX += bWidth;
      innerWidth -= bWidth;
    }
    if (drawTop) {
      topY += bWidth;
      innerHeight -= bWidth;
    }
    if (drawBottom) innerHeight -= bWidth;
    if (drawRight) innerWidth -= bWidth;
    const topLeft = pt(topX, topY);
    const bottomLeft = pt(topLeft.x, topLeft.y + innerHeight);
    const topRight = pt(topLeft.x + innerWidth, topLeft.y);
    const bottomRight = pt(topLeft.x + innerWidth, bottomLeft.y);
    const horizontalInfo = this.bastionInfo(innerWidth);
    const verticalInfo = this.bastionInfo(innerHeight);
    const fillRect = (x: number, y: number, w: number, h: number) => this.fills.rect(x, y, w, h);
    const horizontalBastions = (info: BastionInfo, startX: number, startY: number, before: boolean) => {
      let x = startX + info.interBastionSkip + bLength;
      const y = before ? startY - bWidth : startY;
      for (let i = 0; i < info.numberOfBastions; i++) {
        fillRect(x, y, bLength, bWidth);
        x += bLength + info.interBastionSkip;
      }
    };
    const verticalBastions = (info: BastionInfo, x0: number, startY: number, before: boolean) => {
      let y = startY + info.interBastionSkip + bLength;
      const x = before ? x0 - bWidth : x0;
      for (let i = 0; i < info.numberOfBastions; i++) {
        fillRect(x, y, bWidth, bLength);
        y += bLength + info.interBastionSkip;
      }
    };
    if (drawTop) {
      this.line(topLeft, topRight);
      horizontalBastions(horizontalInfo, topLeft.x, topRight.y, true);
    }
    if (drawBottom) {
      this.line(bottomLeft, bottomRight);
      horizontalBastions(horizontalInfo, bottomLeft.x, bottomLeft.y, false);
    }
    if (drawLeft) {
      this.line(topLeft, bottomLeft);
      verticalBastions(verticalInfo, topLeft.x, topLeft.y, true);
    }
    if (drawRight) {
      this.line(topRight, bottomRight);
      verticalBastions(verticalInfo, topRight.x, topRight.y, false);
    }
    // Corner bastions.
    if (drawTop && drawLeft) {
      const x = topLeft.x - bWidth;
      const y = topLeft.y - bWidth;
      fillRect(x, y, bWidth + bLength, bWidth);
      fillRect(x, y, bWidth, bWidth + bLength);
    }
    if (drawBottom && drawLeft) {
      const x = topLeft.x - bWidth;
      const y = bottomLeft.y;
      fillRect(x, y - bLength, bWidth, bLength);
      fillRect(x, y, bWidth + bLength, bWidth);
    }
    if (drawTop && drawRight) {
      const x = topRight.x;
      const y = topRight.y - bWidth;
      fillRect(x - bLength, y, bWidth + bLength, bWidth);
      fillRect(x, y, bWidth, bWidth + bLength);
    }
    if (drawBottom && drawRight) {
      const x = topRight.x;
      const y = bottomRight.y;
      fillRect(x, y - bLength, bWidth, bLength);
      fillRect(x - bLength, y, bWidth + bLength, bWidth);
    }
  }

  private bastionInfo(available: number): BastionInfo {
    const bastionWidth = this.style.geometry.bastionLength;
    let numberOfBastions = Math.trunc((available - 3 * bastionWidth) / (2 * bastionWidth));
    let interBastionSkip = (available - (numberOfBastions + 2) * bastionWidth) / (numberOfBastions + 1);
    if (numberOfBastions === 1) {
      // One intermediary bastion is ugly, and seems to be avoided.
      numberOfBastions = 0;
      interBastionSkip = 0;
    }
    return { numberOfBastions, interBastionSkip };
  }

  // ---- Circular enclosures (CircularEnclosureDrawer)

  circularHorizontal(c: Cartouche): string {
    const g = this.style.geometry;
    const [leftElement, rightElement] = this.sides(c);
    const [p1, p2, p3, p4] = this.horizontalCorners(this.partLength(c, leftElement), this.partLength(c, rightElement));
    const dx = g.cartoucheLineWidth / 2;
    const loopSkip = g.cartoucheLoopLength / 3;
    const center = pt(this.width / 2, this.height / 2);
    let leftC: [Point, Point] | null = null;
    if (leftElement !== 0) {
      leftC = [pt(dx - loopSkip, p1.y), pt(dx - loopSkip, p2.y)];
      this.lines.moveTo(p1.x, p1.y).curveTo(leftC[0].x, leftC[0].y, leftC[1].x, leftC[1].y, p2.x, p2.y);
    }
    this.line(p1, p3);
    this.line(p2, p4);
    let rightC: [Point, Point] | null = null;
    if (rightElement !== 0) {
      rightC = [pt(this.width + loopSkip - dx, p3.y), pt(this.width + loopSkip - dx, p4.y)];
      this.lines.moveTo(p3.x, p3.y).curveTo(rightC[0].x, rightC[0].y, rightC[1].x, rightC[1].y, p4.x, p4.y);
    }
    this.bastionRow(p1, p3, 0, -1);
    this.bastionRow(p2, p4, 0, 1);
    if (leftC !== null) this.bastionsAlongCubic(p1, leftC[0], leftC[1], p2, center);
    if (rightC !== null) this.bastionsAlongCubic(p3, rightC[0], rightC[1], p4, center);
    return this.result();
  }

  circularVertical(c: Cartouche): string {
    const g = this.style.geometry;
    const [p1, p2, p3, p4] = this.verticalCorners(this.partLength(c, c.startPart), this.partLength(c, c.endPart));
    const d = g.cartoucheLineWidth / 2;
    // Java quirk: the loop skip uses the line width, not the loop length.
    const loopSkip = g.cartoucheLineWidth / 3;
    const center = pt(this.width / 2, this.height / 2);
    let topC: [Point, Point] | null = null;
    if (c.startPart !== 0) {
      topC = [pt(p1.x, d - loopSkip), pt(p2.x, d - loopSkip)];
      this.lines.moveTo(p1.x, p1.y).curveTo(topC[0].x, topC[0].y, topC[1].x, topC[1].y, p2.x, p2.y);
    }
    this.line(p1, p3);
    this.line(p2, p4);
    let bottomC: [Point, Point] | null = null;
    if (c.endPart !== 0) {
      bottomC = [pt(p3.x, this.height + loopSkip - d), pt(p4.x, this.height + loopSkip - d)];
      this.lines.moveTo(p3.x, p3.y).curveTo(bottomC[0].x, bottomC[0].y, bottomC[1].x, bottomC[1].y, p4.x, p4.y);
    }
    this.bastionRow(p1, p3, 1, 0);
    this.bastionRow(p2, p4, -1, 0);
    if (topC !== null) this.bastionsAlongCubic(p1, topC[0], topC[1], p2, center);
    if (bottomC !== null) this.bastionsAlongCubic(p3, bottomC[0], bottomC[1], p4, center);
    return this.result();
  }

  private bastionRadius(): number {
    return this.style.geometry.cartoucheLineWidth * 1.5;
  }

  /// Bastions evenly placed along [start, end], bulging towards (nx, ny).
  private bastionRow(start: Point, end: Point, nx: number, ny: number): void {
    const length = Math.hypot(end.x - start.x, end.y - start.y);
    const radius = this.bastionRadius();
    const n = Math.round(length / (radius * 3));
    if (n <= 0) return;
    const actualSpacing = length / n;
    const ux = (end.x - start.x) / length;
    const uy = (end.y - start.y) / length;
    for (let i = 0; i < n; i++) {
      const t = actualSpacing * (i + 0.5);
      this.bastion(start.x + ux * t, start.y + uy * t, nx, ny);
    }
  }

  /// Bastions evenly placed (by arc length) along a cubic curve, bulging
  /// away from `inside`.
  private bastionsAlongCubic(p0: Point, c1: Point, c2: Point, p3: Point, inside: Point): void {
    const samples = 48;
    const pts: Point[] = [];
    const len: number[] = [];
    for (let i = 0; i <= samples; i++) {
      const t = i / samples;
      const u = 1 - t;
      pts.push(
        pt(
          u * u * u * p0.x + 3 * u * u * t * c1.x + 3 * u * t * t * c2.x + t * t * t * p3.x,
          u * u * u * p0.y + 3 * u * u * t * c1.y + 3 * u * t * t * c2.y + t * t * t * p3.y,
        ),
      );
      len.push(i === 0 ? 0 : len[i - 1]! + Math.hypot(pts[i]!.x - pts[i - 1]!.x, pts[i]!.y - pts[i - 1]!.y));
    }
    const totalLength = len[samples]!;
    const n = Math.round(totalLength / (this.bastionRadius() * 3));
    if (n <= 0) return;
    const actualSpacing = totalLength / n;
    let segment = 0;
    for (let i = 0; i < n; i++) {
      const target = actualSpacing * (i + 0.5);
      while (segment < samples - 1 && len[segment + 1]! < target) segment++;
      const a = pts[segment]!;
      const b = pts[segment + 1]!;
      const segLength = len[segment + 1]! - len[segment]!;
      const fraction = segLength <= 0 ? 0 : (target - len[segment]!) / segLength;
      const x = a.x + (b.x - a.x) * fraction;
      const y = a.y + (b.y - a.y) * fraction;
      let tx = b.x - a.x;
      let ty = b.y - a.y;
      const tLen = Math.hypot(tx, ty);
      if (tLen === 0) continue;
      tx /= tLen;
      ty /= tLen;
      let nx = -ty;
      let ny = tx;
      if (nx * (x - inside.x) + ny * (y - inside.y) < 0) {
        nx = -nx;
        ny = -ny;
      }
      this.bastion(x, y, nx, ny);
    }
  }

  /// A half disc on a short stem, centered on x,y, bulging towards (vx, vy).
  private bastion(x: number, y: number, vx: number, vy: number): void {
    const radius = this.bastionRadius();
    const k = radius * 0.5522847498307936;
    const stem = radius / 4;
    const nx = -vy;
    const ny = vx;
    const a0x = x - radius * nx;
    const a0y = y - radius * ny;
    const a1x = x + radius * vx;
    const a1y = y + radius * vy;
    const a2x = x + radius * nx;
    const a2y = y + radius * ny;
    this.fills
      .moveTo(a0x - stem * vx, a0y - stem * vy)
      .lineTo(a0x, a0y)
      .curveTo(x + k * vx - radius * nx, y + k * vy - radius * ny, x + radius * vx - k * nx, y + radius * vy - k * ny, a1x, a1y)
      .curveTo(x + radius * vx + k * nx, y + radius * vy + k * ny, x + k * vx + radius * nx, y + k * vy + radius * ny, a2x, a2y)
      .lineTo(a2x - stem * vx, a2y - stem * vy)
      .closePath();
  }
}

interface BastionInfo {
  numberOfBastions: number;
  interBastionSkip: number;
}
