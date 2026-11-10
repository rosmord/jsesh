/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { describe, expect, it } from "vitest";
import {
  DEFAULT_DOCUMENT_PREFERENCES,
  documentPreferencesFromMap,
  documentPreferencesToMap,
} from "../src/document/preferences.js";
import { applyDocumentPreferences, DEFAULT_STYLE } from "../src/render/style.js";

/// The document header: current format, and conversion of older headers (as `DocumentHeaderCodecTest`).
describe("layout settings in the header", () => {
  const legacy: Record<string, string> = {
    JSesh_page_orientation: "VERTICAL",
    JSesh_page_direction: "RIGHT_TO_LEFT",
    JSesh_max_quadrantHeight: "24.0",
    JSesh_max_quadrant_width: "30.0",
    JSesh_standard_sign_height: "20.0",
    JSesh_line_skip: "7.0",
    JSesh_column_skip: "9.5",
    JSesh_small_skip: "1.5",
    JSesh_cartouche_line_width: "1.2",
    JSesh_alphabetic_text_baseline: "3.0",
    JSesh_small_body_scale_limit: "10.0",
    JSesh_small_sign_centered: "1",
    JSesh_use_lines_for_shading: "true",
    JSesh_limit_quadrat_width_in_lines: "false",
    JSesh_fit_lone_signs: "false",
  };

  it("converts an old header to the same style", () => {
    const prefs = documentPreferencesFromMap(legacy);
    expect(prefs.unit).toBe(24);
    expect(prefs.settings.quadratheight).toBe(100);
    const { geometry: g, options: o } = applyDocumentPreferences(prefs, DEFAULT_STYLE);
    expect(g.maxCadratHeight).toBe(24);
    expect(g.maxCadratWidth).toBe(30);
    expect(g.standardSignHeight).toBe(20);
    expect(g.lineSkip).toBe(7);
    expect(g.columnQuadratSkip).toBe(7);
    expect(g.columnSkip).toBe(9.5);
    expect(g.smallSkip).toBe(1.5);
    expect(g.cartoucheLineWidth).toBe(1.2);
    expect(g.alphabeticTextBaseline).toBe(3);
    expect(g.smallBodyScaleLimit).toBe(10);
    expect(o.textOrientation).toBe("VERTICAL");
    expect(o.textDirection).toBe("RIGHT_TO_LEFT");
    expect(o.smallSignCentered).toBe(true);
    expect(o.honourMaxQuadratWidthInLines).toBe(false);
    expect(o.fitLoneSigns).toBe(false);
  });

  it("writes the zone names, and the old keys for older versions", () => {
    const map = documentPreferencesToMap({
      ...DEFAULT_DOCUMENT_PREFERENCES,
      settings: { ...DEFAULT_DOCUMENT_PREFERENCES.settings, layout: "VERTICAL", linegap: 50 },
    });
    expect(map["JSesh_unit"]).toBe("18");
    expect(map["JSesh_layout"]).toBe("columns");
    expect(map["JSesh_linegap"]).toBe("50");
    expect(map["JSesh_columnquadratgap"]).toBe("33.333333");
    expect(map["JSesh_fitsigns"]).toBe("yes");
    expect(map["JSesh_line_skip"]).toBe("9.0");
    expect(map["JSesh_page_orientation"]).toBe("VERTICAL");
  });

  it("scales the whole text with the unit", () => {
    const { geometry: g } = applyDocumentPreferences({ ...DEFAULT_DOCUMENT_PREFERENCES, unit: 36 }, DEFAULT_STYLE);
    expect(g.maxCadratHeight).toBe(36);
    expect(g.maxCadratWidth).toBe(44);
    expect(g.lineSkip).toBe(12);
    expect(g.standardSignHeight).toBe(36);
  });

  it("round-trips", () => {
    const prefs = documentPreferencesFromMap(legacy);
    expect(documentPreferencesFromMap(documentPreferencesToMap(prefs))).toEqual(prefs);
  });
});
