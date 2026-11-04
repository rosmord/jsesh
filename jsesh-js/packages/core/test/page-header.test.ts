/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { describe, expect, it } from "vitest";
import { stripJSeshHeader, writeJSeshHeader } from "../src/document/header.js";
import {
  DEFAULT_DOCUMENT_PREFERENCES,
  documentPreferencesFromMap,
  documentPreferencesToMap,
} from "../src/document/preferences.js";

describe("page entries of the header", () => {
  it("writes nothing about the page when there is none", () => {
    const map = documentPreferencesToMap(DEFAULT_DOCUMENT_PREFERENCES);
    expect(Object.keys(map).filter((k) => k.startsWith("JSesh_page_") && !/direction|orientation/.test(k))).toEqual([]);
  });

  it("writes the number and the title only when they are used", () => {
    const page = { ...DEFAULT_DOCUMENT_PREFERENCES, pageWidth: 200, pageHeight: 100 };
    expect(documentPreferencesToMap(page)).not.toHaveProperty("JSesh_page_number_position");
    const numbered = documentPreferencesToMap({ ...page, pageNumberPosition: "BOTTOM_RIGHT" });
    expect(numbered["JSesh_page_number_position"]).toBe("BOTTOM_RIGHT");
    expect(numbered).not.toHaveProperty("JSesh_page_title");
  });

  it("writes the alternation only when it is on", () => {
    const page = { ...DEFAULT_DOCUMENT_PREFERENCES, pageWidth: 200, pageHeight: 100, pageNumberPosition: "TOP_LEFT" };
    expect(documentPreferencesToMap(page)).not.toHaveProperty("JSesh_page_texts_alternate");
    const map = documentPreferencesToMap({ ...page, pageTextsAlternate: true });
    expect(map["JSesh_page_texts_alternate"]).toBe("true");
    expect(documentPreferencesFromMap(map).pageTextsAlternate).toBe(true);
  });

  it("keeps a title with spaces and accents", () => {
    const preferences = {
      ...DEFAULT_DOCUMENT_PREFERENCES,
      pageWidth: 200,
      pageHeight: 100,
      pageTitlePosition: "TOP_CENTER",
      pageTitle: "Stèle de Séti Ier",
    };
    const header = writeJSeshHeader(documentPreferencesToMap(preferences));
    expect(header).toContain("++JSesh_page_title Stèle de Séti Ier +s\n");
    const reread = documentPreferencesFromMap(stripJSeshHeader(header + "A1").properties);
    expect(reread.pageTitle).toBe("Stèle de Séti Ier");
    expect(reread.pageTitlePosition).toBe("TOP_CENTER");
    expect(reread.pageWidth).toBe(200);
  });
});
