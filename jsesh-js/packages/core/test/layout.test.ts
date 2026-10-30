/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { beforeAll, describe, expect, it } from "vitest";
import { readMdcDocument } from "../src/document/document.js";
import { parseMdc } from "../src/mdcreader/builder.js";
import type { HieroglyphicText } from "../src/model/model.js";
import { signCodes } from "../src/render/codes.js";
import { HieroglyphDrawer } from "../src/render/glyphs.js";
import { buildView } from "../src/render/layout.js";
import { applyDocumentPreferences, DEFAULT_STYLE, withStyle, type JSeshStyle } from "../src/render/style.js";
import { fixturesAvailable, readCorpusFile, readFixture } from "./fixtures.js";
import {
  compareViews,
  glyphDataAvailable,
  nodeGlyphProvider,
  ReplayMeasurer,
  type JavaLayout,
} from "./layout-support.js";

const available = fixturesAvailable && glyphDataAvailable;

interface LayoutCase {
  input: string;
  variants: Record<string, JavaLayout>;
}

/// The wrapping width of the "wrapped" variants (see `ExportFixtures`).
const WRAP_WIDTH = 150;

const VARIANTS: Record<string, JSeshStyle> = {
  default: DEFAULT_STYLE,
  rtl: withStyle(DEFAULT_STYLE, { options: { textDirection: "RIGHT_TO_LEFT" } }),
  columns: withStyle(DEFAULT_STYLE, { options: { textOrientation: "VERTICAL" } }),
  columnsRtl: withStyle(DEFAULT_STYLE, { options: { textOrientation: "VERTICAL", textDirection: "RIGHT_TO_LEFT" } }),
  centered: withStyle(DEFAULT_STYLE, { options: { smallSignCentered: true } }),
  justified: withStyle(DEFAULT_STYLE, { options: { justified: true } }),
  wrapped: withStyle(DEFAULT_STYLE, { geometry: { wrapWidth: WRAP_WIDTH } }),
  wrappedJustified: withStyle(DEFAULT_STYLE, { geometry: { wrapWidth: WRAP_WIDTH }, options: { justified: true } }),
};

const cases: LayoutCase[] = available ? readFixture("layout-cases.json") : [];
const corpus: string[] = available ? readFixture("corpus/index.json") : [];

async function layOut(text: HieroglyphicText, style: JSeshStyle, java: JavaLayout) {
  const provider = nodeGlyphProvider();
  await provider.loadShapes(signCodes(text, provider));
  const view = buildView(text, {
    style,
    drawer: new HieroglyphDrawer(provider),
    measurer: new ReplayMeasurer(java.measures),
  });
  return compareViews(view, java.view);
}

describe.skipIf(!available)("layout, against Java", () => {
  beforeAll(() => {
    nodeGlyphProvider();
  });
  const rows = cases.flatMap((c) => Object.keys(VARIANTS).map((variant) => [c.input, variant, c] as const));
  it.each(rows)("lays out %j (%s)", async (input, variant, c) => {
    expect(await layOut(parseMdc(input), VARIANTS[variant]!, c.variants[variant]!)).toBeNull();
  });
});

describe.skipIf(!available)("corpus layout, against Java", () => {
  it.each(corpus)("lays out %s", async (file) => {
    const java = readFixture<JavaLayout & { error?: string }>("layout/" + file.replace(/\.gly$/, ".json"));
    if (java.error !== undefined) return;
    const document = readMdcDocument(readCorpusFile(file), { failFast: true });
    const style = applyDocumentPreferences(document.preferences, DEFAULT_STYLE);
    expect(await layOut(document.text, style, java)).toBeNull();
  });
});
