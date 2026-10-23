/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Support for the layout tests: the glyph data, a text measurer replaying
/// the measures made by Java, and the comparison of view trees.

import { existsSync, readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { GlyphIndexProvider, type GlyphIndexData } from "../../glyphs/src/provider.js";
import type { ScriptCode } from "../src/model/constants.js";
import { Hieroglyph } from "../src/model/model.js";
import type { FontSpecification } from "../src/render/style.js";
import type { CharacterMetrics, RunMeasure, TextMeasurer } from "../src/render/text.js";
import type { MDCView } from "../src/render/view.js";

const here = dirname(fileURLToPath(import.meta.url));
const glyphDataDir = join(here, "../../glyphs/data");

export const glyphDataAvailable = existsSync(join(glyphDataDir, "glyph-index.json"));

let provider: GlyphIndexProvider | undefined;

/// The glyph provider for the exported data, reading shapes from disk.
export function nodeGlyphProvider(): GlyphIndexProvider {
  provider ??= new GlyphIndexProvider(
    JSON.parse(readFileSync(join(glyphDataDir, "glyph-index.json"), "utf-8")) as GlyphIndexData,
    async (fileName) => JSON.parse(readFileSync(join(glyphDataDir, "glyphs", fileName + ".json"), "utf-8")),
  );
  return provider;
}

/// A text measure recorded by Java (see `LayoutExporter`).
export type JavaMeasure =
  | ({ kind: "run"; script: ScriptCode; rightToLeft: boolean; pieces: string[] } & RunMeasure)
  | ({ kind: "char"; script: ScriptCode; text: string } & CharacterMetrics)
  | { kind: "small"; text: string; width: number; height: number };

/// A view, as exported by Java.
export interface JavaView {
  t: string;
  x: number;
  y: number;
  w: number;
  h: number;
  sx?: number;
  sy?: number;
  dx?: number;
  dy?: number;
  rtl?: boolean;
  c?: JavaView[];
}

export interface JavaLayout {
  view: JavaView;
  measures: JavaMeasure[];
}

/// Answers with the measures Java made (the fonts are ignored: the tests use
/// the fonts Java used).
export class ReplayMeasurer implements TextMeasurer {
  private readonly runs = new Map<string, RunMeasure>();
  private readonly chars = new Map<string, CharacterMetrics>();
  private readonly small = new Map<string, { width: number; height: number }>();

  constructor(measures: JavaMeasure[]) {
    for (const m of measures) {
      if (m.kind === "run") this.runs.set(JSON.stringify([m.script, m.rightToLeft, m.pieces]), m);
      else if (m.kind === "char") this.chars.set(JSON.stringify([m.script, m.text]), m);
      else this.small.set(m.text, m);
    }
  }

  measureRun(_f: FontSpecification, script: ScriptCode, pieces: readonly string[], rightToLeft: boolean): RunMeasure {
    return this.get(this.runs, JSON.stringify([script, rightToLeft, pieces]));
  }

  characterMetrics(_f: FontSpecification, script: ScriptCode, text: string): CharacterMetrics {
    return this.get(this.chars, JSON.stringify([script, text]));
  }

  smallTextDimensions(_f: FontSpecification, text: string): { width: number; height: number } {
    return this.get(this.small, text);
  }

  private get<T>(map: Map<string, T>, key: string): T {
    const result = map.get(key);
    if (result === undefined) throw new Error("No Java measure for " + key);
    return result;
  }
}

function viewType(v: MDCView): string {
  const name = v.model.constructor.name;
  return v.model instanceof Hieroglyph ? `${name}:${v.model.code}` : name;
}

/// Compares a view tree with Java's. Returns the first difference, or null.
export function compareViews(actual: MDCView, expected: JavaView, path = "", tolerance = 1e-3): string | null {
  const here = `${path}/${expected.t}`;
  const type = viewType(actual);
  if (type !== expected.t) return `${here}: type ${type}`;
  const values: [string, number, number][] = [
    ["x", actual.position.x, expected.x],
    ["y", actual.position.y, expected.y],
    ["w", actual.width, expected.w],
    ["h", actual.height, expected.h],
    ["sx", actual.xScale, expected.sx ?? 1],
    ["sy", actual.yScale, expected.sy ?? 1],
    ["dx", actual.deltaBaseX, expected.dx ?? 0],
    ["dy", actual.deltaBaseY, expected.dy ?? 0],
  ];
  for (const [name, a, e] of values) {
    if (Math.abs(a - e) > tolerance * Math.max(1, Math.abs(e))) return `${here}: ${name} = ${a}, Java: ${e}`;
  }
  if ((actual.direction === "RIGHT_TO_LEFT") !== (expected.rtl ?? false)) return `${here}: direction ${actual.direction}`;
  const children = expected.c ?? [];
  if (actual.numberOfSubviews !== children.length) {
    return `${here}: ${actual.numberOfSubviews} subviews, Java: ${children.length}`;
  }
  for (let i = 0; i < children.length; i++) {
    const d = compareViews(actual.getSubView(i), children[i]!, `${here}[${i}]`, tolerance);
    if (d !== null) return d;
  }
  return null;
}
