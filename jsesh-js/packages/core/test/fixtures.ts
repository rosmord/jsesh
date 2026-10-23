/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Access to the golden fixtures produced by the Java side
/// (`./gradlew :jseshJsExport:exportAll`).

import { existsSync, readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
export const generatedDir = join(here, "../../../fixtures/generated");
export const corpusDir = join(here, "../../../../jsesh-texts/src/main/assets/jsesh-texts");

export const fixturesAvailable = existsSync(join(generatedDir, "parser-cases.json"));

export function readFixture<T>(relativePath: string): T {
  return JSON.parse(readFileSync(join(generatedDir, relativePath), "utf-8")) as T;
}

export function readCorpusFile(name: string): string {
  return readFileSync(join(corpusDir, name), "utf-8");
}

export interface ParserCase {
  input: string;
  symbols: { code: string; text: string; value: unknown }[];
  ast?: unknown;
  astError?: string;
  astPhilologyGrouping?: unknown;
  astPhilologyGroupingError?: string;
  written?: string;
  writtenError?: string;
}

export interface CorpusCase {
  file: string;
  ast?: unknown;
  astError?: string;
  preferences?: Record<string, string>;
  written?: string;
  writtenError?: string;
}

/// Removes the `nodeType` the Java exporter puts on every record, except on
/// the sign subtype (whose TS type keeps it).
export function stripRecordTypes(value: unknown): unknown {
  if (Array.isArray(value)) return value.map(stripRecordTypes);
  if (value !== null && typeof value === "object") {
    const result: Record<string, unknown> = {};
    for (const [k, v] of Object.entries(value)) {
      if (k === "nodeType" && v !== "Philology") continue;
      result[k] = stripRecordTypes(v);
    }
    return result;
  }
  return value;
}
