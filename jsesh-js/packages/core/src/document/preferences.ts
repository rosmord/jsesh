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
  /// In horizontal text, scale quadrats down to the maximal quadrat width. Documents
  /// without the property get `true` (historical layout).
  limitQuadratWidthInLines: boolean;
  /// Width of the page; 0 means that the document has no page (it is as large as its text).
  pageWidth: number;
  /// Height of the page; 0 if not specified.
  pageHeight: number;
  marginLeft: number;
  marginRight: number;
  marginTop: number;
  marginBottom: number;
  /// Where the page number is written in the margins: `NONE`, `TOP_LEFT`, `TOP_CENTER`,
  /// `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_CENTER` or `BOTTOM_RIGHT`. (Stored and written back, but
  /// not drawn by the port yet.)
  pageNumberPosition: string;
  /// Where the title is written (same values).
  pageTitlePosition: string;
  /// True if the page number and the title change side on even pages.
  pageTextsAlternate: boolean;
  /// The title written on each page; may contain spaces.
  pageTitle: string;
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
  limitQuadratWidthInLines: true,
  pageWidth: 0,
  pageHeight: 0,
  marginLeft: 0,
  marginRight: 0,
  marginTop: 0,
  marginBottom: 0,
  pageNumberPosition: "NONE",
  pageTitlePosition: "NONE",
  pageTextsAlternate: false,
  pageTitle: "",
});

/// The preferences which are only written when there is a page.
const PAGE_PROPERTY_KEYS: (keyof DocumentPreferences)[] = [
  "pageWidth",
  "pageHeight",
  "marginLeft",
  "marginRight",
  "marginTop",
  "marginBottom",
];

/// The preferences which are only written when the pages have a number or a title.
const PAGE_TEXT_KEYS: (keyof DocumentPreferences)[] = [
  "pageNumberPosition",
  "pageTitlePosition",
  "pageTextsAlternate",
  "pageTitle",
];

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
  limitQuadratWidthInLines: "JSesh_limit_quadrat_width_in_lines",
  pageWidth: "JSesh_page_width",
  pageHeight: "JSesh_page_height",
  marginLeft: "JSesh_page_margin_left",
  marginRight: "JSesh_page_margin_right",
  marginTop: "JSesh_page_margin_top",
  marginBottom: "JSesh_page_margin_bottom",
  pageNumberPosition: "JSesh_page_number_position",
  pageTitlePosition: "JSesh_page_title_position",
  pageTextsAlternate: "JSesh_page_texts_alternate",
  pageTitle: "JSesh_page_title",
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
  const hasPage = preferences.pageWidth > 0;
  if (!hasPage) {
    // No page: the page entries are not written.
    for (const key of PAGE_PROPERTY_KEYS) delete result[PROPERTY_NAMES[key]];
  }
  const hasPageTexts =
    preferences.pageNumberPosition !== "NONE" ||
    (preferences.pageTitlePosition !== "NONE" && preferences.pageTitle !== "");
  if (!hasPage || !hasPageTexts) {
    for (const key of PAGE_TEXT_KEYS) delete result[PROPERTY_NAMES[key]];
  } else {
    if (preferences.pageTitle === "") delete result[PROPERTY_NAMES.pageTitle];
    if (!preferences.pageTextsAlternate) delete result[PROPERTY_NAMES.pageTextsAlternate];
  }
  return result;
}

function javaDoubleToString(d: number): string {
  return Number.isInteger(d) ? d.toFixed(1) : String(d);
}
