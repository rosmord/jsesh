/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The layout settings shared by the document and by zones: port of
/// `jsesh.model.LayoutSetting` and `jsesh.model.TextLayoutSettings`.
///
/// Each setting has the same name, and the same values, in the options of a
/// zone (`%zone[linegap=50](...)`) and in the header of a document
/// (`++JSesh_linegap 50 +s`). Lengths are percents of the unit of the
/// document, the height of a quadrat. The gaps are named by role: `linegap`
/// (between lines), `columngap` (between columns), `quadratgap` (between the
/// quadrats of a line, and the signs of a quadrat) and `columnquadratgap`
/// (between the quadrats of a column).

import type { TextDirection, TextOrientation } from "./constants.js";

/// The values of the settings, by name. An unset (absent) one is inherited.
export interface TextLayoutSettings {
  readonly layout?: TextOrientation;
  readonly dir?: TextDirection;
  readonly quadratheight?: number;
  readonly quadratwidth?: number;
  readonly signheight?: number;
  readonly linegap?: number;
  readonly columngap?: number;
  readonly quadratgap?: number;
  readonly columnquadratgap?: number;
  readonly cartouchelinewidth?: number;
  readonly textbaseline?: number;
  readonly fitsigns?: boolean;
  readonly honourmaxquadratwidth?: boolean;
  readonly uniformquadratscaling?: boolean;
  readonly centresmallsigns?: boolean;
  readonly retrograde?: boolean;
}

export type LayoutSettingName = keyof TextLayoutSettings;

export type LayoutSettingKind = "LENGTH" | "FLAG" | "ORIENTATION" | "DIRECTION";

/// The unit of the default lengths, in points.
export const DEFAULT_UNIT = 18;

const percentOfDefaultUnit = (points: number) => (points / 18) * 100;

/// The settings, in the order of `LayoutSetting` (the order in which they are written).
export const LAYOUT_SETTINGS: readonly { name: LayoutSettingName; kind: LayoutSettingKind; documentDefault: unknown }[] = [
  { name: "layout", kind: "ORIENTATION", documentDefault: "HORIZONTAL" },
  { name: "dir", kind: "DIRECTION", documentDefault: "LEFT_TO_RIGHT" },
  { name: "quadratheight", kind: "LENGTH", documentDefault: percentOfDefaultUnit(18) },
  { name: "quadratwidth", kind: "LENGTH", documentDefault: percentOfDefaultUnit(22) },
  { name: "signheight", kind: "LENGTH", documentDefault: percentOfDefaultUnit(18) },
  { name: "linegap", kind: "LENGTH", documentDefault: percentOfDefaultUnit(6) },
  { name: "columngap", kind: "LENGTH", documentDefault: percentOfDefaultUnit(8) },
  { name: "quadratgap", kind: "LENGTH", documentDefault: percentOfDefaultUnit(2) },
  { name: "columnquadratgap", kind: "LENGTH", documentDefault: percentOfDefaultUnit(6) },
  { name: "cartouchelinewidth", kind: "LENGTH", documentDefault: percentOfDefaultUnit(1) },
  { name: "textbaseline", kind: "LENGTH", documentDefault: percentOfDefaultUnit(4) },
  { name: "fitsigns", kind: "FLAG", documentDefault: true },
  { name: "honourmaxquadratwidth", kind: "FLAG", documentDefault: true },
  { name: "uniformquadratscaling", kind: "FLAG", documentDefault: false },
  { name: "centresmallsigns", kind: "FLAG", documentDefault: false },
  { name: "retrograde", kind: "FLAG", documentDefault: false },
];

/// Lengths are kept with six decimals, as they are written.
export function normaliseLength(d: number): number {
  return Math.round(d * 1e6) / 1e6;
}

/// Every setting at its default value for a document.
export const DOCUMENT_DEFAULT_SETTINGS: Readonly<Required<TextLayoutSettings>> = Object.freeze(
  Object.fromEntries(
    LAYOUT_SETTINGS.map((s) => [s.name, s.kind === "LENGTH" ? normaliseLength(s.documentDefault as number) : s.documentDefault]),
  ) as unknown as Required<TextLayoutSettings>,
);

export function layoutSettingKind(name: LayoutSettingName): LayoutSettingKind {
  return LAYOUT_SETTINGS.find((s) => s.name === name)!.kind;
}

export function isLayoutSettingName(name: string): name is LayoutSettingName {
  return LAYOUT_SETTINGS.some((s) => s.name === name);
}

/// Reads a yes/no value, as the values of the `FLAG` settings; undefined if invalid
/// (port of `LayoutSetting.parseFlag`).
export function parseFlag(text: string): boolean | undefined {
  return parseLayoutSetting("fitsigns", text) as boolean | undefined;
}

/// Reads a value, as written in zone options or in a header; undefined if invalid.
export function parseLayoutSetting(name: LayoutSettingName, text: string): unknown {
  const t = text.trim().toLowerCase();
  switch (layoutSettingKind(name)) {
    case "LENGTH": {
      if (!/^[+-]?(\d+\.?\d*|\.\d+)([eE][+-]?\d+)?$/.test(t)) return undefined;
      const d = Number.parseFloat(t);
      return Number.isFinite(d) && d >= 0 ? normaliseLength(d) : undefined;
    }
    case "FLAG":
      return t === "yes" || t === "true" || t === "1" ? true : t === "no" || t === "false" || t === "0" ? false : undefined;
    case "ORIENTATION":
      return t === "lines" ? "HORIZONTAL" : t === "columns" ? "VERTICAL" : undefined;
    case "DIRECTION":
      return t === "lr" ? "LEFT_TO_RIGHT" : t === "rl" ? "RIGHT_TO_LEFT" : undefined;
  }
}

/// Writes a value, as `LayoutSetting.format` (lengths with at most six decimals).
export function formatLayoutSetting(name: LayoutSettingName, value: unknown): string {
  switch (layoutSettingKind(name)) {
    case "LENGTH":
      return formatLength(value as number);
    case "FLAG":
      return value ? "yes" : "no";
    case "ORIENTATION":
      return value === "HORIZONTAL" ? "lines" : "columns";
    case "DIRECTION":
      return value === "LEFT_TO_RIGHT" ? "lr" : "rl";
  }
}

export function formatLength(value: number): string {
  return String(normaliseLength(value));
}

/// The settings which are set, in the order in which they are written.
export function setLayoutSettings(settings: TextLayoutSettings): [LayoutSettingName, unknown][] {
  const result: [LayoutSettingName, unknown][] = [];
  for (const s of LAYOUT_SETTINGS) {
    const value = settings[s.name];
    if (value !== undefined) result.push([s.name, value]);
  }
  return result;
}
