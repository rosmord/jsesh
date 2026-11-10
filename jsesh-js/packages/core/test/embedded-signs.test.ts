/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { describe, expect, it } from "vitest";
import { readMdcDocument, writeMdcDocument } from "../src/document/document.js";
import {
  embeddedSignRendering,
  embeddedSignsFromMap,
  embeddedSignSvg,
  embeddedSignsToMap,
  isEmbeddedSignCode,
  nextEmbeddedSignCode,
} from "../src/document/embedded-signs.js";
import { signCodes } from "../src/render/codes.js";
import { withEmbeddedSigns } from "../src/render/embedded-glyphs.js";
import { HieroglyphDrawer } from "../src/render/glyphs.js";
import { buildView } from "../src/render/layout.js";
import { applyDocumentPreferences, DEFAULT_STYLE, withStyle, type JSeshStyle } from "../src/render/style.js";
import { fixturesAvailable, readFixture } from "./fixtures.js";
import { compareViews, glyphDataAvailable, nodeGlyphProvider, ReplayMeasurer, type JavaLayout } from "./layout-support.js";

describe("embedded signs in the header", () => {
  it("recognizes embedded sign codes", () => {
    expect(isEmbeddedSignCode("sign12")).toBe(true);
    for (const c of ["sign", "sign0", "sign01", "SIGN1", "sign1A", "A1"]) expect(isEmbeddedSignCode(c)).toBe(false);
  });

  it("reads and writes signs, keeping the compressed data as is", () => {
    const signs = {
      maxId: 3,
      signs: [
        { code: "sign1", variantOf: "A1", description: "Un homme assis, « tardif »", svg: "H4sIAAAA", rendering: "H4sIBBBB" },
        { code: "sign2", description: "", svg: "H4sICCCC", rendering: "H4sIDDDD" },
      ],
    };
    const map = embeddedSignsToMap(signs);
    expect(map["JSesh_max_embedded_sign_id"]).toBe("3");
    expect(map["JSesh_embedded_sign_sign2"]).toBe("- v0 - H4sICCCC H4sIDDDD");
    expect(embeddedSignsFromMap(map)).toEqual(signs);
    expect(nextEmbeddedSignCode(signs)).toBe("sign4");
  });

  it("skips malformed signs", () => {
    const read = embeddedSignsFromMap({
      JSesh_embedded_sign_sign1: "A1 v0 - H4sIAAAA",
      JSesh_embedded_sign_FOO: "- v0 - H4sIAAAA H4sIAAAA",
      JSesh_embedded_sign_sign2: "- v0 - H4sIAAAA H4sIBBBB",
      JSesh_embedded_sign_sign3: "- v1 - H4sIAAAA H4sIBBBB",
      JSesh_embedded_sign_sign4: "- - H4sIAAAA H4sIBBBB",
    });
    expect(read.signs).toEqual([{ code: "sign2", description: "", svg: "H4sIAAAA", rendering: "H4sIBBBB" }]);
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
  it("reads the document written by Java, and writes the same signs", async () => {
    const document = readMdcDocument(fixture!.source, { failFast: true });
    expect(document.embeddedSigns?.maxId).toBe(2);
    const signs = document.embeddedSigns!.signs;
    expect(signs.map((s) => s.code)).toEqual(["sign1", "sign2"]);
    // The original SVG files are kept, byte for byte.
    expect(new TextDecoder().decode(await embeddedSignSvg(signs[0]!))).toMatch(/^<svg xmlns=.*width='1600'/);
    const rendering = await embeddedSignRendering(signs[0]!);
    expect(rendering.h).toBeCloseTo(18);
    expect(rendering.zones).toHaveLength(3);
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
