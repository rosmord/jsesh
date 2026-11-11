/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Per-document layout preferences, saved in the JSesh file header: port of
/// `jsesh.document.DocumentPreferences` and `jsesh.io.document.DocumentHeaderCodec`.
///
/// The layout of the text is given by the **unit** of the document (the height of a quadrat, in
/// points: changing it scales the whole document) and by the layout settings shared with zones,
/// in percents of the unit (see `layout-settings.ts`). The page and the small body limit are in
/// points.
///
/// A header without `JSesh_unit` is an old one, with every length in points
/// (`JSesh_line_skip`...): it is converted without loss. The old properties are also written,
/// for older versions of JSesh (`DocumentHeaderCodec.WRITE_LEGACY_KEYS`).

import {
  DEFAULT_UNIT,
  DOCUMENT_DEFAULT_SETTINGS,
  formatLayoutSetting,
  formatLength,
  LAYOUT_SETTINGS,
  normaliseLength,
  parseLayoutSetting,
  type LayoutSettingName,
} from "../model/layout-settings.js";

export interface DocumentPreferences {
  /// The unit of the document (the height of a quadrat), in points.
  unit: number;
  /// The layout settings, all set, lengths in percents of the unit.
  settings: Required<import("../model/layout-settings.js").TextLayoutSettings>;
  /// The height of A1, in points, under which signs are drawn with the small body font.
  smallBodyScaleLimit: number;
  useLinesForShading: boolean;
  /// Red quadrats are drawn in black and underlined (monochrome convention), not in red.
  useUnderlineForRed: boolean;
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
  unit: DEFAULT_UNIT,
  settings: DOCUMENT_DEFAULT_SETTINGS,
  smallBodyScaleLimit: 12,
  useLinesForShading: false,
  useUnderlineForRed: false,
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

/// Write the properties of the old format as well (`DocumentHeaderCodec.WRITE_LEGACY_KEYS`).
export const WRITE_LEGACY_KEYS = true;

/// The version written in `JSesh_Info`.
export const HEADER_VERSION = "2.0";

const UNIT = "JSesh_unit";
const SMALL_BODY_LIMIT = "JSesh_smallbodylimit";
const SHADING = "JSesh_shading";
const RED_SIGNS = "JSesh_redsigns";

/// The old property of each setting, in points for lengths.
const LEGACY_KEYS: Partial<Record<LayoutSettingName, string>> = {
  layout: "JSesh_page_orientation",
  dir: "JSesh_page_direction",
  quadratheight: "JSesh_max_quadrantHeight",
  quadratwidth: "JSesh_max_quadrant_width",
  signheight: "JSesh_standard_sign_height",
  linegap: "JSesh_line_skip",
  columngap: "JSesh_column_skip",
  quadratgap: "JSesh_small_skip",
  // The old line skip was also the space between the quadrats of a column.
  columnquadratgap: "JSesh_line_skip",
  cartouchelinewidth: "JSesh_cartouche_line_width",
  textbaseline: "JSesh_alphabetic_text_baseline",
  fitsigns: "JSesh_fit_lone_signs",
  honourmaxquadratwidth: "JSesh_limit_quadrat_width_in_lines",
  centresmallsigns: "JSesh_small_sign_centered",
};

const PAGE = {
  pageWidth: "JSesh_page_width",
  pageHeight: "JSesh_page_height",
  marginLeft: "JSesh_page_margin_left",
  marginRight: "JSesh_page_margin_right",
  marginTop: "JSesh_page_margin_top",
  marginBottom: "JSesh_page_margin_bottom",
} as const;

const PAGE_NUMBER_POSITION = "JSesh_page_number_position";
const PAGE_TITLE_POSITION = "JSesh_page_title_position";
const PAGE_TEXTS_ALTERNATE = "JSesh_page_texts_alternate";
const PAGE_TITLE = "JSesh_page_title";
const PAGE_TEXT_POSITIONS = ["NONE", "TOP_LEFT", "TOP_CENTER", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_CENTER", "BOTTOM_RIGHT"];

/// The header property of a setting.
export function headerKey(name: LayoutSettingName): string {
  return "JSesh_" + name;
}

/// The value in points of a length setting.
export function preferencePoints(preferences: DocumentPreferences, name: LayoutSettingName): number {
  return (preferences.unit * (preferences.settings[name] as number)) / 100;
}

/// Reads preferences from header properties, of the current format or of an older one; unknown
/// properties and invalid values are ignored, missing ones get their default value.
export function documentPreferencesFromMap(map: Record<string, string>): DocumentPreferences {
  const legacy = !(UNIT in map);
  const unit = positive(legacy ? map["JSesh_max_quadrantHeight"] : map[UNIT], DEFAULT_UNIT);
  const settings: Record<string, unknown> = {};
  for (const s of LAYOUT_SETTINGS) {
    const text = map[headerKey(s.name)];
    let value = text === undefined ? undefined : parseLayoutSetting(s.name, text);
    // The settings which are more recent than the old format have no old property.
    const legacyKey = LEGACY_KEYS[s.name];
    if (value === undefined && legacy && legacyKey !== undefined) value = readLegacy(s.name, map[legacyKey], unit);
    settings[s.name] = value ?? DOCUMENT_DEFAULT_SETTINGS[s.name];
  }
  const shading = map[SHADING];
  return {
    unit,
    settings: settings as DocumentPreferences["settings"],
    smallBodyScaleLimit: positive(SMALL_BODY_LIMIT in map ? map[SMALL_BODY_LIMIT] : map["JSesh_small_body_scale_limit"], 12),
    useLinesForShading: shading !== undefined ? shading.trim() === "lines" : flag(map["JSesh_use_lines_for_shading"]) === true,
    useUnderlineForRed: map[RED_SIGNS]?.trim() === "underline",
    pageWidth: num(map[PAGE.pageWidth], 0),
    pageHeight: num(map[PAGE.pageHeight], 0),
    marginLeft: num(map[PAGE.marginLeft], 0),
    marginRight: num(map[PAGE.marginRight], 0),
    marginTop: num(map[PAGE.marginTop], 0),
    marginBottom: num(map[PAGE.marginBottom], 0),
    pageNumberPosition: position(map[PAGE_NUMBER_POSITION]),
    pageTitlePosition: position(map[PAGE_TITLE_POSITION]),
    pageTextsAlternate: flag(map[PAGE_TEXTS_ALTERNATE]) === true,
    pageTitle: map[PAGE_TITLE] ?? "",
  };
}

/// Writes preferences as header properties, sorted by name, as Java does
/// (`DocumentHeaderCodec.toHeader`).
export function documentPreferencesToMap(preferences: DocumentPreferences): Record<string, string> {
  const result: Record<string, string> = {};
  result[UNIT] = formatLength(preferences.unit);
  for (const s of LAYOUT_SETTINGS) result[headerKey(s.name)] = formatLayoutSetting(s.name, preferences.settings[s.name]);
  result[SMALL_BODY_LIMIT] = formatLength(preferences.smallBodyScaleLimit);
  result[SHADING] = preferences.useLinesForShading ? "lines" : "gray";
  result[RED_SIGNS] = preferences.useUnderlineForRed ? "underline" : "colour";
  if (WRITE_LEGACY_KEYS) putLegacyKeys(preferences, result);
  if (preferences.pageWidth > 0) {
    for (const [field, key] of Object.entries(PAGE)) {
      result[key] = javaDoubleToString(preferences[field as keyof typeof PAGE]);
    }
    const hasPageTexts =
      preferences.pageNumberPosition !== "NONE" ||
      (preferences.pageTitlePosition !== "NONE" && preferences.pageTitle !== "");
    if (hasPageTexts) {
      result[PAGE_NUMBER_POSITION] = preferences.pageNumberPosition;
      result[PAGE_TITLE_POSITION] = preferences.pageTitlePosition;
      if (preferences.pageTextsAlternate) result[PAGE_TEXTS_ALTERNATE] = "true";
      if (preferences.pageTitle !== "") result[PAGE_TITLE] = preferences.pageTitle.replace(/[\r\n]+/g, " ");
    }
  }
  return Object.fromEntries(Object.entries(result).sort(([a], [b]) => (a < b ? -1 : a > b ? 1 : 0)));
}

function putLegacyKeys(p: DocumentPreferences, result: Record<string, string>): void {
  for (const s of LAYOUT_SETTINGS) {
    // The old format has a single line skip, the line gap's.
    if (s.name === "columnquadratgap") continue;
    const legacyKey = LEGACY_KEYS[s.name];
    if (legacyKey === undefined) continue; // More recent than the old format.
    const value = p.settings[s.name];
    result[legacyKey] =
      s.kind === "LENGTH" ? javaDoubleToString(Math.round(preferencePoints(p, s.name) * 1e6) / 1e6) : String(value);
  }
  result["JSesh_small_body_scale_limit"] = javaDoubleToString(p.smallBodyScaleLimit);
  result["JSesh_use_lines_for_shading"] = String(p.useLinesForShading);
}

function readLegacy(name: LayoutSettingName, text: string | undefined, unit: number): unknown {
  const s = LAYOUT_SETTINGS.find((x) => x.name === name)!;
  switch (s.kind) {
    case "LENGTH": {
      // A missing length had its old default value, in points.
      const defaultPoints = ((s.documentDefault as number) * DEFAULT_UNIT) / 100;
      return normaliseLength((num(text, defaultPoints) / unit) * 100);
    }
    case "FLAG":
      return flag(text);
    case "ORIENTATION": {
      const t = text?.trim();
      return t === "HORIZONTAL" || t === "VERTICAL" ? t : undefined;
    }
    case "DIRECTION": {
      const t = text?.trim();
      return t === "LEFT_TO_RIGHT" || t === "RIGHT_TO_LEFT" ? t : undefined;
    }
  }
}

function flag(text: string | undefined): boolean | undefined {
  return text === undefined ? undefined : (parseLayoutSetting("fitsigns", text) as boolean | undefined);
}

function position(text: string | undefined): string {
  const t = text?.trim();
  return t !== undefined && PAGE_TEXT_POSITIONS.includes(t) ? t : "NONE";
}

function num(text: string | undefined, defaultValue: number): number {
  if (text === undefined) return defaultValue;
  const d = Number.parseFloat(text.trim());
  return Number.isFinite(d) ? d : defaultValue;
}

function positive(text: string | undefined, defaultValue: number): number {
  const d = num(text, defaultValue);
  return d > 0 ? d : defaultValue;
}

/// `Double.toString`, for the values written in points (`18.0` rather than `18`).
function javaDoubleToString(d: number): string {
  return Number.isInteger(d) ? d.toFixed(1) : String(d);
}
