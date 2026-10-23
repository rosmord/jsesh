/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Small helpers to write SVG as text.

/// A number, written with at most 3 decimals.
export function num(n: number): string {
  const r = Math.round(n * 1000) / 1000;
  return Object.is(r, -0) ? "0" : String(r);
}

/// Escapes text for XML, and removes the characters XML forbids (control
/// characters).
export function escapeXml(s: string): string {
  return s
    // eslint-disable-next-line no-control-regex
    .replace(/[\u0000-\u0008\u000b\u000c\u000e-\u001f\ufffe\uffff]/g, "")
    .replace(/[&<>"']/g, (c) => `&#${c.charCodeAt(0)};`);
}

/// Builds the `d` attribute of a path.
export class PathBuilder {
  private d = "";

  moveTo(x: number, y: number): this {
    this.d += `M${num(x)} ${num(y)}`;
    return this;
  }

  lineTo(x: number, y: number): this {
    this.d += `L${num(x)} ${num(y)}`;
    return this;
  }

  curveTo(x1: number, y1: number, x2: number, y2: number, x3: number, y3: number): this {
    this.d += `C${num(x1)} ${num(y1)} ${num(x2)} ${num(y2)} ${num(x3)} ${num(y3)}`;
    return this;
  }

  line(x1: number, y1: number, x2: number, y2: number): this {
    return this.moveTo(x1, y1).lineTo(x2, y2);
  }

  rect(x: number, y: number, w: number, h: number): this {
    this.d += `M${num(x)} ${num(y)}h${num(w)}v${num(h)}h${num(-w)}Z`;
    return this;
  }

  closePath(): this {
    this.d += "Z";
    return this;
  }

  isEmpty(): boolean {
    return this.d === "";
  }

  toString(): string {
    return this.d;
  }
}

/// A path drawn as a line, as Java draws with a `BasicStroke` (square caps,
/// miter joins).
export function strokedPath(d: PathBuilder | string, color: string, width: number): string {
  const s = d.toString();
  if (s === "") return "";
  return `<path d="${s}" fill="none" stroke="${color}" stroke-width="${num(width)}" stroke-linecap="square" stroke-miterlimit="10"/>`;
}

export function filledPath(d: PathBuilder | string, color: string, evenOdd = false): string {
  const s = d.toString();
  if (s === "") return "";
  return `<path d="${s}" fill="${color}"${evenOdd ? ' fill-rule="evenodd"' : ""}/>`;
}
