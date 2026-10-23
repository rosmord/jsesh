/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Per-document layout preferences, saved in the JSesh file header: port of
/// `jsesh.document.DocumentPreferences`.

import type { TextDirection, TextOrientation } from "../model/constants.js";

export interface DocumentPreferences {
  smallSignCentered: boolean;
  textOrientation: TextOrientation;
  textDirection: TextDirection;
  cartoucheLineWidth: number;
  maxQuadratWidth: number;
  maxQuadratHeight: number;
  lineSkip: number;
  columnSkip: number;
  useLinesForShading: boolean;
  standardSignHeight: number;
  smallBodyScaleLimit: number;
  smallSkip: number;
  /// Height of the alphabetic text baseline above the quadrats' bottom line.
  alphabeticTextBaseline: number;
}

export const DEFAULT_DOCUMENT_PREFERENCES: Readonly<DocumentPreferences> = Object.freeze({
  smallSignCentered: false,
  textOrientation: "HORIZONTAL",
  textDirection: "LEFT_TO_RIGHT",
  cartoucheLineWidth: 1,
  maxQuadratWidth: 22,
  maxQuadratHeight: 18,
  lineSkip: 6,
  columnSkip: 8,
  useLinesForShading: false,
  standardSignHeight: 18,
  smallBodyScaleLimit: 12,
  smallSkip: 2,
  alphabeticTextBaseline: 4,
});

/// Header property names (`JSeshInfoConstants`).
const PROPERTY_NAMES: Record<keyof DocumentPreferences, string> = {
  smallSignCentered: "JSesh_small_sign_centered",
  textOrientation: "JSesh_page_orientation",
  textDirection: "JSesh_page_direction",
  cartoucheLineWidth: "JSesh_cartouche_line_width",
  maxQuadratWidth: "JSesh_max_quadrant_width",
  maxQuadratHeight: "JSesh_max_quadrantHeight",
  lineSkip: "JSesh_line_skip",
  columnSkip: "JSesh_column_skip",
  useLinesForShading: "JSesh_use_lines_for_shading",
  standardSignHeight: "JSesh_standard_sign_height",
  smallBodyScaleLimit: "JSesh_small_body_scale_limit",
  smallSkip: "JSesh_small_skip",
  alphabeticTextBaseline: "JSesh_alphabetic_text_baseline",
};

/// Reads preferences from header properties; unknown properties are ignored,
/// missing ones get their default value.
export function documentPreferencesFromMap(map: Record<string, string>): DocumentPreferences {
  const result: Record<string, unknown> = { ...DEFAULT_DOCUMENT_PREFERENCES };
  for (const [key, property] of Object.entries(PROPERTY_NAMES)) {
    const value = map[property];
    if (value === undefined) continue;
    const defaultValue = (DEFAULT_DOCUMENT_PREFERENCES as Record<string, unknown>)[key];
    if (typeof defaultValue === "boolean") {
      // "1" and "0" for compatibility with old files.
      result[key] = value === "1" ? true : value === "0" ? false : value.toLowerCase() === "true";
    } else if (typeof defaultValue === "number") {
      result[key] = Number.parseFloat(value);
    } else {
      result[key] = value;
    }
  }
  return result as unknown as DocumentPreferences;
}

/// Writes preferences as header properties, in the format Java uses
/// (`Double.toString`, so `18.0` rather than `18`).
export function documentPreferencesToMap(preferences: DocumentPreferences): Record<string, string> {
  const result: Record<string, string> = {};
  for (const [key, property] of Object.entries(PROPERTY_NAMES)) {
    const value = (preferences as unknown as Record<string, unknown>)[key];
    result[property] = typeof value === "number" ? javaDoubleToString(value) : String(value);
  }
  return result;
}

function javaDoubleToString(d: number): string {
  return Number.isInteger(d) ? d.toFixed(1) : String(d);
}
