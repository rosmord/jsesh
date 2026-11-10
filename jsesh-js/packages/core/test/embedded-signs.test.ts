/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { describe, expect, it } from "vitest";
import { readMdcDocument, writeMdcDocument } from "../src/document/document.js";
import {
  embeddedSignsFromMap,
  embeddedSignsToMap,
  isEmbeddedSignCode,
  nextEmbeddedSignCode,
} from "../src/document/embedded-signs.js";
import { signCodes } from "../src/render/codes.js";
import { parseEmbeddedSignSvg, withEmbeddedSigns } from "../src/render/embedded-glyphs.js";
import { HieroglyphDrawer } from "../src/render/glyphs.js";
import { buildView } from "../src/render/layout.js";
import { applyDocumentPreferences, DEFAULT_STYLE, withStyle, type JSeshStyle } from "../src/render/style.js";
import { fixturesAvailable, readFixture } from "./fixtures.js";
import { compareViews, glyphDataAvailable, nodeGlyphProvider, ReplayMeasurer, type JavaLayout } from "./layout-support.js";

const SVG = "<svg width='10' height='18'>\n<path d='M 0 0 L 10 18 Z'/>\n</svg>\n";

describe("embedded signs in the header", () => {
  it("recognizes embedded sign codes", () => {
    expect(isEmbeddedSignCode("SIGN12")).toBe(true);
    for (const c of ["SIGN", "SIGN0", "SIGN01", "sign1", "SIGN1A", "A1"]) expect(isEmbeddedSignCode(c)).toBe(false);
  });

  it("reads and writes signs, keeping the highest number", () => {
    const signs = {
      maxId: 3,
      signs: [
        { code: "SIGN1", svg: SVG, variantOf: "A1", description: "Un homme assis, « tardif »" },
        { code: "SIGN2", svg: SVG, description: "" },
      ],
    };
    const map = embeddedSignsToMap(signs);
    expect(map["JSesh_max_embedded_sign_id"]).toBe("3");
    expect(map["JSesh_embedded_sign_SIGN2"]!.startsWith("- - ")).toBe(true);
    expect(embeddedSignsFromMap(map)).toEqual(signs);
    expect(nextEmbeddedSignCode(signs)).toBe("SIGN4");
  });

  it("skips malformed signs", () => {
    const read = embeddedSignsFromMap({
      JSesh_embedded_sign_SIGN1: "A1 -",
      JSesh_embedded_sign_FOO: "- - PHN2Zy8+",
      JSesh_embedded_sign_SIGN2: "- - PHN2Zy8+",
    });
    expect(read.signs).toEqual([{ code: "SIGN2", svg: "<svg/>", description: "" }]);
  });

  it("reads the size, path and zones of a normalized SVG", () => {
    const glyph = parseEmbeddedSignSvg(
      "<?xml version='1.0' ?>\n<svg width='16' height='18' xmlns='http://www.w3.org/2000/svg'>\n" +
        "<path style='fill:black' d='M 0 18 L 8 0 L 16 18 Z'/>\n" +
        "<rect style='x' id='zone2' width='4.0' height='5.0' x='12.0' y='1.0' inkscape:label='gravity:sb'/>\n</svg>\n",
    );
    expect(glyph?.width).toBe(16);
    expect(glyph?.height).toBe(18);
    expect(glyph?.shape.d).toBe("M 0 18 L 8 0 L 16 18 Z");
    expect(glyph?.zones[0]).toBeUndefined();
    expect(glyph?.zones[1]).toEqual({
      x: 12,
      y: 1,
      width: 4,
      height: 5,
      horizontalGravity: "START",
      verticalGravity: "BOTTOM",
    });
  });
});

interface EmbeddedSignsFixture {
  source: string;
  variants: Record<string, JavaLayout>;
}

const available = fixturesAvailable && glyphDataAvailable;
const fixture: EmbeddedSignsFixture | undefined = available ? readFixture("embedded-signs.json") : undefined;

/// The styles of `ExportFixtures.exportEmbeddedSigns` (other than the document's own).
const VARIANTS: Record<string, JSeshStyle> = {
  columnsRtl: withStyle(DEFAULT_STYLE, { options: { textOrientation: "VERTICAL", textDirection: "RIGHT_TO_LEFT" } }),
  retrograde: withStyle(DEFAULT_STYLE, { options: { retrograde: true } }),
};

describe.skipIf(!available)("embedded signs, against Java", () => {
  it("reads the document written by Java, and writes the same signs", () => {
    const document = readMdcDocument(fixture!.source, { failFast: true });
    expect(document.embeddedSigns?.maxId).toBe(2);
    expect(document.embeddedSigns?.signs.map((s) => s.code)).toEqual(["SIGN1", "SIGN2"]);
    const signLines = (text: string) => text.split("\n").filter((l) => /^\+\+JSesh_(embedded_sign_|max_embedded)/.test(l));
    expect(signLines(writeMdcDocument(document))).toEqual(signLines(fixture!.source));
  });

  it.each(["default", "columnsRtl", "retrograde"])("lays out the document (%s)", async (variant) => {
    const document = readMdcDocument(fixture!.source, { failFast: true });
    const style = VARIANTS[variant] ?? applyDocumentPreferences(document.preferences, DEFAULT_STYLE);
    const provider = withEmbeddedSigns(nodeGlyphProvider(), document.embeddedSigns);
    await provider.loadShapes(signCodes(document.text, provider));
    const java = fixture!.variants[variant]!;
    const view = buildView(document.text, {
      style,
      drawer: new HieroglyphDrawer(provider),
      measurer: new ReplayMeasurer(java.measures),
    });
    expect(compareViews(view, java.view)).toBeNull();
  });
});
